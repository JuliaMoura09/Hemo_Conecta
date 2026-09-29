package com.example.hemo_conecta.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.hemo_conecta.databinding.FragmentRecuperarSenhaBinding
import com.google.firebase.auth.FirebaseAuth

class RecuperarSenhaFragment : Fragment() {

    private var _binding: FragmentRecuperarSenhaBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRecuperarSenhaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        initListener()
    }

    private fun initListener() {

        binding.BtnRecuperar.setOnClickListener {
            validateData()
        }

        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }
    }


    private fun validateData() {
        val email = binding.inputRecuperarSenha.text.toString().trim()

        if (email.isNotBlank()) {
            recoverAccountUser(email)
        } else {
            Toast.makeText(
                requireContext(),
                "Digite seu e-mail",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun recoverAccountUser(email: String) {

        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->

                if (task.isSuccessful) {

                    Toast.makeText(
                        requireContext(),
                        "E-mail de recuperação enviado!",
                        Toast.LENGTH_LONG
                    ).show()

                    findNavController().navigateUp()

                } else {

                    Toast.makeText(
                        requireContext(),
                        task.exception?.message ?: "Erro ao enviar o e-mail",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}