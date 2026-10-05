package com.example.hemo_conecta.questionario

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.example.hemo_conecta.R
import com.example.hemo_conecta.databinding.FragmentQuest5Binding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModel
import com.example.hemo_conecta.questionario.Quest1Fragment.QuestViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class Quest5Fragment : Fragment() {
    private val viewModel: QuestViewModel by activityViewModels()
    private var _binding: FragmentQuest5Binding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
    private lateinit var reference: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentQuest5Binding.inflate(inflater, container, false)

        auth = FirebaseAuth.getInstance()
        reference = FirebaseDatabase.getInstance().reference

        initListener()

        return binding.root
    }

    private fun initListener() {
        binding.opcaoNao.setOnClickListener {
            viewModel.resposta5= false

            val apto = viewModel.respostasEstaoCorretas()
            salvarResultado(apto)

            if (apto) {
                findNavController().navigate(R.id.action_quest5Fragment_to_aptoFragment)
            } else {
                findNavController().navigate(R.id.action_quest5Fragment_to_naoAptoFragment)
            }
        }

        binding.opcaoSimBt.setOnClickListener {
            viewModel.resposta5= true

            val apto = viewModel.respostasEstaoCorretas()

            salvarResultado(apto)

            if (apto) {
                findNavController().navigate(R.id.action_quest5Fragment_to_aptoFragment)
            } else {
                findNavController().navigate(R.id.action_quest5Fragment_to_naoAptoFragment)
            }
        }
    }

    private fun salvarResultado(apto: Boolean) {

        val usuario = FirebaseAuth.getInstance().currentUser

        if (usuario == null) {
            return
        }

        val idUsuario = usuario.uid

        val dados = mapOf(
            "idUsuario" to idUsuario,
            "apto" to apto,
            "data" to "data" to SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date()),
            "horario" to "horário" to SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
        )

        reference.child("questionarios")
            .child(idUsuario)
            .setValue(dados)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}