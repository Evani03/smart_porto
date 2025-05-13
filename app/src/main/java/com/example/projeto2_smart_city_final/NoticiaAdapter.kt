package com.example.projeto2_smart_city_final

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.projeto2_smart_city_final.R
import com.example.projeto2_smart_city_final.Noticia

data class Noticia(
    val titulo: String,
    val descricao: String,
    //val imagem: Int // Referência ao recurso drawable (R.drawable...)
)

class NoticiaAdapter( private val items: List<Noticia>, private val onClick: (Noticia) -> Unit ) : RecyclerView.Adapter<NoticiaAdapter.VH>() {

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        private val tvTitle: TextView = view.findViewById(R.id.tvItemTitle)
        fun bind(noticia: Noticia) {
            tvTitle.text = noticia.titulo
            itemView.setOnClickListener { onClick(noticia) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_news, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(items[position])

    override fun getItemCount() = items.size

}