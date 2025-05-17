package com.example.projeto2_smart_city_final.menuSocial

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.example.projeto2_smart_city_final.databinding.DialogCreateNewsBinding
import androidx.appcompat.app.AppCompatActivity
import com.example.projeto2_smart_city_final.R
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

class CreateNewsActivity : AppCompatActivity() {

    companion object {
        private const val PICK_IMAGE_REQUEST = 1001
    }

    private var selectedImageUri: Uri? = null
    private lateinit var binding: DialogCreateNewsBinding

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            selectedImageUri = result.data?.data
            binding.ivPreviewImage.apply {
                setImageURI(selectedImageUri)
                visibility = View.VISIBLE
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_create_news) // ou o nome que deste ao teu XML
        binding = DialogCreateNewsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val etTitle      = findViewById<EditText>(R.id.etCreateTitle)
        val etLead       = findViewById<EditText>(R.id.etCreateLead)
        val etText       = findViewById<EditText>(R.id.etCreateText)
        val btnPickImage = findViewById<Button>(R.id.btnPickImage)
        val ivPreview    = findViewById<ImageView>(R.id.ivPreviewImage)
        val btnSubmit    = findViewById<Button>(R.id.btnSubmit)

        binding.btnPickImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            pickImageLauncher.launch(intent)
        }

        btnSubmit.setOnClickListener {
            val title = etTitle.text.toString().trim()
            val lead  = etLead.text.toString().trim()
            val body  = etText.text.toString().trim()

            if (title.isEmpty() || lead.isEmpty() || body.isEmpty()) {
                Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val auth    = FirebaseAuth.getInstance()
            val uid     = auth.currentUser?.uid ?: return@setOnClickListener
            val firestore = Firebase.firestore
            val storage   = Firebase.storage.reference

            // Primeiro: se houver imagem, faz upload
            if (selectedImageUri != null) {
                val imgRef = storage.child("news_images/${System.currentTimeMillis()}.jpg")
                imgRef.putFile(selectedImageUri!!)
                    .addOnSuccessListener {
                        imgRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                            saveNews(firestore, uid, title, lead, body, downloadUrl.toString())
                        }
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Erro ao enviar imagem: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            } else {
                // sem imagem
                saveNews(firestore, uid, title, lead, body, null)
            }
        }
    }

    private fun saveNews(
        db: com.google.firebase.firestore.FirebaseFirestore,
        userId: String,
        title: String,
        lead: String,
        body: String,
        imageUrl: String?
    ) {
        val data = mutableMapOf<String, Any>(
            "userId"    to userId,
            "title"     to title,
            "lead"      to lead,
            "body"      to body,
            "timestamp" to Timestamp.now()
        )
        imageUrl?.let { data["imageUrl"] = it }

        db.collection("news")
            .add(data)
            .addOnSuccessListener {
                Toast.makeText(this, "Notícia criada com sucesso!", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Erro ao criar notícia: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

}
