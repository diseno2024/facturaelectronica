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

class Certificado : Fragment() {

    private val PICK_CERTIFICATE_REQUEST_CODE = 1
    private val PICK_PRIVATE_KEY_REQUEST_CODE = 2
    private lateinit var selectedCerTextView: TextView
    private lateinit var selectedClaveTextView: TextView
    private lateinit var database: Database

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
            openFilePicker(PICK_PRIVATE_KEY_REQUEST_CODE)
        }

        return view
    }

    private fun openFilePicker(requestCode: Int) {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.type = "*/*" // Puedes limitar el tipo de archivo según sea necesario
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        startActivityForResult(intent, requestCode)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            data?.data?.let { uri ->
                val fileName = getFileName(uri)
                when (requestCode) {
                    PICK_CERTIFICATE_REQUEST_CODE -> {
                        selectedCerTextView.text = fileName
                        saveToDatabase("certificado", fileName, uri)
                    }
                    PICK_PRIVATE_KEY_REQUEST_CODE -> {
                        selectedClaveTextView.text = fileName
                        saveToDatabase("clave_privada", fileName, uri)
                    }
                }
            }
        }
    }

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

    private fun saveToDatabase(fieldName: String, fileName: String?, fileUri: Uri) {
        val document = MutableDocument()

        // Guarda solo el nombre o la ruta del archivo en la base de datos
        document.setString(fieldName, fileName)
        document.setString("${fieldName}_uri", fileUri.toString())

        // Guarda el documento en Couchbase Lite
        database.save(document)
    }
    companion object {
        // Puedes pasar argumentos aquí si lo necesitas
        fun newInstance(): Certificado {
            return Certificado()
        }
    }
}
