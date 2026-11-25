package com.example.hemo_conecta

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.hemo_conecta.databinding.FragmentCarteirinhaMedulaBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class CarteirinhaMedulaFragment : Fragment() {


    private var _binding: FragmentCarteirinhaMedulaBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
    private lateinit var reference: DatabaseReference

    private var usuarioReference: DatabaseReference? = null
    private var dadosListener: ValueEventListener? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentCarteirinhaMedulaBinding.inflate(
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

        usuarioReference = reference
            .child("usuarios")
            .child(uid)

        dadosListener = object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {

                val currentBinding = _binding ?: return

                val nome = snapshot.child("nome")
                    .getValue(String::class.java)

                val cadastroRedome = snapshot.child("cadastroRedome")
                    .getValue(String::class.java)

                val dataRedome = snapshot.child("dataCadastroRedome")
                    .getValue(String::class.java)

                currentBinding.nomeUsuario.text = nome ?: ""
                currentBinding.tvCadastroRedome.text =
                    cadastroRedome ?: "Não informado"

                currentBinding.tvDataRedome.text =
                    dataRedome ?: "Não informado"
            }

            override fun onCancelled(error: DatabaseError) {

                context?.let {
                    Toast.makeText(
                        it,
                        "Erro ao carregar os dados.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        usuarioReference?.addValueEventListener(dadosListener!!)
    }

    override fun onDestroyView() {

        dadosListener?.let { listener ->
            usuarioReference?.removeEventListener(listener)
        }

        dadosListener = null
        usuarioReference = null

        super.onDestroyView()

        _binding = null
    }
}