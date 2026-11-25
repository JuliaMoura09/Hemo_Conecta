
package com.example.hemo_conecta

import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.MutableData
import com.google.firebase.database.Transaction

class ExemploArtigoFragment : Fragment() {

    private var countDownTimer: CountDownTimer? = null
    private var podeResgatar = false
    private lateinit var btnConcluirLeitura: Button
    private var artigoId: String = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val view = inflater.inflate(
            R.layout.fragment_exemplo_artigo,
            container,
            false
        )

        val titulo = arguments?.getString("titulo").orEmpty()

        artigoId = arguments?.getString("artigoId")
            ?.takeIf { it.isNotBlank() }
            ?: titulo.hashCode().toString()

        view.findViewById<TextView>(R.id.categoriaArtigo).text =
            arguments?.getString("categoria").orEmpty()

        view.findViewById<TextView>(R.id.tituloArtigo).text =
            titulo

        view.findViewById<TextView>(R.id.autorArtigo).text =
            arguments?.getString("autor").orEmpty()

        view.findViewById<TextView>(R.id.introducaoArtigo).text =
            arguments?.getString("introducao").orEmpty()

        view.findViewById<TextView>(R.id.textoPrincipalArtigo).text =
            arguments?.getString("mensagem").orEmpty()

        val curiosidade = arguments?.getString("curiosidade").orEmpty()

        view.findViewById<TextView>(R.id.curiosidadeArtigo).text =
            curiosidade

        if (curiosidade.isBlank()) {
            view.findViewById<View>(R.id.dicaArtigo).visibility =
                View.GONE
        }

        view.findViewById<View>(R.id.btnBack).setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        btnConcluirLeitura =
            view.findViewById(R.id.btnConcluirLeitura)

        btnConcluirLeitura.isEnabled = false

        iniciarTemporizador(90000)

        btnConcluirLeitura.setOnClickListener {

            if (podeResgatar) {

                btnConcluirLeitura.isEnabled = false

                verificarEAtribuirPontos()
            }
        }

        return view
    }

    private fun iniciarTemporizador(tempoMillis: Long) {

        countDownTimer = object : CountDownTimer(
            tempoMillis,
            1000
        ) {

            override fun onTick(millisUntilFinished: Long) {

                if (!isAdded) return

                val segundosRestantes = millisUntilFinished / 1000

                btnConcluirLeitura.text =
                    "Leia o artigo (${segundosRestantes}s)"
            }

            override fun onFinish() {

                if (!isAdded) return

                podeResgatar = true

                btnConcluirLeitura.isEnabled = true

                btnConcluirLeitura.setText(R.string.ui_concluir_leitura_e_ganhar_pontos)
            }
        }.start()
    }

    private fun verificarEAtribuirPontos() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid == null) {

            Toast.makeText(
                requireContext(),
                R.string.ui_usuario_nao_encontrado,
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        val usuarioRef = FirebaseDatabase.getInstance()
            .getReference("usuarios")
            .child(uid)

        usuarioRef.runTransaction(object : Transaction.Handler {

            override fun doTransaction(
                dados: MutableData
            ): Transaction.Result {

                val artigoLido = dados
                    .child("artigos_lidos")
                    .child(artigoId)
                    .getValue(Boolean::class.java) ?: false

                if (artigoLido) {
                    return Transaction.abort()
                }

                val pontosAtuais = dados
                    .child("pontos")
                    .getValue(Int::class.java) ?: 0

                dados
                    .child("artigos_lidos")
                    .child(artigoId)
                    .value = true

                dados.child("pontos").value =
                    pontosAtuais + 10

                return Transaction.success(dados)
            }

            override fun onComplete(
                error: com.google.firebase.database.DatabaseError?,
                committed: Boolean,
                snapshot: com.google.firebase.database.DataSnapshot?
            ) {

                if (!isAdded) return
                when {

                    error != null -> {

                        Toast.makeText(
                            requireContext(),
                            "Erro ao registrar leitura: ${error.message}",
                            Toast.LENGTH_LONG
                        ).show()
                        btnConcluirLeitura.isEnabled = true
                    }

                    committed -> {

                        Toast.makeText(
                            requireContext(),
                            R.string.ui_parabens_voce_ganhou_10_pontos,
                            Toast.LENGTH_LONG
                        ).show()

                        findNavController().navigateUp()
                    }

                    else -> {

                        Toast.makeText(
                            requireContext(),
                            R.string.ui_voce_ja_ganhou_pontos_por_este_artigo,
                            Toast.LENGTH_LONG
                        ).show()

                        findNavController().navigateUp()
                    }
                }
            }
        })
    }

    override fun onDestroyView() {

        countDownTimer?.cancel()
        countDownTimer = null
        super.onDestroyView()
    }

    companion object {

        fun newInstance(
            categoria: String,
            titulo: String,
            autor: String,
            introducao: String,
            mensagem: String,
            curiosidade: String,
            artigoId: String? = null
        ): ExemploArtigoFragment {

            val fragment = ExemploArtigoFragment()

            fragment.arguments = Bundle().apply {

                putString("categoria", categoria)
                putString("titulo", titulo)
                putString("autor", autor)
                putString("introducao", introducao)
                putString("mensagem", mensagem)
                putString("curiosidade", curiosidade)
                putString("artigoId", artigoId)
            }

            return fragment
        }
    }
}
