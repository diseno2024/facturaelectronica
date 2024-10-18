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

class ResFinancieroFragment : Fragment() {
    private lateinit var textViewYear: TextView
    private lateinit var atras: ImageButton
    private lateinit var tableLayout: TableLayout

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_res_financiero, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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

        // Agregar los TextView de encabezados a la fila
        headerRow.addView(textViewMesHeader)
        headerRow.addView(textViewVentasHeader)
        headerRow.addView(textViewFacturasHeader)
        headerRow.addView(textViewCreditoFiscalHeader)

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

            // Agregar las columnas a la fila
            newRow.addView(textViewMes)
            newRow.addView(textViewVentas)
            newRow.addView(textViewFacturas)
            newRow.addView(textViewCreditoFiscal)

            // Agregar la fila a la tabla
            tableLayout.addView(newRow)
        }
    }
    private fun obtenerNombreMes(month: Int): String {
        val months = listOf("Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio", "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre")
        return months[month - 1]
    }


    private fun obtenerVentasTotales(month: Int, year: Int): Double {
        // codigo para obtener el total
        return 5000.0 // Simulación de ventas totales para el mes
    }

    private fun obtenerFacturasTotales(month: Int, year: Int): Double {
        // codigo para obtener el total
        return 3000.0
    }

    private fun obtenerCreditoFiscal(month: Int, year: Int): Double {
        // codigo para obtener el total
        return 200.0
    }
}
