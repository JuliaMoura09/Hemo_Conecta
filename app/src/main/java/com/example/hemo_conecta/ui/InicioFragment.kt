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

        binding.historicoBt.setOnClickListener {
            findNavController().navigate(
                R.id.action_inicioFragment_to_historicoFragment
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

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacksAndMessages(null)
        _binding = null
    }
}

