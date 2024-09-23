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


class Certificado : Fragment() {

    private val PICK_CERTIFICATE_REQUEST_CODE = 1
    private val PICK_PUBLIC_KEY_REQUEST_CODE = 2
    private lateinit var selectedCerUri: Uri
    private lateinit var selectedKeyUri: Uri
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
        intent.type = "*/*" // Limitar a archivos .pem
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        startActivityForResult(intent, requestCode)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            data?.data?.let { uri ->
                val fileName = getFileName(uri)

                if (fileName != null) {
                    when (requestCode) {
                        PICK_CERTIFICATE_REQUEST_CODE -> {
                            selectedCerUri = uri
                            selectedCerTextView.text = fileName
                            cerLoaded = true // Indica que se ha cargado el certificado
                        }
                        PICK_PUBLIC_KEY_REQUEST_CODE -> {
                            selectedKeyUri = uri
                            selectedClaveTextView.text = fileName
                            keyLoaded = true // Indica que se ha cargado la clave pública
                        }
                    }
                } else {
                    // Mostrar error si no se puede obtener el nombre del archivo
                    Toast.makeText(context, "No se pudo obtener el nombre del archivo", Toast.LENGTH_SHORT).show()
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
        val uri=selectedCerUri
        val fileName=getFileName(uri)
        val document = MutableDocument()
        document.setString("tipo", "certificado")  // Establecer el tipo
        document.setString("certificado_fileName", fileName)
        document.setBlob("certificado",
            context?.contentResolver?.openInputStream(uri)?.let { Blob("application/json", it) })
        Log.e("Certificado","guardado: $document")

        // Guarda el documento en Couchbase Lite
        database.save(document)
    }


    // Guardar Clave Pública en la base de datos
    fun guardarClavePublica() {
        val uri=selectedKeyUri
        guardarArchivoEnBaseDeDatos(uri, "clave_publica", "application/x-pem-file")
    }

    // Guardar archivo en Couchbase Lite
   private fun guardarArchivoEnBaseDeDatos(uri: Uri, tipo: String, mimeType: String) {
        try {
            val inputStream = context?.contentResolver?.openInputStream(uri)
            if (inputStream == null) {
                Log.e("Certificado", "No se pudo abrir el archivo: $uri")
                return
            }

            // Convertir el archivo en un Blob
            val blob = Blob(mimeType, inputStream)

            // Crear el documento y asignar tipo (certificado o clave pública)
            val document = MutableDocument()
            document.setBlob(tipo, blob)
            document.setString("tipo", tipo)

            // Guardar el documento en la base de datos
            database.save(document)

            Log.d("Certificado", "$tipo guardado correctamente en la base de datos")
            Toast.makeText(context, "$tipo guardado correctamente", Toast.LENGTH_SHORT).show()

        } catch (e: Exception) {
            Log.e("Certificado", "Error al guardar el archivo en la base de datos: ${e.message}", e)
            Toast.makeText(context, "Error al guardar $tipo", Toast.LENGTH_SHORT).show()
        }
    }


    companion object {
        // Puedes pasar argumentos aquí si lo necesitas
        fun newInstance(): Certificado {
            return Certificado()
        }
    }
}
