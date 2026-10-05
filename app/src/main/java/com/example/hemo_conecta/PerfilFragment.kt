package com.example.hemo_conecta

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.example.hemo_conecta.databinding.FragmentPerfilBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class PerfilFragment : Fragment() {

    private var _binding: FragmentPerfilBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
    private lateinit var reference: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentPerfilBinding.inflate(inflater, container, false)

        auth = FirebaseAuth.getInstance()
        reference = FirebaseDatabase.getInstance().reference

        carregarDados()

        return binding.root
    }

    private fun carregarDados() {

        val uid = auth.currentUser?.uid ?: ""

        Toast.makeText(
            requireContext(),
            "UID atual: $uid",
            Toast.LENGTH_LONG
        ).show()


        reference
            .child("usuarios")
            .child(uid)
            .addValueEventListener(object : ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot) {

                    val nome = snapshot.child("nome").getValue(String::class.java)
                    val email = snapshot.child("email").getValue(String::class.java)
                    val tipoSanguineo =
                        snapshot.child("tipoSanguineo").getValue(String::class.java)

                    binding.nomeUsuario.text = nome
                    binding.emailUsuario.text = email
                    binding.tipoSanguineoPerfil.text = tipoSanguineo

                    if (!nome.isNullOrEmpty()) {

                        val partes = nome.split(" ")

                        if (partes.size >= 2) {
                            binding.iniciaisPerfil.text =
                                "${partes[0].first()}${partes.last().first()}"
                        } else {
                            binding.iniciaisPerfil.text =
                                partes[0].first().toString()
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                    Toast.makeText(
                        requireContext(),
                        "Erro ao carregar os dados",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnLogout.setOnClickListener {

            auth.signOut()

            findNavController().navigate(
                R.id.action_perfilFragment_to_authentication
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}