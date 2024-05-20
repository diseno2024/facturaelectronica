package com.example.facturaelectronica
import android.os.Bundle
import android.widget.ImageButton
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity

class AgregarArticuloActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_agregar_articulo)


        val botonAtras = findViewById<ImageButton>(R.id.BotonAtras)

        // Configura un OnClickListener para el botón
        botonAtras.setOnClickListener {
            // Crea un Intent para iniciar la actividad Comprobantecf
            val intent = Intent(this, EmitirCCFActivity::class.java)
            startActivity(intent) // Inicia la actividad Comprobantecf
            finish()
        }


    }
    override fun onBackPressed() {
        super.onBackPressed() // Llama al método onBackPressed() de la clase base
        val intent = Intent(this, EmitirCCFActivity::class.java)
        startActivity(intent)
        finish()
    }
}
