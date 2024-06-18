package com.billsv.facturaelectronica

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.couchbase.lite.DataSource
import com.couchbase.lite.Expression
import com.couchbase.lite.QueryBuilder
import com.couchbase.lite.SelectResult

class CFiscalFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var facturaAdaptercf: FacturaAdaptercf
    private lateinit var btnLoadMore: Button
    private lateinit var btnLoadPrevious: Button
    private lateinit var etDui: EditText
    private lateinit var btnBuscar: Button
    private lateinit var btnClearFilter: Button

    private var currentPage = 0
    private val pageSize = 3
    private var currentData: List<Factura> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_c_fiscal, container, false)

        recyclerView = view.findViewById(R.id.listRecycler)
        recyclerView.layoutManager = LinearLayoutManager(context)

        btnLoadMore = view.findViewById(R.id.btnLoadMore)
        btnLoadMore.setOnClickListener {
            loadMoreItems()
        }

        btnLoadPrevious = view.findViewById(R.id.btnLoadPrevious)
        btnLoadPrevious.setOnClickListener {
            loadPreviousItems()
        }

        etDui = view.findViewById(R.id.etDui)
        btnBuscar = view.findViewById(R.id.btnBuscar)
        btnBuscar.setOnClickListener {
            val dui = etDui.text.toString().replace("-", "")
            buscarPorDui(dui)
        }

        btnClearFilter = view.findViewById(R.id.btnClearFilter)
        btnClearFilter.setOnClickListener {
            clearFilter()
        }

        // Agregar TextWatcher para formatear el DUI en el EditText
        etDui.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Formatear el DUI automáticamente con un guion después de los primeros 8 caracteres
                if (s?.length == 9 && !s.contains("-")) {
                    etDui.setText(
                        s.subSequence(0, 8).toString() + "-" + s.subSequence(8, 9).toString()
                    )
                    etDui.setSelection(etDui.text.length) // Posicionar el cursor al final
                }
            }
        })

        facturaAdaptercf = FacturaAdaptercf(requireContext(), mutableListOf())
        recyclerView.adapter = facturaAdaptercf

        // Cargar la primera página de datos
        loadMoreItems()

        return view
    }

    private fun loadMoreItems() {
        val newData = obtenerDatosPaginados(currentPage * pageSize, pageSize)
        if (newData.isNotEmpty()) {
            currentData = newData
            facturaAdaptercf.setFacturas(currentData)
            currentPage++
            updateButtonVisibility()

            // Si hay un filtro activo, mostrar el botón de limpiar filtro
            if (etDui.text.isNotBlank()) {
                btnClearFilter.visibility = View.VISIBLE
            }
        }
    }

    private fun loadPreviousItems() {
        if (currentPage > 1) {
            currentPage -= 2 // Retroceder dos páginas para cargar los datos anteriores
            val previousData = obtenerDatosPaginados(currentPage * pageSize, pageSize)
            currentData = previousData
            facturaAdaptercf.setFacturas(currentData)
            currentPage++
            updateButtonVisibility()

            // Si hay un filtro activo, mostrar el botón de limpiar filtro
            if (etDui.text.isNotBlank()) {
                btnClearFilter.visibility = View.VISIBLE
            }
        }
    }

    private fun obtenerDatosPaginados(offset: Int, limit: Int): List<Factura> {
        val app = requireActivity().application as MyApp
        val database = app.database

        val query = QueryBuilder.select(SelectResult.all())
            .from(DataSource.database(database))
            .where(Expression.property("tipoCliente").equalTo(Expression.string("Contribuyente")))
            .limit(Expression.intValue(limit), Expression.intValue(offset))

        val result = query.execute()
        val dataList = mutableListOf<Factura>()

        result.allResults().forEach { result ->
            val dict = result.getDictionary(database.name)
            val nombre = dict?.getString("nombre") ?: ""
            val telefono = dict?.getString("telefono") ?: ""
            val dui = dict?.getString("dui") ?: ""
            val duiFormateado = formatearDUI(dui)
            val telefonoFormateado = formatearTelefono(telefono)
            val factura = Factura(nombre, telefonoFormateado, duiFormateado)
            dataList.add(factura)
        }

        return dataList
    }

    private fun buscarPorDui(dui: String) {
        val app = requireActivity().application as MyApp
        val database = app.database

        val query = QueryBuilder.select(SelectResult.all())
            .from(DataSource.database(database))
            .where(Expression.property("dui").equalTo(Expression.string(dui)))

        val result = query.execute()
        val dataList = mutableListOf<Factura>()

        result.allResults().forEach { result ->
            val dict = result.getDictionary(database.name)
            val nombre = dict?.getString("nombre") ?: ""
            val telefono = dict?.getString("telefono") ?: ""
            val duiResult = dict?.getString("dui") ?: ""
            val duiFormateado = formatearDUI(duiResult)
            val telefonoFormateado = formatearTelefono(telefono)
            val factura = Factura(nombre, telefonoFormateado, duiFormateado)
            dataList.add(factura)
        }

        if (dataList.isNotEmpty()) {
            currentData = dataList
            facturaAdaptercf.setFacturas(currentData)
            btnClearFilter.visibility = View.VISIBLE // Mostrar el botón de limpiar filtro
        } else {
            Toast.makeText(context, "No se encontraron facturas con ese DUI", Toast.LENGTH_SHORT).show()
        }
    }

    private fun clearFilter() {
        etDui.text.clear()
        currentPage = 0
        loadMoreItems()
        btnClearFilter.visibility = View.GONE
    }

    private fun formatearDUI(dui: String): String {
        return if (dui.length >= 9) {
            val primerosDigitos = dui.substring(0, 8)
            val ultimoDigito = dui.substring(8, 9)
            "$primerosDigitos-$ultimoDigito"
        } else {
            dui
        }
    }

    private fun formatearTelefono(telefono: String): String {
        return if (telefono.length >= 8) {
            val primerosDigitos = telefono.substring(0, 4)
            val siguientesDigitos = telefono.substring(4)
            "$primerosDigitos-$siguientesDigitos"
        } else {
            telefono
        }
    }

    private fun updateButtonVisibility() {
        btnLoadMore.visibility = if (currentData.size >= pageSize) View.VISIBLE else View.GONE
        btnLoadPrevious.visibility = if (currentPage > 1) View.VISIBLE else View.GONE
    }
}
