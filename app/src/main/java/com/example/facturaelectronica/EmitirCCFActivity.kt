package com.example.facturaelectronica

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.widget.ImageButton
import android.widget.TextView

class EmitirCCFActivity : AppCompatActivity() {
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
        // Recupera los datos pasados desde la otra actividad
        val datosGuardados = intent.getStringExtra("Contribuyente")
        val Nombre: TextView = findViewById(R.id.nombreR)
        val NRC: TextView = findViewById(R.id.nrcR)
        // Aquí puedes usar los datos como necesites
        datosGuardados?.let{
            val datos = it.split("\n")
            Nombre.text = datos[0]
            NRC.text = datos[4]
        }

    }

    override fun onBackPressed() {
        super.onBackPressed() // Llama al método onBackPressed() de la clase base
        val intent = Intent(this, MenuActivity::class.java)
        startActivity(intent)
        finish()
    }

    fun DataReceptor(view: View) {
        val intent = Intent(this, InfoReceptoresActivity::class.java)
        startActivity(intent)
        finish()
    }
    fun DataArticulo(view: View) {
        val intent = Intent(this, AgregarArticuloActivity::class.java)
        startActivity(intent)
        finish()
    }
}
