package com.example.facturaelectronica

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import android.content.Intent
import android.widget.ImageButton
import android.widget.Button
import androidx.core.view.WindowInsetsCompat

class Comprobantecf : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_emitir_ccf)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val buttonAtras = findViewById<ImageButton>(R.id.atras)
        buttonAtras.setOnClickListener {
            val intent = Intent(this, MenuActivity::class.java)
            startActivity(intent)
            finish()
        }

        val button = findViewById<Button>(R.id.btnSeleccionarContribuyente)
        // Configura el OnClickListener para el botón btnSeleccionarContribuyente
        button.setOnClickListener {
            // Crea un intent para ir a InfoReceptoresActivity
            val intent = Intent(this, InfoReceptoresActivity::class.java)
            startActivity(intent)
        }




    }

    override fun onBackPressed() {
        super.onBackPressed() // Llama al método onBackPressed() de la clase base
        val intent = Intent(this, MenuActivity::class.java)
        startActivity(intent)
        finish()
    }
}