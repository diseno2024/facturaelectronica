package com.billsv.facturaelectronica

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.billsv.facturaelectronica.R

class DescripcionActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_descripcion)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val spinnerTipo: Spinner = findViewById(R.id.TipoS)
// Create an ArrayAdapter using the string array and a default spinner layout.
        ArrayAdapter.createFromResource(
            this,
            R.array.TipoS,
            R.layout.spinner_descripcion
        ).also { adapter ->
            // Specify the layout to use when the list of choices appears.
            adapter.setDropDownViewResource(R.layout.spinner_dropdown_per)
            // Apply the adapter to the spinner.
            spinnerTipo.adapter = adapter
        }
        val spinnerMedida: Spinner = findViewById(R.id.UnidadMedidaS)
// Create an ArrayAdapter using the string array and a default spinner layout.
        ArrayAdapter.createFromResource(
            this,
            R.array.UnidadMedidaS,
            R.layout.spinner_descripcion
        ).also { adapter ->
            // Specify the layout to use when the list of choices appears.
            adapter.setDropDownViewResource(R.layout.spinner_dropdown_per)
            // Apply the adapter to the spinner.
            spinnerMedida.adapter = adapter
        }
        val spinnerTventa: Spinner = findViewById(R.id.Tipo_Venta)
// Create an ArrayAdapter using the string array and a default spinner layout.
        ArrayAdapter.createFromResource(
            this,
            R.array.Tipo_Venta,
            R.layout.spinner_descripcion
        ).also { adapter ->
            // Specify the layout to use when the list of choices appears.
            adapter.setDropDownViewResource(R.layout.spinner_dropdown_per)
            // Apply the adapter to the spinner.
            spinnerTventa.adapter = adapter
        }
        val Cancelar: Button = findViewById(R.id.Cancelar)
        Cancelar.setOnClickListener {
            val intent = Intent(this, EmitirCFActivity::class.java)
            startActivity(intent)
            finish()
        }
        val Agregar: Button = findViewById(R.id.Agregar)
        Agregar.setOnClickListener {
            val Item = Dialog(this)
            Item.setContentView(R.layout.layout_item) // R.layout.layout_custom_dialog es tu diseño personalizado
            val width = (resources.displayMetrics.widthPixels * 0.92).toInt() // 80% del ancho de la pantalla
            val height = (resources.displayMetrics.heightPixels * 0.5).toInt() // 60% del alto de la pantalla
            Item.window?.setLayout(width, height)
            Item.setCanceledOnTouchOutside(false)
            val btnAgregar = Item.findViewById<Button>(R.id.AggItem)
            val btnRegresar = Item.findViewById<Button>(R.id.Regresar)
            btnAgregar.setOnClickListener {
                //cierra el dialogo
                Item.dismiss()
            }

            btnRegresar.setOnClickListener {
                val intent = Intent(this, EmitirCFActivity::class.java)
                startActivity(intent)
                Item.dismiss()
            }

            Item.show()

        }
    }
    override fun onBackPressed() {
        super.onBackPressed() // Llama al método onBackPressed() de la clase base
        val intent = Intent(this, EmitirCFActivity::class.java)
        startActivity(intent)
        finish()
    }
}