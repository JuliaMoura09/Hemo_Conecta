package com.example.hemo_conecta

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.hemo_conecta.databinding.ItemCupomBinding

class CupomAdapter(private val listaCupons: List<Cupom>) :
    RecyclerView.Adapter<CupomAdapter.CupomViewHolder>() {

    class CupomViewHolder(val binding: ItemCupomBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CupomViewHolder {
        val binding = ItemCupomBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CupomViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CupomViewHolder, position: Int) {
        val cupom = listaCupons[position]

        // categoria que o cupom pertence
        holder.binding.tvCategory.text = cupom.categoria ?: "Geral"

        // nome da loja parceira
        holder.binding.tvTitle.text = cupom.parceiro

        // formato do desconto (valor ou porcentagem)
        val valorInt = cupom.valor?.toInt() ?: 0
        holder.binding.tvDiscountPercent.text = if (cupom.tipoValor == "porcentagem") {
            "$valorInt%"
        } else {
            "R$$valorInt"
        }

        // código do cupom
        holder.binding.tvCouponCode.text = cupom.codCupom

        // copia o código ao clicar no botão
        holder.binding.btnCopy.setOnClickListener {
            val context = holder.itemView.context
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Código do Cupom", cupom.codCupom)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(context, "Cupom copiado: ${cupom.codCupom}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun getItemCount(): Int = listaCupons.size
}