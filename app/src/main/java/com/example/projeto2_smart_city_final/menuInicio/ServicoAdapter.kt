
package com.example.projeto2_smart_city_final.menuInicio

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.projeto2_smart_city_final.R

// Adapter que recebe lista de tipos de serviço (String) e uma função onClick
class ServicoAdapter(
    private val tipos: List<String>,
    private val onClick: (String) -> Unit
) : RecyclerView.Adapter<ServicoAdapter.VH>() {

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        private val tvName: TextView = view.findViewById(R.id.tvItemName)
        fun bind(tipo: String) {
            tvName.text = tipo
            itemView.setOnClickListener { onClick(tipo) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_service, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(tipos[position])

    override fun getItemCount() = tipos.size

}