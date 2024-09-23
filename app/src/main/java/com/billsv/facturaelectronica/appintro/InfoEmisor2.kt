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

class InfoEmisor2 : Fragment() {
    private lateinit var database: Database
    private lateinit var nombreC: EditText
    private lateinit var NRC: EditText
    private lateinit var AcEco: EditText
    private lateinit var Direccion: EditText
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.activity_info_emisor2, container, false)
        val app = requireActivity().application as MyApp
        database = app.database
        nombreC = view.findViewById(R.id.nombreC)
        NRC = view.findViewById(R.id.nrc)
        AcEco = view.findViewById(R.id.AcEco)
        Direccion = view.findViewById(R.id.Direccion)
        return view
    }
    fun actualizarInformacion() {
        val nombreC = nombreC.text.toString()
        val nrc = NRC.text.toString()
        val AcEco = AcEco.text.toString()
        val direccion = Direccion.text.toString()
        // Buscar si ya existe un documento del tipo "ConfEmisor"
        val query = QueryBuilder.select(SelectResult.expression(Meta.id))
            .from(DataSource.database(database))
            .where(Expression.property("tipo").equalTo(Expression.string("ConfEmisor")))

        try {
            val resultSet = query.execute()
            val results = resultSet.allResults()

            if (results.isNotEmpty()) {
                // Si existe un documento, actualizarlo
                val docId = results.first().getString(0) // Obtener el ID del primer documento encontrado
                docId?.let {
                    val document = database.getDocument(it)?.toMutable() // Convertir el documento a mutable
                    document?.let {
                        document.setString("nrc", nrc)
                        document.setString("ActividadEco", AcEco)
                        document.setString("nombreC", nombreC)
                        document.setString("direccion", direccion)

                        // Guardar el documento actualizado
                        database.save(document)
                        Log.d("ReClienteActivity", "Documento actualizado correctamente")
                    }
                }
            }
            showToast("Datos guardados correctamente")
        } catch (e: CouchbaseLiteException) {
            Log.e("ReClienteActivity", "Error al guardar los datos en la base de datos: ${e.message}", e)
            showToast("Error al guardar")
        }
    }
    fun validarEntradas(): Boolean {
        val nombrecText = nombreC.text.toString()
        val nrcText = NRC.text.toString()
        val AcEco = AcEco.text.toString()
        val direccionText = Direccion.text.toString()
        // Verifica que todos los campos estén llenos
        if (AcEco.isEmpty() || nombrecText.isEmpty()) {
            return false
        }
        if (!nrcText.matches(Regex("\\d{7}"))) {
            Toast.makeText(requireContext(), "NRC debe ser un número válido", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }
    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    companion object {
        // Puedes pasar argumentos aquí si lo necesitas
        fun newInstance(): InfoEmisor2  {
            return InfoEmisor2 ()
        }
    }
}
