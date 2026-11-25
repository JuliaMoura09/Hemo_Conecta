package com.example.hemo_conecta.data

/**
 * Encapsula um [Hemocentro] e a distância em quilômetros até a localização do usuário.
 */
data class HemocentroProximo(
    val hemocentro: Hemocentro,
    val distanciaKm: Float
)
