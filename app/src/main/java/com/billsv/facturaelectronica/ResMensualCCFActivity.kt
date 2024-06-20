package com.billsv.facturaelectronica

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Build
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
import com.couchbase.lite.SelectResult
import com.couchbase.lite.DataSource
import com.couchbase.lite.Expression
import android.graphics.Color
import android.net.Uri
import android.os.Environment
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.Button
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.couchbase.lite.CouchbaseLiteException
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.util.Calendar


class ResMensualCCFActivity : AppCompatActivity() {
    private lateinit var textViewMes: TextView
    private lateinit var textViewYear: TextView
    private lateinit var tipoD: Spinner
    private lateinit var atras: ImageButton
    private val REQUEST_CODE_NOTIFICATION_PERMISSION = 1
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_res_mensual_ccfactivity)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        requestNotificationPermission()
        var tipoDSeleccionado:String=""
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
                    tipoDSeleccionado = tipoD.selectedItem.toString()
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
                    tipoDSeleccionado = tipoD.selectedItem.toString()
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
                tipoDSeleccionado = tipoD.selectedItem.toString()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // No hacer nada
            }
        }
        documentosList = actualizarTabla()
        val generar: Button = findViewById(R.id.Generar)
        generar.setOnClickListener {
            if(contarDocumentos(tipoDSeleccionado)) {
                // Generar el archivo CSV con los datos recuperados
                val mesSeleccionado = textViewMes.text.toString()
                val anioSeleccionado = textViewYear.text.toString()
                if(tipoDSeleccionado=="Comprobante Crédito Fiscal") {
                    escribirDatosEnCSV(documentosList, "ResumenCCF$mesSeleccionado$anioSeleccionado.csv")
                }else{
                    escribirDatosEnCSV(documentosList, "ResumenCF$mesSeleccionado$anioSeleccionado.csv")
                }
            }else{
                Toast.makeText(this, "No hay DTE's para exportar", Toast.LENGTH_SHORT).show()
            }
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
        val tableLayout2 = findViewById<TableLayout>(R.id.tableLayout2)

        // Limpia todas las filas excepto la cabecera
        tableLayout.removeViews(1, tableLayout.childCount - 1)

        tableLayout2.removeViews(1, tableLayout2.childCount - 1)

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
            var fechE: String = ""//A
            var claseDocumento: String = ""//B
            var tipoDocumento: String = ""//C
            var codGen: String = ""//D
            var sello: String = ""//E
            var numC: String = ""//F
            var numCI: String = ""//G
            var nrc:String=""//H
            var nombre: String = ""//I
            var totalEx: String = ""//J
            var totalNS: String = ""//K
            var totalG: String = ""//L
            var debito: String = "0.00"//M
            var ventasCTD: String = "0.00"//N
            var debitoFVCT: String = "0.00"//O
            var total: String = ""//P
            var dui: String = ""//Q
            var nit: String = ""//
            var Anexo: String = ""//R
            var maquina: String=""
            var InternasExentas: String = ""
            var ExDCA: String = "0.00"
            var ExFCA: String = "0.00"
            var ExSer: String = "0.00"
            var VeZFyDPA: String = "0.00"
            if(tipoDSeleccionado=="Comprobante Crédito Fiscal") {
                // Extrae los valores de los campos del documento y los loguea
                fechE = dict?.getString("fechaEmi") ?: ""
                claseDocumento = "4"
                tipoDocumento = "03"
                codGen = dict?.getString("codigoGeneracion")?.replace("-","").toString()
                sello = ""
                numC = dict?.getString("numeroControl")?.replace("-","").toString()
                numCI = ""
                nrc= dict?.getString("nrc")?.replace("-","").toString()
                nombre  = dict?.getString("nombre") ?: ""
                totalEx = (dict?.getDouble("totalExenta")).toString()
                totalNS = (dict?.getDouble("totalNoSuj")).toString()
                totalG = (dict?.getDouble("totalGravada")).toString()
                debito = "0.00"
                ventasCTD = "0.00"
                debitoFVCT = "0.00"
                total = (dict?.getDouble("total")).toString()
                dui = ""
                Anexo = "1"

            }else{
                // Extrae los valores de los campos del documento y los loguea
                fechE = dict?.getString("fechaEmi") ?: ""
                claseDocumento = "4"
                tipoDocumento = "01"
                codGen = "N/A"
                sello = "N/A"
                numC = "N/A"
                numCI = "N/A"
                nrc= dict?.getString("codigoGeneracion")?.replace("-","").toString()
                nombre  = dict?.getString("codigoGeneracion")?.replace("-","").toString()
                maquina = ""
                totalEx = (dict?.getDouble("totalExenta")).toString()
                InternasExentas = "0.00"
                totalNS = (dict?.getDouble("totalNoSuj")).toString()
                totalG = (dict?.getDouble("totalGravada")).toString()
                ExDCA = "0.00"
                ExFCA = "0.00"
                ExSer = "0.00"
                VeZFyDPA = "0.00"
                ventasCTD = "0.00"
                total = (dict?.getDouble("total")).toString()
                Anexo = "2"
            }
            if(tipoDSeleccionado=="Comprobante Crédito Fiscal") {
                tableLayout2.visibility = View.GONE
                tableLayout.visibility = View.VISIBLE
                // Crea y agrega una fila a la tabla con los datos del documento
                addRowToTable(
                    tableLayout,fechE,
                    claseDocumento,
                    tipoDocumento,
                    codGen,
                    sello,
                    numC,
                    numCI,
                    nrc,
                    nombre,
                    totalEx,
                    totalNS,
                    totalG,
                    debito,
                    ventasCTD,
                    debitoFVCT,
                    total,
                    dui,
                    Anexo
                )

                // Añadir los datos del documento a la lista
                val documento = mapOf(
                    "fechaEmi" to fechE,
                    "claseDoc" to claseDocumento,
                    "tipoDoc" to tipoDocumento,
                    "numR" to codGen,
                    "serieD" to sello,
                    "numD" to numC,
                    "numeroControl" to numCI,
                    "nit" to nrc,
                    "nombre" to  nombre,
                    "totalExenta" to totalEx,
                    "totalNoSuj" to totalNS,
                    "totalGravada" to totalG,
                    "debitoFiscal" to debito,
                    "ventasTerceros" to ventasCTD,
                    "debitoFiscalTerceros" to debitoFVCT,
                    "total" to total,
                    "dui" to dui,
                    "numAnexo" to Anexo
                )
                documentosList.add(documento)
            }else{
                tableLayout.visibility = View.GONE
                tableLayout2.visibility = View.VISIBLE
                // Crea y agrega una fila a la tabla con los datos del documento
                addRowToTable(
                    tableLayout2,fechE,
                    claseDocumento,
                    tipoDocumento,
                    codGen,
                    sello,
                    numC,
                    numCI,
                    nrc,
                    nombre,
                    maquina,
                    totalEx,
                    InternasExentas,
                    totalNS,
                    totalG,
                    ExDCA,
                    ExFCA,
                    ExSer,
                    VeZFyDPA,
                    ventasCTD,
                    total,
                    Anexo
                )

                val documento = mapOf(
                    "fechaEmi" to fechE,
                    "claseDoc" to claseDocumento,
                    "tipoDoc" to tipoDocumento,
                    "numR" to codGen,
                    "serieD" to sello,
                    "numControlDel" to numC, // Asumiendo numC es Número de Control Interno DEL
                    "numControlAl" to numCI, // Asumiendo numCI es Número de Control Interno AL
                    "numDocDel" to nrc, // Asumiendo numD es Número de Documento (DEL)
                    "numDocAl" to nombre, // Asumiendo numDA es Número de Documento (AL)
                    "numMaquina" to maquina, // Asumiendo numMaquina es Número de Maquina Registradora
                    "totalExenta" to totalEx,
                    "ventasInternasExentas" to InternasExentas,
                    "totalNoSuj" to totalNS,
                    "totalGravada" to totalG,
                    "exportCentroAmerica" to ExDCA,
                    "exportFueraCentroAmerica" to ExFCA,
                    "exportServicios" to ExSer,
                    "ventasZonasFrancas" to VeZFyDPA,
                    "ventasTerceros" to ventasCTD,
                    "total" to total,
                    "numAnexo" to Anexo
                )
                documentosList.add(documento)

            }
        }
        return documentosList
    }

    private fun escribirDatosEnCSV(datosList: List<Map<String, String>>, fileName: String) {
        // Get the Downloads directory
        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val csvFile = File(downloadsDir, fileName)
        val tipoDSeleccionado = tipoD.selectedItem.toString()
        if(tipoDSeleccionado=="Comprobante Crédito Fiscal") {
            try {
                FileWriter(csvFile).use { writer ->
                    // Write the CSV header

                    // Write the data
                    for (documento in datosList) {
                        writer.append(documento["fechaEmi"]).append(',')
                        writer.append(documento["claseDoc"]).append(',')
                        writer.append(documento["tipoDoc"]).append(',')
                        writer.append(documento["numR"]).append(',')
                        writer.append(documento["serieD"]).append(',')
                        writer.append(documento["numD"]).append(',')
                        writer.append(documento["numeroControl"]).append(',')
                        writer.append(documento["nit"]).append(',')
                        writer.append(documento["nombre"]).append(',')
                        writer.append(documento["totalExenta"]).append(',')
                        writer.append(documento["totalNoSuj"]).append(',')
                        writer.append(documento["totalGravada"]).append(',')
                        writer.append(documento["debitoFiscal"]).append(',')
                        writer.append(documento["ventasTerceros"]).append(',')
                        writer.append(documento["debitoFiscalTerceros"]).append(',')
                        writer.append(documento["total"]).append(',')
                        writer.append(documento["dui"]).append(',')
                        writer.append(documento["numAnexo"]).append('\n')
                    }
                }
                Toast.makeText(this, "Archivo CSV creado exitosamente!", Toast.LENGTH_SHORT).show()

                // Show notification
                showNotification(csvFile)

            } catch (e: IOException) {
                e.printStackTrace()
                Toast.makeText(
                    this,
                    "Error al crear el archivo CSV: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }else{
            try {
                FileWriter(csvFile).use { writer ->
                    // Write the CSV header
                    writer.append("Fecha de Emisión,Clase de Documento,Tipo de Documento,Número de Resolución,Serie del Documento,Número de Control Interno DEL,Número de Control Interno AL,Número de Documento (DEL),Número de Documento (AL),Número de Maquina Registradora,Ventas Exentas,Ventas Internas Exentas No Sujetas a Proporcionalidad,Ventas No Sujetas,Ventas Gravadas Locales,Exportaciones Dentro del Área de CentroAmérica,Exportaciones Fuera del Área de CentroAmérica,Exportaciones de Servicio,Ventas a Zonas Francas y DPA (Tasa Cero),Ventas a Cuenta de Terceros no Domiciliados,Total de Ventas,Número del Anexo\n")

                    // Write the data
                    for (documento in datosList) {
                        writer.append(documento["fechaEmi"]).append(',')
                        writer.append(documento["claseDoc"]).append(',')
                        writer.append(documento["tipoDoc"]).append(',')
                        writer.append(documento["numR"]).append(',')
                        writer.append(documento["serieD"]).append(',')
                        writer.append(documento["numControlDel"]).append(',')
                        writer.append(documento["numControlAl"]).append(',')
                        writer.append(documento["numDocDel"]).append(',')
                        writer.append(documento["numDocAl"]).append(',')
                        writer.append(documento["numMaquina"]).append(',')
                        writer.append(documento["totalExenta"]).append(',')
                        writer.append(documento["ventasInternasExentas"]).append(',')
                        writer.append(documento["totalNoSuj"]).append(',')
                        writer.append(documento["totalGravada"]).append(',')
                        writer.append(documento["exportCentroAmerica"]).append(',')
                        writer.append(documento["exportFueraCentroAmerica"]).append(',')
                        writer.append(documento["exportServicios"]).append(',')
                        writer.append(documento["ventasZonasFrancas"]).append(',')
                        writer.append(documento["ventasTerceros"]).append(',')
                        writer.append(documento["total"]).append(',')
                        writer.append(documento["numAnexo"]).append('\n')
                    }
                }
                Toast.makeText(this, "Archivo CSV creado exitosamente!", Toast.LENGTH_SHORT).show()

                // Show notification
                showNotification(csvFile)

            } catch (e: IOException) {
                e.printStackTrace()
                Toast.makeText(this, "Error al crear el archivo CSV: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }

    }

    private fun showNotification(csvFile: File) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "csv_channel_id"

        // Crear el canal de notificación para versiones de Android Oreo (API 26) y superiores
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "CSV Channel", NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }

        // Intent para abrir la carpeta de descargas
        val intent = Intent(Intent.ACTION_VIEW)

        // Obtener la URI del archivo CSV usando FileProvider
        val uri = FileProvider.getUriForFile(
            this,
            "${applicationContext.packageName}.fileprovider",
            csvFile
        )
        intent.setDataAndType(uri, "text/csv")
        intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK

        // PendingIntent para la acción de abrir carpeta
        val pendingIntentFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, pendingIntentFlags)

        // Construcción de la notificación
        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("CSV creado")
            .setContentText("Archivo CSV guardado en: ${csvFile.name}")
            .setSmallIcon(R.drawable.ic_notification) // Asegúrate de tener un icono de notificación en tu proyecto
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        // Mostrar la notificación
        notificationManager.notify(0, notification)
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
    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // TIRAMISU is API level 33 (Android 13)
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), REQUEST_CODE_NOTIFICATION_PERMISSION)
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_NOTIFICATION_PERMISSION) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                // Permission granted, you can show notifications
                Log.d("Permissions", "Notification permission granted")
            } else {
                // Permission denied, show a message to the user
                Toast.makeText(this, "Notification permission is required to show notifications", Toast.LENGTH_SHORT).show()
                Log.d("Permissions", "Notification permission denied")
            }
        }
    }
    private fun contarDocumentos(fact:String):Boolean {
        val app = application as MyApp
        val database = app.database
        val mesSeleccionado = textViewMes.text.toString()
        val anioSeleccionado = textViewYear.text.toString()

        // Validación de selección de mes y año
        val mesNumero = getMonthNumber(mesSeleccionado)
        val anioNumero = anioSeleccionado.toInt()
        val whereExpression = Expression.property("tipoD").equalTo(Expression.string(fact))
            .and(Expression.property("fechaEmi").greaterThanOrEqualTo(Expression.string("$anioNumero-${mesNumero.toString().padStart(2, '0')}-01")))
            .and(Expression.property("fechaEmi").lessThanOrEqualTo(Expression.string("$anioNumero-${mesNumero.toString().padStart(2, '0')}-31")))
        val query = QueryBuilder.select(SelectResult.all())
            .from(DataSource.database(database))
            .where(whereExpression)
        try {
            val result = query.execute()
            val count = result.allResults().size
            if(count>0){
                return true
            }
            Log.d("ReClienteActivity", "Número de documentos de tipo 'ConfEmisor': $count")
        } catch (e: CouchbaseLiteException) {
            Log.e("ReClienteActivity", "Error al contar los documentos de tipo 'ConfEmisor': ${e.message}", e)
        }
        return false
    }

}