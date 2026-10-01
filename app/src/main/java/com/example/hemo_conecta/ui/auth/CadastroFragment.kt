package com.example.hemo_conecta.ui.auth

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.example.hemo_conecta.R
import com.example.hemo_conecta.databinding.FragmentCadastroBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class CadastroFragment : Fragment() {

    private var _binding: FragmentCadastroBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
    private lateinit var reference: DatabaseReference

    private var tipoSanguineo = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentCadastroBinding.inflate(inflater, container, false)

        auth = FirebaseAuth.getInstance()
        reference = FirebaseDatabase.getInstance().reference

        initListener()
        selecionarTipoSanguineo()

        return binding.root
    }

    private fun initListener() {

        binding.cadastroBt.setOnClickListener {

            val nome = binding.nomeInput.text.toString()
            val cpf = binding.cpfInput.text.toString()
            val nascimento = binding.nascInput.text.toString()
            val email = binding.emailCadastroInput.text.toString()
            val senha = binding.senhaCadastroInput.text.toString()
            val confirmacao = binding.senhaConfirmacaoInput.text.toString()

            if (senha != confirmacao) {

                Toast.makeText(
                    requireContext(),
                    "As senhas não coincidem",
                    Toast.LENGTH_SHORT
                ).show()

                return@setOnClickListener
            }

            registerUser(
                nome,
                cpf,
                nascimento,
                email,
                senha
            )
        }
    }

    private fun selecionarTipoSanguineo() {

        binding.cardApositivo.setOnClickListener {
            tipoSanguineo = "A+"
        }

        binding.cardAnegativo.setOnClickListener {
            tipoSanguineo = "A-"
        }

        binding.cardBpositivo.setOnClickListener {
            tipoSanguineo = "B+"
        }

        binding.cardBnegativo.setOnClickListener {
            tipoSanguineo = "B-"
        }

        binding.cardABpositivo.setOnClickListener {
            tipoSanguineo = "AB+"
        }

        binding.cardABnegativo.setOnClickListener {
            tipoSanguineo = "AB-"
        }

        binding.cardOpositivo.setOnClickListener {
            tipoSanguineo = "O+"
        }

        binding.cardOnegativo.setOnClickListener {
            tipoSanguineo = "O-"
        }
    }

    private fun registerUser(
        nome: String,
        cpf: String,
        nascimento: String,
        email: String,
        senha: String
    ) {

        auth.createUserWithEmailAndPassword(email, senha)
            .addOnCompleteListener { task ->

                if (task.isSuccessful) {

                    val uid = auth.currentUser?.uid ?: ""

                    val dados = hashMapOf(
                        "nome" to nome,
                        "cpf" to cpf,
                        "nascimento" to nascimento,
                        "tipoSanguineo" to tipoSanguineo,
                        "email" to email
                    )

                    reference
                        .child("usuarios")
                        .child(uid)
                        .setValue(dados)
                        .addOnCompleteListener { resultado ->

                            if (resultado.isSuccessful) {

                                Toast.makeText(
                                    requireContext(),
                                    "Cadastro realizado!",
                                    Toast.LENGTH_SHORT
                                ).show()

                                findNavController().navigate(
                                    R.id.action_global_inicioFragment
                                )

                            } else {

                                Toast.makeText(
                                    requireContext(),
                                    "Erro ao salvar os dados",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }

                } else {

                    Toast.makeText(
                        requireContext(),
                        task.exception?.message,
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