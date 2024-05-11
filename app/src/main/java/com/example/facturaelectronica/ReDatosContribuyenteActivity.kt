package com.example.facturaelectronica
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.couchbase.lite.CouchbaseLite
import com.couchbase.lite.DataSource
import com.couchbase.lite.Database
import com.couchbase.lite.MutableDocument
import com.couchbase.lite.QueryBuilder
import com.couchbase.lite.SelectResult

class ReDatosContribuyenteActivity : AppCompatActivity() {

    private lateinit var datosContribuyente: Database

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_re_datos_contribuyente)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        CouchbaseLite.init(applicationContext)

        datosContribuyente = Database("datosContribuyente")

        val btnGuardar = findViewById<android.widget.Button>(R.id.GuardarButton)
        val btnCancelar = findViewById<android.widget.Button>(R.id.CancelarButton)

        btnGuardar.setOnClickListener{
            guardarDatosContribuyente(
                findViewById<android.widget.EditText>(R.id.RazonSocialText).text.toString(),
                findViewById<android.widget.EditText>(R.id.NitText).text.toString(),
                findViewById<android.widget.EditText>(R.id.ActividadEcoText).text.toString(),
                findViewById<android.widget.EditText>(R.id.NRCText).text.toString(),
                findViewById<android.widget.EditText>(R.id.DireccionText).text.toString(),
                findViewById<android.widget.EditText>(R.id.correoText).text.toString(),
                findViewById<android.widget.EditText>(R.id.NomComercialText).text.toString(),
                findViewById<android.widget.EditText>(R.id.TelefonoText).text.toString()
            )
        }

        btnCancelar.setOnClickListener{
            val intent = Intent(this, MenuActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(this, MenuActivity::class.java)
        startActivity(intent)
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        datosContribuyente.close()
    }

    private fun guardarDatosContribuyente(razonSocialText: String,
                                          nitText: String,
                                          actividadEconomica: String,
                                          nrc: String,
                                          direccion: String,
                                          email: String,
                                          nombreComercial: String,
                                          telefono: String) {
        val docId = "datos_contribuyentes"
        val doc = datosContribuyente.getDocument(docId) ?: MutableDocument(docId)


    }

    private fun returnToMenu() {
        val intent = Intent(this, MenuActivity::class.java)
        startActivity(intent)
        finish()
    }
}