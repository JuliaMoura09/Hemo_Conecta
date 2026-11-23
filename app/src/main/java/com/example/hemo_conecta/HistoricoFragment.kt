
package com.example.hemo_conecta

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.hemo_conecta.databinding.FragmentHistoricoBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.Locale

class HistoricoFragment : Fragment() {

    private var _binding: FragmentHistoricoBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentHistoricoBinding.inflate(
            inflater,
            container,
            false
        )

        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.buttonCadastrarDoacao.setOnClickListener {
            findNavController().navigate(
                R.id.action_historicoFragment_to_adicionarDoacaoFragment
            )
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        carregarDadosDoacoes()
    }

    private fun carregarDadosDoacoes() {

        val userId = FirebaseAuth.getInstance().currentUser?.uid

        if (userId == null) {
            Toast.makeText(
                requireContext(),
                "Usuário não encontrado.",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val databaseRef = FirebaseDatabase.getInstance()
            .getReference("doacoes")
            .child(userId)

        val formatoData = SimpleDateFormat(
            "dd/MM/yyyy",
            Locale.getDefault()
        )

        databaseRef.get()
            .addOnSuccessListener { snapshot ->

                if (!isAdded || _binding == null) return@addOnSuccessListener

                val listaDoacoes = mutableListOf<Doacao>()

                for (item in snapshot.children) {
                    val doacao = item.getValue(Doacao::class.java)

                    if (doacao != null) {
                        listaDoacoes.add(doacao)
                    }
                }

                // ordena da doação mais recente para a mais antiga
                val doacoesOrdenadas = listaDoacoes.sortedByDescending { doacao ->
                    try {
                        formatoData.parse(doacao.data)?.time ?: Long.MIN_VALUE
                    } catch (e: Exception) {
                        Long.MIN_VALUE
                    }
                }

                val cards = mapOf(
                    "PLAQUETAS" to Pair(
                        binding.txtDataCard1,
                        binding.txtLocalCard1
                    ),
                    "SANGUE" to Pair(
                        binding.txtDataCard2,
                        binding.txtLocalCard2
                    ),
                    "MEDULA" to Pair(
                        binding.txtDataCard3,
                        binding.txtLocalCard3
                    ),
                    "PLASMA" to Pair(
                        binding.txtDataCard4,
                        binding.txtLocalCard4
                    )
                )

                cards.values.forEach { (data, local) ->
                    data.text = "Nenhuma doação"
                    local.text = "-"
                }

                // procura a doação mais recente de cada tipo
                for ((tipo, card) in cards) {

                    val doacaoMaisRecente = doacoesOrdenadas.firstOrNull {
                        it.tipo.equals(tipo, ignoreCase = true)
                    }

                    if (doacaoMaisRecente != null) {
                        card.first.text = doacaoMaisRecente.data
                        card.second.text = doacaoMaisRecente.local
                    }
                }
            }
            .addOnFailureListener { erro ->

                if (!isAdded || _binding == null) {
                    return@addOnFailureListener
                }

                Toast.makeText(
                    requireContext(),
                    "Erro ao carregar doações: ${erro.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
