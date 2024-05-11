package com.example.facturaelectronica

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.card.MaterialCardView

class InfoEmisorActivity : AppCompatActivity() {
    private val REQUEST_CODE_IMAGE_PICKER = 123
    private var logo: Uri? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_info_emisor)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val btnAtras: ImageButton = findViewById(R.id.atras)
        btnAtras.setOnClickListener {
            // Crear un intent para ir a MenuActivity
            val intent = Intent(this, MenuActivity::class.java)
            startActivity(intent)
            finish()  // Finalizar la actividad actual si se desea
        }
        //imagen predeterminada
        val drawable = ContextCompat.getDrawable(this, R.drawable.ic_add_photo)
        //quita la imagen seleccionada
        val btnBorrarImagen: ImageButton = findViewById(R.id.btnBorrarImagen)
        btnBorrarImagen.setOnClickListener {
            // Eliminar la imagen seleccionada (puedes reiniciar la variable 'logo' a null)
            logo = null
            val Imagen: ImageView = findViewById(R.id.Logo)
            Imagen.setImageDrawable(drawable)
            // Ocultar el botón de borrar
            btnBorrarImagen.visibility = View.GONE
            val Card: MaterialCardView = findViewById(R.id.Imagen)
            Card.setClickable(true)
            showToast("Imagen borrada")
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
                handleImageSelection(uri)
            } else {
                showToast("Error al obtener la URI de la imagen seleccionada")
            }
        }
    }
    private fun handleImageSelection(uri: Uri) {
        val contentResolver = contentResolver
        val mimeType = contentResolver.getType(uri)

        if (mimeType != null) {
            if (mimeType == "image/jpeg" || mimeType == "image/png") {
                // La imagen es válida (JPEG o PNG)
                logo = uri
                val Imagen: ImageView = findViewById(R.id.Logo)
                Imagen.setImageURI(uri)

                // Mostrar el botón de borrar
                val btnBorrarImagen: ImageButton = findViewById(R.id.btnBorrarImagen)
                btnBorrarImagen.visibility = View.VISIBLE
                val Card: MaterialCardView = findViewById(R.id.Imagen)
                Card.setClickable(false)
                showToast("Imagen cargada")
            } else {
                // La imagen no es válida (otro formato)
                showToast("Selecciona una imagen en formato JPEG o PNG")
            }
        } else {
            // No se pudo determinar el tipo MIME
            showToast("Error al obtener el tipo de la imagen")
        }
    }

    // Método para mostrar un mensaje en un Toast
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}