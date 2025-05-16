package com.example.projeto2_smart_city_final.menuConta

import android.os.Bundle
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.projeto2_smart_city_final.R
import com.example.projeto2_smart_city_final.databinding.ActivityServiceListBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

data class ServiceItem(
    val id: String = "",
    val tipoServico: String = "",
    val descricao: String = "",
    val contacto: String = "",
    val valor: Double = 0.0
)

class ServiceListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityServiceListBinding
    private val db = Firebase.firestore
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityServiceListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.rvServices.layoutManager = LinearLayoutManager(this)
        binding.swipeRefresh.setOnRefreshListener { loadServices() }
        binding.fabAddService.setOnClickListener { showNewServiceDialog() }

        loadServices()
    }

    private fun loadServices() {
        binding.swipeRefresh.isRefreshing = true
        val uid = auth.currentUser?.uid ?: return

        db.collection("servico")
            .whereEqualTo("userId", uid)
            .get()
            .addOnSuccessListener { snaps ->
                val list = snaps.documents.mapNotNull { doc ->
                    val id = doc.id
                    val t  = doc.getString("tipo_servico") ?: ""
                    val d  = doc.getString("descricao")    ?: ""
                    val c  = doc.getString("contacto")     ?: ""
                    val v  = doc.getDouble("valor")        ?: 0.0
                    ServiceItem(id,t, d, c, v)
                }
                // passamos um lambda que recebe o ServiceItem clicado
                binding.rvServices.adapter = ServiceAdapter(list) { service ->
                       showDeleteServiceDialog(service) }
                binding.swipeRefresh.isRefreshing = false
            }
            .addOnFailureListener {
                binding.swipeRefresh.isRefreshing = false
            }
    }

        private fun showNewServiceDialog() {
            val uid = auth.currentUser?.uid ?: return
            val dialogView = layoutInflater.inflate(R.layout.dialog_new_service, null)
            val spinnerTipo = dialogView.findViewById<Spinner>(R.id.spinnerTipoServico)
            val etDesc     = dialogView.findViewById<EditText>(R.id.etServicoDescricao)
            val etContato  = dialogView.findViewById<EditText>(R.id.etServicoContacto)
            val etValor    = dialogView.findViewById<EditText>(R.id.etServicoValor)

            AlertDialog.Builder(this)
                .setTitle("Novo Serviço")
                .setView(dialogView)
                .setPositiveButton("Criar") { _, _ ->
                    val tipo     = spinnerTipo.selectedItem.toString()
                    val desc     = etDesc.text.toString().trim()
                    val contacto = etContato.text.toString().trim()
                    val valorStr = etValor.text.toString().trim()
                    if (desc.isEmpty() || contacto.isEmpty() || valorStr.isEmpty()) {
                        Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show()
                        return@setPositiveButton
                    }
                    val valor = valorStr.toDoubleOrNull()
                    if (valor == null) {
                        Toast.makeText(this, "Valor inválido", Toast.LENGTH_SHORT).show()
                        return@setPositiveButton
                    }
                    // Prepara mapa para Firestore
                    val data = mapOf(
                        "userId"       to uid,
                        "tipo_servico" to tipo,
                        "descricao"    to desc,
                        "contacto"     to contacto,
                        "valor"        to valor
                    )
                    binding.swipeRefresh.isRefreshing = true
                    db.collection("servico")
                        .add(data)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Serviço criado", Toast.LENGTH_SHORT).show()
                            loadServices()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this, "Erro: ${e.message}", Toast.LENGTH_SHORT).show()
                            binding.swipeRefresh.isRefreshing = false
                        }
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }

    private fun showDeleteServiceDialog(service: ServiceItem) {
          AlertDialog.Builder(this)
            .setTitle("Apagar serviço?")
            .setMessage("Deseja realmente apagar “${service.descricao}”?")
            .setPositiveButton("Sim") { _, _ ->
                  binding.swipeRefresh.isRefreshing = true
                  db.collection("servico")
                    .document(service.id)                      // ← usamos o documentId
                    .delete()
                    .addOnSuccessListener {
                          Toast.makeText(this, "Serviço apagado", Toast.LENGTH_SHORT).show()
                          loadServices()                           // refresca a lista
                        }
                    .addOnFailureListener { e ->
                          Toast.makeText(this, "Erro: ${e.message}", Toast.LENGTH_SHORT).show()
                          binding.swipeRefresh.isRefreshing = false
                        }
                }
            .setNegativeButton("Não", null)
            .show()
        }

    }
