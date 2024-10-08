package com.billsv.facturaelectronica

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.couchbase.lite.Expression
import com.couchbase.lite.QueryBuilder
import com.couchbase.lite.SelectResult
import com.couchbase.lite.DataSource
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import com.couchbase.lite.CouchbaseLiteException
class CFiscalFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var facturaAdapterc: FacturaAdaptercf
    private lateinit var btnLoadMore: ImageButton
    private lateinit var btnLoadPrevious: ImageButton
    private lateinit var etDui: EditText
    private lateinit var btnBuscar: ImageButton
    private lateinit var btnClearFilter: ImageButton
    private lateinit var viewpage: TextView
    private var totalResults = 0

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
        // Inicializa viewpage
        viewpage = view.findViewById(R.id.viewpage)
        etDui = view.findViewById(R.id.etDui)
        btnBuscar = view.findViewById(R.id.btnBuscar)
        btnBuscar.setOnClickListener {
            val dui = etDui.text.toString()
            if(dui!="" && dui.length >= 10) {
                buscarPorDui(dui)
            }else{
                Toast.makeText(context, "ingrese un dui valido", Toast.LENGTH_SHORT).show()
            }
        }

        btnClearFilter = view.findViewById(R.id.btnClearFilter)
        btnClearFilter.setOnClickListener {
            clearFilter()
        }

        // Agregar TextWatcher para formatear el DUI en el EditText
        etDui.addTextChangedListener(object : TextWatcher {
            private var isFormatting = false
            override fun afterTextChanged(s: Editable?) {
                if (isFormatting) {
                    return
                }
                isFormatting = true
                // Formatear automáticamente con ########-#
                if (s?.length == 9 && s[8] != '-') {
                    val formattedText = s.substring(0, 8) + "-" + s[8]
                    etDui.setText(formattedText)
                    etDui.setSelection(etDui.text.length) // Posicionar el cursor al final
                }
                // Limitar la entrada adicional después de ########-#
                if (s?.length == 10) {
                    etDui.setSelection(etDui.text.length) // Posicionar el cursor al final
                }
                // Bloquear la entrada de caracteres adicionales después de ########-#
                if (s?.length == 11) {
                    etDui.setText(s.substring(0, 10))
                    etDui.setSelection(etDui.text.length) // Posicionar el cursor al final
                }
                isFormatting = false
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        facturaAdapterc = FacturaAdaptercf(requireContext(), mutableListOf())
        recyclerView.adapter = facturaAdapterc

        // Cargar la primera página de datos
        loadMoreItems()

        return view
    }

    private fun loadMoreItems() {
        val newData = obtenerDatosPaginados(currentPage * pageSize, pageSize)
        if (newData.isNotEmpty()) {
            currentData = newData
            facturaAdapterc.setFacturas(currentData)
            currentPage++
            totalResults = obtenerTotalResultados() // Asegúrate de actualizar totalResults
            updateButtonVisibility()
            updatePageNumberTextView() // Actualizar el número de página
        }
    }

    private fun loadPreviousItems() {
        if (currentPage > 1) {
            currentPage-- // Retroceder una página
            val previousData = obtenerDatosPaginados((currentPage - 1) * pageSize, pageSize)
            currentData = previousData
            facturaAdapterc.setFacturas(currentData)
            updateButtonVisibility()
            updatePageNumberTextView() // Actualizar el número de página
        }
    }


    private fun updatePageNumberTextView() {
        if (totalResults > pageSize) {
            val totalPages = getTotalPages()
            val pageNumberText = "Page $currentPage/$totalPages"
            viewpage.text = pageNumberText
            viewpage.visibility = View.VISIBLE
        } else {
            viewpage.visibility = View.GONE
        }
    }

    private fun getTotalPages(): Int {
        // Calcular el número total de páginas basado en el total de resultados y el tamaño de página
        return (totalResults + pageSize - 1) / pageSize
    }

    private fun obtenerTotalDatos(): Int {
        // Retorna el tamaño total de los datos
        // Aquí deberías implementar la lógica para obtener el total de datos desde tu fuente de datos
        // Por ejemplo, si currentData es una lista, puedes retornar currentData.size
        return currentData.size
    }



    private fun obtenerDatosPaginados(offset: Int, limit: Int): List<Factura> {
        val app = requireActivity().application as MyApp
        val database = app.database

        val query = QueryBuilder.select(SelectResult.all())
            .from(DataSource.database(database))
            .where(Expression.property("tipoD").equalTo(Expression.string("Comprobante Crédito Fiscal")))
            .limit(Expression.intValue(limit), Expression.intValue(offset))

        val result = query.execute()
        val dataList = mutableListOf<Factura>()

        result.allResults().forEach { result ->
            val dict = result.getDictionary(database.name)
            val nombre = dict?.getString("nombre") ?: ""
            val numeroControl = dict?.getString("numeroControl") ?: ""
            val dui = dict?.getString("dui") ?: ""
            val factura = Factura(nombre, numeroControl, dui)
            dataList.add(factura)
        }

        // Actualizar el total de resultados cuando se cargan nuevos datos
        totalResults = obtenerTotalResultados()

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
            val numeroControl = dict?.getString("numeroControl") ?: ""
            val duiResult = dict?.getString("dui") ?: ""
            val factura = Factura(nombre, numeroControl, dui)
            dataList.add(factura)
        }

        if (dataList.isNotEmpty()) {
            currentData = dataList
            facturaAdapterc.setFacturas(currentData)
            btnClearFilter.visibility = View.VISIBLE // Mostrar el botón de limpiar filtro
            btnBuscar.visibility = View.GONE // Ocultar el botón de buscar
        } else {
            Toast.makeText(context, "No se encontraron facturas con ese DUI", Toast.LENGTH_SHORT).show()
        }
    }

    private fun clearFilter() {
        etDui.text.clear()
        currentPage = 0
        loadMoreItems()
        btnClearFilter.visibility = View.GONE // Ocultar el botón de limpiar filtro
        btnBuscar.visibility = View.VISIBLE // Mostrar el botón de buscar
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
    private fun updateButtonVisibility() {
        btnLoadMore.visibility = if ((currentPage * pageSize) < totalResults) View.VISIBLE else View.GONE
        btnLoadPrevious.visibility = if (currentPage > 1) View.VISIBLE else View.GONE
    }

    private fun obtenerTotalResultados(): Int {
        val app = requireActivity().application as MyApp
        val database = app.database
        val query = QueryBuilder.select(SelectResult.all())
            .from(DataSource.database(database))
            .where(Expression.property("tipoD").equalTo(Expression.string("Comprobante Crédito Fiscal")))

        try {
            val result = query.execute()
            totalResults = result.allResults().size
            Log.d("ReClienteActivity", "Número de documentos de tipo 'cf': $totalResults")
        } catch (e: CouchbaseLiteException) {
            Log.e("ReClienteActivity", "Error al contar los documentos de tipo 'ConfEmisor': ${e.message}", e)
        }

        return totalResults
    }
}