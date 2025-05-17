package com.example.projeto2_smart_city_final.menuInicio

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.projeto2_smart_city_final.R
import android.widget.ImageView
import com.bumptech.glide.Glide


data class Noticia(
    val id: String,
    val titulo: String,
    val lead: String,
    val imageUrl: String? = null
)

class NoticiaAdapter(
    private val items: List<Noticia>,
    private val onClick: (Noticia) -> Unit
) : RecyclerView.Adapter<NoticiaAdapter.VH>() {

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        private val ivImage: ImageView  = view.findViewById(R.id.ivItemImage)
        private val tvTitle: TextView   = view.findViewById(R.id.tvItemTitle)
        private val tvLead: TextView    = view.findViewById(R.id.tvItemLead)

        fun bind(noticia: Noticia) {
            tvTitle.text = noticia.titulo
            tvLead .text = noticia.lead
            // Se tiver URL de imagem, carrega; senão placeholder
            if (!noticia.imageUrl.isNullOrEmpty()) {
                Glide.with(ivImage.context)
                    .load(noticia.imageUrl)
                    .centerCrop()
                    .into(ivImage)
            } else {
                ivImage.setImageResource(R.drawable.ic_placeholder)
            }
            itemView.setOnClickListener { onClick(noticia) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_news, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) =
        holder.bind(items[position])

    override fun getItemCount() = items.size
}
