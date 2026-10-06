package com.example.hemo_conecta.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.example.hemo_conecta.R
import com.example.hemo_conecta.databinding.FragmentDadosDoadorMedulaBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class DadosDoadorMedulaFragment : Fragment() {

    private var _binding: FragmentDadosDoadorMedulaBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
    private lateinit var reference: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDadosDoadorMedulaBinding.inflate(inflater, container, false)
        auth = FirebaseAuth.getInstance()
        reference = FirebaseDatabase.getInstance().reference

        initListener()
        return binding.root
    }

    private fun initListener() {
        binding.BtnAtualizar.setOnClickListener {
            val dataCadastroRedome = binding.dataRedomeInput.text.toString()
            val cadastroRedome = binding.registroRedomeInput.text.toString()
            saveDadosDoador(dataCadastroRedome, cadastroRedome)
        }
    }

    private fun saveDadosDoador(dataCadastroRedome: String, cadastroRedome: String) {
        val uid = auth.currentUser?.uid ?: return

        val dados: Map<String, Any> = hashMapOf(
            "dataCadastroRedome" to dataCadastroRedome,
            "cadastroRedome" to cadastroRedome
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
                    findNavController().navigate(R.id.action_DoadorMedulaFragment_to_inicioFragment)
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