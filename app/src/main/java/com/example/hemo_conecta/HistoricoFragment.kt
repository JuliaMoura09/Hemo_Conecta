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
                R.string.ui_usuario_nao_encontrado,
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
                val currentBinding = _binding ?: return@addOnSuccessListener

                if (!isAdded) return@addOnSuccessListener
                val listaDoacoes = mutableListOf<Doacao>()

                for (item in snapshot.children) {
                    val doacao = item.getValue(Doacao::class.java)

                    if (doacao != null) {
                        listaDoacoes.add(doacao)
                    }
                }

                var qtdSangue = 0
                var qtdMedula = 0
                var qtdPlaquetas = 0
                var qtdPlasma = 0

                for (doacao in listaDoacoes) {
                    when (doacao.tipo.uppercase(Locale.ROOT)) {
                        "SANGUE" -> qtdSangue++
                        "MEDULA" -> qtdMedula++
                        "PLAQUETAS" -> qtdPlaquetas++
                        "PLASMA" -> qtdPlasma++
                    }
                }

                val vidasSalvas = qtdSangue * 4
                val sangueDoadoMl = qtdSangue * 450

                currentBinding.txtVidasSalvas.text =
                    vidasSalvas.toString()

                currentBinding.txtQtdSangue.text =
                    qtdSangue.toString()

                currentBinding.txtSangueDoado.text =
                    "$sangueDoadoMl ml"

                currentBinding.txtQtdMedula.text =
                    qtdMedula.toString()

                currentBinding.txtQtdPlaquetas.text =
                    qtdPlaquetas.toString()

                currentBinding.txtQtdPlasma.text =
                    qtdPlasma.toString()

                val doacoesOrdenadas = listaDoacoes.sortedByDescending { doacao ->
                    try {
                        formatoData.parse(doacao.data)?.time ?: Long.MIN_VALUE
                    } catch (e: Exception) {
                        Long.MIN_VALUE
                    }
                }

                val cards = mapOf(
                    "PLAQUETAS" to Pair(
                        currentBinding.txtDataCard1,
                        currentBinding.txtLocalCard1
                    ),

                    "SANGUE" to Pair(
                        currentBinding.txtDataCard2,
                        currentBinding.txtLocalCard2
                    ),

                    "MEDULA" to Pair(
                        currentBinding.txtDataCard3,
                        currentBinding.txtLocalCard3
                    ),

                    "PLASMA" to Pair(
                        currentBinding.txtDataCard4,
                        currentBinding.txtLocalCard4
                    )
                )

                cards.values.forEach { (data, local) ->
                    data.setText(R.string.ui_nenhuma_doacao)
                    local.setText(R.string.ui_texto_2)
                }

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