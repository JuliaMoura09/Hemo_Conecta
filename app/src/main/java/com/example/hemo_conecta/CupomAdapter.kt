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
        holder.binding.tvCategory.text = cupom.categoria
        holder.binding.tvTitle.text = cupom.titulo
        holder.binding.tvDiscountPercent.text = cupom.desconto
        holder.binding.tvCouponCode.text = cupom.codigo


        // copia o código do cupom ao clicar no botão do copiar
        holder.binding.btnCopy.setOnClickListener {
            val context = holder.itemView.context
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Código do Cupom", cupom.codigo)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(context, "Cupom copiado: ${cupom.codigo}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun getItemCount(): Int = listaCupons.size
}