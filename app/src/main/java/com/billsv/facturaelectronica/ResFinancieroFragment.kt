package com.billsv.facturaelectronica

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import java.util.Calendar
import android.graphics.Color
import com.couchbase.lite.*
import android.content.Context
import android.widget.Toast
import com.couchbase.lite.Function
import android.util.Log


class ResFinancieroFragment : Fragment() {
    private lateinit var textViewYear: TextView
    private lateinit var atras: ImageButton
    private lateinit var tableLayout: TableLayout
    private lateinit var database: Database

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_res_financiero, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val app = requireActivity().application as MyApp
        database = app.database

        val mainLayout = view.findViewById<View>(R.id.main)
        ViewCompat.setOnApplyWindowInsetsListener(mainLayout) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        textViewYear = view.findViewById(R.id.textViewYear)
        atras = view.findViewById(R.id.atras)
        val cardViewYear: CardView = view.findViewById(R.id.cardViewYear)
        tableLayout = view.findViewById(R.id.tableLayout)

        // Inicializar con el año actual
        val calendar = Calendar.getInstance()
        val currentYear = calendar.get(Calendar.YEAR)
        textViewYear.text = calendar.get(Calendar.YEAR).toString()

        // Cargar la tabla con los datos del año actual
        tableLayout.removeAllViews()
        cargarEncabezadosTabla()
        updateTableWithSalesData(currentYear)

        cardViewYear.setOnClickListener {
            val yearPickerDialog = YearPickerDialog()
            yearPickerDialog.setOnYearSelectedListener(object :
                YearPickerDialog.OnYearSelectedListener {
                override fun onYearSelected(year: Int) {
                    textViewYear.text = year.toString()
                    // Limpiar la tabla y volver a cargar encabezados y datos
                    tableLayout.removeAllViews()
                    cargarEncabezadosTabla()
                    updateTableWithSalesData(year)
                }
            })
            yearPickerDialog.show(
                childFragmentManager,
                "YearPickerDialog"
            )
        }

