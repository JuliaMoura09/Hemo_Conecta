package com.example.hemo_conecta.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.hemo_conecta.databinding.FragmentCarteirinhaAmbosBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class CarteirinhaAmbosFragment : Fragment() {

    private var _binding: FragmentCarteirinhaAmbosBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth
    private lateinit var reference: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentCarteirinhaAmbosBinding.inflate(
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
                    // resgata os dados salvos anteriormente no firebase
                    val nome = snapshot.child("nome").getValue(String::class.java)
                    val tipoSanguineo = snapshot.child("tipoSanguineo").getValue(String::class.java)
                    val cadastroRedome = snapshot.child("cadastroRedome").getValue(String::class.java)
                    val cadastroHemoes = snapshot.child("cadastroHemoes").getValue(String::class.java)

                    // atriui os valores salvos no banco de dados aos textviews no CarteirinhaAmbos
                    binding.nomeUsuario.text = nome ?: ""
                    binding.tvTipoSanguineo.text = tipoSanguineo ?: ""
                    binding.tvCadastroRedome.text = cadastroRedome ?: "Não é doador de medula"
                    binding.tvCadastroHemoes.text = cadastroHemoes ?: "Não é doador de sangue"
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
