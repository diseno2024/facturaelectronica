package com.example.facturaelectronica
import android.os.Bundle
import android.widget.ImageButton
import android.content.Intent
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class InfoReceptoresActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_info_receptores)

        val linearLayout = findViewById<LinearLayout>(R.id.containerLayout)
        val dataList = obtenerDatosGuardados()

        dataList.forEach { data ->
            val itemLayout = layoutInflater.inflate(R.layout.layout_list_item_contribuyentes, null)

            val textViewNombreComercial = itemLayout.findViewById<TextView>(R.id.textViewNombreComercial)
            val textViewCorreo = itemLayout.findViewById<TextView>(R.id.textViewCorreo)
            val textViewTelefono = itemLayout.findViewById<TextView>(R.id.textViewTelefono)
            val textViewNIT = itemLayout.findViewById<TextView>(R.id.textViewNIT)
            val textViewDUI = itemLayout.findViewById<TextView>(R.id.textViewDUI)

            val datos = data.split("\n")
            textViewNombreComercial.text = datos[0]
            textViewCorreo.text = datos[1]
            textViewTelefono.text = datos[2]
            textViewNIT.text = datos[3]
            textViewDUI.text = datos[4]
            linearLayout.addView(itemLayout)
        }

        val botonAtras = findViewById<ImageButton>(R.id.BotonAtras)
        botonAtras.setOnClickListener {
            // Crea un Intent para iniciar la actividad Comprobantecf
            val intent = Intent(this, Comprobantecf::class.java)
            startActivity(intent) // Inicia la actividad Comprobantecf
            finish()
        }
    }
    override fun onBackPressed() {
        super.onBackPressed() // Llama al método onBackPressed() de la clase base
        val intent = Intent(this, Comprobantecf::class.java)
        startActivity(intent)
        finish()
    }
}
