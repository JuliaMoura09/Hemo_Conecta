
package com.example.hemo_conecta.ui

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.hemo_conecta.databinding.FragmentNotificacoesBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class NotificacoesFragment : Fragment() {

    private var _binding: FragmentNotificacoesBinding? = null
    private val binding get() = _binding!!

    private val auth = FirebaseAuth.getInstance()
    private val reference = FirebaseDatabase.getInstance().reference

    private val handler = Handler(Looper.getMainLooper())
    private val vinteSegundos = 20_000L

    private lateinit var adapter: NotificacaoAdapter
    private var listenerNotificacoes: ValueEventListener? = null
    private var referenciaNotificacoes: DatabaseReference? = null

    private val verificarNovamente = Runnable {
        if (isAdded) {
            verificarQuestionario()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNotificacoesBinding.inflate(
            inflater,
            container,
            false
        )

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = NotificacaoAdapter()

        binding.rvNotificacoes.layoutManager =
            LinearLayoutManager(requireContext())

        binding.rvNotificacoes.adapter = adapter

        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        carregarNotificacoes()
        verificarQuestionario()
    }

    private fun carregarNotificacoes() {
        val usuario = auth.currentUser ?: return

        referenciaNotificacoes = reference
            .child("notificacoes")
            .child(usuario.uid)

        listenerNotificacoes = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val lista = mutableListOf<Notificacao>()

                for (item in snapshot.children) {
                    val notificacao =
                        item.getValue(Notificacao::class.java)

                    if (notificacao != null) {
                        notificacao.id = item.key ?: ""
                        lista.add(notificacao)
                    }
                }

                lista.sortByDescending { it.data }
                adapter.atualizarLista(lista)
            }

            override fun onCancelled(error: DatabaseError) {
                if (isAdded) {
                    Toast.makeText(
                        requireContext(),
                        "Não foi possível carregar as notificações.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        referenciaNotificacoes?.addValueEventListener(
            listenerNotificacoes!!
        )
    }

    private fun verificarQuestionario() {
        val usuario = auth.currentUser ?: return

        reference
            .child("questionarios")
            .child(usuario.uid)
            .child("data")
            .get()
            .addOnSuccessListener { resultado ->
                if (!isAdded || !resultado.exists()) {
                    return@addOnSuccessListener
                }

                val timestamp = resultado.getValue(Long::class.java)
                    ?: return@addOnSuccessListener

                val tempoPassado =
                    System.currentTimeMillis() - timestamp

                if (tempoPassado >= vinteSegundos) {
                    criarNotificacaoQuestionario(timestamp)
                } else {
                    handler.removeCallbacks(verificarNovamente)

                    handler.postDelayed(
                        verificarNovamente,
                        vinteSegundos - tempoPassado
                    )
                }
            }
    }

    private fun criarNotificacaoQuestionario(timestamp: Long) {
        val usuario = auth.currentUser ?: return

        val notificacao = Notificacao(
            id = timestamp.toString(),
            titulo = "Está na hora de refazer o questionário",
            descricao = "Responda novamente ao questionário para manter suas informações atualizadas.",
            data = timestamp + vinteSegundos,
            lida = false
        )

        reference
            .child("notificacoes")
            .child(usuario.uid)
            .child(timestamp.toString())
            .setValue(notificacao)
    }

    override fun onDestroyView() {
        handler.removeCallbacks(verificarNovamente)

        if (listenerNotificacoes != null) {
            referenciaNotificacoes?.removeEventListener(
                listenerNotificacoes!!
            )
        }

        listenerNotificacoes = null
        referenciaNotificacoes = null

        _binding = null
        super.onDestroyView()
    }
}
