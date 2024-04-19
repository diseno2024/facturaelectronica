package com.example.facturaelectronica


import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ResMensualActivity : AppCompatActivity() {

    private lateinit var txtMes: TextView
    private lateinit var txtAnio: TextView
    private lateinit var btnDerecho: ImageButton
    private lateinit var btnIzquierda: ImageButton
    private lateinit var progressBarSem1: ProgressBar
    private lateinit var progressBarSem2: ProgressBar
    private lateinit var progressBarSem3: ProgressBar
    private lateinit var progressBarSem4: ProgressBar
    private lateinit var txtPorcentaje1: TextView
    private lateinit var txtPorcentaje2: TextView
    private lateinit var txtPorcentaje3: TextView
    private lateinit var txtPorcentaje4: TextView
    private lateinit var txtTotal: TextView
    private var calendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_res_mensual)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val btnMenu: ImageButton = findViewById(R.id.btnMenu)
        btnMenu.setOnClickListener {
            // Crear un intent para ir a MenuActivity
            val intent = Intent(this, MenuActivity::class.java)
            startActivity(intent)
            finish()  // Finalizar la actividad actual si se desea
        }

        // Inicializar vistas
        txtMes = findViewById(R.id.txtMes)
        txtAnio = findViewById(R.id.txtAnio)
        progressBarSem1 = findViewById(R.id.progressBarSem1)
        progressBarSem2 = findViewById(R.id.progressBarSem2)
        progressBarSem3 = findViewById(R.id.progressBarSem3)
        progressBarSem4 = findViewById(R.id.progressBarSem4)
        txtPorcentaje1 = findViewById(R.id.txtPorcentaje1)
        txtPorcentaje2 = findViewById(R.id.txtPorcentaje2)
        txtPorcentaje3 = findViewById(R.id.txtPorcentaje3)
        txtPorcentaje4 = findViewById(R.id.txtPorcentaje4)
        txtTotal = findViewById(R.id.txtTotal)

        btnDerecho = findViewById(R.id.btnDerecho)
        btnIzquierda = findViewById(R.id.btnIzquierda)

        // Mostrar mes y año actual
        mostrarFecha()

        btnDerecho.setOnClickListener {
            avanzarMes()
        }
        btnIzquierda.setOnClickListener {
            retrocederMes()
        }
    }

    private fun mostrarFecha() {
        val monthFormat = SimpleDateFormat("MMMM", Locale.getDefault())
        txtMes.text = monthFormat.format(calendar.time).capitalize()
        txtAnio.text = calendar.get(Calendar.YEAR).toString()
    }

    private fun avanzarMes() {
        val currentYear = calendar.get(Calendar.YEAR)
        val currentMonth = calendar.get(Calendar.MONTH)
        calendar.add(Calendar.MONTH, 1)
        if (calendar.get(Calendar.MONTH) == Calendar.JANUARY && currentMonth == Calendar.DECEMBER) {
            // Si avanzamos de diciembre a enero, pero el año no ha cambiado todavía, avanzamos el año
            calendar.set(Calendar.YEAR, currentYear + 1)
        }
        mostrarFecha()
    }

    private fun retrocederMes() {
        val currentYear = calendar.get(Calendar.YEAR)
        val currentMonth = calendar.get(Calendar.MONTH)
        calendar.add(Calendar.MONTH, -1)
        if (calendar.get(Calendar.MONTH) == Calendar.DECEMBER && currentMonth == Calendar.JANUARY) {
            // Si retrocedemos de enero a diciembre, pero el año no ha cambiado todavía, retrocedemos el año
            calendar.set(Calendar.YEAR, currentYear - 1)
        }
        mostrarFecha()

    }


}