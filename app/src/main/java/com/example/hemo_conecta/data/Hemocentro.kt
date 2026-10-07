package com.example.hemo_conecta.data

/**
 * Representa um hemocentro/unidade de coleta, salvo no Firebase
 * Realtime Database em /hemocentros/<chave>.
 */
data class Hemocentro @JvmOverloads constructor(
    val cidade: String = "",
    val nomeUnidade: String = "",
    val endereco: String = "",
    val telefone: String = "",
    val email: String = "",
    val horarioFuncionamento: String = ""
)
