package com.example.hemo_conecta

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.hemo_conecta.databinding.ItemCupomBinding

class CupomAdapter(
    private val listaCupons: List<Cupom>,
    private var pontosUsuario: Int,
    private val cuponsResgatados: Set<String>,
    private val onResgatar: (Cupom) -> Unit
) : RecyclerView.Adapter<CupomAdapter.CupomViewHolder>() {

    class CupomViewHolder(
        val binding: ItemCupomBinding
    ) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CupomViewHolder {

        val binding = ItemCupomBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CupomViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: CupomViewHolder,
        position: Int
    ) {

        val cupom = listaCupons[position]
        val binding = holder.binding
        val context = holder.itemView.context
        val chaveCupom = "${cupom.parceiroId}/${cupom.id}"
        val custo = cupom.pontos ?: 0
        val jaResgatado = cuponsResgatados.contains(chaveCupom)
        val possuiPontos = pontosUsuario >= custo && custo > 0

        binding.tvCategory.text = cupom.categoria ?: "Geral"
        binding.tvTitle.text = cupom.parceiro ?: "Parceiro"
        val valor = cupom.valor ?: 0.0

        binding.tvDiscountPercent.text =
            if (cupom.tipoValor.equals("porcentagem", ignoreCase = true)) {
                "${valor.toInt()}%"
            } else {
                "R$ ${valor.toInt()}"
            }

        binding.tvPontosNecessarios.text =
            "$custo pontos necessários"

        if (jaResgatado) {
            binding.tvCouponCode.visibility = View.VISIBLE
            binding.btnCopy.visibility = View.VISIBLE
            binding.btnResgatar.visibility = View.GONE
            binding.tvCouponCode.text = cupom.codCupom ?: ""

        } else {
            binding.tvCouponCode.visibility = View.GONE
            binding.btnCopy.visibility = View.GONE
            binding.btnResgatar.visibility = View.VISIBLE

            if (possuiPontos) {
                binding.btnResgatar.isEnabled = true

            } else {
                binding.btnResgatar.isEnabled = false
            }
        }
        binding.btnResgatar.setOnClickListener {

            if (!jaResgatado && possuiPontos) {
                onResgatar(cupom)
            }
        }
        binding.btnCopy.setOnClickListener {

            if (!jaResgatado) return@setOnClickListener
            val codigo = cupom.codCupom.orEmpty()

            if (codigo.isBlank()) {
                Toast.makeText(
                    context,
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            val clipboard = context.getSystemService(
                Context.CLIPBOARD_SERVICE
            ) as ClipboardManager

            val clip = ClipData.newPlainText(
                "Código do Cupom",
                codigo
            )
            clipboard.setPrimaryClip(clip)

            Toast.makeText(
                context,
                "Cupom copiado: $codigo",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    fun atualizarEstado(
        novosPontos: Int,
        novosResgatados: Set<String>
    ) {

        pontosUsuario = novosPontos
        (cuponsResgatados as? MutableSet<String>)?.let {
            it.clear()
            it.addAll(novosResgatados)
        }
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return listaCupons.size
    }
}
