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
import android.provider.OpenableColumns
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import androidx.core.content.ContextCompat
import com.google.android.material.card.MaterialCardView


class CertificadoM : AppCompatActivity() {

    private lateinit var database: Database
    private lateinit var btnSelectCer: Button
    private lateinit var btnSelectClave: Button
    private lateinit var selectedCerTextView: TextView
    private lateinit var selectedClaveTextView: TextView
    private lateinit var borrarCer: ImageButton
    private lateinit var borrarClave: ImageButton
    private lateinit var selectedCerUri: Uri
    private lateinit var selectedKeyPrivUri: Uri
    private val PICK_CERTIFICATE_REQUEST_CODE = 1
    private val PICK_PUBLIC_KEY_REQUEST_CODE = 2
    private var cerLoaded = false
    private var keyLoaded = false

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
        borrarCer=findViewById(R.id.btnBorrarCer)
        borrarClave=findViewById(R.id.btnBorrarClave)

        // Verifica si existen documentos en la base de datos
        verificarDocumentos()

        // Botones de selección de archivos
        btnSelectCer.setOnClickListener {
            openFilePicker(PICK_CERTIFICATE_REQUEST_CODE)
        }
        btnSelectClave.setOnClickListener {
            openFilePicker(PICK_PUBLIC_KEY_REQUEST_CODE)
        }
        if (hayCertificado()){
            btnSelectCer.isEnabled=false
            borrarCer.visibility=View.VISIBLE
        }else{
            btnSelectCer.isEnabled=true
        }
        if (hayClavePrivada()){
            btnSelectClave.isEnabled=false
            borrarClave.visibility=View.VISIBLE
        }else{
            btnSelectClave.isEnabled=true
        }
        borrarCer.setOnClickListener {
           borrarCertificado()
        }
        borrarClave.setOnClickListener {
            borrarClave()
        }