        atras.setOnClickListener {
            val intent = Intent(requireContext(), MenuActivity::class.java)
            startActivity(intent)
            requireActivity().finish()
        }
    }

    private fun cargarEncabezadosTabla() {
        // Crear y agregar la fila de encabezado
        val headerRow = TableRow(context).apply {
            layoutParams = TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT
            )
            setBackgroundColor(Color.parseColor("#4A8C74")) // Color de fondo para el encabezado
        }

        // Encabezado para "Mes"
        val textViewMesHeader = TextView(context).apply {
            text = "Mes"
            setTextColor(Color.WHITE)
            setPadding(16, 16, 16, 16)
            gravity = Gravity.CENTER
        }

        // Encabezado para "Ventas"
        val textViewVentasHeader = TextView(context).apply {
            text = "Ventas"
            setTextColor(Color.WHITE)
            setPadding(16, 16, 16, 16)
            gravity = Gravity.CENTER
        }

        // Encabezado para "Facturas"
        val textViewFacturasHeader = TextView(context).apply {
            text = "Facturas"
            setTextColor(Color.WHITE)
            setPadding(16, 16, 16, 16)
            gravity = Gravity.CENTER
        }

        // Encabezado para "Crédito Fiscal"
        val textViewCreditoFiscalHeader = TextView(context).apply {
            text = "Crédito Fiscal"
            setTextColor(Color.WHITE)
            setPadding(16, 16, 16, 16)
            gravity = Gravity.CENTER
        }

        // Encabezado para "Numero de Facturas"
        val textViewNumeroFacturasHeader = TextView(context).apply {
            text = "Num. Facturas"
            setTextColor(Color.WHITE)
            setPadding(16, 16, 16, 16)
            gravity = Gravity.CENTER
        }

        // Encabezado para "Numero de Credito Fiscal"
        val textViewNumeroCreditoFiscalHeader = TextView(context).apply {
            text = "Num. Crédito Fiscal"
            setTextColor(Color.WHITE)
            setPadding(16, 16, 16, 16)
            gravity = Gravity.CENTER
        }


        // Agregar los TextView de encabezados a la fila
        headerRow.addView(textViewMesHeader)
        headerRow.addView(textViewVentasHeader)
        headerRow.addView(textViewFacturasHeader)
        headerRow.addView(textViewCreditoFiscalHeader)
        headerRow.addView(textViewNumeroFacturasHeader)
        headerRow.addView(textViewNumeroCreditoFiscalHeader)

        // Agregar la fila de encabezado a la tabla
        tableLayout.addView(headerRow)
    }

    private fun updateTableWithSalesData(year: Int) {
        // Limpiar filas previas excepto el encabezado
        val rowCount = tableLayout.childCount
        if (rowCount > 1) {
            tableLayout.removeViews(1, rowCount - 1)
        }

        // Iterar sobre los 12 meses y agregar los datos correspondientes
        for (month in 1..12) {
            val ventasTotales = obtenerVentasTotales(month, year)
            val facturasTotales = obtenerFacturasTotales(month, year)
            val creditoFiscal = obtenerCreditoFiscal(month, year)
            val numFacturas = obtenerNumeroFacturas(month, year)
            val numCreditoFiscal = obtenerNumeroCreditoFiscal(month, year)

            val newRow = TableRow(context)
            newRow.layoutParams = TableRow.LayoutParams(
                TableRow.LayoutParams.WRAP_CONTENT,
                TableRow.LayoutParams.WRAP_CONTENT
            )

            val textViewMes = TextView(context).apply {
                text = obtenerNombreMes(month)
                setTextColor(Color.BLACK)
                setPadding(16, 16, 16, 16)
                gravity = Gravity.CENTER
            }

            val textViewVentas = TextView(context).apply {
                text = ventasTotales.toString()
                setTextColor(Color.BLACK)
                setPadding(16, 16, 16, 16)
                gravity = Gravity.CENTER
            }

            val textViewFacturas = TextView(context).apply {
                text = facturasTotales.toString()
                setTextColor(Color.BLACK)
                setPadding(16, 16, 16, 16)
                gravity = Gravity.CENTER
            }

            val textViewCreditoFiscal = TextView(context).apply {
                text = creditoFiscal.toString()
                setTextColor(Color.BLACK)
                setPadding(16, 16, 16, 16)
                gravity = Gravity.CENTER
            }

            val textViewNumFacturas = TextView(context).apply {
                text = numFacturas.toString()
                setTextColor(Color.BLACK)
                setPadding(16, 16, 16, 16)
                gravity = Gravity.CENTER
            }

            val textViewNumCreditoFiscal = TextView(context).apply {
                text = numCreditoFiscal.toString()
                setTextColor(Color.BLACK)
                setPadding(16, 16, 16, 16)
                gravity = Gravity.CENTER
            }

            // Agregar las columnas a la fila
            newRow.addView(textViewMes)
            newRow.addView(textViewVentas)
            newRow.addView(textViewFacturas)
            newRow.addView(textViewCreditoFiscal)
            newRow.addView(textViewNumFacturas)
            newRow.addView(textViewNumCreditoFiscal)

            // Agregar la fila a la tabla
            tableLayout.addView(newRow)
        }
    }
    private fun obtenerNombreMes(month: Int): String {
        val months = listOf("Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio", "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre")
        return months[month - 1]
    }


    private fun obtenerVentasTotales(month: Int, year: Int): Double {

        val totalFacturas = obtenerFacturasTotales(month, year)
        val totalCreditoFiscal = obtenerCreditoFiscal(month, year)

        val totalVentas = totalFacturas + totalCreditoFiscal
        return String.format("%.2f", totalVentas).toDouble()
    }

    private fun obtenerFacturasTotales(month: Int, year: Int): Double {
        val app = requireActivity().application as MyApp
        val database = app.database
        val startDate = "$year-${String.format("%02d", month)}-01"
        val endDate = "$year-${String.format("%02d", month)}-31"
        val query = QueryBuilder.select(SelectResult.expression(Function.sum(Expression.property("total"))))
            .from(DataSource.database(database))
            .where(
                Expression.property("tipoD").equalTo(Expression.string("Factura Consumidor Final"))
                    .and(Expression.property("fechaEmi").greaterThanOrEqualTo(Expression.string(startDate)))
                    .and(Expression.property("fechaEmi").lessThanOrEqualTo(Expression.string(endDate)))
            )

        val result = query.execute().firstOrNull()
        val totalFacturas = result?.getDouble(0) ?: 0.0

        return String.format("%.2f", totalFacturas).toDouble()
    }

    private fun obtenerCreditoFiscal(month: Int, year: Int): Double {
        val app = requireActivity().application as MyApp
        val database = app.database

        val startDate = "$year-${String.format("%02d", month)}-01"
        val endDate = "$year-${String.format("%02d", month)}-31"

        val query = QueryBuilder.select(SelectResult.expression(Function.sum(Expression.property("total"))))
            .from(DataSource.database(database))
            .where(
                Expression.property("tipoD").equalTo(Expression.string("Comprobante Crédito Fiscal"))
                    .and(Expression.property("fechaEmi").greaterThanOrEqualTo(Expression.string(startDate)))
                    .and(Expression.property("fechaEmi").lessThanOrEqualTo(Expression.string(endDate)))
            )

        val result = query.execute().firstOrNull()
        val totalCreditoFiscal = result?.getDouble(0) ?: 0.0

        return String.format("%.2f", totalCreditoFiscal).toDouble()
    }

    private fun obtenerNumeroFacturas(month: Int, year: Int): Int {
        val app = requireActivity().application as MyApp
        val database = app.database

        val startDate = "$year-${String.format("%02d", month)}-01"
        val endDate = "$year-${String.format("%02d", month)}-31"

        val query = QueryBuilder.select(SelectResult.expression(Function.count(Expression.property("total"))))
            .from(DataSource.database(database))
            .where(
                Expression.property("tipoD").equalTo(Expression.string("Factura Consumidor Final"))
                    .and(Expression.property("fechaEmi").greaterThanOrEqualTo(Expression.string(startDate)))
                    .and(Expression.property("fechaEmi").lessThanOrEqualTo(Expression.string(endDate)))
            )

        val result = query.execute().firstOrNull()
        return result?.getInt(0) ?: 0
    }

    private fun obtenerNumeroCreditoFiscal(month: Int, year: Int): Int {
        val app = requireActivity().application as MyApp
        val database = app.database

        val startDate = "$year-${String.format("%02d", month)}-01"
        val endDate = "$year-${String.format("%02d", month)}-31"

        val query = QueryBuilder.select(SelectResult.expression(Function.count(Expression.property("total"))))
            .from(DataSource.database(database))
            .where(
                Expression.property("tipoD").equalTo(Expression.string("Comprobante Crédito Fiscal"))
                    .and(Expression.property("fechaEmi").greaterThanOrEqualTo(Expression.string(startDate)))
                    .and(Expression.property("fechaEmi").lessThanOrEqualTo(Expression.string(endDate)))
            )

        val result = query.execute().firstOrNull()
        return result?.getInt(0) ?: 0
    }
}
