package com.example.hemo_conecta.questionario

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.example.hemo_conecta.R
import com.example.hemo_conecta.databinding.FragmentQuest1Binding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModel

class Quest1Fragment : Fragment() {

    private val viewModel: QuestViewModel by activityViewModels()

    private var _binding: FragmentQuest1Binding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
    private lateinit var reference: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentQuest1Binding.inflate(inflater, container, false)

        auth = FirebaseAuth.getInstance()
        reference = FirebaseDatabase.getInstance().reference

        initListener()

        return binding.root
    }

    class QuestViewModel : ViewModel() {

        var resposta1: Boolean? = null
        var resposta2: Boolean? = null
        var resposta3: Boolean? = null
        var resposta4: Boolean? = null
        var resposta5: Boolean? = null

        fun respostasEstaoCorretas(): Boolean {
            return resposta1 == true &&
                    resposta2 == true &&
                    resposta3 == false &&
                    resposta4 == false &&
                    resposta5 == false
        }
    }

    private fun initListener() {
        binding.opcaoNaoBt.setOnClickListener {
            viewModel.resposta1= false

            findNavController().navigate(
                R.id.action_quest1Fragment_to_quest2Fragment)
        }

        binding.opcaoSimBt.setOnClickListener {
            viewModel.resposta1= true

            findNavController().navigate(
                R.id.action_quest1Fragment_to_quest2Fragment)
        }
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}