package com.example.hemo_conecta.ui

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.hemo_conecta.R
import com.example.hemo_conecta.databinding.FragmentInicioBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class InicioFragment : Fragment() {

    private var _binding: FragmentInicioBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth

    private lateinit var reference: DatabaseReference

    private val vinteSegundos= 5000L
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInicioBinding.inflate(inflater, container, false)

        auth = FirebaseAuth.getInstance()
        reference = FirebaseDatabase.getInstance().reference

        binding.btnNotificacoes.setOnClickListener {
            findNavController().navigate(
                R.id.action_inicioFragment_to_notificacoesFragment
            )
        }

        binding.processoBt.setOnClickListener {
            findNavController().navigate(
                R.id.action_inicioFragment_to_processoDoacaoFragment
            )
        }

        binding.questBt.setOnClickListener {
            findNavController().navigate(
                R.id.action_inicioFragment_to_quest1Fragment
            )
        }

        binding.cuponsBt.setOnClickListener {
            findNavController().navigate(
                R.id.action_inicioFragment_to_cuponsFragment
            )
        }

        verificarQuestionario()

        return binding.root
    }

    private fun verificarQuestionario() {

        val usuario = auth.currentUser

        if (usuario == null) {
            return
        }

        val idUsuario = usuario.uid

        reference
            .child("questionarios")
            .child(idUsuario)
            .get()
            .addOnSuccessListener { resultado ->

                if (!resultado.exists()) {
                    return@addOnSuccessListener
                }

                val timestamp = resultado.child("data").getValue(Long::class.java) ?: return@addOnSuccessListener
                val agora = System.currentTimeMillis()
                val tempoPassado = agora - timestamp

                if (tempoPassado >= vinteSegundos) {
                    Toast.makeText(requireContext(),"Está na hora de refazer o questionário",Toast.LENGTH_SHORT).show()
                } else {
                    //Ainda não passaram 20 segundos.
                    //Calcula quanto falta.
                    val tempoRestante = vinteSegundos - tempoPassado

                    handler.postDelayed({

                        if (isAdded) {
                            Toast.makeText(requireContext(), "Está na hora de refazer o questionário", Toast.LENGTH_SHORT).show()
                        }

                    }, tempoRestante)
                }
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacksAndMessages(null)
        _binding = null
    }
}
