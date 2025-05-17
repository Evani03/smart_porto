package com.example.projeto2_smart_city_final.menuSocial

import com.google.firebase.Timestamp

data class NewsItem(
    val id: String = "",
    val title: String = "",
    val lead: String = "",
    val body: String = "",
    val imageUrl: String? = null,
    val timestamp: Timestamp = Timestamp.now(),
    val userId: String   = ""
)