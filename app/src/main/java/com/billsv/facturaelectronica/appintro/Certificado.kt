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
        intent.type = "*/*" // Permite cualquier tipo de archivo y verificamos despues
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        startActivityForResult(intent, requestCode)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            data?.data?.let { uri ->
                val fileName = getFileName(uri)

                if (fileName != null && fileName.endsWith(".pem")) {
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
            val fileName = getFileName(selectedCerUri)
            val document = MutableDocument()
            document.setString("tipo", "certificado")  // Establecer el tipo
            document.setString("certificado_fileName", fileName)
            document.setString("certificado_uri", selectedCerUri.toString())  // Guardar la URI

            Log.d("Certificado", "Certificado guardado: $document")

            // Guarda el documento en Couchbase Lite
            database.save(document)
        } else {
            Toast.makeText(context, "Por favor selecciona un certificado antes de guardar", Toast.LENGTH_SHORT).show()
        }
    }


    // Guardar Clave Pública en la base de datos
    fun guardarClavePublica() {
        if (::selectedKeyUri.isInitialized) {
            val fileName = getFileName(selectedKeyUri) // Obtener el nombre del archivo
            val document = MutableDocument()
            document.setString("tipo", "clave_publica")  // Establecer el tipo como clave pública
            document.setString("clave_publica_fileName", fileName)  // Guardar el nombre del archivo
            document.setString("clave_publica_uri", selectedKeyUri.toString())  // Guardar la URI

            Log.d("Certificado", "Clave pública guardada: $document")

            // Guarda el documento en Couchbase Lite
            database.save(document)

            Toast.makeText(context, "Clave pública guardada correctamente", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Por favor selecciona una clave pública antes de guardar", Toast.LENGTH_SHORT).show()
        }
    }



    companion object {
        // Puedes pasar argumentos aquí si lo necesitas
        fun newInstance(): Certificado {
            return Certificado()
        }
    }
}
