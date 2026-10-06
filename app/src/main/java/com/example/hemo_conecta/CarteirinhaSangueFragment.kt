package com.example.hemo_conecta.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.hemo_conecta.databinding.FragmentCarteirinhaSangueBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class CarteirinhaSangueFragment : Fragment() {

    private var _binding: FragmentCarteirinhaSangueBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth
    private lateinit var reference: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentCarteirinhaSangueBinding.inflate(
            inflater,
            container,
            false
        )

        auth = FirebaseAuth.getInstance()
        reference = FirebaseDatabase.getInstance().reference

        buscarDadosCarteirinha()
        return binding.root
    }

    private fun buscarDadosCarteirinha() {
        val uid = auth.currentUser?.uid ?: return

        reference
            .child("usuarios")
            .child(uid)
            .addValueEventListener(object : ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot) {
                    // Resgata os dados do Firebase informados pelo usuário
                    val nome = snapshot.child("nome").getValue(String::class.java)
                    val cadastroHemoes = snapshot.child("cadastroHemoes").getValue(String::class.java)
                    val dataHemoes = snapshot.child("dataCadastroHemoes").getValue(String::class.java)
                    val tipoSanguineo = snapshot.child("tipoSanguineo").getValue(String::class.java)

                    // atribui os valores salvos no banco de dados às views
                    binding.nomeUsuario.text = nome ?: ""
                    binding.tvCadastroHemoes.text = cadastroHemoes ?: "Não informado"
                    binding.tvDataHemoes.text = dataHemoes ?: "Não informado"
                    binding.tvTipoSanguineo.text = tipoSanguineo ?: "Não informado"
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(
                        requireContext(),
                        "Erro ao carregar os dados.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}