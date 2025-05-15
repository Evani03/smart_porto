package com.example.projeto2_smart_city_final.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
//import com.example.projeto2_smart_city_final.Inicio
import com.example.projeto2_smart_city_final.R
import com.example.projeto2_smart_city_final.ServiceDb

class ServiceDetailAdapter(
    private val items: List<ServiceDb>
) : RecyclerView.Adapter<ServiceDetailAdapter.VH>() {

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        private val tvDescricao: TextView = view.findViewById(R.id.tvServiceDescricao)
        private val tvContacto: TextView  = view.findViewById(R.id.tvServiceContacto)
        private val tvValor: TextView     = view.findViewById(R.id.tvServiceValor)

        @SuppressLint("SetTextI18n")
        fun bind(s: ServiceDb) {
            tvDescricao.text = s.descricao
            tvContacto.text  = "Contacto: ${s.contacto}"
            tvValor.text     = "Valor: ${s.valor}"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_service_detail, parent, false)
        return VH(view)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount() = items.size
}
