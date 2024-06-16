package com.billsv.facturaelectronica

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.Spinner
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class ResMensualCCFActivity : AppCompatActivity() {
    private lateinit var textViewMes: TextView
    private lateinit var textViewYear: TextView
    private lateinit var tipoD: Spinner
    private lateinit var atras:ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_res_mensual_ccfactivity)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        tipoD=findViewById(R.id.tipoD)
        // spinner tipoC
        val opcionesD = arrayOf("Comprobante Crédito Fiscal", "Factura Consumidor Final")
        val adapterD = ArrayAdapter(this, R.layout.spinner_descripcion, opcionesD)
        adapterD.setDropDownViewResource(R.layout.spinner_dropdown_per)
        tipoD.adapter = adapterD
        textViewMes = findViewById(R.id.textViewMes)
        textViewYear = findViewById(R.id.textViewYear)
        atras=findViewById(R.id.atras)

        val cardViewMonth: CardView = findViewById(R.id.cardViewMes)
        val cardViewYear: CardView = findViewById(R.id.cardViewYear)

        cardViewMonth.setOnClickListener {
            val monthPickerDialog = MonthPickerDialog()
            monthPickerDialog.setOnMonthSelectedListener(object : MonthPickerDialog.OnMonthSelectedListener {
                override fun onMonthSelected(month: Int) {
                    textViewMes.text = getMonthName(month)
                }
            })
            monthPickerDialog.show(supportFragmentManager, "MonthPickerDialog")
        }

        cardViewYear.setOnClickListener {
            val yearPickerDialog = YearPickerDialog()
            yearPickerDialog.setOnYearSelectedListener(object : YearPickerDialog
                .OnYearSelectedListener {
                override fun onYearSelected(year: Int) {
                    textViewYear.text = year.toString()
                }
            })
            yearPickerDialog.show(supportFragmentManager, "YearPickerDialog")
        }
        atras.setOnClickListener{
            super.onBackPressed() // Llama al método onBackPressed() de la clase base
            val intent = Intent(this, MenuActivity::class.java)
            startActivity(intent)
            finish()
        }


    }
    private fun getMonthName(month: Int): String {
        val months = arrayOf(
            "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
            "Julio", "Augosto", "Septiembre", "Octubre", "Noviembre", "Diciembre")
        return months[month]
    }
    override fun onBackPressed() {
        super.onBackPressed() // Llama al método onBackPressed() de la clase base
        val intent = Intent(this, MenuActivity::class.java)
        startActivity(intent)
        finish()
    }
}