package com.example.projeto2_smart_city_final

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

class IssueAdapter(private val items: List<IssueItem>) :
    RecyclerView.Adapter<IssueAdapter.VH>() {

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        private val tvTitle = view.findViewById<TextView>(R.id.tvIssueTitle)
        private val tvType  = view.findViewById<TextView>(R.id.tvIssueType)
        private val tvDesc  = view.findViewById<TextView>(R.id.tvIssueDesc)
        private val tvRes   = view.findViewById<TextView>(R.id.tvIssueResolved)

        fun bind(item: IssueItem) {
            tvTitle.text = item.titulo
            tvType .text = item.tipo
            tvDesc .text = item.descricao
            tvRes  .text = if (item.problemResolvido=="sim") "Resolvido" else "Por resolver"
            val color = if (item.problemResolvido=="sim")
                R.color.green_700 else R.color.red_700
            tvRes.setTextColor(ContextCompat.getColor(itemView.context, color))
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_issue, parent, false)
        return VH(v)
    }

    override fun getItemCount() = items.size
    override fun onBindViewHolder(holder: VH, position: Int) =
        holder.bind(items[position])
}
