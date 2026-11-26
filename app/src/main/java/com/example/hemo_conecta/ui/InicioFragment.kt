package com.example.hemo_conecta.ui

import android.content.Intent
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.core.content.ContextCompat
import com.example.hemo_conecta.R
import com.example.hemo_conecta.databinding.FragmentInicioBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

class InicioFragment : Fragment() {

    private var _binding: FragmentInicioBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
    private lateinit var reference: DatabaseReference

    private lateinit var usuariosReference: DatabaseReference
    private lateinit var referenciaDoacoesUsuario: DatabaseReference

    private var usuariosSnapshot: DataSnapshot? = null
    private var usuariosListener: ValueEventListener? = null

    private var dadosReference: DatabaseReference? = null
    private var dadosListener: ValueEventListener? = null

    private var listenerDoacaoSangue: ValueEventListener? = null

    private var intervaloDoacao = 90

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentInicioBinding.inflate(inflater, container, false)

        auth = FirebaseAuth.getInstance()
        reference = FirebaseDatabase.getInstance().reference

        carregarDados()
        carregarRanking()
        carregarProximaDoacao()

        binding.btnNotificacoes.setOnClickListener {
            findNavController().navigate(
                R.id.action_inicioFragment_to_notificacoesFragment
            )
        }

        binding.historicoBt.setOnClickListener {
            findNavController().navigate(
                R.id.action_inicioFragment_to_historicoFragment
            )
        }

        binding.processoBt.setOnClickListener {
            findNavController().navigate(
                R.id.action_inicioFragment_to_processoDoacaoFragment
            )
        }

        binding.questBt.setOnClickListener {
            findNavController().navigate(
                R.id.action_inicioFragment_to_quest1Fragment
            )
        }

        binding.cuponsBt.setOnClickListener {
            findNavController().navigate(
                R.id.action_inicioFragment_to_cuponsFragment
            )
        }

        binding.btnAgendarAgora.setOnClickListener {
            val intent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://agendamento.saude.es.gov.br/")
            )

