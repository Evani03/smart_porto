package com.example.projeto2_smart_city_final

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ServiceAdapter(private val items: List<ServiceItem>) :
    RecyclerView.Adapter<ServiceAdapter.VH>() {

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        private val tvTipo    = view.findViewById<TextView>(R.id.tvServiceTipo)
        private val tvDesc    = view.findViewById<TextView>(R.id.tvServiceDesc)
        private val tvContact = view.findViewById<TextView>(R.id.tvServiceContact)
        private val tvValue   = view.findViewById<TextView>(R.id.tvServiceValue)

        fun bind(item: ServiceItem) {
            tvTipo   .text = item.tipoServico
            tvDesc   .text = item.descricao
            tvContact.text = "Contacto: ${item.contacto}"
            tvValue  .text = "Valor: €%.2f".format(item.valor)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_service_list, parent, false)
        return VH(v)
    }

    override fun getItemCount() = items.size
    override fun onBindViewHolder(holder: VH, position: Int) =
        holder.bind(items[position])
}
