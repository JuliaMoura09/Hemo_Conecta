package com.example.hemo_conecta

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.example.hemo_conecta.databinding.FragmentPerfilBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class PerfilFragment : Fragment() {

    private var _binding: FragmentPerfilBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
    private lateinit var reference: DatabaseReference

    private var uidAtual: String? = null

    private var usuarioListener: ValueEventListener? = null
    private var doacoesListener: ValueEventListener? = null

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
        }

        usuarioListener = null
        doacoesListener = null
        uidAtual = null

        super.onDestroyView()
        _binding = null
    }
}
