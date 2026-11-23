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
        artigoId = titulo.hashCode().toString()

        view.findViewById<TextView>(R.id.categoriaArtigo).text =
            arguments?.getString("categoria").orEmpty()

        view.findViewById<TextView>(R.id.tituloArtigo).text = titulo

        view.findViewById<TextView>(R.id.autorArtigo).text =
            arguments?.getString("autor").orEmpty()

        view.findViewById<TextView>(R.id.introducaoArtigo).text =
            arguments?.getString("introducao").orEmpty()

        view.findViewById<TextView>(R.id.textoPrincipalArtigo).text =
            arguments?.getString("mensagem").orEmpty()

        val curiosidade = arguments?.getString("curiosidade").orEmpty()

        view.findViewById<TextView>(R.id.curiosidadeArtigo).text = curiosidade

        if (curiosidade.isBlank()) {
            view.findViewById<View>(R.id.dicaArtigo).visibility = View.GONE
        }

        view.findViewById<View>(R.id.btnBack).setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        btnConcluirLeitura = view.findViewById(R.id.btnConcluirLeitura)
        btnConcluirLeitura.isEnabled = false

        // inicia a contagem de tempo (90 segundos = 90000 ms)
        iniciarTemporizador(90000)

        btnConcluirLeitura.setOnClickListener {
            if (podeResgatar) {
                verificarEAtribuirPontos()
            }
        }

        return view
    }

    private fun iniciarTemporizador(tempoMillis: Long) {
        countDownTimer = object : CountDownTimer(tempoMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                if (!isAdded) return
                val segundosRestantes = millisUntilFinished / 1000
                btnConcluirLeitura.text = "Leia o artigo ($segundosRestantes s)"
            }

            override fun onFinish() {
                if (!isAdded) return
                podeResgatar = true
                btnConcluirLeitura.isEnabled = true
                btnConcluirLeitura.text = "Concluir Leitura e Ganhar Pontos"
            }
        }.start()
    }

    private fun verificarEAtribuirPontos() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val usuarioRef = FirebaseDatabase.getInstance().getReference("usuarios").child(uid)
        val artigoLidoRef = usuarioRef.child("artigos_lidos").child(artigoId)

        artigoLidoRef.get().addOnSuccessListener { snapshot ->
            if (!isAdded) return@addOnSuccessListener
            val jaLeu = snapshot.getValue(Boolean::class.java) ?: false

            if (jaLeu) {
                Toast.makeText(requireContext(), "Você já ganhou pontos por este artigo!", Toast.LENGTH_SHORT).show()
                findNavController().navigateUp()
            } else {
                artigoLidoRef.setValue(true)
                usuarioRef.child("pontos").get().addOnSuccessListener { pontosSnapshot ->
                    if (!isAdded) return@addOnSuccessListener
                    val pontosAtuais = pontosSnapshot.getValue(Int::class.java) ?: 0
                    val novosPontos = pontosAtuais + 10

                    usuarioRef.child("pontos").setValue(novosPontos).addOnSuccessListener {
                        if (!isAdded) return@addOnSuccessListener
                        Toast.makeText(requireContext(), "Parabéns! Você ganhou 10 pontos!", Toast.LENGTH_LONG).show()
                        findNavController().navigateUp()
                    }
                }
            }
        }
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
            curiosidade: String
        ): ExemploArtigoFragment {

            val fragment = ExemploArtigoFragment()

            fragment.arguments = Bundle().apply {
                putString("categoria", categoria)
                putString("titulo", titulo)
                putString("autor", autor)
                putString("introducao", introducao)
                putString("mensagem", mensagem)
                putString("curiosidade", curiosidade)
            }

            return fragment
        }
    }
}