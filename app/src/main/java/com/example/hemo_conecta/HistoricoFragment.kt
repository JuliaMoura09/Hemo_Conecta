package com.example.hemo_conecta

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.hemo_conecta.databinding.FragmentHistoricoBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

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
            findNavController().navigate(R.id.action_historicoFragment_to_adicionarDoacaoFragment)
        }

        carregarDadosDoacoes()
        return binding.root
    }

    private fun carregarDadosDoacoes() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val databaseRef = FirebaseDatabase.getInstance().getReference("doacoes").child(userId)

        databaseRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!isAdded || _binding == null) return

                var qtdSangue = 0
                var qtdMedula = 0
                var qtdPlaquetas = 0

                val listaDoacoes = mutableListOf<Doacao>()

                for (doacaoSnapshot in snapshot.children) {
                    val doacao = doacaoSnapshot.getValue(Doacao::class.java)
                    if (doacao != null) {
                        listaDoacoes.add(doacao)
                        when (doacao.tipo) {
                            "SANGUE" -> qtdSangue++
                            "MEDULA" -> qtdMedula++
                            "PLAQUETAS" -> qtdPlaquetas++
                        }
                    }
                }

                binding.txtQtdSangue.text = qtdSangue.toString()
                binding.txtQtdMedula.text = qtdMedula.toString()
                binding.txtQtdPlaquetas.text = qtdPlaquetas.toString()

                val vidasSalvas = qtdSangue * 4
                val sangueDoadoMl = qtdSangue * 450

                binding.txtVidasSalvas.text = vidasSalvas.toString()
                binding.txtSangueDoado.text = "$sangueDoadoMl ml"

                // preenche os cards com as últimas doações cadastradas (final da lista)
                val doacoesInvertidas = listaDoacoes.reversed()

                binding.txtDataCard1.text = "Nenhuma doação recente"
                binding.txtLocalCard1.text = "-"

                binding.txtDataCard2.text = "Nenhuma doação recente"
                binding.txtLocalCard2.text = "-"

                binding.txtDataCard3.text = "Nenhuma doação recente"
                binding.txtLocalCard3.text = "-"

                // preenche os cards se existir pelo menos uma doação
                if (doacoesInvertidas.isNotEmpty()) {
                    binding.txtDataCard1.text = doacoesInvertidas[0].data
                    binding.txtLocalCard1.text = doacoesInvertidas[0].local
                }

                if (doacoesInvertidas.size > 1) {
                    binding.txtDataCard2.text = doacoesInvertidas[1].data
                    binding.txtLocalCard2.text = doacoesInvertidas[1].local
                }

                if (doacoesInvertidas.size > 2) {
                    binding.txtDataCard3.text = doacoesInvertidas[2].data
                    binding.txtLocalCard3.text = doacoesInvertidas[2].local
                }
            }

            override fun onCancelled(error: DatabaseError) {
                if (!isAdded || _binding == null) return
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}