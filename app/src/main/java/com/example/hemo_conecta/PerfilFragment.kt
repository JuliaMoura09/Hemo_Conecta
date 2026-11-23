package com.example.hemo_conecta

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import android.widget.ImageView
import android.widget.TextView
import android.graphics.Color
import android.content.res.ColorStateList
import androidx.navigation.fragment.findNavController
import com.example.hemo_conecta.databinding.FragmentPerfilBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Locale
import com.example.hemo_conecta.ui.GerenciadorNotificacoes

class PerfilFragment : Fragment() {

    private var _binding: FragmentPerfilBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
    private lateinit var reference: DatabaseReference

    private var uidAtual: String? = null

    private var usuarioListener: ValueEventListener? = null
    private var doacoesListener: ValueEventListener? = null
    private var artigosListener: ValueEventListener? = null

    // HemoCoins necessários para cada nível
    private val pontuacaoNiveis = listOf(
        0L,
        250L,
        1500L,
        2500L,
        3500L,
        4500L,
        5750L,
        7000L,
        8500L,
        10000L
    )

    // Nome de cada nível
    private val nomesNiveis = listOf(
        "Gota de Esperança",
        "Doador Iniciante",
        "Coração Solidário",
        "Guardião da Vida",
        "Doador de Esperança",
        "Protetor da Vida",
        "Herói da Solidariedade",
        "Anjo da Doação",
        "Guardião da Humanidade",
        "Lenda da Vida"
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentPerfilBinding.inflate(
            inflater,
            container,
            false
        )

        auth = FirebaseAuth.getInstance()
        reference = FirebaseDatabase.getInstance().reference

        uidAtual = auth.currentUser?.uid

        if (!uidAtual.isNullOrEmpty()) {
            carregarDados(uidAtual!!)
            observarDoacoes(uidAtual!!)
            observarArtigosLidos(uidAtual!!)
        }

        return binding.root
    }

