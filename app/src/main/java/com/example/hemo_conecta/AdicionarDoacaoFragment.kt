package com.example.hemo_conecta

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.hemo_conecta.databinding.FragmentAdicionarDoacaoBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue

class AdicionarDoacaoFragment : Fragment() {

    private var _binding: FragmentAdicionarDoacaoBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentAdicionarDoacaoBinding.inflate(
            inflater,
            container,
            false
        )

        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val items = listOf(
            "HEMOES Colatina",
            "HEMOES Linhares",
            "HEMOES São Mateus",
            "HEMOES Serra",
            "HEMOES Vitória"
        )

        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            items
        )

        adapter.setDropDownViewResource(
            android.R.layout.simple_spinner_dropdown_item
        )

        binding.spinnerLocalDoacao.adapter = adapter

        binding.buttonCadastrarDoacao.setOnClickListener {
            cadastrarDoacao()
        }
    }

    private fun cadastrarDoacao() {

        val dataDoacao = binding.dataDoacaoInput.text.toString().trim()

        val selectedId = binding.radioGroupDoacao.checkedRadioButtonId

        if (selectedId == -1) {

            Toast.makeText(
                requireContext(),
                R.string.ui_por_favor_selecione_seu_tipo_de_doacao,
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        val tipoDoacao = when (selectedId) {
            R.id.option_doacao_sangue -> "SANGUE"
            R.id.option_doacao_medula -> "MEDULA"
            R.id.option_doacao_plaquetas -> "PLAQUETAS"
            R.id.option_doacao_plasma -> "PLASMA"
            else -> ""
        }

        val localDoacao = binding.spinnerLocalDoacao.selectedItem.toString()

        if (dataDoacao.isEmpty()) {

            binding.dataDoacaoInput.error = "Digite a data da doação"
            binding.dataDoacaoInput.requestFocus()

            return
        }

        if (dataDoacao.length < 10) {

            binding.dataDoacaoInput.error =
                "Digite a data completa (DD/MM/AAAA)"

            binding.dataDoacaoInput.requestFocus()

            return
        }

        val pontos = when (tipoDoacao) {
            "SANGUE" -> 500
            "MEDULA" -> 1000
            "PLASMA" -> 250
            "PLAQUETAS" -> 250
            else -> 0
        }

        val novaDoacao = Doacao(
            data = dataDoacao,
            tipo = tipoDoacao,
            local = localDoacao
        )

        val userId = FirebaseAuth.getInstance().currentUser?.uid

        if (userId == null) {

            Toast.makeText(
                requireContext(),
                "Usuário não autenticado",
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        val reference = FirebaseDatabase.getInstance().reference

        val idDoacao = reference
            .child("doacoes")
            .child(userId)
            .push()
            .key

        if (idDoacao == null) {

            Toast.makeText(
                requireContext(),
                "Erro ao gerar o registro",
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        val atualizacoes = mapOf(
            "doacoes/$userId/$idDoacao" to novaDoacao,
            "usuarios/$userId/hemocoins" to ServerValue.increment(pontos.toLong())
        )

        reference.updateChildren(atualizacoes)
            .addOnSuccessListener {

                if (_binding == null) return@addOnSuccessListener

                Toast.makeText(
                    requireContext(),
                    R.string.ui_doacao_cadastrada_com_sucesso,
                    Toast.LENGTH_SHORT
                ).show()

                findNavController().navigate(
                    R.id.action_adicionarDoacaoFragment_to_historicoFragment2
                )
            }
            .addOnFailureListener { e ->

                if (_binding == null) return@addOnFailureListener

                Toast.makeText(
                    requireContext(),
                    "Erro ao cadastrar: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}