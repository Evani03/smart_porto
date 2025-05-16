package com.example.projeto2_smart_city_final.menuConta

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.projeto2_smart_city_final.menuMapa.Mapa
import com.example.projeto2_smart_city_final.R
import com.example.projeto2_smart_city_final.criarRegistarConta.RegistarConta
import com.example.projeto2_smart_city_final.menuInicio.Inicio
import com.example.projeto2_smart_city_final.databinding.ActivityContaBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

data class UserProfile(
    val nomeUtilizador: String = "",
    val tipoConta: String = "",
    val email: String = ""
)

class ContaActivity : AppCompatActivity() {
    private lateinit var binding: ActivityContaBinding
    private val auth = FirebaseAuth.getInstance()
    private val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityContaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNav.selectedItemId = R.id.nav_conta

        // Carregar dados do perfil
        val uid = auth.currentUser?.uid
        if (uid == null) {
            startActivity(Intent(this, RegistarConta::class.java))
            finish()
            return
        }

        db.collection("users")
            .document(uid)
            .get()
            .addOnSuccessListener { snap ->
                // 1) converte em UserProfile
                val profile = snap.toObject(UserProfile::class.java)
                // 2) usa o nomeUtilizador ou mostra o UID se estiver vazio
                val nome = profile
                    ?.nomeUtilizador
                    ?.takeIf { it.isNotBlank() }
                    ?: auth.currentUser?.uid
                    ?: "Sem nome"
                binding.tvUserName.text = nome
            }
            .addOnFailureListener {
                Toast.makeText(this, "Não foi possível carregar perfil", Toast.LENGTH_SHORT).show()
            }


        // 2) Clique em “Editar”
        binding.tvEditProfile.setOnClickListener {
            showEditProfileDialog(uid)
        }

        // 3) Navegação interna
        binding.btnMyIssues.setOnClickListener {
            startActivity(Intent(this, IssueListActivity::class.java))
            //finish()
        }
        binding.btnMyServices.setOnClickListener {
            startActivity(Intent(this, ServiceListActivity::class.java))
            //finish()
        }
        binding.btnNotifications.setOnClickListener {
            showSimpleDialog("Notificações", "Configurações de notificação serão aqui.")
        }
        binding.btnAbout.setOnClickListener {
            showSimpleDialog("Sobre", "App Smart City v1.0\n© 2025")
        }

        // 4) Logout
        binding.btnLogout.setOnClickListener {
            auth.signOut()
            startActivity(Intent(this, RegistarConta::class.java))
            finish()
        }

        // 5) Bottom nav
        findViewById<BottomNavigationView>(R.id.bottom_navigation)
            .setOnItemSelectedListener { item ->
                when (item.itemId) {
                    R.id.nav_home ->{
                        startActivity(Intent(this, Inicio::class.java))
                        finish()}
                    R.id.nav_mapa -> {
                        startActivity(Intent(this, Mapa::class.java))
                        finish()}
                    R.id.nav_social -> {}/*startActivity(Intent(this, SocialActivity::class.java)) */
                    R.id.nav_conta -> { }
                }
                //finish()
                true
            }
    }

    private fun showSimpleDialog(title: String, message: String) {
        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
    }

    @SuppressLint("MissingInflatedId")
    private fun showEditProfileDialog(uid: String) {
        // Um diálogo simples para editar nome e tipo de conta
        val view = layoutInflater.inflate(R.layout.dialog_edit_profile, null)
        val etName = view.findViewById<android.widget.EditText>(R.id.etProfileName)

        AlertDialog.Builder(this)
            .setTitle("Editar Perfil")
            .setView(view)
            .setPositiveButton("Guardar") { _, _ ->
                val name = etName.text.toString().trim()
                db.collection("users").document(uid)
                    .update(mapOf(
                        "nomeUtilizador" to name
                    ))
                    .addOnSuccessListener {
                        binding.tvUserName.text = name
                        Toast.makeText(this, "Perfil atualizado", Toast.LENGTH_SHORT).show()
                    }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}