    // Carregar informações do usuário
    private fun carregarDados(uid: String) {

        val usuarioRef = reference
            .child("usuarios")
            .child(uid)

        usuarioListener = object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {

                if (_binding == null) return

                val nome = snapshot.child("nome")
                    .getValue(String::class.java)

                val email = snapshot.child("email")
                    .getValue(String::class.java)

                val tipoSanguineo = snapshot.child("tipoSanguineo")
                    .getValue(String::class.java)

                binding.nomeUsuario.text = nome ?: ""
                binding.emailUsuario.text = email ?: ""
                binding.tipoSanguineoPerfil.text = tipoSanguineo ?: ""

                // Gerar iniciais
                if (!nome.isNullOrEmpty()) {

                    val partes = nome.trim()
                        .split(" ")
                        .filter { it.isNotBlank() }

                    if (partes.size >= 2) {
                        binding.iniciaisPerfil.text =
                            "${partes.first().first()}${partes.last().first()}"
                    } else if (partes.isNotEmpty()) {
                        binding.iniciaisPerfil.text =
                            partes[0].first().toString()
                    }
                }

                // Ler HemoCoins do Firebase
                val hemocoins = snapshot.child("hemocoins")
                    .getValue(Long::class.java) ?: 0L

                atualizarNivel(hemocoins)
            }

            override fun onCancelled(error: DatabaseError) {

                if (_binding == null) return

                Toast.makeText(
                    requireContext(),
                    "Erro ao carregar os dados do perfil",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        usuarioRef.addValueEventListener(usuarioListener!!)
    }

    // Observar as doações do usuário
    private fun observarDoacoes(uid: String) {

        val doacoesRef = reference
            .child("doacoes")
            .child(uid)

        doacoesListener = object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {

                if (_binding == null) return

                // Verificar medalhas de doação
                verificarMedalhas(snapshot, uid)

                var totalHemoCoins = 0L

                for (doacao in snapshot.children) {

                    val tipo = doacao.child("tipo")
                        .getValue(String::class.java)
                        ?.trim()
                        ?.uppercase()
                        ?.replace("Ó", "O")

                    val moedas = when (tipo) {

                        "SANGUE" -> 500L

                        "MEDULA OSSEA" -> 1000L

                        "PLASMA" -> 250L

                        "PLAQUETAS" -> 250L

                        else -> 0L
                    }

                    totalHemoCoins += moedas
                }

                val hemocoinsRef = reference
                    .child("usuarios")
                    .child(uid)
                    .child("hemocoins")

                hemocoinsRef.get().addOnSuccessListener { atual ->

                    if (_binding == null) return@addOnSuccessListener

                    val valorAtual = atual.getValue(Long::class.java) ?: 0L

                    // Evitar gravações desnecessárias
                    if (valorAtual != totalHemoCoins) {

                        hemocoinsRef.setValue(totalHemoCoins)
                            .addOnFailureListener {

                                if (_binding == null) return@addOnFailureListener

                                Toast.makeText(
                                    requireContext(),
                                    "Erro ao salvar HemoCoins",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {

                if (_binding == null) return

                Toast.makeText(
                    requireContext(),
                    "Erro ao carregar histórico de doações",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        doacoesRef.addValueEventListener(doacoesListener!!)
    }

    // Verificar medalhas de doação
    private fun verificarMedalhas(snapshot: DataSnapshot, uid: String) {

        var totalSangue = 0
        var totalPlasma = 0
        var totalPlaquetas = 0
        var totalDoacoes = 0
        var doacoesAno = 0

        val formato = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        formato.isLenient = false

        val agora = System.currentTimeMillis()
        val umAno = 365L * 24 * 60 * 60 * 1000

        for (doacao in snapshot.children) {

            val tipo = doacao.child("tipo")
                .getValue(String::class.java)
                ?.trim()
                ?.uppercase()
                ?.replace("Ó", "O")

            val dataString = doacao.child("data")
                .getValue(String::class.java)

            when (tipo) {
                "SANGUE" -> totalSangue++
                "PLASMA" -> totalPlasma++
                "PLAQUETAS" -> totalPlaquetas++
            }

            if (tipo == "SANGUE" ||
                tipo == "PLASMA" ||
                tipo == "MEDULA OSSEA" ||
                tipo == "PLAQUETAS"
            ) {

                totalDoacoes++

                if (!dataString.isNullOrEmpty()) {

                    val data = formato.parse(dataString)?.time

                    if (data != null &&
                        agora - data <= umAno &&
                        data <= agora
                    ) {
                        doacoesAno++
                    }
                }
            }
        }

        val usuarioRef = reference
            .child("usuarios")
            .child(uid)

        // Verificar cadastro no REDOME
        usuarioRef.child("cadastroRedome").get()
            .addOnSuccessListener { redomeSnapshot ->

                if (_binding == null) return@addOnSuccessListener

                val cadastroRedome = redomeSnapshot
                    .getValue(String::class.java)

                val cadastradoRedome = !cadastroRedome.isNullOrBlank()

                // Primeira Gota
                atualizarMedalha(
                    binding.iconePrimeiraGota,
                    binding.tituloPrimeiraGota,
                    binding.descricaoPrimeiraGota,
                    totalSangue >= 1,
                    R.drawable.ic_blood,
                    "Realizou a primeira doação",
                    "primeira_gota",
                    "Primeira Gota"
                )

                // Primeira doação de plaquetas
                atualizarMedalha(
                    binding.iconePlaquetas,
                    binding.tituloPlaquetas,
                    binding.descricaoPlaquetas,
                    totalPlaquetas >= 1,
                    R.drawable.ic_star,
                    "Fez a primeira doação de plaquetas",
                    "plaquetas",
                    "Plaquetas"
                )

                // Cadastro no REDOME
                atualizarMedalha(
                    binding.iconeMedula,
                    binding.tituloMedula,
                    binding.descricaoMedula,
                    cadastradoRedome,
                    R.drawable.ic_heart,
                    "Cadastrado no REDOME",
                    "medula",
                    "Medula"
                )

                // 3 doações nos últimos 12 meses
                atualizarMedalha(
                    binding.iconeFrequente,
                    binding.tituloFrequente,
                    binding.descricaoFrequente,
                    doacoesAno >= 3,
                    R.drawable.ic_doador_frequente,
                    "Doou 3 vezes em um ano",
                    "doador_frequente",
                    "Doador Frequente"
                )

                // 20 doações no histórico
                atualizarMedalha(
                    binding.iconeLenda,
                    binding.tituloLenda,
                    binding.descricaoLenda,
                    totalDoacoes >= 20,
                    R.drawable.ic_lenda,
                    "20 doações realizadas",
                    "lenda",
                    "Lenda"
                )
            }
            .addOnFailureListener {

                if (_binding == null) return@addOnFailureListener

                Toast.makeText(
                    requireContext(),
                    "Erro ao verificar cadastro no REDOME",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    // Observar artigos lidos para verificar a medalha Estudioso
    private fun observarArtigosLidos(uid: String) {

        val artigosRef = reference
            .child("usuarios")
            .child(uid)
            .child("artigos_lidos")

        artigosListener = object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {

                if (_binding == null) return

                val totalArtigosLidos = snapshot.childrenCount

                // Medalha Estudioso
                atualizarMedalha(
                    binding.iconeEstudioso,
                    binding.tituloEstudioso,
                    binding.descricaoEstudioso,
                    totalArtigosLidos >= 3,
                    R.drawable.ic_estudioso,
                    "Leu 3 artigos educativos",
                    "estudioso",
                    "Estudioso"
                )
            }

            override fun onCancelled(error: DatabaseError) {

                if (_binding == null) return

                Toast.makeText(
                    requireContext(),
                    "Erro ao verificar artigos lidos",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        artigosRef.addValueEventListener(artigosListener!!)
    }

    // Atualizar o visual de cada medalha
    private fun atualizarMedalha(
        icone: ImageView,
        titulo: TextView,
        descricao: TextView,
        desbloqueada: Boolean,
        iconeMedalha: Int,
        textoDescricao: String,
        medalhaId: String,
        nomeMedalha: String
    ) {

        if (desbloqueada) {

            icone.setImageResource(iconeMedalha)

            icone.backgroundTintList =
                ColorStateList.valueOf(Color.parseColor("#FDE2E2"))

            titulo.setTextColor(Color.parseColor("#20283A"))
            descricao.setTextColor(Color.parseColor("#64748B"))

            GerenciadorNotificacoes().registrarMedalha(
                medalhaId,
                nomeMedalha
            )

        } else {

            icone.setImageResource(R.drawable.ic_padlock)

            icone.backgroundTintList =
                ColorStateList.valueOf(Color.parseColor("#F1F5F9"))

            titulo.setTextColor(Color.parseColor("#64748B"))
            descricao.setTextColor(Color.parseColor("#94A3B8"))
        }

        descricao.text = textoDescricao
    }

    // Atualizar nível e progresso
    private fun atualizarNivel(hemocoins: Long) {

        val total = hemocoins.coerceAtLeast(0L)

        var indiceNivel = 0

        for (i in pontuacaoNiveis.indices) {

            if (total >= pontuacaoNiveis[i]) {
                indiceNivel = i
            }
        }

        val nivel = indiceNivel + 1

        if (nivel > 1) {
            GerenciadorNotificacoes().registrarNivel(nivel)
        }

        binding.nivelAtual.text = "Nível $nivel"
        binding.nomeNivelAtual.text = nomesNiveis[indiceNivel]

        binding.pontosTotais.text =
            String.format("%,d", total).replace(",", ".")

        if (indiceNivel == pontuacaoNiveis.lastIndex) {

            binding.progressoNivel.max = 100
            binding.progressoNivel.progress = 100

            binding.textoProximoNivel.text =
                "Você alcançou o nível máximo!"

        } else {

            val hemocoinsAtuais = pontuacaoNiveis[indiceNivel]
            val hemocoinsProximo = pontuacaoNiveis[indiceNivel + 1]

            val necessario = hemocoinsProximo - hemocoinsAtuais
            val progresso = total - hemocoinsAtuais

            binding.progressoNivel.max = necessario.toInt()
            binding.progressoNivel.progress = progresso.toInt()

            val faltam = hemocoinsProximo - total

            binding.textoProximoNivel.text =
                "$faltam HemoCoins para o próximo nível"
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        super.onViewCreated(view, savedInstanceState)

        binding.btnLogout.setOnClickListener {

            auth.signOut()

            findNavController().navigate(
                R.id.action_perfilFragment_to_authentication
            )
        }
    }

    override fun onDestroyView() {

        val uid = uidAtual

        if (uid != null) {

            usuarioListener?.let {
                reference.child("usuarios")
                    .child(uid)
                    .removeEventListener(it)
            }

            doacoesListener?.let {
                reference.child("doacoes")
                    .child(uid)
                    .removeEventListener(it)
            }

            artigosListener?.let {
                reference.child("usuarios")
                    .child(uid)
                    .child("artigos_lidos")
                    .removeEventListener(it)
            }
        }

        usuarioListener = null
        doacoesListener = null
        artigosListener = null
        uidAtual = null

        super.onDestroyView()
        _binding = null
    }
}
