package com.example.hemo_conecta

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.RadioButton
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.hemo_conecta.databinding.FragmentAdicionarDoacaoBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class AdicionarDoacaoFragment : Fragment() {

    private var _binding: FragmentAdicionarDoacaoBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdicionarDoacaoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val items = listOf("HEMOES Colatina", "HEMOES Linhares", "HEMOES São Mateus", "HEMOES Serra", "HEMOES Vitória")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, items)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerLocalDoacao.adapter = adapter
        binding.buttonCadastrarDoacao.setOnClickListener {
            cadastrarDoacao()
        }
    }

    private fun cadastrarDoacao() {
        val dataDoacao = binding.dataDoacaoInput.text.toString().trim()

        // identifica a opção selecionada no RadioGroup
        val selectedId = binding.radioGroupDoacao.checkedRadioButtonId

        // validação - obriga o usuário a marcar uma opção antes de cadastrar
        if (selectedId == -1) {
            Toast.makeText(
                requireContext(),
                "Por favor, selecione seu tipo de doação",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val tipoDoacao = when (selectedId) {
            R.id.option_doacao_sangue -> "SANGUE"
            R.id.option_doacao_medula -> "MEDULA"
            R.id.option_doacao_plaquetas -> "PLAQUETAS"
            else -> ""
        }

        val localDoacao = binding.spinnerLocalDoacao.selectedItem.toString()

        // validação - obriga o usuário a informar a data
        if (dataDoacao.isEmpty()) {
            binding.dataDoacaoInput.error = "Digite a data da doação"
            binding.dataDoacaoInput.requestFocus()
            return
        }

        val novaDoacao = Doacao(
            data = dataDoacao,
            tipo = tipoDoacao,
            local = localDoacao
        )
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        val databaseRef = FirebaseDatabase.getInstance().getReference("doacoes").child(userId)

        databaseRef.push().setValue(novaDoacao)
            .addOnSuccessListener {
                Toast.makeText(
                    requireContext(),
                    "Doação cadastrada com sucesso!",
                    Toast.LENGTH_SHORT
                ).show()
                findNavController().navigate(R.id.action_adicionarDoacaoFragment_to_historicoFragment2)            }
            .addOnFailureListener { e ->
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