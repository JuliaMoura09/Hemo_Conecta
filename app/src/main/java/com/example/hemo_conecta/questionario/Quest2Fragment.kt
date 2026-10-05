package com.example.hemo_conecta.questionario

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.example.hemo_conecta.R
import com.example.hemo_conecta.databinding.FragmentQuest2Binding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModel
import com.example.hemo_conecta.questionario.Quest1Fragment.QuestViewModel

class Quest2Fragment : Fragment() {
    private val viewModel: QuestViewModel by activityViewModels()
    private var _binding: FragmentQuest2Binding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
    private lateinit var reference: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentQuest2Binding.inflate(inflater, container, false)

        auth = FirebaseAuth.getInstance()
        reference = FirebaseDatabase.getInstance().reference

        initListener()

        return binding.root
    }

    private fun initListener() {
        binding.opcaoNao.setOnClickListener {
            viewModel.resposta2= false

            findNavController().navigate(
                R.id.action_quest2Fragment_to_quest3Fragment)
        }

        binding.opcaoSimBt.setOnClickListener {
            viewModel.resposta2= true

            findNavController().navigate(
                R.id.action_quest2Fragment_to_quest3Fragment)
        }
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}