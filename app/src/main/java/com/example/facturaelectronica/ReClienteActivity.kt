package com.example.facturaelectronica

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Spinner
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.couchbase.lite.CouchbaseLite
import com.couchbase.lite.Database
import com.couchbase.lite.MutableDocument
import com.example.facturaelectronica.R


class ReClienteActivity : AppCompatActivity() {
    private lateinit var spinnerDep: Spinner
    private lateinit var spinnerMun: Spinner
    private lateinit var nombreEditText: EditText
    private lateinit var nitEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var direccionEditText: EditText
    private lateinit var departamentoSpinner: Spinner


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
        spinnerDep = findViewById(R.id.departamento)
        val departamentos = arrayOf("Ahuachapán", "Cabañas", "Chalatenango", "Cuscatlán",
            "La Libertad","La paz", "La Unión", "Morazán", "San Miguel", "San Vicente", "Santa Ana",
            "Sonsonate", "Usulután")

        // Configura el adaptador para el Spinner
        val adapterDep = ArrayAdapter(this, R.layout.spinner_personalizado, departamentos)
        adapterDep.setDropDownViewResource(R.layout.spinner_dropdown_per)
        spinnerDep.adapter = adapterDep

        // Inicializa el Spinner
        spinnerMun = findViewById(R.id.municipio)
        val municipios = arrayOf("Municipios", "de", "cada", "departamento")

        // Configura el adaptador para el Spinner
        val adapterMun = ArrayAdapter(this, R.layout.spinner_personalizado, municipios)
        adapterMun.setDropDownViewResource(R.layout.spinner_dropdown_per)
        spinnerMun.adapter = adapterMun

        val botonAgregar = findViewById<Button>(R.id.btnAgregar) // Reemplaza con el ID real de tu botón
        botonAgregar.setOnClickListener {
            val intent = Intent(this, EmitirCFActivity::class.java)
            startActivity(intent)
        }
        val btnCancelar = findViewById<Button>(R.id.btnCancelar) // Reemplaza con el ID real de tu botón
        btnCancelar.setOnClickListener {
            val intent = Intent(this, EmitirCFActivity::class.java)
            startActivity(intent)
        }


        // Inicializa las vistas
        nombreEditText = findViewById(R.id.nombre)
        nitEditText = findViewById(R.id.nit)
        emailEditText = findViewById(R.id.email)
        direccionEditText = findViewById(R.id.direccion)
        departamentoSpinner = findViewById(R.id.departamento)




    }




}
