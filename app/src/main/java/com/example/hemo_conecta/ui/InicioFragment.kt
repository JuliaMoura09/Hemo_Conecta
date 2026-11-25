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

    private lateinit var doacoesReference: DatabaseReference
    private lateinit var usuariosReference: DatabaseReference

    private lateinit var referenciaDoacoesUsuario: DatabaseReference

    private var doacoesSnapshot: DataSnapshot? = null
    private var usuariosSnapshot: DataSnapshot? = null

    private var doacoesListener: ValueEventListener? = null
    private var usuariosListener: ValueEventListener? = null
    private var dadosListener: ValueEventListener? = null
    private var listenerDoacaoSangue: ValueEventListener? = null

    private var dadosReference: DatabaseReference? = null

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

                binding.nomeUsuarioInicio.text = nome
                binding.tipoSanguineoInicio.text = tipoSanguineo
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

        // Busca o gênero do usuário
        reference.child("usuarios")
            .child(uid)
            .child("genero")
            .get()
            .addOnSuccessListener { snapshot ->

                if (_binding == null) return@addOnSuccessListener

                val genero = snapshot.getValue(String::class.java)
                    ?.trim()
                    ?.uppercase()

                // Define o intervalo de acordo com o gênero
                intervaloDoacao = when (genero) {

                    "MASCULINO" -> 60

                    "FEMININO" -> 90

                    else -> 90
                }

                // Busca as doações do usuário
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

                        // Caso não existam doações de sangue
                        if (ultimaDoacao == null) {

                            binding.diasProximaDoacao.setText(R.string.ui_dias)

                            binding.textoProximaDoacao.setText(R.string.ui_registre_sua_primeira_doacao_de_sangue)

                            binding.progressoDoacao.progress = 0

                            return
                        }

                        // Calcula a data de liberação
                        val dataLiberacao = ultimaDoacao.clone() as Calendar

                        dataLiberacao.add(
                            Calendar.DAY_OF_YEAR,
                            intervaloDoacao
                        )

                        // Data atual
                        val hoje = Calendar.getInstance(
                            TimeZone.getTimeZone("UTC")
                        )

                        hoje.set(Calendar.HOUR_OF_DAY, 0)
                        hoje.set(Calendar.MINUTE, 0)
                        hoje.set(Calendar.SECOND, 0)
                        hoje.set(Calendar.MILLISECOND, 0)

                        // Calcula a diferença entre as datas
                        val diferenca = (
                                dataLiberacao.timeInMillis - hoje.timeInMillis
                                ) / (24 * 60 * 60 * 1000)

                        val diasRestantes = diferenca.toInt().coerceAtLeast(0)

                        val diasPassados = (
                                intervaloDoacao - diasRestantes
                                ).coerceIn(0, intervaloDoacao)

                        // Atualiza a barra de progresso
                        binding.progressoDoacao.max = intervaloDoacao
                        binding.progressoDoacao.progress = diasPassados

                        // Exibe os dias restantes
                        if (diasRestantes > 0) {

                            binding.diasProximaDoacao.text =
                                "$diasRestantes dias"

                            binding.textoProximaDoacao.text =
                                "Faltam $diasRestantes dias para completar o período de referência."

                        } else {

                            binding.diasProximaDoacao.setText(R.string.ui_0_dias)

                            binding.textoProximaDoacao.setText(R.string.ui_o_periodo_de_referencia_foi_concluido_consulte_o_hemoce)
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

        doacoesReference = reference.child("doacoes")
        usuariosReference = reference.child("usuarios")

        doacoesListener = object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {

                doacoesSnapshot = snapshot
                atualizarRanking()
            }

            override fun onCancelled(error: DatabaseError) {

                if (_binding == null) return

                Toast.makeText(
                    requireContext(),
                    R.string.ui_erro_ao_carregar_as_doacoes,
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

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

        doacoesReference.addValueEventListener(doacoesListener!!)
        usuariosReference.addValueEventListener(usuariosListener!!)
    }

    // CALCULAR E EXIBIR RANKING
    private fun atualizarRanking() {

        val doacoes = doacoesSnapshot ?: return
        val usuarios = usuariosSnapshot ?: return

        if (_binding == null) return

        val uidAtual = auth.currentUser?.uid ?: return

        val ranking = mutableListOf<Pair<String, Int>>()

        // Percorre todos os usuários cadastrados
        for (usuario in usuarios.children) {

            val uid = usuario.key ?: continue

            var pontos = 0

            // Busca as doações do usuário
            val doacoesUsuario = doacoes.child(uid)

            for (doacao in doacoesUsuario.children) {

                val tipo = doacao.child("tipo")
                    .getValue(String::class.java)
                    ?.uppercase()
                    ?.trim()
                    ?.replace("Ó", "O")
                    ?.replace("_", " ")

                when (tipo) {

                    "SANGUE" -> pontos += 500

                    "MEDULA OSSEA" -> pontos += 1000

                    "PLASMA" -> pontos += 250

                    "PLAQUETAS" -> pontos += 250
                }
            }

            ranking.add(Pair(uid, pontos))
        }

        // Ordena pela pontuação e pelo nome em caso de empate
        ranking.sortWith(
            compareByDescending<Pair<String, Int>> { it.second }
                .thenBy {
                    usuarios.child(it.first)
                        .child("nome")
                        .getValue(String::class.java)
                        ?.lowercase() ?: ""
                }
        )

        // Encontra a posição do usuário logado
        val posicaoUsuario = ranking.indexOfFirst {
            it.first == uidAtual
        }

        // Define as posições que serão exibidas
        val posicoes = mutableSetOf<Int>()

        // Adiciona os três primeiros colocados
        for (i in 0 until minOf(3, ranking.size)) {
            posicoes.add(i)
        }

        // Adiciona o usuário e seus vizinhos
        if (posicaoUsuario != -1) {

            if (posicaoUsuario > 0) {
                posicoes.add(posicaoUsuario - 1)
            }

            posicoes.add(posicaoUsuario)

            if (posicaoUsuario < ranking.size - 1) {
                posicoes.add(posicaoUsuario + 1)
            }
        }

        // Limpa o ranking anterior
        binding.rankingLista.removeAllViews()

        // Exibe cada posição
        for (posicao in posicoes.sorted()) {

            val (uid, pontos) = ranking[posicao]

            val nome = usuarios.child(uid)
                .child("nome")
                .getValue(String::class.java) ?: "Doador"

            val usuarioAtual = uid == uidAtual

            // Cria a linha
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

                    val padding = (8 * resources.displayMetrics.density).toInt()

                    setPadding(
                        padding,
                        0,
                        padding,
                        0
                    )
                }
            }

            // Número da posição
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

            // Nome do usuário
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

            // Pontuação
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

        doacoesListener?.let {
            if (::doacoesReference.isInitialized) {
                doacoesReference.removeEventListener(it)
            }
        }

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
