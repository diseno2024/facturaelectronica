package com.billsv.facturaelectronica

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.couchbase.lite.Database
import com.couchbase.lite.Expression
import com.couchbase.lite.QueryBuilder
import com.couchbase.lite.SelectResult
import com.couchbase.lite.DataSource
import com.couchbase.lite.Dictionary
import com.couchbase.lite.Result
import com.couchbase.lite.ResultSet


class FacturaFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var facturaAdapter: FacturaAdapter
    private lateinit var facturaList: List<Factura>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_factura, container, false)

        recyclerView = view.findViewById(R.id.listRecycler)
        recyclerView.layoutManager = LinearLayoutManager(context)

        // Obtener datos guardados
        facturaList = obtenerDatosGuardados()

        facturaAdapter = FacturaAdapter(requireContext(), facturaList)
        recyclerView.adapter = facturaAdapter

        return view
    }

    private fun obtenerDatosGuardados(): List<Factura> {
        // Obtén la instancia de la base de datos desde la aplicación
        val app = requireActivity().application as MyApp
        val database = app.database

        // Crea una consulta para seleccionar todos los documentos con tipo = "cliente"
        val query = QueryBuilder.select(SelectResult.all())
            .from(DataSource.database(database))
            .where(Expression.property("tipo").equalTo(Expression.string("cliente")))

        // Ejecuta la consulta
        val result = query.execute()

        // Lista para almacenar los datos obtenidos
        val dataList = mutableListOf<Factura>()

        // Itera sobre todos los resultados de la consulta
        result.allResults().forEach { result ->
            // Obtiene el diccionario del documento del resultado actual
            val dict = result.getDictionary(database.name)

            // Extrae los valores de los campos del documento
            val nombre = dict?.getString("nombre") ?: ""
            val telefono = dict?.getString("telefono") ?: ""
            val dui = dict?.getString("dui") ?: ""

            // Formatea el DUI con un guion antes del último dígito
            val duiFormateado = formatearDUI(dui)

            // Formatea el número de teléfono con un guion después de los primeros 4 dígitos
            val telefonoFormateado = formatearTelefono(telefono)

            // Crea una instancia de Factura y la agrega a la lista
            val factura = Factura(nombre, telefonoFormateado, duiFormateado)
            dataList.add(factura)
        }

        // Devuelve la lista de datos
        return dataList
    }

    // Función para formatear el DUI con un guion antes del último dígito
    private fun formatearDUI(dui: String): String {
        return if (dui.length >= 9) {
            val primerosDigitos = dui.substring(0, dui.length - 1)
            val ultimoDigito = dui.substring(dui.length - 1)
            "$primerosDigitos-$ultimoDigito"
        } else {
            // Si el DUI no tiene suficientes dígitos, devolvemos el mismo DUI sin cambios
            dui
        }
    }

    // Función para formatear el número de teléfono con un guion después de los primeros 4 dígitos
    private fun formatearTelefono(telefono: String): String {
        return if (telefono.length >= 8) {
            val primerosDigitos = telefono.substring(0, 4)
            val siguientesDigitos = telefono.substring(4)
            "$primerosDigitos-$siguientesDigitos"
        } else {
            // Si el número de teléfono no tiene suficientes dígitos, devolvemos el mismo número sin cambios
            telefono
        }
    }



}