package com.example.hemo_conecta.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.hemo_conecta.R
import com.example.hemo_conecta.databinding.FragmentDadosDoadorAmbosBinding
import com.example.hemo_conecta.databinding.FragmentInicioBinding

class DadosDoadorAmbosFragment : Fragment() {

    private var _binding: FragmentDadosDoadorAmbosBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDadosDoadorAmbosBinding.inflate(inflater, container, false)

        binding.BtnAtualizar.setOnClickListener {
            findNavController().navigate(
                R.id.action_DoadorAmbosFragment_to_inicioFragment
            )
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
