package com.example.facturaelectronica

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import android.widget.ImageButton
import android.content.Intent
import android.widget.Button
import androidx.core.view.WindowInsetsCompat

class ReClienteActivity : AppCompatActivity() {
    private lateinit var spinner1: Spinner
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_re_cliente)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        // Inicializa el Spinner
        spinner1 = findViewById(R.id.contribuyente)
        val opciones = arrayOf("Tipo de Contribuyente","Crédito Fiscal", "Consumidor Final")

        // Configura el adaptador para el Spinner
        val adapter = ArrayAdapter(this, R.layout.spinner_personalizado, opciones)
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_per)
        spinner1.adapter = adapter


        val btnAtras: ImageButton = findViewById(R.id.atras)
        btnAtras.setOnClickListener {
            // Crear un intent para ir a MenuActivity
            val intent = Intent(this, MenuActivity::class.java)
            startActivity(intent)
            finish()  // Finalizar la actividad actual si se desea
        }

        val btnCancelar: Button = findViewById(R.id.btnCancelar)
        btnCancelar.setOnClickListener {
            // Crear un intent para ir a MenuActivity
            val intent = Intent(this, EmitirCFActivity::class.java)
            startActivity(intent)
            finish()  // Finalizar la actividad actual si se desea
        }
    }

    override fun onBackPressed() {
        super.onBackPressed() // Llama al método onBackPressed() de la clase base
        val intent = Intent(this, MenuActivity::class.java)
        startActivity(intent)
        finish()
    }
}
