package com.billsv.facturaelectronica.appintro

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.billsv.facturaelectronica.MyApp
import com.billsv.facturaelectronica.R
import com.couchbase.lite.Database
import com.couchbase.lite.MutableDocument
import android.util.Log
import android.widget.Toast
import com.couchbase.lite.Blob
import com.couchbase.lite.CouchbaseLiteException
import com.couchbase.lite.DataSource
import com.couchbase.lite.Expression
import com.couchbase.lite.Meta
import com.couchbase.lite.QueryBuilder
import com.couchbase.lite.SelectResult


class Certificado : Fragment() {

    private val PICK_CERTIFICATE_REQUEST_CODE = 1
    private val PICK_PUBLIC_KEY_REQUEST_CODE = 2
    private lateinit var selectedCerUri: Uri
    private lateinit var selectedKeyUri: Uri
    private lateinit var selectedKeyPrivUri: Uri
    private lateinit var selectedCerTextView: TextView
    private lateinit var selectedClaveTextView: TextView
    private lateinit var database: Database
    private var cerLoaded = false
    private var keyLoaded = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.activity_certificado, container, false)

        // Acceder a la base de datos
        val app = requireActivity().application as MyApp
        database = app.database

        // Inicializa los elementos de la vista
        val btnSelectCer = view.findViewById<Button>(R.id.btn_select_cer)
        val btnSelectClave = view.findViewById<Button>(R.id.btn_select_clave)
        selectedCerTextView = view.findViewById(R.id.selected_cer)
        selectedClaveTextView = view.findViewById(R.id.selected_clave)

        // Maneja el evento de clic de los botones "Examinar"
        btnSelectCer.setOnClickListener {
            openFilePicker(PICK_CERTIFICATE_REQUEST_CODE)
        }
        btnSelectClave.setOnClickListener {
            openFilePicker(PICK_PUBLIC_KEY_REQUEST_CODE)
        }

        return view
    }

    // Abrir el picker de archivos
    // Abrir el picker de archivos, solo permite archivos .pem
    private fun openFilePicker(requestCode: Int) {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.type = "*/*" // Permite cualquier tipo de archivo y verificamos despues
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

                        }
                        PICK_PUBLIC_KEY_REQUEST_CODE -> {
                            selectedKeyPrivUri = uri
                            selectedClaveTextView.text = fileName
                            keyLoaded = true // Indica que se ha cargado la clave pública

                        }
                    }
                } else {
                    // Mostrar error si no se puede obtener el nombre del archivo
                    Toast.makeText(context, "Por favor selecciona un archivo .pem válido", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // Obtener el nombre del archivo
    private fun getFileName(uri: Uri): String? {
        var fileName: String? = null
        context?.contentResolver?.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (cursor.moveToFirst()) {
                fileName = cursor.getString(nameIndex)
            }
        }
        return fileName
    }

    // Guardar Certificado en la base de datos
    fun guardarCertificado() {
        if (::selectedCerUri.isInitialized) {
            // Conceder permisos de lectura persistentes
            try {
                context?.contentResolver?.takePersistableUriPermission(
                    selectedCerUri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (e: Exception) {
                Log.e("Certificado", "Error al conceder permisos persistentes: ${e.message}")
                Toast.makeText(context, "Error al conceder permisos persistentes", Toast.LENGTH_SHORT).show()
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
            Toast.makeText(context, "Certificado guardado correctamente ", Toast.LENGTH_SHORT)
                .show()
            } catch (e: CouchbaseLiteException) {
                Log.e("Certificado", "Error en la operación: ${e.message}")
            }
        } else {
            Toast.makeText(context, "Por favor selecciona un certificado antes de guardar", Toast.LENGTH_SHORT).show()
        }
    }

    // Guardar Clave privada en la base de datos
    fun guardarClavePrivada() {
        if (::selectedKeyPrivUri.isInitialized) {
            // Conceder permisos de lectura persistentes
            try {
                context?.contentResolver?.takePersistableUriPermission(
                    selectedKeyPrivUri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (e: Exception) {
                Log.e("Clave", "Error al conceder permisos persistentes: ${e.message}")
                Toast.makeText(context, "Error al conceder permisos persistentes", Toast.LENGTH_SHORT).show()
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
                Toast.makeText(context, "Clave privada guardada correctamente ", Toast.LENGTH_SHORT)
                    .show()
            }catch (e:CouchbaseLiteException){
                    Log.e("Certificado", "Error en la operación: ${e.message}")
                }
            } else {
            Toast.makeText(context, "Por favor selecciona una clave antes de guardar", Toast.LENGTH_SHORT).show()
        }
    }

    // Variable para verificar si el mensaje ya se mostró
    private var MensajeError1: Boolean = false

    fun validarCertificados() :Boolean{
        if (::selectedKeyPrivUri.isInitialized && ::selectedCerUri.isInitialized) {

        } else {
            if (!MensajeError1) {
                Toast.makeText(context, "Selecciona un certificado y una clave privada", Toast.LENGTH_SHORT).show()
                MensajeError1 = true // El mensaje ya se mostró, no se volverá a mostrar
            }
            return false
        }
        return true
    }


    companion object {
        // Puedes pasar argumentos aquí si lo necesitas
        fun newInstance(): Certificado {
            return Certificado()
        }
    }
}