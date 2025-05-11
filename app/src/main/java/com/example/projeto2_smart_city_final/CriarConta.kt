package com.example.projeto2_smart_city_final

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.example.projeto2_smart_city_final.databinding.CriarContaBinding
import com.google.firebase.auth.FirebaseAuth

class CriarConta : ComponentActivity() {

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

            if (email.isNotEmpty() && pass.isNotEmpty() && confirm_pass.isNotEmpty()) {
                if (pass == confirm_pass) {
                    firebaseAuth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener {
                        if (it.isSuccessful) {
                            val intent = Intent(this, RegistarConta::class.java)
                            startActivity(intent)
                            finish()
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
            val intent = Intent(this, Mapa::class.java)
            startActivity(intent)
            finish()
        }
    }
}



