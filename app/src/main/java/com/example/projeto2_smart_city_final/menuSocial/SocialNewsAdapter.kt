package com.example.projeto2_smart_city_final.menuSocial

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.projeto2_smart_city_final.R
import java.text.DateFormat
import java.util.*

class SocialNewsAdapter(
    private val items: MutableList<NewsItem>,
    private val onClick: (NewsItem) -> Unit
) : RecyclerView.Adapter<SocialNewsAdapter.VH>() {

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        private val iv = view.findViewById<ImageView>(R.id.ivNewsImage)
        private val tvTitle = view.findViewById<TextView>(R.id.tvNewsTitle)
        private val tvLead  = view.findViewById<TextView>(R.id.tvNewsLead)
        private val tvTime  = view.findViewById<TextView>(R.id.tvNewsTime)

        fun bind(item: NewsItem) {
            tvTitle.text = item.title
            tvLead.text  = item.lead
            tvTime.text  = DateFormat.getDateTimeInstance().format(item.timestamp.toDate())
            if (item.imageUrl?.isNotEmpty() == true) {
                Glide.with(iv).load(item.imageUrl).into(iv)
            } else {
                iv.setImageResource(R.drawable.ic_placeholder)
            }
            itemView.setOnClickListener { onClick(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(LayoutInflater.from(parent.context).inflate(R.layout.item_social_news, parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(items[position])

    override fun getItemCount() = items.size

    fun add(item: NewsItem) {
        items.add(item)
        notifyItemInserted(items.size - 1)
    }

    fun clear() {
        val oldSize = items.size
        items.clear()
        //notifyDataSetChanged()
        notifyItemRangeRemoved(0, oldSize)
    }
}