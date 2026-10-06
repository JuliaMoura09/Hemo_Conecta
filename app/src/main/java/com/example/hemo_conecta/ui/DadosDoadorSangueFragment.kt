package com.example.hemo_conecta.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.hemo_conecta.R
import com.example.hemo_conecta.databinding.FragmentDadosDoadorSangueBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class DadosDoadorSangueFragment : Fragment() {

    private var _binding: FragmentDadosDoadorSangueBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth
    private lateinit var reference: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDadosDoadorSangueBinding.inflate(inflater, container, false)
        auth = FirebaseAuth.getInstance()
        reference = FirebaseDatabase.getInstance().reference

        initListener()
        return binding.root
    }

    private fun initListener() {
        binding.BtnAtualizar.setOnClickListener {
            val dataCadastroHemoes = binding.dataHemoesInput.text.toString()
            val cadastroHemoes = binding.registroHemoesInput.text.toString()
            saveDadosDoador(dataCadastroHemoes, cadastroHemoes)
        }
    }

    private fun saveDadosDoador(dataCadastroHemoes: String, cadastroHemoes: String) {
        val uid = auth.currentUser?.uid ?: return
        val dados: Map<String, Any> = hashMapOf(
            "dataCadastroHemoes" to dataCadastroHemoes,
            "cadastroHemoes" to cadastroHemoes
        )

        reference
            .child("usuarios")
            .child(uid)
            .updateChildren(dados)
            .addOnCompleteListener { resultado ->
                if (resultado.isSuccessful) {
                    Toast.makeText(
                        requireContext(),
                        "Dados atualizados com sucesso!",
                        Toast.LENGTH_SHORT
                    ).show()
                    findNavController().navigate(R.id.action_DoadorSangueFragment_to_inicioFragment)

                } else {
                    Toast.makeText(
                        requireContext(),
                        "Erro ao salvar os dados.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}