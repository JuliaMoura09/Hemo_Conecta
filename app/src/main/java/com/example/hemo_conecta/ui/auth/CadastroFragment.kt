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
        binding.buttonCadastro.setOnClickListener {
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

            val cards = listOf(
                binding.cardApositivo,
                binding.cardAnegativo,
                binding.cardBpositivo,
                binding.cardBnegativo,
                binding.cardABpositivo,
                binding.cardABnegativo,
                binding.cardOpositivo,
                binding.cardOnegativo
            )

            val tiposMap = mapOf(
                binding.cardApositivo to "A+",
                binding.cardAnegativo to "A-",
                binding.cardBpositivo to "B+",
                binding.cardBnegativo to "B-",
                binding.cardABpositivo to "AB+",
                binding.cardABnegativo to "AB-",
                binding.cardOpositivo to "O+",
                binding.cardOnegativo to "O-"
            )

            // código para que o card fique colorido ao usuário selecionar-lo
            cards.forEach { selectedCard ->
                selectedCard.setOnClickListener {

                    cards.forEach { card ->
                        card.isSelected = false
                        card.setCardBackgroundColor(
                            androidx.core.content.ContextCompat.getColor(requireContext(), R.color.cinza)
                        )
                    }

                    selectedCard.isSelected = true
                    selectedCard.setCardBackgroundColor(
                        androidx.core.content.ContextCompat.getColor(requireContext(), R.color.vermelho)
                    )

                    tipoSanguineo = tiposMap[selectedCard] ?: ""
                }
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

    private fun navegabilidadeTipoDoador() {
        val selectedId = binding.radioGroupDoador.checkedRadioButtonId

        when (selectedId) {
            R.id.option_doador_sangue -> {
                findNavController().navigate(R.id.action_cadastroFragment2_to_DoadorSangueFragment)
            }
            R.id.option_doador_medula -> {
                findNavController().navigate(R.id.action_cadastroFragment2_to_DoadorMedulaFragment)
            }
            R.id.option_doador_ambos -> {
                findNavController().navigate(R.id.action_cadastroFragment2_to_DoadorAmbosFragment)
            }
            R.id.option_nao_doador -> {
                findNavController().navigate(R.id.action_global_inicioFragment)
            }

            else -> {
                Toast.makeText(
                    requireContext(),
                    "Por favor, selecione uma opção na quinta pergunta",
                    Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}