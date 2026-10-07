package com.example.hemo_conecta.data

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

/**
 * Busca os hemocentros salvos em /hemocentros no Firebase Realtime
 * Database. Como consulta ao Firebase é assíncrona (a resposta não
 * vem na hora), os resultados chegam por um callback [onResultado].
 *
 * Os 5 hemocentros são poucos, então a lista inteira é baixada uma
 * vez, guardada em memória (cache), e o filtro por texto é feito
 * aqui no app — não precisa de uma query especial no Firebase.
 */
object HemocentroRepository {

    private var cache: List<Hemocentro>? = null

    /**
     * Busca hemocentros cujo nome da cidade ou da unidade contenha
     * [termo] (ignorando maiúsculas/minúsculas). Se [termo] estiver
     * vazio, devolve a lista inteira. [onResultado] é chamado sempre
     * na thread principal.
     */
    fun buscarPorCidade(termo: String, onResultado: (List<Hemocentro>) -> Unit) {
        val cacheAtual = cache
        if (cacheAtual != null) {
            onResultado(filtrar(cacheAtual, termo))
            return
        }

        val referencia = FirebaseDatabase.getInstance().getReference("hemocentros")
        referencia.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val lista = snapshot.children.mapNotNull { it.getValue(Hemocentro::class.java) }
                cache = lista
                onResultado(filtrar(lista, termo))
            }

            override fun onCancelled(error: DatabaseError) {
                // Falha de rede/permissão: não derruba a tela, só não mostra resultado.
                onResultado(emptyList())
            }
        })
    }

    private fun filtrar(lista: List<Hemocentro>, termo: String): List<Hemocentro> {
        if (termo.isBlank()) return lista
        return lista.filter { hemocentro ->
            hemocentro.cidade.contains(termo, ignoreCase = true) ||
                hemocentro.nomeUnidade.contains(termo, ignoreCase = true)
        }
    }
}
