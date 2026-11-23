
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.MutableData
import com.google.firebase.database.Transaction
import com.google.firebase.database.ValueEventListener

class CuponsFragment : Fragment() {

    private var _binding: FragmentCuponsBinding? = null
    private val binding get() = _binding!!
    private val database = FirebaseDatabase.getInstance()
    private val uid: String?
        get() = FirebaseAuth.getInstance().currentUser?.uid
    private var pontosUsuario = 0
    private val listaCupons = mutableListOf<Cupom>()
    private val cuponsResgatados = mutableSetOf<String>()
    private var cupomAdapter: CupomAdapter? = null
    private var usuarioListener: ValueEventListener? = null
    private var parceirosListener: ValueEventListener? = null
    private var usuarioRef = database.reference
    private var parceirosRef = database.reference

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentCuponsBinding.inflate(
            inflater,
            container,
            false
        )

        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.recyclerViewCupons.layoutManager =
            LinearLayoutManager(requireContext())

        val usuarioId = uid

        if (usuarioId == null) {
            Toast.makeText(
                requireContext(),
                "Faça login para visualizar seus cupons.",
                Toast.LENGTH_SHORT
            ).show()
            return binding.root
        }

        usuarioRef = database
            .getReference("usuarios")
            .child(usuarioId)

        parceirosRef = database
            .getReference("parceiros")

        carregarDadosUsuario()
        carregarCuponsDoFirebase()
        return binding.root
    }

    private fun carregarDadosUsuario() {
        usuarioListener = object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {
                if (!isAdded || _binding == null) return
                pontosUsuario = snapshot
                    .child("pontos")
                    .getValue(Int::class.java) ?: 0
                cuponsResgatados.clear()

                val resgatadosSnapshot =
                    snapshot.child("cupons_resgatados")

                for (parceiroSnapshot in resgatadosSnapshot.children) {
                    val parceiroId = parceiroSnapshot.key ?: continue

                    for (cupomSnapshot in parceiroSnapshot.children) {
                        val cupomId = cupomSnapshot.key ?: continue
                        val resgatado = cupomSnapshot
                            .child("resgatado")
                            .getValue(Boolean::class.java) ?: false

                        if (resgatado) {
                            cuponsResgatados.add("$parceiroId/$cupomId")
                        }
                    }
                }

                cupomAdapter?.atualizarEstado(
                    pontosUsuario,
                    cuponsResgatados
                )
            }

            override fun onCancelled(error: DatabaseError) {

                if (!isAdded || _binding == null) return
                Toast.makeText(
                    requireContext(),
                    "Erro ao buscar dados: ${error.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        usuarioRef.addValueEventListener(usuarioListener!!)
    }

    private fun carregarCuponsDoFirebase() {
        parceirosListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!isAdded || _binding == null) return
                listaCupons.clear()

                for (parceiroSnapshot in snapshot.children) {
                    val parceiroId = parceiroSnapshot.key ?: continue
                    val nomeParceiro = parceiroId
                    val cuponsSnapshot =
                        parceiroSnapshot.child("cupons")

                    for (cupomItem in cuponsSnapshot.children) {
                        val cupom = cupomItem.getValue(Cupom::class.java)

                        if (cupom != null && cupom.ativo) {
                            cupom.id = cupomItem.key ?: ""
                            cupom.parceiroId = parceiroId
                            cupom.parceiro = nomeParceiro
                            listaCupons.add(cupom)
                        }
                    }
                }

                cupomAdapter = CupomAdapter(
                    listaCupons = listaCupons,
                    pontosUsuario = pontosUsuario,
                    cuponsResgatados = cuponsResgatados
                ) { cupom ->
                    resgatarCupom(cupom)
                }
                binding.recyclerViewCupons.adapter = cupomAdapter
            }

            override fun onCancelled(error: DatabaseError) {

                if (!isAdded || _binding == null) return
                Toast.makeText(
                    requireContext(),
                    "Erro ao carregar cupons: ${error.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        parceirosRef.addValueEventListener(parceirosListener!!)
    }

    private fun resgatarCupom(cupom: Cupom) {
        val usuarioId = uid ?: return
        val custo = cupom.pontos ?: 0

        if (custo <= 0) {
            Toast.makeText(
                requireContext(),
                "Pontuação inválida para este cupom.",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val cupomId = cupom.id
        val parceiroId = cupom.parceiroId

        if (cupomId.isBlank() || parceiroId.isBlank()) return
        usuarioRef.runTransaction(object : Transaction.Handler {

            override fun doTransaction(
                dados: MutableData
            ): Transaction.Result {

                val pontosAtuais = dados
                    .child("pontos")
                    .getValue(Int::class.java) ?: 0

                val registroCupom = dados
                    .child("cupons_resgatados")
                    .child(parceiroId)
                    .child(cupomId)

                if (registroCupom.value != null) {
                    return Transaction.abort()
                }

                if (pontosAtuais < custo) {
                    return Transaction.abort()
                }

                dados.child("pontos").value =
                    pontosAtuais - custo

                registroCupom.child("codigo").value =
                    cupom.codCupom ?: ""

                registroCupom.child("parceiro").value =
                    cupom.parceiro ?: ""

                registroCupom.child("pontosGastos").value =
                    custo

                registroCupom.child("resgatado").value = true

                registroCupom.child("dataResgate").value =
                    System.currentTimeMillis()

                return Transaction.success(dados)
            }

            override fun onComplete(
                error: DatabaseError?,
                committed: Boolean,
                snapshot: DataSnapshot?
            ) {

                if (!isAdded || _binding == null) return
                when {
                    error != null -> {
                        Toast.makeText(
                            requireContext(),
                            "Erro ao resgatar cupom: ${error.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    committed -> {

                        Toast.makeText(
                            requireContext(),
                            "Cupom resgatado com sucesso!",
                            Toast.LENGTH_LONG
                        ).show()
                    }

                    else -> {
                        Toast.makeText(
                            requireContext(),
                            "Você já resgatou este cupom ou não possui pontos suficientes.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        })
    }

    override fun onDestroyView() {

        usuarioListener?.let {
            usuarioRef.removeEventListener(it)
        }

        parceirosListener?.let {
            parceirosRef.removeEventListener(it)
        }

        usuarioListener = null
        parceirosListener = null
        cupomAdapter = null

        super.onDestroyView()

        _binding = null
    }
}
