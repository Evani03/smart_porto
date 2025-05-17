package com.example.projeto2_smart_city_final.menuInicio

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.projeto2_smart_city_final.menuConta.ContaActivity
import com.example.projeto2_smart_city_final.criarRegistarConta.RegistarConta
import com.example.projeto2_smart_city_final.menuMapa.Mapa
import com.example.projeto2_smart_city_final.R
import com.example.projeto2_smart_city_final.databinding.InicioBinding
import com.example.projeto2_smart_city_final.menuSocial.SocialActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.firestore.Query

class Inicio : AppCompatActivity() {


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
                R.id.nav_social -> {startActivity(Intent(this, SocialActivity::class.java))
                    finish()}
                R.id.nav_conta ->{ startActivity(Intent(this, ContaActivity::class.java))
                finish()}
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

        // 1) Primeiro, a lista e o adapter
        val newsList = mutableListOf<Noticia>()
        val newsAdapter = NoticiaAdapter(newsList) { noticia ->
            val intent = Intent(this, SocialActivity::class.java)
            intent.putExtra("open_news_id", noticia.id)
            startActivity(intent)
        }
        binding.rvNews.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.rvNews.adapter = newsAdapter

// 2) Busca as 4 notícias mais recentes
        Firebase.firestore
            .collection("news")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(4)
            .get()
            .addOnSuccessListener { snaps ->
                newsList.clear()
                snaps.documents.forEach { doc ->
                    newsList.add(
                        Noticia(
                            id       = doc.id,
                            titulo   = doc.getString("title") ?: "",
                            lead     = doc.getString("lead")  ?: "",
                            imageUrl = doc.getString("imageUrl")
                        )
                    )
                }
                newsAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Erro a carregar notícias: ${e.message}", Toast.LENGTH_SHORT).show()
            }



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
                                          showServicesForType(tipoSelecionado)
                                      }
                                 binding.rvServices.layoutManager =
                                          LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
                                  binding.rvServices.adapter = servicoAdapter
                     }


    }

    private fun showServicesForType(tipo: String) {
        // 1) Filtra a lista de todos os serviços
        val lista = allServices.filter { it.tipoServico == tipo }


        // 2) Infla o layout do diálogo que tem a RecyclerView
        val dialogView = layoutInflater.inflate(R.layout.dialog_service_list, null)

        // 3) Liga o RecyclerView ao adapter
        val rv = dialogView.findViewById<RecyclerView>(R.id.rvServiceDetails)
        rv.layoutManager = LinearLayoutManager(this)
        rv.adapter = ServiceDetailAdapter(lista)

        // 4) Mostra o diálogo com o RecyclerView
        //AlertDialog.Builder(this)
        MaterialAlertDialogBuilder(this)
            .setTitle("Serviço – $tipo")
            .setView(dialogView)
            .setPositiveButton("Fechar", null)
            .show()
    }


}


