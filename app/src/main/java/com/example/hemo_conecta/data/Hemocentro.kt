package com.example.hemo_conecta.data

/**
 * Representa um hemocentro/unidade de coleta, salvo no Firebase
 * Realtime Database em /hemocentros/<chave>.
 *
 * [latitude] e [longitude] são opcionais: se não estiverem no banco,
 * o app descobre a posição a partir do [endereco] (ver HemocentroRepository).
 */
data class Hemocentro @JvmOverloads constructor(
    val cidade: String = "",
    val nomeUnidade: String = "",
    val endereco: String = "",
    val telefone: String = "",
    val email: String = "",
    val horarioFuncionamento: String = "",
    val latitude: Double? = null,
    val longitude: Double? = null
)

/** Um hemocentro junto com a distância (em km) até o usuário. */
data class HemocentroProximo(
    val hemocentro: Hemocentro,
    val distanciaKm: Float
)
