package com.example.projeto2_smart_city_final

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.projeto2_smart_city_final.databinding.MapaBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.Timestamp
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.text.DateFormat
import java.util.*

class Mapa : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: MapaBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var googleMap: GoogleMap? = null
    private var reportLatLng: LatLng? = null
    private var reportingMode = false

    data class Issue(
        val lat: Double = 0.0,
        val lng: Double = 0.0,
        val titulo: String = "",
        val tipo: String = "",
        val enviarPara: String = "",
        val descricao: String = "",
        val timestamp: Timestamp = Timestamp.now() // Firestore Timestamp
    )

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1000
        private val PORTO_CENTER = LatLng(41.1579, -8.6291)
        private const val INITIAL_ZOOM = 12f
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = MapaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.id_map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // FAB “Reportar problema”
        binding.root.findViewById<FloatingActionButton>(R.id.btn_reportar_problema)
            .setOnClickListener {
                reportingMode = true
                Toast.makeText(
                    this,
                    "Toque no mapa para escolher localização do problema",
                    Toast.LENGTH_SHORT
                ).show()
            }

        // Formulário de reporte
        val form = binding.root.findViewById<View>(R.id.viewReportProblema)
        val etTitulo = form.findViewById<EditText>(R.id.etTituloProblema)
        val spinnerTipo = form.findViewById<Spinner>(R.id.spinnerTipoProblema)
        val spinnerEnviar = form.findViewById<Spinner>(R.id.spinnerEnviarPara)
        val etDescricao = form.findViewById<EditText>(R.id.etDescricaoProblema)
        val btnCancelar = form.findViewById<Button>(R.id.btnCancelarReport)
        val btnGuardar = form.findViewById<Button>(R.id.btnGuardarReport)

        btnCancelar.setOnClickListener { form.visibility = View.GONE }

        btnGuardar.setOnClickListener {
            val titulo = etTitulo.text.toString().trim()
            val tipo = spinnerTipo.selectedItem.toString()
            val enviarPara = spinnerEnviar.selectedItem.toString()
            val descricao = etDescricao.text.toString().trim()
            val latLng = reportLatLng

            if (titulo.isNotEmpty() && descricao.isNotEmpty() && latLng != null) {
                val issue = Issue(
                    lat = latLng.latitude,
                    lng = latLng.longitude,
                    titulo = titulo,
                    tipo = tipo,
                    enviarPara = enviarPara,
                    descricao = descricao
                )
                Firebase.firestore.collection("issues")
                    .add(issue)
                    .addOnSuccessListener { reference ->
                        // Adiciona marcador local se não for só para Câmara
                        if (enviarPara != "Câmara") {
                            val marker = googleMap?.addMarker(
                                MarkerOptions()
                                    .position(latLng)
                                    .title(issue.titulo)
                                    .snippet(issue.descricao)
                            )
                            marker?.tag = issue
                        }
                        // limpa e esconde form
                        etTitulo.setText("")
                        spinnerTipo.setSelection(0)
                        spinnerEnviar.setSelection(0)
                        etDescricao.setText("")
                        form.visibility = View.GONE
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(
                            this,
                            "Erro ao guardar: ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            } else {
                Toast.makeText(this, "Preenche todos os campos", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(PORTO_CENTER, INITIAL_ZOOM))
        enableUserLocation()

        // Carrega e desenha problemas salvos
        Firebase.firestore.collection("issues")
            .get()
            .addOnSuccessListener { snaps ->
                snaps.documents.forEach { doc ->
                    val lat = doc.getDouble("lat") ?: return@forEach
                    val lng = doc.getDouble("lng") ?: return@forEach
                    val titulo = doc.getString("titulo") ?: ""
                    val tipo = doc.getString("tipo") ?: ""
                    val enviarPara = doc.getString("enviarPara") ?: ""
                    val descricao = doc.getString("descricao") ?: ""
                    val timestamp = doc.getTimestamp("timestamp") ?: Timestamp.now()
                    val issue = Issue(lat, lng, titulo, tipo, enviarPara, descricao, timestamp)
                    val marker = map.addMarker(
                        MarkerOptions()
                            .position(LatLng(lat, lng))
                            .title(issue.titulo)
                            .snippet(issue.descricao)
                    )
                    marker?.tag = issue
                }
            }

        // Custom InfoWindow
        map.setInfoWindowAdapter(object : InfoWindowAdapter {
            override fun getInfoWindow(marker: Marker): View? = null
            override fun getInfoContents(marker: Marker): View {
                val infoView = layoutInflater.inflate(
                    R.layout.custom_info_window,
                    null
                )
                val titleView = infoView.findViewById<TextView>(R.id.tvInfoTitle)
                val tipoView = infoView.findViewById<TextView>(R.id.tvInfoTipo)
                val descView = infoView.findViewById<TextView>(R.id.tvInfoDescricao)
                val enviarView = infoView.findViewById<TextView>(R.id.tvInfoEnviarPara)
                val timeView = infoView.findViewById<TextView>(R.id.tvInfoTimestamp)

                titleView.text = marker.title
                // extrai Issue do tag
                val issue = marker.tag as? Issue
                tipoView.text = "Tipo: ${issue?.tipo ?: ""}"
                descView.text = issue?.descricao
                enviarView.text = "Enviar para: ${issue?.enviarPara ?: ""}"
                val timestamp = issue?.timestamp?.toDate()?.time ?: 0L
                timeView.text = DateFormat.getDateTimeInstance().format(Date(timestamp))

                return infoView
            }
        })

        // Clique no mapa para reportar
        map.setOnMapClickListener { latLng ->
            if (reportingMode) {
                reportingMode = false
                reportLatLng = latLng
                binding.root.findViewById<View>(R.id.viewReportProblema).visibility =
                    View.VISIBLE
            }
        }
    }

    private fun enableUserLocation() {
        val map = googleMap ?: return
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        } else {
            @Suppress("MissingPermission")
            map.isMyLocationEnabled = true
            fusedLocationClient.lastLocation.addOnSuccessListener { loc: Location? ->
                loc?.let {
                    map.animateCamera(
                        CameraUpdateFactory.newLatLngZoom(
                            LatLng(it.latitude, it.longitude),
                            INITIAL_ZOOM
                        )
                    )
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE &&
            grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            enableUserLocation()
        }
    }

}

