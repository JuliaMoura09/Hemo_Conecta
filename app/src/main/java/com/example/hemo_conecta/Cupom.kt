package com.example.hemo_conecta

data class Cupom(
    val ativo: Boolean = false,
    val codCupom: String? = "",
    val dataCriacao: Long? = 0L,
    val pontos: Int? = 0,
    val tipoValor: String? = "",
    val valor: Double? = 0.0,
    val categoria: String? = "",
    var parceiro: String? = ""
)