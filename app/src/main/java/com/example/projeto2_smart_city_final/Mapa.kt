package com.example.projeto2_smart_city_final

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.location.Location
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.projeto2_smart_city_final.databinding.MapaBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
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

    private val issues = mutableListOf<Issue>()
    private val selectedTypes = mutableSetOf<String>()

    data class Issue(
        val lat: Double = 0.0,
        val lng: Double = 0.0,
        val titulo: String = "",
        val tipo: String = "",
        val enviarPara: String = "",
        val descricao: String = "",
        val timestamp: Timestamp = Timestamp.now()
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
        val mapFragment = supportFragmentManager.findFragmentById(R.id.id_map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // FAB “Reportar problema”
        binding.root.findViewById<FloatingActionButton>(R.id.btn_reportar_problema)
            .setOnClickListener { enterReportingMode() }

        // FAB “Filtrar tipos”
        binding.root.findViewById<FloatingActionButton>(R.id.btn_filter)
            .setOnClickListener { showFilterDialog() }

        setupForm()
    }

    private fun enterReportingMode() {
        reportingMode = true
        binding.btnFilter.visibility = View.GONE
        findViewById<FloatingActionButton>(R.id.btn_filter).visibility = View.GONE
        Toast.makeText(this, "Toque no mapa para escolher localização do problema", Toast.LENGTH_SHORT).show()
    }

    private fun setupForm() {
        val form = binding.root.findViewById<View>(R.id.viewReportProblema)
        val etTitulo = form.findViewById<EditText>(R.id.etTituloProblema)
        val spinnerTipo = form.findViewById<Spinner>(R.id.spinnerTipoProblema)
        val spinnerEnviar = form.findViewById<Spinner>(R.id.spinnerEnviarPara)
        val etDescricao = form.findViewById<EditText>(R.id.etDescricaoProblema)
        form.findViewById<Button>(R.id.btnCancelarReport).setOnClickListener { form.visibility = View.GONE
            findViewById<View>(R.id.viewReportProblema).visibility = View.GONE
            exitReportingMode()}
        form.findViewById<Button>(R.id.btnGuardarReport).setOnClickListener {
            val titulo = etTitulo.text.toString().trim()
            val tipo = spinnerTipo.selectedItem.toString()
            val enviarPara = spinnerEnviar.selectedItem.toString()
            val descricao = etDescricao.text.toString().trim()
            val latLng = reportLatLng
            if (titulo.isNotEmpty() && descricao.isNotEmpty() && latLng != null) {
                val issue = Issue(lat = latLng.latitude, lng = latLng.longitude, titulo = titulo,
                    tipo = tipo, enviarPara = enviarPara, descricao = descricao)
                Firebase.firestore.collection("issues").add(issue)
                    .addOnSuccessListener {
                        if (enviarPara != "Câmara") addMarkerForIssue(issue)
                        etTitulo.setText(""); spinnerTipo.setSelection(0)
                        spinnerEnviar.setSelection(0); etDescricao.setText("")
                        form.visibility = View.GONE
                        findViewById<View>(R.id.viewReportProblema).visibility = View.GONE
                        exitReportingMode()
                    }.addOnFailureListener { e -> Toast.makeText(this,
                        "Erro ao guardar: ${e.message}", Toast.LENGTH_SHORT).show() }
            } else Toast.makeText(this, "Preenche todos os campos", Toast.LENGTH_SHORT).show()
        }
    }

    private fun exitReportingMode() {
        reportingMode = false
        binding.btnFilter.visibility = View.VISIBLE
        findViewById<FloatingActionButton>(R.id.btn_filter).visibility = View.VISIBLE
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(PORTO_CENTER, INITIAL_ZOOM))
        enableUserLocation()

        // Carrega Firestore e adiciona marcadores iniciais
        Firebase.firestore.collection("issues").get().addOnSuccessListener { snaps ->
            snaps.documents.forEach { doc ->
                val issue = Issue(
                    lat = doc.getDouble("lat") ?: return@forEach,
                    lng = doc.getDouble("lng") ?: return@forEach,
                    titulo = doc.getString("titulo") ?: "",
                    tipo = doc.getString("tipo") ?: "",
                    enviarPara = doc.getString("enviarPara") ?: "",
                    descricao = doc.getString("descricao") ?: "",
                    timestamp = doc.getTimestamp("timestamp") ?: Timestamp.now()
                )
                issues.add(issue)
                if (issue.enviarPara != "Câmara") addMarkerForIssue(issue)
            }
        }

        map.setInfoWindowAdapter(object : InfoWindowAdapter {
            override fun getInfoWindow(marker: Marker) = null
            override fun getInfoContents(marker: Marker): View {
                val view = layoutInflater.inflate(R.layout.custom_info_window, null)
                view.findViewById<TextView>(R.id.tvInfoTitle).text = marker.title
                view.findViewById<TextView>(R.id.tvInfoDescricao).text = (marker.tag as Issue).descricao
                val ts = (marker.tag as Issue).timestamp.toDate().time
                view.findViewById<TextView>(R.id.tvInfoTimestamp).text =
                    DateFormat.getDateTimeInstance().format(Date(ts))
                return view
            }
        })

        map.setOnMapClickListener { latLng ->
            if (reportingMode) {
                reportingMode = false; reportLatLng = latLng
                //binding.viewReportProblema.visibility = View.VISIBLE
                binding.root.findViewById<View>(R.id.viewReportProblema).visibility = View.VISIBLE
            }
        }
    }

    private fun addMarkerForIssue(issue: Issue) {
        val map = googleMap ?: return
        val marker = map.addMarker(
            MarkerOptions().position(LatLng(issue.lat, issue.lng))
                .icon(iconForTipo(issue.tipo))
                .title(issue.titulo)
                .snippet(issue.descricao)
        )
        marker?.tag = issue
    }

    private fun iconForTipo(tipo: String): BitmapDescriptor {
        val id = when (tipo) {
            "Sinalização ou acessibilidades" -> R.drawable.ic_tipo_sinalizacao
            "Higiene urbana" -> R.drawable.ic_tipo_higiene
            "Estradas e ciclovias" -> R.drawable.ic_tipo_estradas
            "Segurança pública e ruido" -> R.drawable.ic_tipo_seguranca
            "Recursos naturais e espaços verdes" -> R.drawable.ic_tipo_verdes
            "Saneamento" -> R.drawable.ic_tipo_saneamento
            else -> R.drawable.ic_tipo_outros
        }
        val vector = ContextCompat.getDrawable(this, id)!!
        vector.setBounds(0, 0, vector.intrinsicWidth, vector.intrinsicHeight)
        val bm = Bitmap.createBitmap(vector.intrinsicWidth, vector.intrinsicHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bm)
        vector.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(bm)
    }

    private fun showFilterDialog() {
        val tipos = resources.getStringArray(R.array.tipos_problema)
        val checked = BooleanArray(tipos.size) { tipos[it] in selectedTypes }
        AlertDialog.Builder(this)
            .setTitle("Filtrar tipos de problema")
            .setMultiChoiceItems(tipos, checked) { _, which, isChecked ->
                if (isChecked) selectedTypes.add(tipos[which]) else selectedTypes.remove(tipos[which])
            }
            .setPositiveButton("Aplicar") { _, _ -> refreshMarkers() }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun refreshMarkers() {
        googleMap?.clear()
        val toShow = if (selectedTypes.isEmpty()) issues
        else issues.filter { it.tipo in selectedTypes }
        toShow
            .filter { it.enviarPara != "Câmara" }
            .forEach { addMarkerForIssue(it) }
    }

    private fun enableUserLocation() {
        val map = googleMap ?: return
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
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
                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(it.latitude, it.longitude), INITIAL_ZOOM))
                }
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            enableUserLocation()
        }
    }

}

