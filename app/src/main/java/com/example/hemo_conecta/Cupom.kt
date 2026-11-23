
package com.example.hemo_conecta

data class Cupom(
    var ativo: Boolean = false,
    var codCupom: String? = "",
    var dataCriacao: Long? = 0L,
    var pontos: Int? = 0,
    var tipoValor: String? = "",
    var valor: Double? = 0.0,
    var categoria: String? = "",
    var parceiro: String? = "",
    var id: String = "",
    var parceiroId: String = ""
)
