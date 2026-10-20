package com.example.hemo_conecta

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.hemo_conecta.databinding.FragmentHistoricoBinding
class HistoricoFragment : Fragment() {

    private var _binding: FragmentHistoricoBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHistoricoBinding.inflate(
            inflater,
            container,
            false
        )

        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.buttonCadastrarDoacao.setOnClickListener {
            findNavController().navigate( R.id.action_historicoFragment_to_adicionarDoacaoFragment)

        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
