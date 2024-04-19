package com.example.facturaelectronica

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import android.widget.ImageButton
import android.content.Intent
import android.widget.Button
import androidx.core.view.WindowInsetsCompat

class LogoActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_logo)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Obtener referencia al botón de retroceso
        val backButton: ImageButton = findViewById(R.id.atras)

        // Agregar OnClickListener al botón de retroceso
        backButton.setOnClickListener {
            // Iniciar MenuActivity al hacer clic en el botón de retroceso
            val intent = Intent(this, MenuActivity::class.java)
            startActivity(intent)
            finish() // Opcional: finalizar la actividad actual si no se desea volver a ella
        }

        val btnCancelar: Button = findViewById(R.id.btnCancelar)
        btnCancelar.setOnClickListener {
            // Crear un intent para ir a MenuActivity
            val intent = Intent(this, MenuActivity::class.java)
            startActivity(intent)
            finish() // Finalizar la actividad actual si se desea
        }
    }
}