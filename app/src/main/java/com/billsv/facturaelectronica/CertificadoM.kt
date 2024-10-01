package com.billsv.facturaelectronica

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.couchbase.lite.Database
import android.net.Uri
import android.util.Log
import android.widget.Toast
import com.couchbase.lite.CouchbaseLiteException
import com.couchbase.lite.DataSource
import com.couchbase.lite.Expression
import com.couchbase.lite.Meta
import com.couchbase.lite.MutableDocument
import com.couchbase.lite.QueryBuilder
import com.couchbase.lite.SelectResult
import android.app.Activity
import android.content.Intent
import android.widget.ImageButton


class CertificadoM : AppCompatActivity() {

    private lateinit var database: Database
    private lateinit var btnGuardarEditar: Button
    private lateinit var btnSelectCer: Button
    private lateinit var btnSelectClave: Button
    private lateinit var selectedCerTextView: TextView
    private lateinit var selectedClaveTextView: TextView

    private val PICK_CERTIFICATE_REQUEST_CODE = 1
    private val PICK_PUBLIC_KEY_REQUEST_CODE = 2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_certificado_m)

        // Acceder a la base de datos
        val app = application as MyApp
        database = app.database

        // Inicializa los botones y textviews
        btnSelectCer = findViewById(R.id.btn_select_cer)
        btnSelectClave = findViewById(R.id.btn_select_clave)
        selectedCerTextView = findViewById(R.id.selected_cer)
        selectedClaveTextView = findViewById(R.id.selected_clave)

        // Verifica si existen documentos en la base de datos
        verificarDocumentos()

        // Botones de selección de archivos
        btnSelectCer.setOnClickListener {
            openFilePicker(PICK_CERTIFICATE_REQUEST_CODE)
        }
        btnSelectClave.setOnClickListener {
            openFilePicker(PICK_PUBLIC_KEY_REQUEST_CODE)
        }

        val atras = findViewById<ImageButton>(R.id.atras)
        atras.setOnClickListener {
            super.onBackPressed() // Llama al método onBackPressed() de la clase base
            val intent = Intent(this, MenuActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    // Verifica si existen documentos de tipo "certificado" y "clave_privada"
    private fun verificarDocumentos() {
        val query = QueryBuilder.select(
            SelectResult.property("certificado_fileName"),
            SelectResult.property("clave_privada_fileName")
        )
            .from(DataSource.database(database))
            .where(Expression.property("tipo").`in`(
                Expression.string("certificado"),
                Expression.string("clave_privada")
            ))

        val resultSet = query.execute()

        // Muestra los nombres de los archivos guardados si existen
        for (result in resultSet) {
            val certificadoFileName = result.getString("certificado_fileName")
            val claveFileName = result.getString("clave_privada_fileName")

            if (certificadoFileName != null) {
                selectedCerTextView.text = "$certificadoFileName"
            }

            if (claveFileName != null) {
                selectedClaveTextView.text = "$claveFileName"
            }
        }
    }

    // Abrir el picker de archivos
    private fun openFilePicker(requestCode: Int) {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.type = "*/*"
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        startActivityForResult(intent, requestCode)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK && data != null) {
            // Aquí iría la lógica de carga de archivos...
        }
    }
}
