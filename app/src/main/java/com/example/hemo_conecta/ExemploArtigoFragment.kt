
package com.example.hemo_conecta

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment

class ExemploArtigoFragment : Fragment() {

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

        view.findViewById<TextView>(R.id.categoriaArtigo).text =
            arguments?.getString("categoria").orEmpty()

        view.findViewById<TextView>(R.id.tituloArtigo).text =
            arguments?.getString("titulo").orEmpty()

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
            view.findViewById<View>(R.id.dicaArtigo).visibility = View.GONE
        }

        view.findViewById<View>(R.id.btnBack).setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        return view
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
