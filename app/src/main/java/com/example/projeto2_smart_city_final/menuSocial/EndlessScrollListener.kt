package com.example.projeto2_smart_city_final.menuSocial

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

abstract class EndlessScrollListener(
    private val layoutManager: LinearLayoutManager
) : RecyclerView.OnScrollListener() {

    override fun onScrolled(rv: RecyclerView, dx: Int, dy: Int) {
        super.onScrolled(rv, dx, dy)
        val total = layoutManager.itemCount
        val lastVisible = layoutManager.findLastVisibleItemPosition()
        if (!rv.canScrollVertically(1) && lastVisible == total - 1) {
            onLoadMore()
        }
    }

    abstract fun onLoadMore()
}
