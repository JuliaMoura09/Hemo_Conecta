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
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class CuponsFragment : Fragment() {

    private var _binding: FragmentCuponsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCuponsBinding.inflate(inflater, container, false)

        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.recyclerViewCupons.layoutManager = LinearLayoutManager(requireContext())

        carregarCuponsDoFirebase()
        return binding.root
    }

    private fun carregarCuponsDoFirebase() {
        val databaseRef = FirebaseDatabase.getInstance().getReference("parceiros")

        databaseRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!isAdded || _binding == null) return

                val listaCupons = mutableListOf<Cupom>()

                // verifica as lojas parceiras
                for (parceiroSnapshot in snapshot.children) {
                    val nomeParceiro = parceiroSnapshot.key ?: "Parceiro"
                    val cuponsSnapshot = parceiroSnapshot.child("cupons")

                    // procura os cupons daquele parceiro
                    for (cupomItem in cuponsSnapshot.children) {
                        val cupom = cupomItem.getValue(Cupom::class.java)
                        // Sverifica se o cupom está ativo
                        if (cupom != null && cupom.ativo) {
                            cupom.parceiro = nomeParceiro
                            listaCupons.add(cupom)
                        }
                    }
                }

                binding.recyclerViewCupons.adapter = CupomAdapter(listaCupons)
            }

            override fun onCancelled(error: DatabaseError) {
                if (!isAdded || _binding == null) return
                Toast.makeText(requireContext(), "Erro ao carregar cupons: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}