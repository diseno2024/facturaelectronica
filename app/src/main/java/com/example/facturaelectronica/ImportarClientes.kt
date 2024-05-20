package com.example.facturaelectronica

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.couchbase.lite.CouchbaseLiteException
import com.couchbase.lite.Database
import com.couchbase.lite.QueryBuilder
import com.couchbase.lite.SelectResult
import com.couchbase.lite.DataSource
import com.couchbase.lite.Expression
import com.couchbase.lite.Meta
import com.google.android.material.card.MaterialCardView

class ImportarClientes : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_importar_clientes)
        val linearLayout = findViewById<LinearLayout>(R.id.containerLayout)
        val dataList = obtenerDatosGuardados()

        dataList.forEach { data ->
            val itemLayout = layoutInflater.inflate(R.layout.layout_list_item_cliente, null)

            val borrar = itemLayout.findViewById<ImageButton>(R.id.btnBorrarData)
            val textViewNombre = itemLayout.findViewById<TextView>(R.id.textViewNombre)
            val textViewTelefono = itemLayout.findViewById<TextView>(R.id.textViewTelefono)
            val textViewNit = itemLayout.findViewById<TextView>(R.id.textViewNit)

            val datos = data.split("\n")
            textViewNombre.text = datos[0]
            textViewTelefono.text = datos[2]
            textViewNit.text = datos[4]

            // Establece un onClickListener para cada tarjeta
            itemLayout.setOnClickListener {
                Pasardata(data)
            }

            borrar.setOnClickListener {
                Borrardatos(data, itemLayout, linearLayout)
            }

            linearLayout.addView(itemLayout)
        }

        val botonAtras = findViewById<ImageButton>(R.id.BotonAtras)
        botonAtras.setOnClickListener {
            val intent = Intent(this, EmitirCFActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(this, EmitirCFActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun obtenerDatosGuardados(): List<String> {
        // Obtén la instancia de la base de datos desde la aplicación
        val app = application as MyApp
        val database = app.database

        // Crea una consulta para seleccionar todos los documentos con tipo = "cliente"
        val query = QueryBuilder.select(SelectResult.all())
            .from(DataSource.database(database))
            .where(Expression.property("tipo").equalTo(Expression.string("cliente")))

        // Ejecuta la consulta
        val result = query.execute()

        // Lista para almacenar los datos obtenidos
        val dataList = mutableListOf<String>()

        // Itera sobre todos los resultados de la consulta
        result.allResults().forEach { result ->
            // Obtiene el diccionario del documento del resultado actual
            val dict = result.getDictionary(database.name)

            // Extrae los valores de los campos del documento
            val nombre = dict?.getString("nombre")
            val nit = dict?.getString("nit")
            val email = dict?.getString("email")
            val direccion = dict?.getString("direccion")
            val telefono = dict?.getString("telefono")

            // Formatea los datos como una cadena y la agrega a la lista
            val dataString = "$nombre\n$email\n$telefono\n$direccion\n$nit"
            dataList.add(dataString)
        }

        // Devuelve la lista de datos
        return dataList
    }

    private fun Pasardata(data: String) {
        val intent = Intent(this, EmitirCFActivity::class.java)
        // Pasa los datos de la carta seleccionada a la siguiente actividad
        intent.putExtra("Cliente", data)
        startActivity(intent)
        finish()
    }

    private fun Borrardatos(data: String, itemLayout: View, linearLayout: LinearLayout) {
        val app = application as MyApp
        val database = app.database
        val datos = data.split("\n")
        val query = QueryBuilder.select(SelectResult.expression(Meta.id))
            .from(DataSource.database(database))
            .where(Expression.property("nombre").equalTo(Expression.string(datos[0])))

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

                Log.d("Prin_Re_Cliente", "Se eliminó el cliente")
                showToast("Cliente eliminado")
            } else {
                Log.d("Prin_Re_Cliente", "No existe el cliente")
                showToast("No se encontró el cliente para eliminar")
            }
        } catch (e: CouchbaseLiteException) {
            Log.e("Prin_Re_Cliente", "Error al eliminar al cliente: ${e.message}", e)
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}