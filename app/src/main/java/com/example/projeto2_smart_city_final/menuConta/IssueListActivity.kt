package com.example.projeto2_smart_city_final.menuConta

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.projeto2_smart_city_final.databinding.ActivityIssueListBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

data class IssueItem(
    val titulo: String = "",
    val tipo: String = "",
    val descricao: String = "",
    val problemResolvido: String = "nao"
)

class IssueListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityIssueListBinding
    private val db = Firebase.firestore
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityIssueListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.rvIssues.layoutManager = LinearLayoutManager(this)
        binding.swipeRefresh.setOnRefreshListener { loadIssues() }

        loadIssues()
    }

    private fun loadIssues() {
        binding.swipeRefresh.isRefreshing = true
        val uid = auth.currentUser?.uid ?: return

        db.collection("issues")
            .whereEqualTo("userId", uid)
            .get()
            .addOnSuccessListener { snaps ->
                val list = snaps.documents.mapNotNull { doc ->
                    val t = doc.getString("titulo") ?: return@mapNotNull null
                    val ty = doc.getString("tipo") ?: ""
                    val d = doc.getString("descricao") ?: ""
                    val r = doc.getString("problemResolvido") ?: "nao"
                    IssueItem(t, ty, d, r)
                }
                binding.rvIssues.adapter = IssueAdapter(list)
                binding.swipeRefresh.isRefreshing = false
            }
            .addOnFailureListener {
                binding.swipeRefresh.isRefreshing = false
            }
    }
}
