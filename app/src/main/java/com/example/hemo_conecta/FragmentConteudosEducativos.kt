
package com.example.hemo_conecta

import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class FragmentConteudosEducativos : Fragment() {

    private var containerArtigos: LinearLayout? = null
    private var artigosListener: ValueEventListener? = null

    private val artigosRef by lazy {
        FirebaseDatabase.getInstance()
            .getReference("artigosEducacionais")
            .orderByChild("status")
            .equalTo("publicado")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(
            R.layout.fragment_conteudos_educativos,
            container,
            false
        )

        containerArtigos = view.findViewById(R.id.containerArtigos)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        artigosListener = object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {
                val container = containerArtigos ?: return

                container.removeAllViews()

                if (!snapshot.exists()) {
                    mostrarMensagem("Nenhum artigo publicado ainda.")
                    return
                }

                for (artigo in snapshot.children) {
                    val titulo = artigo.child("titulo")
                        .getValue(String::class.java).orEmpty()

                    val categoria = artigo.child("categoria")
                        .getValue(String::class.java).orEmpty()

                    val autor = artigo.child("autor")
                        .getValue(String::class.java).orEmpty()

                    val introducao = artigo.child("introducao")
                        .getValue(String::class.java).orEmpty()

                    val mensagem = artigo.child("mensagem")
                        .getValue(String::class.java).orEmpty()

                    val curiosidade = artigo.child("curiosidade")
                        .getValue(String::class.java).orEmpty()

                    adicionarCard(
                        categoria,
                        titulo,
                        autor,
                        introducao,
                        mensagem,
                        curiosidade
                    )
                }
            }

            override fun onCancelled(error: DatabaseError) {
                if (isAdded) {
                    Toast.makeText(
                        requireContext(),
                        "Não foi possível carregar os artigos.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        artigosRef.addValueEventListener(artigosListener!!)
    }

    private fun adicionarCard(
        categoria: String,
        titulo: String,
        autor: String,
        introducao: String,
        mensagem: String,
        curiosidade: String
    ) {
        val context = requireContext()

        val card = CardView(context).apply {
            radius = dp(16).toFloat()
            cardElevation = dp(2).toFloat()
            setCardBackgroundColor(Color.WHITE)
            useCompatPadding = false

            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = dp(12)
            }

            isClickable = true
            isFocusable = true

            setOnClickListener {
                val bundle = Bundle().apply {
                    putString("categoria", categoria)
                    putString("titulo", titulo)
                    putString("autor", autor)
                    putString("introducao", introducao)
                    putString("mensagem", mensagem)
                    putString("curiosidade", curiosidade)
                }

                findNavController().navigate(
                    R.id.action_fragmentConteudosEducativos_to_exemploArtigoFragment,
                    bundle
                )
            }
        }

        val conteudoCard = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

        val parteSuperior = FrameLayout(context).apply {
            setBackgroundResource(
                R.drawable.bg_card_conteudos_educativos_gradient
            )

            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(130)
            )

            setPadding(dp(12), dp(12), dp(12), dp(12))
        }

        val textoCategoria = TextView(context).apply {
            text = categoria
            setBackgroundResource(R.drawable.border_text)
            setTextColor(Color.BLACK)
            textSize = 12f
            setPadding(dp(8), dp(4), dp(8), dp(4))

            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                Gravity.TOP or Gravity.START
            )
        }

        val icone = ImageView(context).apply {
            setImageResource(R.drawable.text_icon)
            contentDescription = "Artigo educativo"

            layoutParams = FrameLayout.LayoutParams(
                dp(35),
                dp(35),
                Gravity.CENTER
            )
        }

        parteSuperior.addView(icone)
        parteSuperior.addView(textoCategoria)

        val parteInferior = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.WHITE)
            setPadding(dp(20), dp(16), dp(20), dp(12))

            minimumHeight = dp(85)

            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

        val textoTitulo = TextView(context).apply {
            text = titulo
            setTextColor(Color.BLACK)
            textSize = 16f
            setTypeface(null, android.graphics.Typeface.BOLD)
            maxLines = 2
            ellipsize = android.text.TextUtils.TruncateAt.END

            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

        val textoAutor = TextView(context).apply {
            text = autor
            setTextColor(Color.parseColor("#737373"))
            textSize = 12f

            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dp(4)
            }
        }

        parteInferior.addView(textoTitulo)
        parteInferior.addView(textoAutor)

        conteudoCard.addView(parteSuperior)
        conteudoCard.addView(parteInferior)

        card.addView(conteudoCard)

        containerArtigos?.addView(card)
    }

    private fun mostrarMensagem(mensagem: String) {
        val context = context ?: return

        val texto = TextView(context).apply {
            text = mensagem
            textSize = 14f
            setTextColor(Color.parseColor("#6B7280"))
            gravity = Gravity.CENTER
            setPadding(dp(16), dp(24), dp(16), dp(24))
        }

        containerArtigos?.addView(texto)
    }

    private fun dp(valor: Int): Int {
        return (valor * resources.displayMetrics.density).toInt()
    }

    override fun onDestroyView() {
        artigosListener?.let {
            artigosRef.removeEventListener(it)
        }

        artigosListener = null
        containerArtigos = null

        super.onDestroyView()
    }
}
