package com.example.facturaelectronica

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.couchbase.lite.CouchbaseLiteException
import com.couchbase.lite.DataSource
import com.couchbase.lite.Database
import com.couchbase.lite.Expression
import com.couchbase.lite.Meta
import com.couchbase.lite.MutableDocument
import com.couchbase.lite.QueryBuilder
import com.couchbase.lite.SelectResult
import com.google.android.material.card.MaterialCardView

class InfoEmisorActivity : AppCompatActivity() {
    private lateinit var database: Database
    private val REQUEST_CODE_IMAGE_PICKER = 123
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_info_emisor)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val app = application as MyApp
        database = app.database
        verificar()
        val btnAtras: ImageButton = findViewById(R.id.atras)
        btnAtras.setOnClickListener {
            // Crear un intent para ir a MenuActivity
            val intent = Intent(this, MenuActivity::class.java)
            startActivity(intent)
            finish()  // Finalizar la actividad actual si se desea
        }
        //quita la imagen seleccionada
        val btnBorrarImagen: ImageButton = findViewById(R.id.btnBorrarImagen)
        btnBorrarImagen.setOnClickListener {
            borrarImagen()
        }
    }
    private fun verificar(){
        val uri = obtenerUriGuardada()?.toUri()
        if(uri!=null){
            val Imagen: ImageView = findViewById(R.id.Logo)
            Imagen.setImageURI(uri)
            // Mostrar el botón de borrar
            val btnBorrarImagen: ImageButton = findViewById(R.id.btnBorrarImagen)
            btnBorrarImagen.visibility = View.VISIBLE
            val Card: MaterialCardView = findViewById(R.id.Imagen)
            Card.setClickable(false)
        }else{
            val Imagen: ImageView = findViewById(R.id.Logo)
            val drawable = ContextCompat.getDrawable(this, R.drawable.ic_add_photo)
            Imagen.setImageDrawable(drawable)
        }
    }
    private fun mostrarImagen(){
        val uri = obtenerUriGuardada()?.toUri()
        if(uri!=null){
            val Imagen: ImageView = findViewById(R.id.Logo)
            Imagen.setImageURI(uri)
            // Mostrar el botón de borrar
            val btnBorrarImagen: ImageButton = findViewById(R.id.btnBorrarImagen)
            btnBorrarImagen.visibility = View.VISIBLE
            val Card: MaterialCardView = findViewById(R.id.Imagen)
            Card.setClickable(false)
            showToast("Imagen cargada")
        }else{
            val Imagen: ImageView = findViewById(R.id.Logo)
            val drawable = ContextCompat.getDrawable(this, R.drawable.ic_add_photo)
            Imagen.setImageDrawable(drawable)
        }
    }
    private fun obtenerUriGuardada(): String? {
        val app = application as MyApp
        val database = app.database

        val query = QueryBuilder.select(SelectResult.property("URI"))
            .from(DataSource.database(database))
            .where(Expression.property("tipo").equalTo(Expression.string("Imagen")))

        return try {
            val resultSet = query.execute()
            val result = resultSet.next()

            result?.getString("URI")
        } catch (e: CouchbaseLiteException) {
            Log.e("Prin_Re_Cliente", "Error al obtener la URI de la base de datos: ${e.message}", e)
            null
        }
    }
    fun showGallery(view: View?) {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, REQUEST_CODE_IMAGE_PICKER)

    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_IMAGE_PICKER && resultCode == RESULT_OK) {
            val uri = data?.data
            if (uri != null) {
                onHandleresult(uri)
            } else {
                showToast("Error al obtener la URI de la imagen seleccionada")
            }
        }
    }
    private fun onHandleresult(uri: Uri){
        val contentResolver = contentResolver
        val mimeType = contentResolver.getType(uri)

        if (mimeType != null) {
            if (mimeType == "image/jpeg" || mimeType == "image/png") {
                // La imagen es válida (JPEG o PNG)
                guardarURI(uri)
            } else {
                // La imagen no es válida (otro formato)
                showToast("Selecciona una imagen en formato JPEG o PNG")
            }
        } else {
            // No se pudo determinar el tipo MIME
            showToast("Error al obtener el tipo de la imagen")
        }
    }
    private fun guardarURI(uri: Uri) {
        val uriString = uri.toString()
        // Consulta para verificar si la URI ya existe
        val query = QueryBuilder.select(SelectResult.all())
            .from(DataSource.database(database))
            .where(Expression.property("URI").equalTo(Expression.string(uriString)))

        try {
            val resultSet = query.execute()
            if (resultSet.allResults().isEmpty()) {
                // Crear un documento mutable para guardar en la base de datos
                val document = MutableDocument()
                    .setString("URI", uriString)
                    .setString("tipo", "Imagen")

                // Guardar el documento en la base de datos
                database.save(document)
                Log.d("Prin_Re_Cliente", "Datos guardados correctamente: \n $document")
                Toast.makeText(this, "Datos guardados correctamente", Toast.LENGTH_SHORT).show()
            } else {
                Log.d("Prin_Re_Cliente", "La URI ya existe en la base de datos: $uriString")
                Toast.makeText(this, "La URI ya existe en la base de datos", Toast.LENGTH_SHORT).show()
            }
        } catch (e: CouchbaseLiteException) {
            Log.e("Prin_Re_Cliente", "Error al consultar o guardar los datos en la base de datos: ${e.message}", e)
            Toast.makeText(this, "Error al consultar o guardar los datos", Toast.LENGTH_SHORT).show()
        }
        mostrarImagen()
    }
    private fun borrarImagen(){
        // Eliminar la imagen seleccionada (puedes reiniciar la variable 'logo' a null)
        val Imagen: ImageView = findViewById(R.id.Logo)
        val drawable = ContextCompat.getDrawable(this, R.drawable.ic_add_photo)
        Imagen.setImageDrawable(drawable)
        // Ocultar el botón de borrar
        val btnBorrarImagen: ImageButton = findViewById(R.id.btnBorrarImagen)
        btnBorrarImagen.visibility = View.GONE
        val Card: MaterialCardView = findViewById(R.id.Imagen)
        Card.setClickable(true)
        // Realiza una consulta para obtener todos los documentos que contienen URI
        val query = QueryBuilder.select(SelectResult.expression(Meta.id))
            .from(DataSource.database(database))

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
                showToast("Imagen Eliminada")
            } else {
                Log.d("Prin_Re_Cliente", "No hay URI en la base de datos para borrar")
                showToast("No hay URI en la base de datos para borrar")
            }
        } catch (e: CouchbaseLiteException) {
            Log.e("Prin_Re_Cliente", "Error al eliminar las URI de la base de datos: ${e.message}", e)
        }
    }

    // Método para mostrar un mensaje en un Toast
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

}