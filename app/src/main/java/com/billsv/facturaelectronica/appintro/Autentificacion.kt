package com.billsv.facturaelectronica.appintro

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.billsv.facturaelectronica.MyApp
import com.billsv.facturaelectronica.R
import com.couchbase.lite.CouchbaseLiteException
import com.couchbase.lite.DataSource
import com.couchbase.lite.Database
import com.couchbase.lite.Expression
import com.couchbase.lite.Meta
import com.couchbase.lite.MutableDocument
import com.couchbase.lite.QueryBuilder
import com.couchbase.lite.SelectResult


class Autentificacion : Fragment() {

    private lateinit var database: Database
    private lateinit var userp: EditText
    private lateinit var contrap: EditText
    private lateinit var userpr: EditText
    private lateinit var contrapr: EditText

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Inicializa la base de datos desde la aplicación
        database = (requireActivity().application as MyApp).database

        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.activity_autentificacion, container, false)

        // Inicializa los EditText
        userp = view.findViewById(R.id.userp)
        contrap = view.findViewById(R.id.contrap)
        userpr = view.findViewById(R.id.userpr)
        contrapr = view.findViewById(R.id.contrapr)

        return view
    }

    fun guardarInformacion() {
        val userpText = userp.text.toString()
        val contrapText = contrap.text.toString()
        val userprText = userpr.text.toString()
        val contraprText = contrapr.text.toString()

        // Buscar si ya existe un documento del tipo "Autentificacion"
        val query = QueryBuilder.select(SelectResult.expression(Meta.id))
            .from(DataSource.database(database))
            .where(Expression.property("tipo").equalTo(Expression.string("Autentificacion")))

        try {
            val resultSet = query.execute()
            val results = resultSet.allResults()

            if (results.isNotEmpty()) {
                // Iterar sobre los resultados y eliminar cada documento
                for (result in results) {
                    val docId = result.getString(0) // Obtenemos el ID del documento en el índice 0
                    docId?.let {
                        val document = database.getDocument(it)
                        document?.let {
                            database.delete(it)
                        }
                    }
                }
                Log.d("ReClienteActivity", "Documento existente borrado")
            }

            // Crear un nuevo documento
            val document = MutableDocument()
                .setString("usuarioPrueba", userpText)
                .setString("contraseñaPrueba", contrapText)
                .setString("usuarioProduccion", userprText)
                .setString("contraseñaProduccion", contraprText)
                .setString("tipo", "Autentificacion")

            // Guardar el nuevo documento
            database.save(document)
            Log.e("Authetificacion", "Datos guardados correctamente: \n $document")

            // Mostrar Toast usando el contexto de la actividad
            Toast.makeText(requireContext(), "Datos guardados correctamente", Toast.LENGTH_SHORT).show()
        } catch (e: CouchbaseLiteException) {
            Log.e("ReClienteActivity", "Error al guardar los datos en la base de datos: ${e.message}", e)
            Toast.makeText(requireContext(), "Error al guardar los datos", Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        // Puedes pasar argumentos aquí si lo necesitas
        fun newInstance(): Autentificacion {
            return Autentificacion()
        }
    }
}
