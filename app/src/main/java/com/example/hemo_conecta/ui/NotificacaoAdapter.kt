package com.example.hemo_conecta.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.hemo_conecta.databinding.ItemNotificacaoBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class NotificacaoAdapter :
    RecyclerView.Adapter<NotificacaoAdapter.NotificacaoViewHolder>() {

    private val listaNotificacoes = mutableListOf<Notificacao>()

    class NotificacaoViewHolder(
        val binding: ItemNotificacaoBinding
    ) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): NotificacaoViewHolder {
        val binding = ItemNotificacaoBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        return NotificacaoViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: NotificacaoViewHolder,
        position: Int
    ) {
        val notificacao = listaNotificacoes[position]

        holder.binding.tvTituloNotificacao.text = notificacao.titulo
        holder.binding.tvDescricaoNotificacao.text = notificacao.descricao

        val formato = SimpleDateFormat(
            "dd/MM/yyyy HH:mm",
            Locale("pt", "BR")
        )

        holder.binding.tvDataNotificacao.text =
            formato.format(Date(notificacao.data))
    }

    override fun getItemCount(): Int {
        return listaNotificacoes.size
    }

    fun atualizarLista(novasNotificacoes: List<Notificacao>) {
        listaNotificacoes.clear()
        listaNotificacoes.addAll(novasNotificacoes)
        notifyDataSetChanged()
    }
}
