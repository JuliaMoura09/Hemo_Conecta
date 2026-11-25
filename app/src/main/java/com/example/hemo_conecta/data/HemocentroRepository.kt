package com.example.hemo_conecta.data

import android.content.Context
import android.location.Geocoder
import android.location.Location
import android.os.Handler
import android.os.Looper
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.io.IOException
import java.util.Locale

/**
 * Busca os hemocentros salvos em /hemocentros no Firebase Realtime
 * Database. Como a consulta ao Firebase é assíncrona, os resultados
 * chegam por callbacks (sempre na thread principal).
 *
 * A lista inteira é baixada uma vez e guardada em memória (cache);
 * filtro por texto e cálculo de distância são feitos aqui no app.
 */
object HemocentroRepository {

    private var cache: List<Hemocentro>? = null

    // endereço -> (latitude, longitude), ou null se não foi possível descobrir
    private val coordenadasCache = mutableMapOf<String, Pair<Double, Double>?>()

    private val mainHandler = Handler(Looper.getMainLooper())

    /**
     * Busca hemocentros cujo nome da cidade ou da unidade contenha
     * [termo]. Se [termo] estiver vazio, devolve a lista inteira.
     */
    fun buscarPorCidade(termo: String, onResultado: (List<Hemocentro>) -> Unit) {
        carregarTodos { lista -> onResultado(filtrar(lista, termo)) }
    }

    /**
     * Descobre o hemocentro mais próximo da posição ([latUsuario], [lonUsuario]).
     * Usa latitude/longitude do banco quando existirem; senão, converte o
     * endereço em coordenadas com o Geocoder do Android. Devolve null se
     * nenhum hemocentro tiver posição conhecida.
     */
    fun buscarMaisProximo(
        context: Context,
        latUsuario: Double,
        lonUsuario: Double,
        onResultado: (HemocentroProximo?) -> Unit
    ) {
        val appContext = context.applicationContext

        carregarTodos { lista ->
            // Geocoder faz rede: não pode rodar na thread principal
            Thread {
                var maisProximo: HemocentroProximo? = null
                val resultado = FloatArray(1)

                for (hemocentro in lista) {
                    val posicao = posicaoDe(appContext, hemocentro) ?: continue
                    Location.distanceBetween(
                        latUsuario, lonUsuario, posicao.first, posicao.second, resultado
                    )
                    val km = resultado[0] / 1000f
                    if (maisProximo == null || km < maisProximo.distanciaKm) {
                        maisProximo = HemocentroProximo(hemocentro, km)
                    }
                }

                val encontrado = maisProximo
                mainHandler.post { onResultado(encontrado) }
            }.start()
        }
    }

    private fun carregarTodos(onLista: (List<Hemocentro>) -> Unit) {
        val cacheAtual = cache
        if (cacheAtual != null) {
            onLista(cacheAtual)
            return
        }

        val referencia = FirebaseDatabase.getInstance().getReference("hemocentros")
        referencia.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val lista = snapshot.children.mapNotNull { it.getValue(Hemocentro::class.java) }
                cache = lista
                onLista(lista)
            }

            override fun onCancelled(error: DatabaseError) {
                onLista(emptyList())
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

    private fun posicaoDe(context: Context, hemocentro: Hemocentro): Pair<Double, Double>? {
        val lat = hemocentro.latitude
        val lon = hemocentro.longitude
        if (lat != null && lon != null) return lat to lon

        return geocodificar(context, hemocentro.endereco)
    }

    @Suppress("DEPRECATION")
    @Synchronized
    private fun geocodificar(context: Context, endereco: String): Pair<Double, Double>? {
        if (coordenadasCache.containsKey(endereco)) return coordenadasCache[endereco]
        if (!Geocoder.isPresent()) return null

        // Tira o "(ao lado do Hospital...)" e o CEP, que atrapalham a busca
        val enderecoLimpo = endereco
            .substringBefore("(")
            .substringBefore(". CEP")
            .trim()
            .trimEnd(',', '.')

        return try {
            val encontrados = Geocoder(context, Locale("pt", "BR"))
                .getFromLocationName("$enderecoLimpo, Brasil", 1)
            val posicao = encontrados?.firstOrNull()?.let { it.latitude to it.longitude }
            coordenadasCache[endereco] = posicao
            posicao
        } catch (e: IOException) {
            // Sem rede/serviço indisponível: não guarda no cache, tenta de novo depois
            null
        }
    }
}