            startActivity(intent)
        }

        return binding.root
    }

    // CARREGAR DADOS DO USUÁRIO
    private fun carregarDados() {

        val uid = auth.currentUser?.uid ?: return

        dadosReference = reference
            .child("usuarios")
            .child(uid)

        dadosListener = object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {

                if (_binding == null) return

                val nome = snapshot.child("nome")
                    .getValue(String::class.java)

                val tipoSanguineo = snapshot.child("tipoSanguineo")
                    .getValue(String::class.java)

                binding.nomeUsuarioInicio.text = nome ?: ""
                binding.tipoSanguineoInicio.text = tipoSanguineo ?: ""
            }

            override fun onCancelled(error: DatabaseError) {

                if (_binding == null) return

                Toast.makeText(
                    requireContext(),
                    R.string.ui_erro_ao_carregar_os_dados,
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        dadosReference?.addValueEventListener(dadosListener!!)
    }

    // CARREGAR PRÓXIMA DOAÇÃO DE SANGUE
    private fun carregarProximaDoacao() {

        val uid = auth.currentUser?.uid ?: return

        reference.child("usuarios")
            .child(uid)
            .child("genero")
            .get()
            .addOnSuccessListener { snapshot ->

                if (_binding == null) return@addOnSuccessListener

                val genero = snapshot.getValue(String::class.java)
                    ?.trim()
                    ?.uppercase()

                intervaloDoacao = when (genero) {
                    "MASCULINO" -> 60
                    "FEMININO" -> 90
                    else -> 90
                }

                referenciaDoacoesUsuario = reference
                    .child("doacoes")
                    .child(uid)

                listenerDoacaoSangue = object : ValueEventListener {

                    override fun onDataChange(snapshot: DataSnapshot) {

                        if (_binding == null) return

                        val formato = SimpleDateFormat(
                            "dd/MM/yyyy",
                            Locale("pt", "BR")
                        ).apply {
                            isLenient = false
                            timeZone = TimeZone.getTimeZone("UTC")
                        }

                        var ultimaDoacao: Calendar? = null

                        for (doacao in snapshot.children) {

                            val tipo = doacao.child("tipo")
                                .getValue(String::class.java)
                                ?.uppercase()
                                ?.trim()
                                ?.replace("Ó", "O")

                            if (tipo == "SANGUE") {

                                val dataTexto = doacao.child("data")
                                    .getValue(String::class.java)

                                try {

                                    if (dataTexto != null) {

                                        val data = formato.parse(dataTexto)

                                        if (data != null) {

                                            val calendario = Calendar.getInstance(
                                                TimeZone.getTimeZone("UTC")
                                            )

                                            calendario.time = data

                                            calendario.set(
                                                Calendar.HOUR_OF_DAY,
                                                0
                                            )

                                            calendario.set(
                                                Calendar.MINUTE,
                                                0
                                            )

                                            calendario.set(
                                                Calendar.SECOND,
                                                0
                                            )

                                            calendario.set(
                                                Calendar.MILLISECOND,
                                                0
                                            )

                                            if (
                                                ultimaDoacao == null ||
                                                calendario.after(ultimaDoacao)
                                            ) {
                                                ultimaDoacao = calendario
                                            }
                                        }
                                    }

                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                        }

                        // CASO NÃO EXISTAM DOAÇÕES DE SANGUE
                        if (ultimaDoacao == null) {

                            binding.diasProximaDoacao.setText(
                                R.string.ui_dias
                            )

                            binding.textoProximaDoacao.setText(
                                R.string.ui_registre_sua_primeira_doacao_de_sangue
                            )

                            binding.progressoDoacao.progress = 0

                            return
                        }

                        // CALCULA A DATA DE LIBERAÇÃO
                        val dataLiberacao = ultimaDoacao.clone() as Calendar

                        dataLiberacao.add(
                            Calendar.DAY_OF_YEAR,
                            intervaloDoacao
                        )

                        // DATA ATUAL
                        val hoje = Calendar.getInstance(
                            TimeZone.getTimeZone("UTC")
                        )

                        hoje.set(Calendar.HOUR_OF_DAY, 0)
                        hoje.set(Calendar.MINUTE, 0)
                        hoje.set(Calendar.SECOND, 0)
                        hoje.set(Calendar.MILLISECOND, 0)

                        // CALCULA A DIFERENÇA ENTRE AS DATAS
                        val diferenca = (
                                dataLiberacao.timeInMillis -
                                        hoje.timeInMillis
                                ) / (24 * 60 * 60 * 1000)

                        val diasRestantes = diferenca.toInt()
                            .coerceAtLeast(0)

                        val diasPassados = (
                                intervaloDoacao - diasRestantes
                                ).coerceIn(0, intervaloDoacao)

                        // ATUALIZA A BARRA DE PROGRESSO
                        binding.progressoDoacao.max = intervaloDoacao
                        binding.progressoDoacao.progress = diasPassados

                        // EXIBE OS DIAS RESTANTES
                        if (diasRestantes > 0) {

                            binding.diasProximaDoacao.text =
                                "$diasRestantes dias"

                            binding.textoProximaDoacao.text =
                                "Faltam $diasRestantes dias para completar o período de referência."

                        } else {

                            binding.diasProximaDoacao.setText(
                                R.string.ui_0_dias
                            )

                            binding.textoProximaDoacao.setText(
                                R.string.ui_o_periodo_de_referencia_foi_concluido_consulte_o_hemoce
                            )
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {

                        if (_binding == null) return

                        Toast.makeText(
                            requireContext(),
                            R.string.ui_erro_ao_calcular_a_proxima_doacao,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                referenciaDoacoesUsuario.addValueEventListener(
                    listenerDoacaoSangue!!
                )
            }
            .addOnFailureListener {

                if (_binding == null) return@addOnFailureListener

                Toast.makeText(
                    requireContext(),
                    R.string.ui_erro_ao_buscar_o_genero_do_usuario,
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    // CARREGAR RANKING
    private fun carregarRanking() {

        usuariosReference = reference.child("usuarios")

        usuariosListener = object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {

                usuariosSnapshot = snapshot
                atualizarRanking()
            }

            override fun onCancelled(error: DatabaseError) {

                if (_binding == null) return

                Toast.makeText(
                    requireContext(),
                    R.string.ui_erro_ao_carregar_os_usuarios,
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        usuariosReference.addValueEventListener(usuariosListener!!)
    }

    // CALCULAR E EXIBIR RANKING
    private fun atualizarRanking() {

        val usuarios = usuariosSnapshot ?: return

        if (_binding == null) return

        val uidAtual = auth.currentUser?.uid ?: return

        val ranking = mutableListOf<Pair<String, Int>>()

        // PERCORRE TODOS OS USUÁRIOS
        for (usuario in usuarios.children) {

            val uid = usuario.key ?: continue

            // LÊ A PONTUAÇÃO ARMAZENADA NO PERFIL
            val hemocoins = usuario.child("hemocoins")
                .getValue(Long::class.java)
                ?.toInt()
                ?: usuario.child("hemocoins")
                    .getValue(Int::class.java)
                ?: 0

            ranking.add(Pair(uid, hemocoins))
        }

        // ORDENA PELA PONTUAÇÃO E PELO NOME
        ranking.sortWith(
            compareByDescending<Pair<String, Int>> { it.second }
                .thenBy {
                    usuarios.child(it.first)
                        .child("nome")
                        .getValue(String::class.java)
                        ?.lowercase() ?: ""
                }
        )

        // ENCONTRA A POSIÇÃO DO USUÁRIO LOGADO
        val posicaoUsuario = ranking.indexOfFirst {
            it.first == uidAtual
        }

        // DEFINE AS POSIÇÕES QUE SERÃO EXIBIDAS
        val posicoes = mutableSetOf<Int>()

        // ADICIONA OS TRÊS PRIMEIROS
        for (i in 0 until minOf(3, ranking.size)) {
            posicoes.add(i)
        }

        // ADICIONA O USUÁRIO E SEUS VIZINHOS
        if (posicaoUsuario != -1) {

            if (posicaoUsuario > 0) {
                posicoes.add(posicaoUsuario - 1)
            }

            posicoes.add(posicaoUsuario)

            if (posicaoUsuario < ranking.size - 1) {
                posicoes.add(posicaoUsuario + 1)
            }
        }

        // LIMPA O RANKING ANTERIOR
        binding.rankingLista.removeAllViews()

        // EXIBE CADA POSIÇÃO
        for (posicao in posicoes.sorted()) {

            val (uid, pontos) = ranking[posicao]

            val nome = usuarios.child(uid)
                .child("nome")
                .getValue(String::class.java) ?: "Doador"

            val usuarioAtual = uid == uidAtual

            // CRIA A LINHA
            val linha = LinearLayout(requireContext()).apply {

                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL

                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    (43 * resources.displayMetrics.density).toInt()
                )

                if (usuarioAtual) {

                    setBackgroundResource(
                        R.drawable.bg_ranking_selected
                    )

                    val padding = (
                            8 * resources.displayMetrics.density
                            ).toInt()

                    setPadding(
                        padding,
                        0,
                        padding,
                        0
                    )
                }
            }

            // NÚMERO DA POSIÇÃO
            val textoPosicao = TextView(requireContext()).apply {

                layoutParams = LinearLayout.LayoutParams(
                    (30 * resources.displayMetrics.density).toInt(),
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )

                text = "${posicao + 1}º"
                textSize = 13f
                setTypeface(null, Typeface.BOLD)

                val cor = when (posicao) {
                    0 -> R.color.ranking_gold
                    1 -> R.color.ranking_second
                    2 -> R.color.ranking_third
                    else -> R.color.text_dark
                }

                setTextColor(
                    ContextCompat.getColor(requireContext(), cor)
                )
            }

            // NOME DO USUÁRIO
            val textoNome = TextView(requireContext()).apply {

                layoutParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f
                )

                text = if (usuarioAtual) {
                    "$nome (Você)"
                } else {
                    nome
                }

                textSize = 13f

                val cor = if (usuarioAtual) {
                    R.color.red_dark
                } else {
                    R.color.text_dark
                }

                setTextColor(
                    ContextCompat.getColor(requireContext(), cor)
                )

                if (usuarioAtual) {
                    setTypeface(null, Typeface.BOLD)
                }
            }

            // HEMOCOINS
            val textoPontos = TextView(requireContext()).apply {

                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )

                text = "$pontos hemocoins"
                textSize = 13f
                setTypeface(null, Typeface.BOLD)

                setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.text_dark
                    )
                )
            }

            linha.addView(textoPosicao)
            linha.addView(textoNome)
            linha.addView(textoPontos)

            binding.rankingLista.addView(linha)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        usuariosListener?.let {
            if (::usuariosReference.isInitialized) {
                usuariosReference.removeEventListener(it)
            }
        }

        dadosListener?.let {
            dadosReference?.removeEventListener(it)
        }

        listenerDoacaoSangue?.let {
            if (::referenciaDoacoesUsuario.isInitialized) {
                referenciaDoacoesUsuario.removeEventListener(it)
            }
        }

        _binding = null
    }
}