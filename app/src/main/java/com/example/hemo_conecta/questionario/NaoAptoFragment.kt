package com.example.hemo_conecta.questionario

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.hemo_conecta.R
import com.example.hemo_conecta.databinding.FragmentNaoAptoBinding

class NaoAptoFragment : Fragment() {

    private var _binding: FragmentNaoAptoBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNaoAptoBinding.inflate(inflater, container, false)

        binding.voltarBt.setOnClickListener {
            findNavController().navigate(
                R.id.action_naoAptoFragment_to_inicioFragment
            )

        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
