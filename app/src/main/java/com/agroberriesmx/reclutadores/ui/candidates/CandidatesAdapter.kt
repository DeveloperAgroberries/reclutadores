package com.agroberriesmx.reclutadores.ui.candidates

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.agroberriesmx.reclutadores.data.network.response.CandidateRemote
import com.agroberriesmx.reclutadores.databinding.ItemCandidateBinding

class CandidatesAdapter(
    private var list: List<CandidateRemote>,
    private val onItemClick: (CandidateRemote) -> Unit // 👈 1. Agregamos el parámetro de click
) : RecyclerView.Adapter<CandidatesAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemCandidateBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemCandidateBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        with(holder.binding) {
            tvNombreCandidato.text = item.vNomcandidato
            tvOrigen.text = item.vLorigen

            // 💰 Lógica de Pago en la lista
            if (item.cPagado == "1") {
                tvPagado.text = "PAGADO"
                tvPagado.setTextColor(android.graphics.Color.parseColor("#4CAF50")) // Verde
            } else {
                tvPagado.text = "PENDIENTE"
                tvPagado.setTextColor(android.graphics.Color.parseColor("#F44336")) // Rojo
            }

            // 🖼️ CARGAR IMAGEN EN LA LISTA
            if (!item.vInedoc.isNullOrEmpty()) {
                com.bumptech.glide.Glide.with(ivIne.context)
                    .load(item.vInedoc)
                    .placeholder(android.R.drawable.ic_menu_gallery) // Imagen mientras carga
                    .error(android.R.drawable.stat_notify_error)    // Imagen si falla
                    .into(ivIne)
            } else {
                ivIne.setImageResource(android.R.drawable.ic_menu_gallery)
            }

            holder.itemView.setOnClickListener { onItemClick(item) }
        }
    }

    override fun getItemCount() = list.size

    fun updateList(newList: List<CandidateRemote>) {
        list = newList
        notifyDataSetChanged()
    }
}