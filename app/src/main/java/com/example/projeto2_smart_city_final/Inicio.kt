package com.example.projeto2_smart_city_final

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.projeto2_smart_city_final.databinding.InicioBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class Inicio : AppCompatActivity() {

    data class ServiceDb(
             val tipoServico: String = "",
              val descricao: String = "",
              val contacto: String = "",
              val valor: Double = 0.0
                  )

    private lateinit var binding: InicioBinding
    private var allServices: List<ServiceDb> = emptyList()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.inicio)
        binding = InicioBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {}
                R.id.nav_mapa ->  {
                    startActivity(Intent(this, Mapa::class.java))
                    finish()}
                //R.id.nav_social -> startActivity(Intent(this, SocialActivity::class.java))
                //R.id.nav_conta -> startActivity(Intent(this, ContaActivity::class.java))
            }
            true
        }
        // Header Image estática
        val ivHeader = findViewById<ImageView>(R.id.ivHeader)
        Glide.with(this)
            .load(R.drawable.porto_ft_cidade)
            .centerCrop()
            .into(ivHeader)

        // Bem-vindo
        binding.welcomeText.text = "Bem-vindo ao Porto"

        // Botão definições
        val noticias = listOf(
            Noticia("Título 1", "Descrição da notícia 1"),
            Noticia("Título 2", "Descrição da notícia 2"),
            Noticia("Título 3", "Descrição da notícia 3"),
            Noticia("Título 4", "Descrição da notícia 4")
        )

        val noticiaAdapter = NoticiaAdapter(noticias) { noticia ->
            val intent = Intent(this, RegistarConta::class.java)
            intent.putExtra("titulo", noticia.titulo)
            intent.putExtra("descricao", noticia.descricao)
            startActivity(intent)
        }

        binding.rvNews.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.rvNews.adapter = noticiaAdapter

        // Botão para ver todas as notícias

        Firebase.firestore.collection("servico")
                 .get()
                  .addOnSuccessListener { snaps ->
                        // converte cada doc num ServiceDb
                        allServices = snaps.documents.map { doc ->
                             ServiceDb(
                                    tipoServico = doc.getString("tipo_servico") ?: "",
                                    descricao    = doc.getString("descricao")    ?: "",
                                    contacto     = doc.getString("contacto")     ?: "",
                                    valor        = doc.getDouble("valor")        ?: 0.0
                                          )
                            }
                        // extrai apenas os tipos distintos
                        val tipos = allServices.map { it.tipoServico }.distinct()
                        // adapta para lista de tipos, com clique a mostrar serviços desse tipo

                        // cria adapter que recebe uma lista de tipos e abre diálogo ao clicar
                        val servicoAdapter = ServicoAdapter(tipos) { tipoSelecionado ->
                                          showServicesForType(tipoSelecionado.toString())
                                      }
                                 binding.rvServices.layoutManager =
                                          LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
                                  binding.rvServices.adapter = servicoAdapter
                     }


    }

    private fun showServicesForType(tipo: String) {
        val lista = allServices.filter { it.tipoServico == tipo }
        val itens  = lista.map { "${it.descricao} (Contacto: ${it.contacto}, valor: ${it.valor})" }
        AlertDialog.Builder(this)
            .setTitle("Serviços – $tipo")
            .setItems(itens.toTypedArray(), null)
            .setPositiveButton("OK", null)
            .show()
    }
}


