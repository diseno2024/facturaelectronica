package com.example.facturaelectronica
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.couchbase.lite.CouchbaseLite
import com.couchbase.lite.CouchbaseLiteException
import com.couchbase.lite.DataSource
import com.couchbase.lite.Database
import com.couchbase.lite.MutableDocument
import com.couchbase.lite.QueryBuilder
import com.couchbase.lite.SelectResult

class ReDatosContribuyenteActivity : AppCompatActivity() {

    private lateinit var datosContribuyente: Database
    private lateinit var razonSocial: EditText
    private lateinit var nit: EditText
    private lateinit var actividadEconomica: EditText
    private lateinit var nrc: EditText
    private lateinit var direccion: EditText
    private lateinit var email: EditText
    private lateinit var nombreComercial: EditText
    private lateinit var telefono: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_re_datos_contribuyente)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val app = application as MyApp
        datosContribuyente = app.database

        val btnGuardar = findViewById<android.widget.Button>(R.id.guardarButton)
        val btnCancelar = findViewById<android.widget.Button>(R.id.cancelarButton)

        // Inicializar las vistas
        razonSocial = findViewById(R.id.RazonSocialText)
        nit = findViewById(R.id.NitText)
        actividadEconomica = findViewById(R.id.ActividadEcoText)
        nrc = findViewById(R.id.NRCText)
        direccion = findViewById(R.id.DireccionText)
        email = findViewById(R.id.correoText)
        nombreComercial = findViewById(R.id.NomComercialText)
        telefono = findViewById(R.id.TelefonoText)
        btnGuardar.setOnClickListener{
            guardarDatosContribuyente()
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

    private fun guardarDatosContribuyente() {
        val razonSocialText = razonSocial.text.toString()
        val nitText = nit.text.toString()
        val actividadEconomicaText = actividadEconomica.text.toString()
        val nrcText = nrc.text.toString()
        val direccionText = direccion.text.toString()
        val emailText = email.text.toString()
        val nombreComercialText = nombreComercial.text.toString()
        val telefonoText = telefono.text.toString().replace("-", "")

        // Crear un documento mutable para guardar en la base de datos
        val document = MutableDocument()
            .setString("RazonSocial", razonSocialText)
            .setString("NIT", nitText)
            .setString("ActividadEconomica", actividadEconomicaText)
            .setString("NRC", nrcText)
            .setString("Direccion", direccionText)
            .setString("Email", emailText)
            .setString("NombreComercial", nombreComercialText)
            .setString("Telefono", telefonoText)
            .setString("tipo", "contribuyente")

        try {
            // Guardar el documento en la base de datos
            datosContribuyente.save(document)
            Log.d("ContribuyenteActivity", "Datos guardados correctamente: \n $document")
            Toast.makeText(this, "Datos guardados correctamente", Toast.LENGTH_SHORT).show()
            limpiarEditText()  // Limpiar los EditTexts
            returnToMenu()  // Regresar al menú
        } catch (e: CouchbaseLiteException) {
            Log.e("ContribuyenteActivity", "Error al guardar los datos en la base de datos: ${e.message}", e)
            Toast.makeText(this, "Error al guardar los datos", Toast.LENGTH_SHORT).show()
        }
    }


    private fun limpiarEditText() {
        razonSocial.setText("")
        nit.setText("")
        actividadEconomica.setText("")
        nrc.setText("")
        direccion.setText("")
        email.setText("")
        nombreComercial.setText("")
        telefono.setText("")
    }

    private fun returnToMenu() {
        val intent = Intent(this, MenuActivity::class.java)
        startActivity(intent)
        finish()
    }
}