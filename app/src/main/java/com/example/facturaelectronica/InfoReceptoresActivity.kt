package com.example.facturaelectronica
import android.os.Bundle
import android.widget.ImageButton
import android.content.Intent
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.couchbase.lite.DataSource
import com.couchbase.lite.Expression
import com.couchbase.lite.QueryBuilder
import com.couchbase.lite.SelectResult

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
            val textViewDUI = itemLayout.findViewById<TextView>(R.id.textViewNrc)

            val datos = data.split("\n")
            textViewNombreComercial.text = datos[0]
            textViewCorreo.text = datos[1]
            textViewTelefono.text = datos[2]
            textViewNIT.text = datos[3]
            textViewDUI.text = datos[4]
            linearLayout.addView(itemLayout)
        }

        val botonAtras = findViewById<ImageButton>(R.id.atras)
        botonAtras.setOnClickListener {
            // Crea un Intent para iniciar la actividad Comprobantecf
            val intent = Intent(this, EmitirCCFActivity::class.java)
            startActivity(intent) // Inicia la actividad Comprobantecf
            finish()
        }

    }

    override fun onBackPressed() {
        super.onBackPressed() // Llama al método onBackPressed() de la clase base
        val intent = Intent(this, EmitirCCFActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun obtenerDatosGuardados(): List<String> {
        // Obtén la instancia de la base de datos desde la aplicación
        val app = application as MyApp
        val database = app.database

        val query = QueryBuilder.select(SelectResult.all())
            .from(DataSource.database(database))
            .where(Expression.property("tipo").equalTo(Expression.string("contribuyente")))
        val result = query.execute()
        val dataList = mutableListOf<String>()
        result.allResults().forEach { result ->
            val dict = result.getDictionary(database.name)
            val razonSocial = dict?.getString("RazonSocial")
            val nit = dict?.getString("NIT")
            val actividadEconomica = dict?.getString("ActividadEconomica")
            val nrc = dict?.getString("NRC")
            val direccion = dict?.getString("Direccion")
            val email = dict?.getString("Email")
            val nombreComercial = dict?.getString("NombreComercial")
            val telefono = dict?.getString("Telefono")

            val dataString = "Nombre Comercial: $nombreComercial\nCorreo: $email\nTeléfono: $telefono\nNIT: $nit\nNRC: $nrc"
            dataList.add(dataString)
        }
        return dataList
    }

}