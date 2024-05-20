package com.example.facturaelectronica
import android.os.Bundle
import android.widget.ImageButton
import android.content.Intent
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.couchbase.lite.CouchbaseLiteException
import com.couchbase.lite.DataSource
import com.couchbase.lite.Expression
import com.couchbase.lite.Meta
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

            val borrar = itemLayout.findViewById<ImageButton>(R.id.btnBorrarData)
            val textViewNombreComercial = itemLayout.findViewById<TextView>(R.id.textViewNombreComercial)
            val textViewTelefono = itemLayout.findViewById<TextView>(R.id.textViewTelefono)
            val textViewNRC = itemLayout.findViewById<TextView>(R.id.textViewNrc)

            val datos = data.split("\n")
            textViewNombreComercial.text = datos[0]
            textViewTelefono.text = datos[2]
            textViewNRC.text = datos[4]
            // Establece un onClickListener para cada tarjeta
            itemLayout.setOnClickListener {
                Pasardata(data)
            }

            borrar.setOnClickListener {
                Borrardatos(data, itemLayout, linearLayout)
            }

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

            val dataString = "$nombreComercial\n$email\n$telefono\n$nit\n$nrc\n$razonSocial\n$actividadEconomica\n$direccion"
            dataList.add(dataString)
        }
        return dataList
    }
    private fun Pasardata(data: String) {
        val intent = Intent(this, EmitirCCFActivity::class.java)
        // Pasa los datos de la carta seleccionada a la siguiente actividad
        intent.putExtra("Contribuyente", data)
        startActivity(intent)
        finish()
    }

    private fun Borrardatos(data: String, itemLayout: View, linearLayout: LinearLayout) {
        val app = application as MyApp
        val database = app.database
        val datos = data.split("\n")
        val query = QueryBuilder.select(SelectResult.expression(Meta.id))
            .from(DataSource.database(database))
            .where(Expression.property("RazonSocial").equalTo(Expression.string(datos[5])))

        try {
            val resultSet = query.execute()
            val results = resultSet.allResults()

            if (results.isNotEmpty()) {
                // Itera sobre los resultados y elimina cada documento
                for (result in results) {
                    val docId = result.getString(0) // Obtenemos el ID del documento en el índice 0
                    docId?.let {
                        val document = database.getDocument(it)
                        document?.let {
                            database.delete(it)
                        }
                    }
                }
                // Elimina la tarjeta de la vista
                linearLayout.removeView(itemLayout)

                Log.d("Prin_Re_Cliente", "Se eliminó el Contribuyente")
                showToast("Contribuyente eliminado")
            } else {
                Log.d("Prin_Re_Cliente", "No existe el Contribuyente")
                showToast("No se encontró el cliente para eliminar")
            }
        } catch (e: CouchbaseLiteException) {
            Log.e("Prin_Re_Cliente", "Error al eliminar al Contribuyente: ${e.message}", e)
        }
    }
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

}