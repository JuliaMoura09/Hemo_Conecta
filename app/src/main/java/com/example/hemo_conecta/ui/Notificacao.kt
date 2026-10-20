package com.example.hemo_conecta.ui

data class Notificacao(
    var id: String = "",
    var titulo: String = "",
    var descricao: String = "",
    var data: Long = 0L,
    var lida: Boolean = false
)
