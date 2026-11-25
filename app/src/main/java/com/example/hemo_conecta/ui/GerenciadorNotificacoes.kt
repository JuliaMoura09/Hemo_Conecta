package com.example.hemo_conecta.ui

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class GerenciadorNotificacoes {

    private val reference = FirebaseDatabase.getInstance().reference

    private val uid = FirebaseAuth.getInstance().currentUser?.uid

    fun registrarMedalha(
        medalhaId: String,
        nomeMedalha: String
    ) {
        if (uid == null) return

        val medalhaRef = reference
            .child("usuarios")
            .child(uid)
            .child("medalhas")
            .child(medalhaId)

        medalhaRef.runTransaction(object :
            com.google.firebase.database.Transaction.Handler {

            override fun doTransaction(
                dados: com.google.firebase.database.MutableData
            ): com.google.firebase.database.Transaction.Result {

                if (dados.value != null) {
                    return com.google.firebase.database.Transaction.abort()
                }

                dados.value = true

                return com.google.firebase.database.Transaction.success(dados)
            }

            override fun onComplete(
                error: com.google.firebase.database.DatabaseError?,
                committed: Boolean,
                snapshot: com.google.firebase.database.DataSnapshot?
            ) {

                if (committed && error == null) {
                    salvarNotificacao(
                        "Medalha conquistada!",
                        "Você conquistou a medalha $nomeMedalha!"
                    )
                }
            }
        })
    }

    fun registrarNivel(nivel: Int) {
        if (uid == null || nivel <= 0) return

        val nivelRef = reference
            .child("usuarios")
            .child(uid)
            .child("notificacoesNiveis")
            .child(nivel.toString())

        nivelRef.runTransaction(object :
            com.google.firebase.database.Transaction.Handler {

            override fun doTransaction(
                dados: com.google.firebase.database.MutableData
            ): com.google.firebase.database.Transaction.Result {

                if (dados.value != null) {
                    return com.google.firebase.database.Transaction.abort()
                }

                dados.value = true

                return com.google.firebase.database.Transaction.success(dados)
            }

            override fun onComplete(
                error: com.google.firebase.database.DatabaseError?,
                committed: Boolean,
                snapshot: com.google.firebase.database.DataSnapshot?
            ) {

                if (committed && error == null) {
                    salvarNotificacao(
                        "Novo nível!",
                        "Parabéns! Você alcançou o nível $nivel."
                    )
                }
            }
        })
    }

    private fun salvarNotificacao(
        titulo: String,
        descricao: String
    ) {

        if (uid == null) return

        val notificacoesRef = reference
            .child("usuarios")
            .child(uid)
            .child("notificacoes")

        val id = notificacoesRef.push().key ?: return

        val notificacao = Notificacao(
            id = id,
            titulo = titulo,
            descricao = descricao,
            data = System.currentTimeMillis(),
            lida = false
        )

        notificacoesRef.child(id).setValue(notificacao)
    }
}