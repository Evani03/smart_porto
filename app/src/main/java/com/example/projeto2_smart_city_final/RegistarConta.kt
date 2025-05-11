package com.example.projeto2_smart_city_final

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.example.projeto2_smart_city_final.MainActivity
import com.example.projeto2_smart_city_final.databinding.RegistarContaBinding
import com.google.firebase.auth.FirebaseAuth

class RegistarConta : ComponentActivity() {

    private lateinit var binding: RegistarContaBinding
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = RegistarContaBinding.inflate(layoutInflater)
        setContentView(binding.root)


        firebaseAuth = FirebaseAuth.getInstance()
        binding.createAccountButton.setOnClickListener{
            val intent = Intent(this,CriarConta::class.java)
            startActivity(intent)
            finish()
        }

        binding.loginButton.setOnClickListener{
            val  email = binding.emailInput.text.toString()
            val pass = binding.passwordInput.text.toString()


            if(email.isNotEmpty() && pass.isNotEmpty())
            {
                firebaseAuth.signInWithEmailAndPassword(email,pass).addOnCompleteListener{
                    if(it.isSuccessful)
                    {
                        val intent = Intent(this, Mapa::class.java)
                        startActivity(intent)
                        finish()
                    }
                    else
                    {
                        Toast.makeText(this,it.exception.toString(), Toast.LENGTH_SHORT).show()
                    }

                }


            }
            else
            {
                Toast.makeText(this,"Empty Fields Are not Allowed !!", Toast.LENGTH_SHORT).show()
            }

        }
    }

    override fun onStart() {
        super.onStart()
        if(firebaseAuth.currentUser != null)
        {
            val intent = Intent(this, Mapa::class.java)
            startActivity(intent)
            finish()
        }
    }
}