package com.billsv.facturaelectronica.appintro

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.billsv.facturaelectronica.MyApp
import com.billsv.facturaelectronica.databinding.ActivityInfoEmisor1Binding
import com.couchbase.lite.CouchbaseLiteException
import com.couchbase.lite.DataSource
import com.couchbase.lite.Database
import com.couchbase.lite.Expression
import com.couchbase.lite.Meta
import com.couchbase.lite.MutableDocument
import com.couchbase.lite.QueryBuilder
import com.couchbase.lite.SelectResult

class InfoEmisor1 : Fragment() {

    private lateinit var database: Database
    private var _binding: ActivityInfoEmisor1Binding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = ActivityInfoEmisor1Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Obtener la base de datos desde la aplicación
        val app = requireActivity().application as MyApp
        database = app.database

    }

    private fun guardarInformacion() {
        val nombre = binding.nombre.text.toString()
        val dui = binding.duionit.text.toString()
        val telefono = binding.tel.text.toString().replace("-", "")
        val correo = binding.correo.text.toString()

        // Buscar si ya existe un documento del tipo "ConfEmisor"
        val query = QueryBuilder.select(SelectResult.expression(Meta.id))
            .from(DataSource.database(database))
            .where(Expression.property("tipo").equalTo(Expression.string("ConfEmisor")))

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
                Log.d("InfoEmisor1", "Documento existente borrado")
            }

            // Crear un nuevo documento
            val document = MutableDocument()
                .setString("nombre", nombre)
                .setString("dui", dui)
                .setString("telefono", telefono)
                .setString("correo", correo)
                .setString("tipo", "ConfEmisor")

            // Guardar el nuevo documento
            database.save(document)
            Log.d("InfoEmisor1", "Datos guardados correctamente: \n $document")
            showToast("Datos guardados correctamente")
        } catch (e: CouchbaseLiteException) {
            Log.e("InfoEmisor1", "Error al guardar los datos en la base de datos: ${e.message}", e)
            showToast("Error al guardar los datos")
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(): InfoEmisor1 {
            return InfoEmisor1()
        }
    }
}

