package com.example.facturaelectronica

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.widget.ImageButton

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

        val buttonContribuyente = findViewById<Button>(R.id.btnSeleccionarContribuyente)
        // Configura el OnClickListener para el botón btnSeleccionarContribuyente
        buttonContribuyente.setOnClickListener {
            // Crea un intent para ir a InfoReceptoresActivity
            val intent = Intent(this, InfoReceptoresActivity::class.java)
            startActivity(intent)
        }

        val buttonRegistrar2 = findViewById<Button>(R.id.btnRegistrar2)
        // Configura el OnClickListener para el botón btnRegistrar2
        buttonRegistrar2.setOnClickListener {
            // Crea un Intent para iniciar la actividad AgregarArticuloActivity
            val intent = Intent(this, AgregarArticuloActivity::class.java)
            startActivity(intent)
        }
        val btnGenccf: Button = findViewById(R.id.btnGenccf)
        btnGenccf.setOnClickListener {
            // Crear un intent para ir a MenuActivity
            val intent = Intent(this, ComprobanteCCFActivity::class.java)
            startActivity(intent)
            finish() // Finalizar la actividad actual si se desea
        }
    }

    override fun onBackPressed() {
        super.onBackPressed() // Llama al método onBackPressed() de la clase base
        val intent = Intent(this, MenuActivity::class.java)
        startActivity(intent)
        finish()
    }
}
