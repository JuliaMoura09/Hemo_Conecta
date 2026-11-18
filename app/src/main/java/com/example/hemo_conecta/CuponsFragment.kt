package com.example.hemo_conecta

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.hemo_conecta.databinding.FragmentCuponsBinding
import com.google.firebase.firestore.FirebaseFirestore

class CuponsFragment : Fragment() {

    private var _binding: FragmentCuponsBinding? = null
    private val binding get() = _binding!!

    private lateinit var db: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCuponsBinding.inflate(inflater, container, false)
        db = FirebaseFirestore.getInstance()

        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        carregarCuponsDoFirebase()
        return binding.root
    }

    private fun carregarCuponsDoFirebase() {
        db.collection("cupons")
            .get()
            .addOnSuccessListener { result ->
                val listaCupons = mutableListOf<Cupom>()
                for (document in result) {
                    val cupom = document.toObject(Cupom::class.java)
                    listaCupons.add(cupom)
                }

                binding.recyclerViewCupons.layoutManager = LinearLayoutManager(requireContext())
                binding.recyclerViewCupons.adapter = CupomAdapter(listaCupons)
            }
            .addOnFailureListener { exception ->
                Toast.makeText(requireContext(), "Erro ao carregar cupons: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}