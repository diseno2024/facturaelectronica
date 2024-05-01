package com.example.facturaelectronica

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.content.Intent
import android.widget.Button

class PDF_CFActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_pdf_cfactivity)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val btnMenuprin: Button = findViewById(R.id.btnMenuprin)
        btnMenuprin.setOnClickListener {
            // Crear un intent para ir a MenuActivity
            val intent = Intent(this, EmitirCFActivity::class.java)
            startActivity(intent)
            finish() // Finalizar la actividad actual si se desea
        }
    }
    override fun onBackPressed() {
        super.onBackPressed() // Llama al método onBackPressed() de la clase base
        val intent = Intent(this, EmitirCFActivity::class.java)
        startActivity(intent)
        finish()
    }
}