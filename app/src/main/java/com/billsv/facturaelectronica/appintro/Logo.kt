package com.billsv.facturaelectronica.appintro

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.billsv.facturaelectronica.MyApp
import com.billsv.facturaelectronica.R
import com.couchbase.lite.*

class Logo : Fragment() {

    private lateinit var database: Database
    private val REQUEST_CODE_IMAGE_PICKER = 1001
    private var imageUri: Uri? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.activity_logo, container, false)

        // Inicializa la base de datos desde la aplicación
        database = (requireActivity().application as MyApp).database

        // Configura el ImageButton para seleccionar una imagen
        val imageButton: ImageButton = view.findViewById(R.id.imageButton)
        imageButton.setOnClickListener {
            showGallery()
        }

        // Muestra la imagen guardada al cargar el fragmento
        mostrarImagen(view)

        return view
    }

    private fun showGallery() {
        val intent = Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, REQUEST_CODE_IMAGE_PICKER)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_IMAGE_PICKER && resultCode == Activity.RESULT_OK) {
            val uri = data?.data
            if (uri != null) {
                guardarURI(uri)
            } else {
                showToast("Error al obtener la URI de la imagen seleccionada")
            }
        }
    }

    private fun guardarURI(uri: Uri) {
        val uriString = uri.toString() // Convertir Uri a String
        /*val query = QueryBuilder.select(SelectResult.property("URI"))
            .from(DataSource.database(database))
            .where(Expression.property("tipo").equalTo(Expression.string("Imagen")))

        try {
            val resultSet = query.execute()
            if (resultSet.allResults().isEmpty()) {
                val document = MutableDocument()
                    .setString("URI", uriString)
                    .setString("tipo", "Imagen")
                database.save(document)
                showToast("Datos guardados correctamente")
                mostrarImagen(view) // Asumiendo que 'view' es accesible aquí
            } else {
                showToast("La URI ya existe en la base de datos")
            }
        } catch (e: CouchbaseLiteException) {
            showToast("Error al consultar o guardar los datos en la base de datos")
            Log.e("LogoFragment", "Error al consultar o guardar los datos en la base de datos: ${e.message}", e)
        }*/
        // Buscar si ya existe un documento del tipo "ConfEmisor"
        val query = QueryBuilder.select(SelectResult.expression(Meta.id))
            .from(DataSource.database(database))
            .where(Expression.property("tipo").equalTo(Expression.string("Imagen")))

        try {
            val resultSet = query.execute()
            val results = resultSet.allResults()

            if (results.isNotEmpty()) {
                // Iterar sobre los resultados y eliminar cada documento
                for (result in results) {
                    val docId = result.getString(0) // Obtenemos el ID del documento en el índice 0
                    docId?.let {
                        val document = database.getDocument(it)
                        document?.let {
                            database.delete(it)

                        }
                    }
                }
                Log.d("LogoFragment", "Documento existente borrado")
            }

            // Crear un nuevo documento
            val document = MutableDocument()
                .setString("URI",uriString)
                .setString("tipo", "Imagen")

            // Guardar el nuevo documento
            database.save(document)
            showToast("URI guardada correctamente")
            mostrarImagen(view)
            Log.d("LogoFragment", "URI guardados correctamente: \n $document")
        } catch (e: CouchbaseLiteException) {
            Log.e("LogoFragment", "Error al guardar la URI en la base de datos: ${e.message}", e)
            showToast("Error al guardar")
        }
    }

    private fun mostrarImagen(view: View?) {
        val imageView: ImageView = view?.findViewById(R.id.imageButton) ?: return
        val query = QueryBuilder.select(SelectResult.property("URI"))
            .from(DataSource.database(database))
            .where(Expression.property("tipo").equalTo(Expression.string("Imagen")))

        try {
            val resultSet = query.execute()
            val result = resultSet.next()
            val uriString = result?.getString("URI")
            if (uriString != null) {
                imageUri = Uri.parse(uriString) // Convertir String a Uri
                imageView.setImageURI(imageUri)
                imageView.setBackgroundColor(Color.TRANSPARENT)
                showToast("Imagen cargada")
            } else {
                imageView.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.add_logo))
            }
        } catch (e: CouchbaseLiteException) {
            showToast("Error al mostrar la imagen")
            Log.e("LogoFragment", "Error al mostrar la imagen: ${e.message}", e)
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    companion object {
        fun newInstance(): Logo {
            return Logo()
        }
    }
}
