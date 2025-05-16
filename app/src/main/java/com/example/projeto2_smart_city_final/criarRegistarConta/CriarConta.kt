package com.example.projeto2_smart_city_final.criarRegistarConta

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import com.example.projeto2_smart_city_final.databinding.CriarContaBinding
import com.google.firebase.auth.FirebaseAuth
import androidx.appcompat.app.AppCompatActivity
import com.example.projeto2_smart_city_final.menuInicio.Inicio
import com.google.firebase.ktx.Firebase
import com.google.firebase.firestore.ktx.firestore


class CriarConta : AppCompatActivity() {

    private lateinit var binding: CriarContaBinding
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = CriarContaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        binding.loginButton2.setOnClickListener {
            val intent = Intent(this, RegistarConta::class.java)
            startActivity(intent)
        }

        binding.submitButton.setOnClickListener {
            val email = binding.emailInput.text.toString()
            val pass = binding.passwordInput.text.toString()
            val confirm_pass = binding.confirmPasswordInput.text.toString()
            val username = binding.usernameInput.text.toString()

            if (email.isNotEmpty() && pass.isNotEmpty() && confirm_pass.isNotEmpty()) {
                if (pass == confirm_pass) {
                    firebaseAuth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener {
                        if (it.isSuccessful) {
                            // 1) Obter o UID
                                        val uid = firebaseAuth.currentUser!!.uid
                                        // 2) Montar o objeto para a coleção 'users'
                                        val userProfile = mapOf(
                                                "nomeUtilizador" to username,     // ou podes usar uid ou uma string vazia
                                                "email"         to email,
                                                "tipoConta"     to "cidadao"
                                                    )
                                       // 3) Gravar em Firestore
                                        Firebase.firestore
                                            .collection("users")
                                            .document(uid)
                                            .set(userProfile)
                                            .addOnSuccessListener {
                                                    // Só depois daqueles dados escritos é que prosseguimos
                                                    val intent = Intent(this, RegistarConta::class.java)
                                                   startActivity(intent)
                                                    finish()
                                                }
                                             .addOnFailureListener { e ->
                                                    Toast.makeText(this,
                                                            "Conta criada mas falhou gravar perfil: ${e.message}",
                                                            Toast.LENGTH_LONG
                                                                ).show()
                                                }
                        } else {
                            Toast.makeText(this, it.exception.toString(), Toast.LENGTH_SHORT).show()
                        }

                    }
                } else {
                    Toast.makeText(this, "Password is not matching", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Empty Fields Are not Allowed !!", Toast.LENGTH_SHORT).show()
            }

        }

    }

    override fun onStart() {
        super.onStart()
        if (firebaseAuth.currentUser != null) {
            val intent = Intent(this, Inicio::class.java)
            startActivity(intent)
            finish()
        }
    }


}



