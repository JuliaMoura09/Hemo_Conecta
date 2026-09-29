package com.example.hemo_conecta.ui.auth

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.example.hemo_conecta.R
import com.example.hemo_conecta.databinding.FragmentLoginBinding
import com.google.firebase.auth.FirebaseAuth

class LoginFragment : Fragment() {
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()

        initListener()
    }

    private fun initListener() {
        binding.btnTelaCadastro.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_cadastroFragment2)
        }

        binding.btnRecuperarSenha.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_RecuperarSenhaFragment)
        }

        binding.loginBt.setOnClickListener {
            val email = binding.emailLoginInput.text.toString().trim()
            val senha = binding.senhaLoginInput.text.toString().trim()

            if (email.isNotEmpty() && senha.isNotEmpty()) {
                auth.signInWithEmailAndPassword(email, senha)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            findNavController().navigate(R.id.action_global_inicioFragment)
                        } else {
                            Toast.makeText(
                                requireContext(),
                                "E-mail ou senha incorretos",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
            } else {
                Toast.makeText(
                    requireContext(),
                    "Preencha o e-mail e a senha",
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