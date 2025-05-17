package com.example.projeto2_smart_city_final.menuSocial

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.projeto2_smart_city_final.R
import com.example.projeto2_smart_city_final.databinding.ActivitySocialBinding
import com.example.projeto2_smart_city_final.menuConta.ContaActivity
import com.example.projeto2_smart_city_final.menuInicio.Inicio
import com.example.projeto2_smart_city_final.menuMapa.Mapa
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class SocialActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySocialBinding
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val newsAdapter = SocialNewsAdapter(mutableListOf()) { news ->
        showNewsDialog(news)
    }
    private var lastDoc: DocumentSnapshot? = null
    private var loadingPage = false

    @SuppressLint("SuspiciousIndentation")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySocialBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.rvSocialNews.adapter = newsAdapter

        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigation.selectedItemId = R.id.nav_social  // ou o respectivo ID da Activity

        // Toolbar back
        onBackPressedDispatcher.addCallback(this) {
            // este bloco substitui onBackPressed()
            finish()
        }

        // Só admins veem o FAB
        binding.fabCreateNews.visibility = View.GONE
           // Só depois fazemos a leitura assíncrona do tipoConta
           auth.currentUser?.uid?.let { uid ->
                 db.collection("users").document(uid)
                   .get()
                   .addOnSuccessListener { snap ->
                         val tipoConta = snap.getString("tipoConta")
                         if (tipoConta == "admin") {
                               binding.fabCreateNews.visibility = View.VISIBLE
                             }
                       }
                   .addOnFailureListener {
                         // falha a ler: mantemos invisível
                       }
               }

        binding.fabCreateNews.setOnClickListener {
            startActivity(Intent(this, CreateNewsActivity::class.java))
        }

        // Recycler + adapter
        binding.rvSocialNews.apply {
            layoutManager = LinearLayoutManager(this@SocialActivity)
            adapter = newsAdapter
            addOnScrollListener(object : EndlessScrollListener(layoutManager as LinearLayoutManager) {
                override fun onLoadMore() {
                    loadNextPage()
                }
            })
        }

        // Swipe to refresh
        binding.swipeRefreshNews.setOnRefreshListener {
            lastDoc = null
            newsAdapter.clear()
            loadNextPage()
        }

        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, Inicio::class.java))
                    finish()}
                R.id.nav_mapa -> { startActivity(Intent(this, Mapa::class.java))
                finish()}
                R.id.nav_social -> {}
                R.id.nav_conta -> {startActivity(Intent(this, ContaActivity::class.java))
                    finish()}
            }
            true
        }

        // Carrega primeira página
        loadNextPage()
    }

    private fun loadNextPage() {
        if (loadingPage) return
        loadingPage = true
        binding.tvEndOfList.visibility = View.GONE

        var query: Query = db.collection("news")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(4)

        lastDoc?.let {
            query = query.startAfter(it)
        }

        query.get()
            .addOnSuccessListener { snaps ->
                if (snaps.isEmpty) {
                    binding.tvEndOfList.visibility = View.VISIBLE
                } else {
                    snaps.documents.forEach { doc ->
                        val item = NewsItem(
                            title = doc.getString("title") ?: "",
                            lead  = doc.getString("lead")  ?: "",
                            body  = doc.getString("body")  ?: "",
                            timestamp = doc.getTimestamp("timestamp") ?: Timestamp.now(),
                            imageUrl  = doc.getString("imageUrl") ?: "",
                            userId    = doc.getString("userId") ?: ""
                        )
                        newsAdapter.add(item)
                    }
                    lastDoc = snaps.documents.last()
                }
                binding.swipeRefreshNews.isRefreshing = false
                loadingPage = false
            }
            .addOnFailureListener {
                binding.swipeRefreshNews.isRefreshing = false
                loadingPage = false
            }
    }

    private fun showNewsDialog(news: NewsItem) {
        // 1. Infla o layout do diálogo
        val view = LayoutInflater.from(this)
            .inflate(R.layout.dialog_news_detail, null)

        // 2. Preenche os campos já existentes
        view.findViewById<TextView>(R.id.tvDetailTitle).text = news.title
        view.findViewById<TextView>(R.id.tvDetailLead).text  = news.lead
        view.findViewById<ImageView>(R.id.ivDetailImage).apply {
            if (news.imageUrl?.isNotEmpty() == true) {
                Glide.with(this).load(news.imageUrl).into(this)
            } else {
                setImageResource(R.drawable.ic_placeholder)
            }
        }
        view.findViewById<TextView>(R.id.tvDetailText).text  = news.body

        // 3. Busca e mostra o nome do utilizador autor (tvDetailAuthor)
        val tvAuthor = view.findViewById<TextView>(R.id.tvDetailAuthor)
        FirebaseFirestore.getInstance()
            .collection("users")
            .document(news.userId)
            .get()
            .addOnSuccessListener { snap ->
                val nome = snap.getString("nomeUtilizador").takeIf { !it.isNullOrBlank() }
                    ?: "Anónimo"
                tvAuthor.text = "Por: $nome"
            }
            .addOnFailureListener {
                tvAuthor.text = "Por: Anónimo"
            }

        // 4. Exibe o diálogo
        AlertDialog.Builder(this)
            .setView(view)
            .setPositiveButton("Fechar", null)
            .show()
    }

}
