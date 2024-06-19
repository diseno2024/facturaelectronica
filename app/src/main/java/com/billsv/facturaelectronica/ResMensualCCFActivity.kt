package com.billsv.facturaelectronica

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.Spinner
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.couchbase.lite.QueryBuilder
import com.couchbase.lite.ResultSet
import com.couchbase.lite.SelectResult
import com.couchbase.lite.DataSource
import com.couchbase.lite.Expression
import android.graphics.Color
import android.os.Environment
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.Button
import android.widget.Toast
import com.couchbase.lite.CouchbaseLiteException
import com.couchbase.lite.MutableDocument
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.util.Calendar


class ResMensualCCFActivity : AppCompatActivity() {
    private lateinit var textViewMes: TextView
    private lateinit var textViewYear: TextView
    private lateinit var tipoD: Spinner
    private lateinit var atras: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_res_mensual_ccfactivity)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        var documentosList:MutableList<Map<String, String>>
        tipoD = findViewById(R.id.tipoD)
        // spinner tipoC
        val opcionesD = arrayOf("Comprobante Crédito Fiscal", "Factura Consumidor Final")
        val adapterD = ArrayAdapter(this, R.layout.spinner_descripcion, opcionesD)
        adapterD.setDropDownViewResource(R.layout.spinner_dropdown_per)
        tipoD.adapter = adapterD
        textViewMes = findViewById(R.id.textViewMes)
        textViewYear = findViewById(R.id.textViewYear)
        atras = findViewById(R.id.atras)

        val cardViewMonth: CardView = findViewById(R.id.cardViewMes)
        val cardViewYear: CardView = findViewById(R.id.cardViewYear)

        // Inicializar con el mes y año actuales
        val calendar = Calendar.getInstance()
        textViewMes.text = getMonthName(calendar.get(Calendar.MONTH))
        textViewYear.text = calendar.get(Calendar.YEAR).toString()

        cardViewMonth.setOnClickListener {
            val monthPickerDialog = MonthPickerDialog()
            monthPickerDialog.setOnMonthSelectedListener(object : MonthPickerDialog.OnMonthSelectedListener {
                override fun onMonthSelected(month: Int) {
                    textViewMes.text = getMonthName(month)
                    documentosList = actualizarTabla()
                }
            })
            monthPickerDialog.show(supportFragmentManager, "MonthPickerDialog")
        }

        cardViewYear.setOnClickListener {
            val yearPickerDialog = YearPickerDialog()
            yearPickerDialog.setOnYearSelectedListener(object : YearPickerDialog.OnYearSelectedListener {
                override fun onYearSelected(year: Int) {
                    textViewYear.text = year.toString()
                    documentosList = actualizarTabla()
                }
            })
            yearPickerDialog.show(supportFragmentManager, "YearPickerDialog")
        }

        atras.setOnClickListener {
            super.onBackPressed() // Llama al método onBackPressed() de la clase base
            val intent = Intent(this, MenuActivity::class.java)
            startActivity(intent)
            finish()
        }

        tipoD.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                documentosList = actualizarTabla()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // No hacer nada
            }
        }
        documentosList = actualizarTabla()
        val generar: Button = findViewById(R.id.Generar)
        generar.setOnClickListener {
            // Generar el archivo CSV con los datos recuperados
            escribirDatosEnCSV(documentosList, "documentos.csv")
        }

    }

    private fun getMonthName(month: Int): String {
        val months = arrayOf(
            "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
            "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
        )
        return months[month]
    }

    private fun getMonthNumber(monthName: String): Int {
        val months = arrayOf(
            "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
            "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
        )
        return months.indexOf(monthName) + 1
    }

    override fun onBackPressed() {
        super.onBackPressed() // Llama al método onBackPressed() de la clase base
        val intent = Intent(this, MenuActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun actualizarTabla():MutableList<Map<String, String>> {
        val tableLayout = findViewById<TableLayout>(R.id.tableLayout)

        // Limpia todas las filas excepto la cabecera
        tableLayout.removeViews(1, tableLayout.childCount - 1)

        // Obtén una referencia a la base de datos
        val app = application as MyApp
        val database = app.database
        val tipoDSeleccionado = tipoD.selectedItem.toString()
        val mesSeleccionado = textViewMes.text.toString()
        val anioSeleccionado = textViewYear.text.toString()

        // Validación de selección de mes y año
        val mesNumero = getMonthNumber(mesSeleccionado)
        val anioNumero = anioSeleccionado.toInt()

        // Crear expresiones para la consulta
        val whereExpression = Expression.property("tipoD").equalTo(Expression.string(tipoDSeleccionado))
            .and(Expression.property("fechaEmi").greaterThanOrEqualTo(Expression.string("$anioNumero-${mesNumero.toString().padStart(2, '0')}-01")))
            .and(Expression.property("fechaEmi").lessThanOrEqualTo(Expression.string("$anioNumero-${mesNumero.toString().padStart(2, '0')}-31")))

        // Crea una consulta para seleccionar todos los documentos con el tipo seleccionado y rango de fechas
        val query = QueryBuilder.select(SelectResult.all())
            .from(DataSource.database(database))
            .where(whereExpression)

        // Ejecuta la consulta
        val result = query.execute()

        // Lista para almacenar los datos de los documentos
        val documentosList = mutableListOf<Map<String, String>>()

        // Itera sobre todos los resultados de la consulta
        result.allResults().forEach { result ->
            // Obtiene el diccionario del documento del resultado actual
            val dict = result.getDictionary(database.name)
            Log.e("RES", "$dict")

            // Extrae los valores de los campos del documento y los loguea
            val nombre = dict?.getString("nombre") ?: ""
            val nit = dict?.getString("nit") ?: ""
            val dui = dict?.getString("dui") ?: ""
            val totalNS = dict?.getDouble("totalNoSuj") ?: 0.0
            val totalEx = dict?.getDouble("totalExenta") ?: 0.0
            val totalG = dict?.getDouble("totalGravada") ?: 0.0
            val total = dict?.getDouble("total") ?: 0.0
            val numC = dict?.getString("numeroControl") ?: ""
            val fechE = dict?.getString("fechaEmi") ?: ""
            val numR = "15041-RES-IN-05608-2024"
            val serieD = "OFIC0001"
            val numD = "1"

            Log.e("VALOR NOMBRE", nombre)
            Log.e("VALOR NIT", nit)
            Log.e("VALOR DUI", dui)
            Log.e("VALOR TOTAL NS", totalNS.toString())
            Log.e("VALOR TOTAL EX", totalEx.toString())
            Log.e("VALOR TOTAL G", totalG.toString())
            Log.e("VALOR TOTAL", total.toString())
            Log.e("VALOR NUM CONTROL", numC)
            Log.e("VALOR FECHA", fechE) // Imprime la fecha de emisión del documento actual

            // Crea y agrega una fila a la tabla con los datos del documento
            addRowToTable(
                tableLayout,
                fechE, numR, serieD, numD, numC, nit,
                nombre, totalEx.toString(), totalNS.toString(), totalG.toString(), total.toString(),
                dui
            )

            // Añadir los datos del documento a la lista
            val documento = mapOf(
                "nombre" to nombre,
                "nit" to nit,
                "dui" to dui,
                "totalNoSuj" to totalNS.toString(),
                "totalExenta" to totalEx.toString(),
                "totalGravada" to totalG.toString(),
                "total" to total.toString(),
                "numeroControl" to numC,
                "fechaEmi" to fechE,
                "numR" to numR,
                "serieD" to serieD,
                "numD" to numD
            )
            documentosList.add(documento)
        }
        return documentosList
    }

    private fun escribirDatosEnCSV(datosList: List<Map<String, String>>, fileName: String) {
        // Get the Downloads directory
        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val csvFile = File(downloadsDir, fileName)

        try {
            FileWriter(csvFile).use { writer ->
                // Write the CSV header
                writer.append("nombre,nit,dui,totalNoSuj,totalExenta,totalGravada,total,numeroControl,fechaEmi,numR,serieD,numD\n")

                // Write the data
                for (documento in datosList) {
                    writer.append(documento["nombre"]).append(',')
                    writer.append(documento["nit"]).append(',')
                    writer.append(documento["dui"]).append(',')
                    writer.append(documento["totalNoSuj"]).append(',')
                    writer.append(documento["totalExenta"]).append(',')
                    writer.append(documento["totalGravada"]).append(',')
                    writer.append(documento["total"]).append(',')
                    writer.append(documento["numeroControl"]).append(',')
                    writer.append(documento["fechaEmi"]).append(',')
                    writer.append(documento["numR"]).append(',')
                    writer.append(documento["serieD"]).append(',')
                    writer.append(documento["numD"]).append('\n')
                }
            }
            Toast.makeText(this, "Archivo CSV creado exitosamente!", Toast.LENGTH_SHORT).show()
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(this, "Error al crear el archivo CSV: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }


    private fun addRowToTable(
        tableLayout: TableLayout,
        vararg values: String?
    ) {
        val tableRow = TableRow(this)
        val params = TableRow.LayoutParams(
            TableRow.LayoutParams.WRAP_CONTENT,
            TableRow.LayoutParams.WRAP_CONTENT
        )
        tableRow.layoutParams = params

        values.forEach { value ->
            val textView = TextView(this).apply {
                text = value ?: ""
                gravity = Gravity.CENTER
                setPadding(16, 16, 16, 16)
                setTextColor(Color.BLACK)
            }
            tableRow.addView(textView)
        }

        tableLayout.addView(tableRow)
    }

}