        val atras = findViewById<ImageButton>(R.id.atras)
        atras.setOnClickListener {
            if (hayCertificado() && hayClavePrivada()){
                super.onBackPressed() // Llama al método onBackPressed() de la clase base
                val intent = Intent(this, ConfHacienda::class.java)
                startActivity(intent)
                finish()
            }else{
                if (!hayCertificado()){
                    Toast.makeText(this,"Debe agregar su certificado",Toast.LENGTH_SHORT).show()
                }
                if (!hayClavePrivada()){
                    Toast.makeText(this,"Debe agregar su clave privada",Toast.LENGTH_SHORT).show()
                }

            }
        }
    }

    private fun hayCertificado(): Boolean {
        val query = QueryBuilder.select(SelectResult.expression(Meta.id))
            .from(DataSource.database(database))
            .where(Expression.property("tipo").equalTo(Expression.string("certificado")))
        try {
            val resultSet = query.execute()
            val results = resultSet.allResults()

            // Verificar si ya existen documentos
            if (results.isNotEmpty()) {
                return true
            }
        } catch (e: CouchbaseLiteException) {
            Log.e("Certificado", "Error en la operación: ${e.message}")
        }
        return false
    }
    private fun hayClavePrivada(): Boolean {
        val query = QueryBuilder.select(SelectResult.expression(Meta.id))
            .from(DataSource.database(database))
            .where(Expression.property("tipo").equalTo(Expression.string("clave_privada")))
        try {
            val resultSet = query.execute()
            val results = resultSet.allResults()

            // Verificar si ya existen documentos
            if (results.isNotEmpty()) {
                return true
            }
        } catch (e: CouchbaseLiteException) {
            Log.e("Clave", "Error en la operación: ${e.message}")
        }
        return false
    }
    override fun onBackPressed() {
        super.onBackPressed() // Llama al método onBackPressed() de la clase base
        val intent = Intent(this, ConfHacienda::class.java)
        startActivity(intent)
        finish()
    }

    fun guardarCertificado() {
        if (::selectedCerUri.isInitialized) {
            // Conceder permisos de lectura persistentes
            try {
                this?.contentResolver?.takePersistableUriPermission(
                    selectedCerUri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (e: Exception) {
                Log.e("Certificado", "Error al conceder permisos persistentes: ${e.message}")
                Toast.makeText(this, "Error al conceder permisos persistentes", Toast.LENGTH_SHORT).show()
                return
            }
            // Ejecutar una consulta para eliminar los documentos anteriores
            val query = QueryBuilder.select(SelectResult.expression(Meta.id))
                .from(DataSource.database(database))
                .where(Expression.property("tipo").equalTo(Expression.string("certificado")))

            try {
                val resultSet = query.execute()
                val results = resultSet.allResults()

                // Verificar si ya existen documentos
                if (results.isNotEmpty()) {
                    // Iterar sobre los resultados y eliminar cada documento
                    for (result in results) {
                        val docId = result.getString(0)  // Obtenemos el ID del documento en el índice 0
                        docId?.let {
                            val document = database.getDocument(it)
                            document?.let {
                                database.delete(it)
                                Log.d("Certificado", "Documento existente borrado: $docId")
                            }
                        }
                    }
                }
                val fileName = getFileName(selectedCerUri)
                val document = MutableDocument()
                document.setString("tipo", "certificado")  // Establecer el tipo
                document.setString("certificado_fileName", fileName)
                document.setString("certificado_uri", selectedCerUri.toString())  // Guardar la URI

                Log.d("Certificado", "Certificado guardado: $document")

                // Guarda el documento en Couchbase Lite
                database.save(document)
                Toast.makeText(this, "Certificado guardado correctamente ", Toast.LENGTH_SHORT)
                    .show()
                btnSelectCer.isEnabled=false
                borrarCer.visibility=View.VISIBLE
            } catch (e: CouchbaseLiteException) {
                Log.e("Certificado", "Error en la operación: ${e.message}")
            }
        } else {
            Toast.makeText(this, "Por favor selecciona un certificado antes de guardar", Toast.LENGTH_SHORT).show()
        }
    }

    // Guardar Clave privada en la base de datos
    fun guardarClavePrivada() {
        if (::selectedKeyPrivUri.isInitialized) {
            // Conceder permisos de lectura persistentes
            try {
                this?.contentResolver?.takePersistableUriPermission(
                    selectedKeyPrivUri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (e: Exception) {
                Log.e("Clave", "Error al conceder permisos persistentes: ${e.message}")
                Toast.makeText(this, "Error al conceder permisos persistentes", Toast.LENGTH_SHORT).show()
                return
            }
            // Buscar si ya existe un documento del tipo "Clave privada"
            val query = QueryBuilder.select(SelectResult.expression(Meta.id))
                .from(DataSource.database(database))
                .where(Expression.property("tipo").equalTo(Expression.string("clave_privada")))

            try {
                val resultSet = query.execute()
                val results = resultSet.allResults()

                if (results.isNotEmpty()) {
                    // Iterar sobre los resultados y eliminar cada documento
                    for (result in results) {
                        val docId =
                            result.getString(0) // Obtenemos el ID del documento en el índice 0
                        docId?.let {
                            val document = database.getDocument(it)
                            document?.let {
                                database.delete(it)
                            }
                        }
                    }
                    Log.d("Clave Privada", "Documento existente borrado")
                }

                val fileName = getFileName(selectedKeyPrivUri)

                val document = MutableDocument()
                document.setString("tipo", "clave_privada")  // Establecer el tipo
                document.setString("clave_privada_fileName", fileName)
                document.setString(
                    "clave_privada_uri",
                    selectedKeyPrivUri.toString()
                )  // Guardar la URI

                Log.d("Clave Privada", "Clave privada guardada: $document")

                // Guarda el documento en Couchbase Lite
                database.save(document)
                Toast.makeText(this, "Clave privada guardada correctamente ", Toast.LENGTH_SHORT)
                    .show()
                btnSelectClave.isEnabled=false
                borrarClave.visibility=View.VISIBLE
            }catch (e:CouchbaseLiteException){
                Log.e("Certificado", "Error en la operación: ${e.message}")
            }
        } else {
            Toast.makeText(this, "Por favor selecciona una clave antes de guardar", Toast.LENGTH_SHORT).show()
        }
    }
    // Obtener el nombre del archivo
    private fun getFileName(uri: Uri): String? {
        var fileName: String? = null
        this?.contentResolver?.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (cursor.moveToFirst()) {
                fileName = cursor.getString(nameIndex)
            }
        }
        return fileName
    }
    private fun borrarCertificado() {
        // Realiza una consulta para obtener todos los documentos que contienen URI
        val query = QueryBuilder.select(SelectResult.expression(Meta.id))
            .from(DataSource.database(database))
            .where(Expression.property("tipo").equalTo(Expression.string("certificado")))
        try {
            val resultSet = query.execute()
            val results = resultSet.allResults()

            if (results.isNotEmpty()) {
                // Itera sobre los resultados y elimina cada documento
                for (result in results) {
                    val docId = result.getString(0) // Obtenemos el ID del documento en el índice 0
                    docId?.let {
                        val document = database.getDocument(it)
                        document?.let {
                            database.delete(it)
                        }
                    }
                }
                Log.d("Prin_Re_Cliente", "La URI ha sido eliminada de la base de datos")
                Toast.makeText(this, "Certificado eliminado",Toast.LENGTH_SHORT).show()
                btnSelectCer.isEnabled=true
                borrarCer.visibility=View.GONE
                selectedCerTextView.setText("Agregue su certificado")
            } else {
                Log.d("Prin_Re_Cliente", "No hay URI en la base de datos para borrar")
                Toast.makeText(this, "\"No hay URI en la base de datos para borrar\"a",Toast.LENGTH_SHORT).show()
            }
        } catch (e: CouchbaseLiteException) {
            Log.e("Prin_Re_Cliente", "Error al eliminar las URI de la base de datos: ${e.message}", e)
        }
    }
    private fun borrarClave() {
        // Realiza una consulta para obtener todos los documentos que contienen URI
        val query = QueryBuilder.select(SelectResult.expression(Meta.id))
            .from(DataSource.database(database))
            .where(Expression.property("tipo").equalTo(Expression.string("clave_privada")))
        try {
            val resultSet = query.execute()
            val results = resultSet.allResults()

            if (results.isNotEmpty()) {
                // Itera sobre los resultados y elimina cada documento
                for (result in results) {
                    val docId = result.getString(0) // Obtenemos el ID del documento en el índice 0
                    docId?.let {
                        val document = database.getDocument(it)
                        document?.let {
                            database.delete(it)
                        }
                    }
                }
                Log.d("Prin_Re_Cliente", "La URI ha sido eliminada de la base de datos")
                Toast.makeText(this, "Clave privada eliminada",Toast.LENGTH_SHORT).show()
                btnSelectClave.isEnabled=true
                borrarClave.visibility=View.GONE
                selectedClaveTextView.setText("Agregue su clave privada")
            } else {
                Log.d("Prin_Re_Cliente", "No hay URI en la base de datos para borrar")
                Toast.makeText(this, "\"No hay URI en la base de datos para borrar\"a",Toast.LENGTH_SHORT).show()
            }
        } catch (e: CouchbaseLiteException) {
            Log.e("Prin_Re_Cliente", "Error al eliminar las URI de la base de datos: ${e.message}", e)
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

        if (resultCode == Activity.RESULT_OK) {
            data?.data?.let { uri ->
                val fileName = getFileName(uri)

                if (fileName != null && (fileName.endsWith(".pem") || fileName.endsWith(".key"))) {
                    when (requestCode) {
                        PICK_CERTIFICATE_REQUEST_CODE -> {
                            selectedCerUri = uri
                            selectedCerTextView.text = fileName
                            cerLoaded = true // Indica que se ha cargado el certificado
                            guardarCertificado()
                        }
                        PICK_PUBLIC_KEY_REQUEST_CODE -> {
                            selectedKeyPrivUri = uri
                            selectedClaveTextView.text = fileName
                            keyLoaded = true // Indica que se ha cargado la clave pública
                            guardarClavePrivada()
                        }
                    }
                } else {
                    // Mostrar error si no se puede obtener el nombre del archivo
                    Toast.makeText(this, "Por favor selecciona un archivo .pem válido", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
