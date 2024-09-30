package com.billsv.facturaelectronica


import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.graphics.pdf.PdfRenderer
import android.os.Bundle
import android.os.Environment
import android.os.ParcelFileDescriptor
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.geometry.isEmpty
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.couchbase.lite.CouchbaseLiteException
import com.couchbase.lite.DataSource
import com.couchbase.lite.Database
import com.couchbase.lite.Expression
import com.couchbase.lite.Meta
import com.couchbase.lite.MutableDocument
import com.couchbase.lite.Query
import com.couchbase.lite.QueryBuilder
import com.couchbase.lite.ResultSet
import com.couchbase.lite.SelectResult
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.google.android.material.card.MaterialCardView
import com.google.gson.GsonBuilder
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.FileWriter
import java.io.InputStream
import java.lang.Exception
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.UUID
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import org.json.JSONArray
import java.io.ByteArrayOutputStream
import java.io.IOException


class EmitirCFActivity : AppCompatActivity() {
    private lateinit var tableLayout: TableLayout
    private lateinit var database: Database
    private lateinit var spinnerOp:Spinner
    private var currentControlNumber = 0L
    var totalNoSuj=0.0
    var totalExenta=0.0
    var totalGravada=0.0
    var totalIva=0.0
    // Si el monto de la factura es igual o superior a este valor entonces se necesitan los datos del cliente
    var montoMaximo = 1024


    var total = 0.0
    @SuppressLint("InflateParams")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_emitir_cf)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val app = application as MyApp
        database = app.database
        contarDocumentosConfEmisor()
        val Num = obtenerNumeroControl()
        Num.forEach { data ->
            val nums = data.split("\n")
            currentControlNumber = nums[0].toLong()
        }
        tableLayout = findViewById(R.id.Tabla)


        val dataList = obtenerDatosGuardados()
        if (dataList.isNotEmpty()) {
            dataList.forEach { data ->
                val datos = data.split("\n")
                val cantidad = datos[1]
                val producto = datos[3]
                val precio = datos[5]
                val totalNS=datos[8].toDouble()
                val totalEx=datos[7].toDouble()
                val totalGr=datos[6].toDouble()
                val totalIv=datos[9].toDouble()

                totalNoSuj+=totalNS
                totalExenta+=totalEx
                totalGravada+=totalGr
                totalIva+=totalIv
                total += ((cantidad.toIntOrNull() ?: 0) * (precio.toDoubleOrNull() ?: 0.0))


                val tableRow = TableRow(this)
                val textSiz = if (isTablet()) 26f else 14f
                val cantidadTextView = TextView(this).apply {
                    text = cantidad
                    layoutParams = TableRow.LayoutParams(40, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                        setMargins(8, 8, 8, 8)
                        gravity = Gravity.CENTER
                        textSize = textSiz
                    }
                    textAlignment = TextView.TEXT_ALIGNMENT_CENTER
                }

                val productoTextView = TextView(this).apply {
                    text = producto
                    layoutParams = TableRow.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f).apply {
                        setMargins(8, 8, 8, 8)
                        textSize = textSiz
                    }
                    textAlignment = TextView.TEXT_ALIGNMENT_CENTER
                }

                val precioTextView = TextView(this).apply {
                    text = precio
                    layoutParams = TableRow.LayoutParams(70, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                        setMargins(8, 8, 8, 8)
                        textSize = textSiz
                    }
                    textAlignment = TextView.TEXT_ALIGNMENT_CENTER
                }

                tableRow.addView(cantidadTextView)
                tableRow.addView(productoTextView)
                tableRow.addView(precioTextView)

                tableLayout.addView(tableRow)
            }
        } else {
            // Si no hay datos, puedes agregar una fila indicando que no hay datos disponibles
            val emptyRow = TableRow(this)
            val textSiz = if (isTablet()) 26f else 14f
            val emptyTextView = TextView(this).apply {
                text = "No hay artículos"
                layoutParams = TableRow.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f).apply {
                    setMargins(8, 8, 8, 8)
                    textSize = textSiz
                }
                textAlignment = TextView.TEXT_ALIGNMENT_CENTER
            }
            emptyRow.addView(emptyTextView)
            tableLayout.addView(emptyRow)
        }
        val Total: TextView = findViewById(R.id.Total)
        total=BigDecimal(total).setScale(2, RoundingMode.HALF_UP).toDouble()
        Total.text = total.toString()

        spinnerOp= findViewById(R.id.CoOperacion)
        // Create an ArrayAdapter using the string array and a default spinner layout.
        ArrayAdapter.createFromResource(
            this,
            R.array.Operacion,
            R.layout.spinner_descripcion
        ).also { adapter ->
            // Specify the layout to use when the list of choices appears.
            adapter.setDropDownViewResource(R.layout.spinner_dropdown_per)
            // Apply the adapter to the spinner.
            spinnerOp.adapter = adapter
        }
        val btnAtras: ImageButton = findViewById(R.id.atras)
        btnAtras.setOnClickListener {
           verificar()
        }
        val siguiente: Button = findViewById(R.id.Siguiente)
        siguiente.setOnClickListener {
            if (total < montoMaximo){
                if(validarfactura()) {
                    emitirFactura()
                    enviarDatos()
                }
            }else{
                if(validarfactura()&&validarcliente()) {
                    emitirFactura()
                    enviarDatos()
                }
            }
            //generarPdf()
        }
        // Recupera los datos pasados desde la otra actividad
        val datosGuardados = intent.getStringExtra("Cliente")
        val Letrai = intent.getStringExtra("letrai")
        val Nombre: TextView = findViewById(R.id.nombre)
        val DUI: TextView = findViewById(R.id.dui)
        // Aquí puedes usar los datos como necesites
        if (datosGuardados != null) {
            if(datosGuardados.isNotEmpty())
                datosGuardados.let{
                    val datos = it.split("\n")
                    guardarCliente(Letrai, datos[8])
                }
        }else{
            val duiRecibido = intent.getStringExtra("dui")
            val Letra = intent.getStringExtra("letra")
            if(Letra!=null){
                guardarCliente(Letra, duiRecibido)
            }
        }
        val datacliente = obtenerDui()
        if(datacliente.isNotEmpty()){
            datacliente.forEach{data->
                val datos = data.split("\n")
                val dui = datos[0]
                val letra = datos[1]
                val cliente = cargarData(dui,letra)
                Log.d("ReClienteActivity", cliente.toString())
                if (cliente.isNotEmpty()) {
                    cliente.forEach{info->
                        val infocliente = info.split("\n")
                        Nombre.text = infocliente[0]
                        DUI.text = infocliente[12]
                        /*= datos[14]
                        = datos[15]
                        = datos[3]
                         = datos[2]
                         = datos[13]
                         = datos[11]*/
                    }
                }
            }
        }
        val editar: ImageButton = findViewById(R.id.cambiarCliente)
        val carta: MaterialCardView = findViewById(R.id.DatosdelCliente)
        if (Nombre.text != ""){
            carta.isEnabled = false
            editar.visibility = View.VISIBLE
        }
        editar.setOnClickListener{
            carta.isEnabled = true
            Nombre.text = ""
            DUI.text = ""
            editar.visibility = View.GONE
            if (datacliente.isNotEmpty()){
                datacliente.forEach{data->
                    val datos = data.split("\n")
                    val letra = datos[1]
                    if(letra=="T"){
                        borrarDui()
                        borrarClienteTemporal()
                    }else if(letra=="P"){
                        borrarDui()
                    }
                }
            }
        }
        val editarA: ImageButton = findViewById(R.id.editarArticulos)
        if (Total.text != "0.0"){
            editarA.visibility = View.VISIBLE
        }
        editarA.setOnClickListener {
            val dialogoCliente = Dialog(this@EmitirCFActivity) // Usa el contexto de la actividad
            dialogoCliente.setContentView(R.layout.dialogo_articulos)
            val width = (resources.displayMetrics.widthPixels * 0.9).toInt()
            val height = (resources.displayMetrics.heightPixels * 0.8).toInt()
            dialogoCliente.window?.setLayout(width, height)
            dialogoCliente.setCanceledOnTouchOutside(false)

            // Busca containerLayout dentro del diálogo, no de la actividad
            val linearLayout1 = dialogoCliente.findViewById<LinearLayout>(R.id.containerLayout)
            val articulos = obtenerDatosGuardados()

            if(articulos.isNotEmpty()) {
                articulos.forEach { data ->
                    val datosv = data.split("\n")
                    val itemLayout2 = dialogoCliente.layoutInflater.inflate(R.layout.articulos, linearLayout1, false)

                    val Editar2 = itemLayout2.findViewById<ImageButton>(R.id.btnEditarData)
                    val textViewCantidad = itemLayout2.findViewById<TextView>(R.id.cantidad)
                    val textViewProducto = itemLayout2.findViewById<TextView>(R.id.producto)
                    val textViewPrecio = itemLayout2.findViewById<TextView>(R.id.precio)

                    textViewCantidad.text = datosv[1]
                    textViewProducto.text = datosv[3]
                    textViewPrecio.text = datosv[5]

                    Editar2.setOnClickListener {
                        borrararticulo(data, itemLayout2, linearLayout1)
                    }
                    linearLayout1.addView(itemLayout2)
                }
            }

            val btnExit = dialogoCliente.findViewById<ImageButton>(R.id.exit)
            btnExit.setOnClickListener {
                dialogoCliente.dismiss()
                recreate()
            }

            dialogoCliente.show()
        }
        val control = intent.getStringExtra("numeroControl")
        val codigoG = intent.getStringExtra("codigoGeneracion")
        if(control != null && codigoG != null){
            guardarNCyCG(control, codigoG)
        }
    }

    private fun guardarNCyCG(control: String, codigoG: String) {
        val app = application as MyApp
        val database = app.database

        val query = QueryBuilder.select(SelectResult.expression(Meta.id))
            .from(DataSource.database(database))
            .where(Expression.property("tipo").equalTo(Expression.string("NCCG")))

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
                .setString("numeroControl", control)
                .setString("codigoGeneracion", codigoG)
                .setString("tipo", "NCCG")

            // Guardar el nuevo documento
            database.save(document)
            Log.d("ReClienteActivity", "Datos NCCG guardados correctamente: \n $document")
        } catch (e: CouchbaseLiteException) {
            Log.e("ReClienteActivity", "Error al guardar los datos en la base de datos: ${e.message}", e)
        }
    }

    private fun borrararticulo(data: String, itemLayout2: View, linearLayout: LinearLayout) {
        val app = application as MyApp
        val database = app.database
        val datos = data.split("\n")
        val query = QueryBuilder.select(SelectResult.expression(Meta.id))
            .from(DataSource.database(database))
            .where(Expression.property("Producto").equalTo(Expression.string(datos[3])))

        try {
            val resultSet = query.execute()
            val results = resultSet.allResults()

            if (results.isNotEmpty()) {
                // Itera sobre los resultados y elimina cada documento
                for (result in results) {
                    val docId = result.getString(0) // Obtenemos el ID del documento en el índice 0
                    docId?.let {
                        val document = database.getDocument(it)
                        document?.let {
                            database.delete(it)
                        }
                    }
                }
                // Elimina la tarjeta de la vista

                linearLayout.removeView(itemLayout2)


                Log.d("Prin_Re_Cliente", "Se eliminó el cliente")
            } else {
                Log.d("Prin_Re_Cliente", "No existe el cliente")
            }
        } catch (e: CouchbaseLiteException) {
            Log.e("Prin_Re_Cliente", "Error al eliminar al cliente: ${e.message}", e)
        }
    }


    private fun validarcliente(): Boolean {
        val app = application as MyApp
        val database = app.database

        val queryCliente = QueryBuilder.select(SelectResult.expression(Meta.id))
            .from(DataSource.database(database))
            .where(Expression.property("tipo").equalTo(Expression.string("DUI")))
        try {
            val resulCliente = queryCliente.execute()
            val resultsC = resulCliente.allResults()

            if (resultsC.isNotEmpty()) {
                return true
            }else{
                showToast("En ventas mayor o igual a $$montoMaximo se necesitan los datos del cliente")
                return false
            }
        } catch (e: CouchbaseLiteException) {
            Log.e("ReClienteActivity", "Error al guardar los datos en la base de datos: ${e.message}", e)
            Toast.makeText(this, "Error al guardar los datos", Toast.LENGTH_SHORT).show()
        }
        return false
    }

    private fun validarfactura(): Boolean {
        val app = application as MyApp
        val database = app.database

        val queryEmisor = QueryBuilder.select(SelectResult.expression(Meta.id))
            .from(DataSource.database(database))
            .where(Expression.property("tipo").equalTo(Expression.string("ConfEmisor")))
        val queryArticulos = QueryBuilder.select(SelectResult.expression(Meta.id))
            .from(DataSource.database(database))
            .where(Expression.property("tipo").equalTo(Expression.string("Articulocf")))
        try {
            val resulEmisor = queryEmisor.execute()
            val resultsE = resulEmisor.allResults()

            val resulArticulo = queryArticulos.execute()
            val resultsA = resulArticulo.allResults()

            if (resultsE.isNotEmpty() && resultsA.isNotEmpty()) {
               return true
            }else{
                if(resultsE.isEmpty()){
                    showToast("Falta la información del Emisor")
                    val faltaempresa = Dialog(this)
                    faltaempresa.setContentView(R.layout.layout_dialogo_llenar) // R.layout.layout_custom_dialog es tu diseño personalizado
                    val width = (resources.displayMetrics.widthPixels * 0.9).toInt() // 80% del ancho de la pantalla
                    val height = (resources.displayMetrics.heightPixels * 0.5).toInt() // 60% del alto de la pantalla
                    faltaempresa.window?.setLayout(width, height)
                    faltaempresa.setCanceledOnTouchOutside(false)
                    val llenar = faltaempresa.findViewById<Button>(R.id.btnllenar)
                    llenar.setOnClickListener {
                        val intent = Intent(this, InfoEmisorActivity::class.java)
                        intent.putExtra("clave","faltacf")
                        startActivity(intent)
                    }
                    faltaempresa.show()
                }
                if(resultsA.isEmpty()){
                    showToast("Debe de haber al menos un Artículo")
                }
                return false
            }
        } catch (e: CouchbaseLiteException) {
            Log.e("ReClienteActivity", "Error al guardar los datos en la base de datos: ${e.message}", e)
            Toast.makeText(this, "Error al guardar los datos", Toast.LENGTH_SHORT).show()
        }
        return false
    }

    private fun enviarDatos() {
        val condicionO=spinnerOp.selectedItem.toString()
        var codigo=""
        when (condicionO) {
            "Contado" -> {
                codigo="1"

            }
            "Credito" -> {
                codigo="2"
            }
            else -> {
                codigo="3"
            }
        }
        val totalNS=totalNoSuj.toString()
        val totalEx=totalExenta.toString()
        val totalGr=totalGravada.toString()
        val totalT=total.toString()
        val intent=Intent(this, PDF_CFActivity::class.java)
        intent.putExtra("totalNoSuj",totalNS)
        intent.putExtra("totalExenta",totalEx)
        intent.putExtra("totalGravada",totalGr)
        intent.putExtra("total",totalT)
        intent.putExtra("totalIva",totalT)
        intent.putExtra("condicionOperacion",codigo)
    }



    private fun emitirFactura() {
        val NCCG = obtenerNCCG()
        var numeroControl:String = ""
        var codigoGeneracion:String = ""
        if(NCCG.isNotEmpty()){
            NCCG.forEach{data->
                val datos = data.split("\n")
                numeroControl = datos[0]
                codigoGeneracion = datos[1]
            }
        }else{
            numeroControl = numeroControl()
            codigoGeneracion = generarUUIDv4()
        }
        val currentreceptor = obtenerDui()
        val condicionO=spinnerOp.selectedItem.toString()
        var codigo=""
        when (condicionO) {
            "Contado" -> {
                codigo="1"

            }
            "Credito" -> {
                codigo="2"
            }
            else -> {
                codigo="3"
            }
        }
        val totalNS=totalNoSuj.toString()
        val totalEx=totalExenta.toString()
        val totalGr=totalGravada.toString()
        val totalT=total.toString()
        val totalI = totalIva.toString()
        var Info: String = ""
        if(currentreceptor.isNotEmpty()){
            currentreceptor.forEach{data->
                val datos = data.split("\n")
                val receptor = cargarData(datos[0],datos[1])
                if (receptor.isNotEmpty()) {
                    receptor.forEach { info ->
                        Info = info
                    }
                }
            }
        }
        val intent = Intent(this, PDF_CFActivity::class.java)
        intent.putExtra("Cliente", Info)
        intent.putExtra("numeroControl", numeroControl)
        intent.putExtra("codGeneracion", codigoGeneracion)
        intent.putExtra("totalNoSuj",totalNS)
        intent.putExtra("totalExenta",totalEx)
        intent.putExtra("totalGravada",totalGr)
        intent.putExtra("total",totalT)
        intent.putExtra("totalIva",totalI)
        intent.putExtra("condicionOperacion",codigo)
        intent.putExtra("JSON","Factura")


        startActivity(intent)
        finish()
    }

    private fun obtenerNCCG(): List<String> {
        val app = application as MyApp
        val database = app.database

        // Crea una consulta para seleccionar todos los documentos con tipo = "cliente"
        val query2 = QueryBuilder.select(SelectResult.all())
            .from(DataSource.database(database))
            .where(Expression.property("tipo").equalTo(Expression.string("NCCG")))

        // Ejecuta la consulta
        val result2 = query2.execute()

        // Lista para almacenar los datos obtenidos
        val info = mutableListOf<String>()

        // Itera sobre todos los resultados de la consulta
        result2.allResults().forEach { resul ->
            // Obtiene el diccionario del documento del resultado actual
            val dict = resul.getDictionary(database.name)

            // Extrae los valores de los campos del documento
            val control = dict?.getString("numeroControl")
            val codigoG = dict?.getString("codigoGeneracion")

            // Formatea los datos como una cadena y la agrega a la lista
            val dataString = "$control\n$codigoG"
            info.add(dataString)
        }
        return info
    }

    private fun borrarClienteTemporal() {
        val app = application as MyApp
        val database = app.database

        val query = QueryBuilder.select(SelectResult.expression(Meta.id))
            .from(DataSource.database(database))
            .where(Expression.property("tipo").equalTo(Expression.string("clientetemporal")))

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
        } catch (e: CouchbaseLiteException) {
            Log.e("ReClienteActivity", "Error al guardar los datos en la base de datos: ${e.message}", e)
            Toast.makeText(this, "Error al guardar los datos", Toast.LENGTH_SHORT).show()
        }
    }

    private fun borrarDui() {
        val app = application as MyApp
        val database = app.database

        val query = QueryBuilder.select(SelectResult.expression(Meta.id))
            .from(DataSource.database(database))
            .where(Expression.property("tipo").equalTo(Expression.string("DUI")))

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
        } catch (e: CouchbaseLiteException) {
            Log.e("ReClienteActivity", "Error al guardar los datos en la base de datos: ${e.message}", e)
            Toast.makeText(this, "Error al guardar los datos", Toast.LENGTH_SHORT).show()
        }
    }

    private fun guardarCliente(Letra:String?, dui:String?){
        if(Letra=="T"){
            guardarDUI(dui,Letra)
        }else if(Letra=="P"){
            guardarDUI(dui,Letra)
        }

    }

    private fun cargarData(dui: String, letra: String): List<String> {
        val app = application as MyApp
        val database = app.database
        val query: Query
        val result: ResultSet

        if (letra == "T") {
            // Crea una consulta para seleccionar todos los documentos con tipo = "cliente"
            query = QueryBuilder.select(SelectResult.all())
                .from(DataSource.database(database))
                .where(Expression.property("tipo").equalTo(Expression.string("clientetemporal")))
            result = query.execute()
        } else if (letra == "P") {
            // Crea una consulta para seleccionar todos los documentos con tipo = "cliente"
            query = QueryBuilder.select(SelectResult.all())
                .from(DataSource.database(database))
                .where(Expression.property("dui").equalTo(Expression.string(dui)))
            result = query.execute()
        } else {
            // Initialize result with an empty ResultSet or handle the error appropriately
            return emptyList()
        }

        // Lista para almacenar los datos obtenidos
        val dataList = mutableListOf<String>()

        // Itera sobre todos los resultados de la consulta
        result.allResults().forEach { result ->
            // Obtiene el diccionario del documento del resultado actual
            val dict = result.getDictionary(database.name)

            // Extrae los valores de los campos del documento
            val nombre = dict?.getString("nombre")
            val nit = dict?.getString("nit")
            val email = dict?.getString("email")
            val direccion = dict?.getString("direccion")
            val departamento = dict?.getString("departamento")
            val municipio = dict?.getString("municipio")
            val telefono = dict?.getString("telefono")
            val tipo = dict?.getString("tipoCliente")
            val dui = dict?.getString("dui")
            val nrc = dict?.getString("nrc")
            val AcEco = dict?.getString("actividadEconomica")
            val nitM = dict?.getString("nitM")
            val duiM = dict?.getString("duiM")
            val telM = dict?.getString("telefonoM")
            val depaText = dict?.getString("departamentoT")
            val muniText = dict?.getString("municipioT")

            // Formatea los datos como una cadena y la agrega a la lista
            val dataString = "$nombre\n$nit\n$email\n$direccion\n$departamento\n$municipio\n$telefono\n$tipo\n$dui\n$nrc\n$AcEco\n$nitM\n$duiM\n$telM\n$depaText\n$muniText"
            dataList.add(dataString)
        }

        // Devuelve la lista de datos
        return dataList
    }


    private fun guardarDUI(dui: String?, letra: String?){
        val app = application as MyApp
        val database = app.database

        val query = QueryBuilder.select(SelectResult.expression(Meta.id))
            .from(DataSource.database(database))
            .where(Expression.property("tipo").equalTo(Expression.string("DUI")))

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
                .setString("Dui", dui)
                .setString("letra", letra)
                .setString("tipo", "DUI")

            // Guardar el nuevo documento
            database.save(document)
            Log.d("ReClienteActivity", "Datos DUI guardados correctamente: \n $document")
        } catch (e: CouchbaseLiteException) {
            Log.e("ReClienteActivity", "Error al guardar los datos en la base de datos: ${e.message}", e)
        }
    }
    private fun obtenerDui(): List<String>{
        val app = application as MyApp
        val database = app.database

        // Crea una consulta para seleccionar todos los documentos con tipo = "cliente"
        val query2 = QueryBuilder.select(SelectResult.all())
            .from(DataSource.database(database))
            .where(Expression.property("tipo").equalTo(Expression.string("DUI")))

        // Ejecuta la consulta
        val result2 = query2.execute()

        // Lista para almacenar los datos obtenidos
        val info = mutableListOf<String>()

        // Itera sobre todos los resultados de la consulta
        result2.allResults().forEach { resul ->
            // Obtiene el diccionario del documento del resultado actual
            val dict = resul.getDictionary(database.name)

            // Extrae los valores de los campos del documento
            val DUI = dict?.getString("Dui")
            val letra = dict?.getString("letra")

            // Formatea los datos como una cadena y la agrega a la lista
            val dataString = "$DUI\n$letra"
            info.add(dataString)
        }
        return info
    }

    private fun numeroControl(): String {
        val numerocontrolBase = "DTE-01-OFIC0001-"
        val numeroDigitos = 15
        val formatoNumero = "%0${numeroDigitos}d"

        // Incrementar el número de control
        currentControlNumber = (currentControlNumber + 1) % 1000000000000000L

        // Formatear el número de control
        val numeroFormateado = String.format(formatoNumero, currentControlNumber)

        guardarNumeroControl(currentControlNumber)

        // Retornar el número de control completo
        return numerocontrolBase + numeroFormateado
    }
    private fun guardarNumeroControl(Long :Long) {
        // Buscar si ya existe un documento del tipo "ConfEmisor"
        val query = QueryBuilder.select(SelectResult.expression(Meta.id))
            .from(DataSource.database(database))
            .where(Expression.property("tipo").equalTo(Expression.string("NumeroControl")))

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
                .setString("numero", Long.toString())
                //.setString("numero", 0.toString())//para recetear
                .setString("tipo", "NumeroControl")

            // Guardar el nuevo documento
            database.save(document)
            Log.d("ReClienteActivity", "Datos LONG guardados correctamente: \n $document")
        } catch (e: CouchbaseLiteException) {
            Log.e("ReClienteActivity", "Error al guardar los datos en la base de datos: ${e.message}", e)
        }
    }
    private fun obtenerNumeroControl(): List<String> {
        // Obtén la instancia de la base de datos desde la aplicación
        val app = application as MyApp
        val database = app.database

        // Crea una consulta para seleccionar todos los documentos con tipo = "cliente"
        val query = QueryBuilder.select(SelectResult.all())
            .from(DataSource.database(database))
            .where(Expression.property("tipo").equalTo(Expression.string("NumeroControl")))

        // Ejecuta la consulta
        val result = query.execute()

        // Lista para almacenar los datos obtenidos
        val dataList = mutableListOf<String>()

        // Itera sobre todos los resultados de la consulta
        result.allResults().forEach { result ->
            // Obtiene el diccionario del documento del resultado actual
            val dict = result.getDictionary(database.name)

            // Extrae los valores de los campos del documento
            val numero = dict?.getString("numero")

            // Formatea los datos como una cadena y la agrega a la lista
            val dataString = "$numero"
            dataList.add(dataString)
        }

        // Devuelve la lista de datos
        return dataList
    }
    private fun generarUUIDv4(): String {
        val uuid = UUID.randomUUID()
        return uuid.toString().toUpperCase()
    }
    fun showDescription(view: View?) {
        val intent = Intent(this, DescripcionActivity::class.java)
        startActivity(intent)
        finish()

    }
    fun showDataClient(view: View) {
        val app = application as MyApp
        val database = app.database

        // Crea una consulta para seleccionar todos los documentos con tipo = "cliente"
        val query = QueryBuilder.select(SelectResult.all())
            .from(DataSource.database(database))
            .where(Expression.property("tipo").equalTo(Expression.string("cliente")))
        try {
            val resultSet = query.execute()
            val results = resultSet.allResults()

            if (results.isNotEmpty()) {
                val dialogoCliente = Dialog(this)
                dialogoCliente.setContentView(R.layout.layout_dialogo_cliente) // R.layout.layout_custom_dialog es tu diseño personalizado
                val width = (resources.displayMetrics.widthPixels * 0.9).toInt() // 80% del ancho de la pantalla
                val height = (resources.displayMetrics.heightPixels * 0.5).toInt() // 60% del alto de la pantalla
                dialogoCliente.window?.setLayout(width, height)
                dialogoCliente.setCanceledOnTouchOutside(false)
                val btnImportar = dialogoCliente.findViewById<Button>(R.id.btnImportar)
                val btnAgregar = dialogoCliente.findViewById<Button>(R.id.btnAgregar)
                val btnExit = dialogoCliente.findViewById<ImageButton>(R.id.exit)
                btnAgregar.setOnClickListener {
                    //Pagina para agregar datos de clientes
                    val intent = Intent(this, ReClienteActivity::class.java)
                    startActivity(intent)
                    finish()
                }
                btnImportar.setOnClickListener {
                    //Pagina para agregar datos de clientes
                    val intent = Intent(this, ImportarClientes::class.java)
                    intent.putExtra("letra","c")
                    startActivity(intent)
                    finish()
                }
                btnExit.setOnClickListener {
                    // Acción al hacer clic en el botón "Cancelar"
                    dialogoCliente.dismiss()
                }

                dialogoCliente.show()

            }else{
                val intent = Intent(this, ReClienteActivity::class.java)
                startActivity(intent)
                finish()
            }

        } catch (e: CouchbaseLiteException) {
            Log.e("EmitirCFActivity", "Error al cargar cliente: ${e.message}", e)
        }
    }
    override fun onBackPressed() {
        val app = application as MyApp
        val database = app.database
        val query = QueryBuilder.select(SelectResult.expression(Meta.id))
            .from(DataSource.database(database))
            .where(Expression.property("tipo").equalTo(Expression.string("Articulocf")))

        // Ejecuta la consulta
        val result = query.execute()
        if(result.allResults().isNotEmpty()){
            val dialogoCliente = Dialog(this)
            dialogoCliente.setContentView(R.layout.layout_datos_perdidos) // R.layout.layout_custom_dialog es tu diseño personalizado
            val width = (resources.displayMetrics.widthPixels * 0.9).toInt() // 80% del ancho de la pantalla
            val height = (resources.displayMetrics.heightPixels * 0.5).toInt() // 60% del alto de la pantalla
            dialogoCliente.window?.setLayout(width, height)
            dialogoCliente.setCanceledOnTouchOutside(false)
            val btnsi = dialogoCliente.findViewById<Button>(R.id.btnsi)
            val btnno = dialogoCliente.findViewById<Button>(R.id.btnno)
            btnsi.setOnClickListener {
                borrararticulos()
                borrarDui()
                borrarClienteTemporal()
                val intent = Intent(this, MenuActivity::class.java)
                startActivity(intent)
                finish()
            }
            btnno.setOnClickListener {
                dialogoCliente.dismiss()
            }
            dialogoCliente.show()
        } else {
            // Si no hay artículos, llama a super.onBackPressed()
            super.onBackPressed()
            borrarDui()
            borrarClienteTemporal()
            val intent = Intent(this, MenuActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
    private fun verificar(){
        val app = application as MyApp
        val database = app.database
        val query = QueryBuilder.select(SelectResult.expression(Meta.id))
            .from(DataSource.database(database))
            .where(Expression.property("tipo").equalTo(Expression.string("Articulocf")))

        // Ejecuta la consulta
        val result = query.execute()
        if(result.allResults().isNotEmpty()){
            val dialogoCliente = Dialog(this)
            dialogoCliente.setContentView(R.layout.layout_datos_perdidos) // R.layout.layout_custom_dialog es tu diseño personalizado
            val width = (resources.displayMetrics.widthPixels * 0.9).toInt() // 80% del ancho de la pantalla
            val height = (resources.displayMetrics.heightPixels * 0.5).toInt() // 60% del alto de la pantalla
            dialogoCliente.window?.setLayout(width, height)
            dialogoCliente.setCanceledOnTouchOutside(false)
            val btnsi = dialogoCliente.findViewById<Button>(R.id.btnsi)
            val btnno = dialogoCliente.findViewById<Button>(R.id.btnno)
            btnsi.setOnClickListener {
                borrararticulos()
                borrarDui()
                borrarClienteTemporal()
                val intent = Intent(this, MenuActivity::class.java)
                startActivity(intent)
                finish()
            }
            btnno.setOnClickListener {
                dialogoCliente.dismiss()
            }
            dialogoCliente.show()
        }else{
            borrarDui()//falta dialogo que se borrara el cliente
            borrarClienteTemporal()
            val intent = Intent(this, MenuActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun borrararticulos() {
        val app = application as MyApp
        val database = app.database
        val query = QueryBuilder.select(SelectResult.expression(Meta.id))
            .from(DataSource.database(database))
            .where(Expression.property("tipo").equalTo(Expression.string("Articulocf")))

        try {
            val resultSet = query.execute()
            val results = resultSet.allResults()

            if (results.isNotEmpty()) {
                // Itera sobre los resultados y elimina cada documento
                for (result in results) {
                    val docId = result.getString("id") // Obtener el ID del documento
                    docId?.let {
                        val document = database.getDocument(it)
                        document?.let {
                            database.delete(it)
                        }
                    }
                }

                Log.d("Prin_Re_Cliente", "Se eliminaron los artículos")
                showToast("Artículos eliminados")
            } else {
                Log.d("Prin_Re_Cliente", "No se encontraron artículos")
                showToast("No se encontraron artículos")
            }
        } catch (e: CouchbaseLiteException) {
            Log.e("Prin_Re_Cliente", "Error al eliminar los artículos: ${e.message}", e)
        }
    }



    private fun obtenerDatosGuardados(): List<String> {
        // Obtén la instancia de la base de datos desde la aplicación
        val app = application as MyApp
        val database = app.database

        // Crea una consulta para seleccionar todos los documentos con tipo = "cliente"
        val query = QueryBuilder.select(SelectResult.all())
            .from(DataSource.database(database))
            .where(Expression.property("tipo").equalTo(Expression.string("Articulocf")))

        // Ejecuta la consulta
        val result = query.execute()

        // Lista para almacenar los datos obtenidos
        val dataList = mutableListOf<String>()

        // Itera sobre todos los resultados de la consulta
        result.allResults().forEach { results ->
            // Obtiene el diccionario del documento del resultado actual
            val dict = results.getDictionary(database.name)

            // Extrae los valores de los campos del documento
            val Tipo = dict?.getString("Tipod")
            val cantidad = dict?.getString("Cantidad")
            val unidad = dict?.getString("Unidad")
            val Producto = dict?.getString("Producto")
            val TipoV = dict?.getString("Tipo de Venta")
            val Precio = dict?.getString("Precio")
            val VentaG = dict?.getString("ventaG")
            val VentaE = dict?.getString("ventaE")
            val VentaNS = dict?.getString("ventaNS")
            val ivaItem = dict?.getString("ivaItem")
            val codigoP = dict?.getString("ventaG")
            val codigoT = dict?.getString("codigoT")
            val tributo = dict?.getString("tributo")
            val psv = dict?.getString("psv")
            val noGravado = dict?.getString("noGravado")
            val montoDes = dict?.getString("montoDesc")
            val numItem = dict?.getString("numItem")

            // Formatea los datos como una cadena y la agrega a la lista
            val dataString = "$Tipo\n$cantidad\n$unidad\n$Producto\n$TipoV\n$Precio\n$VentaG\n$VentaE\n$VentaNS\n$ivaItem\n$codigoP\n$codigoT\n$tributo\n$psv\n$noGravado\n$montoDes\n$numItem"
            dataList.add(dataString)
        }

        // Devuelve la lista de datos
        return dataList
    }
    private fun generarPdf():PdfDocument {
        // Variable para poder almacenar el contenido del json através de una función
        //val jsonData = leerJsonDesdeAssets("DTE-01-OFIC0001-000000000000012.json")
        val jsonData = leerJsonDesdeAssets("CF.json")

        val pdfDocument = PdfDocument()
        // Crea una página tamaño carta para el PDF
        val paginaInfo = PdfDocument.PageInfo.Builder(612, 792, 1).create() // Tamaño carta
        val pagina1 = pdfDocument.startPage(paginaInfo)
        val canvas = pagina1.canvas

        // Estilo de Letra 1 - Para el encabezado del documento
        val paintEncabezado = Paint().apply {
            color = Color.BLACK
            textSize = 12f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        // Estilo de Letra 2 - Para la mayoría de información del documento
        val paintInfoDocumento = Paint().apply {
            color = Color.BLACK
            textSize = 7f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        }
        // Estilo de Letra 3 - Para los títulos
        val paintTITULO = Paint().apply {
            color = Color.BLACK
            textSize = 8f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        // Estilo de Letra 4 - Para la info del emisor y receptor
        val paintInfoContribuyentes = Paint().apply {
            color = Color.BLACK
            textSize = 7f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        }
        // Estilo de Rectángulo 1
        val paintRect1 = Paint().apply {
            style = Paint.Style.STROKE // Solo dibujar el contorno
            color = Color.BLACK // Color del contorno
            strokeWidth = 2f // Ancho del contorno
        }
        // Estilo de Rectángulo 2
        val paintRect2 = Paint().apply {
            style = Paint.Style.STROKE // Solo dibujar el contorno
            color = Color.BLACK // Color del contorno
            strokeWidth = 0.5f // Ancho del contorno
        }
        // Estilo de Borde 1 - Para el rectángulo del emisor y receptor
        val cornerRadius1 = 5f

        // Extraer y mostrar información del JSON
        try {
            val jsonObject = JSONObject(jsonData)

            // Aquí se empieza a generar el PDF en base a toda la info

            // Dibujar el Encabezado
            canvas.drawText("DOCUMENTO TRIBUTARIO ELECTRÓNICO", 210f, 25f, paintEncabezado)
            canvas.drawText("COMPROBANTE DE CONSUMIDOR FINAL", 220f, 40f, paintEncabezado)

            // IDENTIFICACIÓN
            /*   Lado izquierdo   */
            val identificacion = jsonObject.getJSONObject("identificacion")
            val codigoGeneracion = identificacion.getString("codigoGeneracion")
            val numeroControl = identificacion.getString("numeroControl")
            // Dibujar el texto en el PDF
            // Info Código de Generación
            canvas.drawText("Código de Generación: $codigoGeneracion", 25f, 175f, paintInfoDocumento)
            // Info Número de Control
            canvas.drawText("Número de Control: $numeroControl", 25f, 185f, paintInfoDocumento)
            /*   Lado derecho   */
            val tipoModelo = identificacion.getInt("tipoModelo")
            val tipoOperacion = identificacion.getInt("tipoOperacion")
            val fecEmi = identificacion.getString("fecEmi")
            val horEmi = identificacion.getString("horEmi")
            // Dibujar el texto en el PDF
            // Info Modelo de Facturación
            if (tipoModelo == 1) {
                canvas.drawText("Modelo de Facturación: Modelo Facturación Previo", 375f, 175f, paintInfoDocumento)
            } else if (tipoModelo == 2) {
                canvas.drawText("Modelo de Facturación: Modelo Facturación Diferido", 375f, 175f, paintInfoDocumento)
            }
            // Info Tipo de Transmisión
            if (tipoOperacion == 1) {
                canvas.drawText("Tipo de Transmisión: Transmisión Normal", 375f, 185f, paintInfoDocumento)
            } else if (tipoOperacion == 2) {
                canvas.drawText("Tipo de Transmisión: Transmisión por Contingencia", 375f, 185f, paintInfoDocumento)
            }
            // Info de fecha y hora de generación
            canvas.drawText("Fecha y Hora de Generación: $fecEmi $horEmi", 375f, 195f, paintInfoDocumento)



            // SELLO DE RECEPCIÓN
            //val respuestaHacienda = jsonObject.getJSONObject("respuestaHacienda")
            //val selloRecibido = respuestaHacienda.getString("selloRecibido")
            // Este es el sello de recibido otrogado por el Ministerio de Hacienda
            //canvas.drawText("Sello de Recepción: $selloRecibido", 25f, 195f, paintInfoDocumento)



            // EMISOR
            val emisor = jsonObject.getJSONObject("emisor")
            val nombre1 = emisor.getString("nombre")
            val nit1 = emisor.getString("nit")
            val nrc1 = emisor.getString("nrc")
            val descActividad1 = emisor.getString("descActividad")
            val direccion1 = emisor.getJSONObject("direccion")
            val complemento1 = direccion1.getString("complemento")
            val telefono1 = emisor.getString("telefono")
            val correo1 = emisor.getString("correo")
            // Coordenadas del rectángulo del EMISOR
            val emisorLeftEmisor = 25f
            val emisorTopEmisor = 215f
            val emisorRightEmisor = 306f
            val emisorBottomEmisor = 340f
            // Dibujar rectángulo del EMISOR
            canvas.drawRoundRect(emisorLeftEmisor, emisorTopEmisor, emisorRightEmisor, emisorBottomEmisor, cornerRadius1, cornerRadius1, paintRect1)
            // Dibujar el texto del EMISOR
            canvas.drawText("EMISOR", 130f, 230f, paintTITULO) // Dibuja que es la info del Emisor
            canvas.drawText("Nombre o razón social: $nombre1", 40f, 250f, paintInfoContribuyentes)
            canvas.drawText("NIT: $nit1", 40f, 260f, paintInfoContribuyentes)
            canvas.drawText("NRC: $nrc1", 40f, 270f, paintInfoContribuyentes)
            canvas.drawText("Actividad Económica: $descActividad1", 40f, 280f, paintInfoContribuyentes)
            canvas.drawText("Dirección: $complemento1", 40f, 290f, paintInfoContribuyentes)
            canvas.drawText("Número de Teléfono: $telefono1", 40f, 300f, paintInfoContribuyentes)
            canvas.drawText("Correo Electrónico: $correo1", 40f, 310f, paintInfoContribuyentes)



            // RECEPTOR
            val receptor = jsonObject.getJSONObject("receptor")
            val nombre2 = receptor.getString("nombre")
            val nit2 = receptor.getString("numDocumento")
            val nrc2 = receptor.getString("nrc")
            val descActividad2 = receptor.getString("descActividad")
            val direccion2 = receptor.getJSONObject("direccion")
            val complemento2 = direccion2.getString("complemento")
            val telefono2 = receptor.getString("telefono")
            val correo2 = receptor.getString("correo")
            // Coordenadas del rectángulo del RECEPTOR
            val emisorLeftReceptor = 321f
            val emisorTopReceptor = 215f
            val emisorRightReceptor = 587f
            val emisorBottomReceptor = 340f
            // Dibujar rectángulo del RECEPTOR
            canvas.drawRoundRect(emisorLeftReceptor, emisorTopReceptor, emisorRightReceptor, emisorBottomReceptor, cornerRadius1, cornerRadius1, paintRect1)
            // Dibujar el texto del RECEPTOR
            canvas.drawText("RECEPTOR", 425f, 230f, paintTITULO) // Dibuja que es la info del Receptor
            canvas.drawText("Nombre o razón social: $nombre2", 336f, 250f, paintInfoContribuyentes)
            canvas.drawText("NIT: $nit2", 336f, 260f, paintInfoContribuyentes)
            canvas.drawText("NRC: $nrc2", 336f, 270f, paintInfoContribuyentes)
            canvas.drawText("Actividad Económica: $descActividad2", 336f, 280f, paintInfoContribuyentes)
            canvas.drawText("Dirección: $complemento2", 336f, 290f, paintInfoContribuyentes)
            canvas.drawText("Número de Teléfono: $telefono2", 336f, 300f, paintInfoContribuyentes)
            canvas.drawText("Correo Electrónico: $correo2", 336f, 310f, paintInfoContribuyentes)



            // CUERPO
            // Tabla de ítems
            val cuerpoDocumento = jsonObject.getJSONArray("cuerpoDocumento")
            val startX = 40f // Posición X de inicio de la tabla
            var startY = 380f // Posición Y de inicio de la tabla
            val rowHeight = 25f // Altura de cada fila de la tabla
            // Dibujar encabezados de la tabla
            canvas.drawText("N°", startX, startY, paintTITULO)
            canvas.drawText("Cantidad", startX + 30, startY, paintTITULO)
            canvas.drawText("Unidad", startX + 80, startY, paintTITULO)
            canvas.drawText("Descripción", startX + 125, startY, paintTITULO)
            canvas.drawText("Precio", startX + 300, startY, paintTITULO)
            canvas.drawText("Unitario", startX + 300, startY + 10, paintTITULO)
            canvas.drawText("Descuento", startX + 350, startY, paintTITULO)
            canvas.drawText("por Item", startX + 350, startY + 10, paintTITULO)
            canvas.drawText("Ventas No", startX + 405, startY, paintTITULO)
            canvas.drawText("Sujetas", startX + 405, startY + 10, paintTITULO)
            canvas.drawText("Ventas", startX + 460, startY, paintTITULO)
            canvas.drawText("Exentas", startX + 460, startY + 10, paintTITULO)
            canvas.drawText("Ventas", startX + 505, startY, paintTITULO)
            canvas.drawText("Gravadas", startX + 505, startY + 10, paintTITULO)
            startY += rowHeight
            // Dibujar cada fila de la tabla
            for (i in 0 until cuerpoDocumento.length()) {
                val item = cuerpoDocumento.getJSONObject(i)
                val numItem = item.getInt("numItem").toString()
                val cantidad = item.getDouble("cantidad").toString()
                val uniMedida = item.getInt("uniMedida").toString()
                val descripcion = item.getString("descripcion")
                val precioUni = item.getDouble("precioUni").toString()
                val montoDescu = item.getDouble("montoDescu").toString()
                val ventaNoSuj = item.getDouble("ventaNoSuj").toString()
                val ventaExenta = item.getDouble("ventaExenta").toString()
                val ventaGravada = item.getDouble("ventaGravada").toString()
                // Dibujar cada celda de la fila
                canvas.drawText(numItem, startX + 2, startY, paintInfoDocumento)
                canvas.drawText(cantidad, startX + 33, startY, paintInfoDocumento)
                canvas.drawText(uniMedida, startX + 86, startY, paintInfoDocumento)
                canvas.drawText(descripcion, startX + 128, startY, paintInfoDocumento)
                canvas.drawText("$$precioUni", startX + 301, startY, paintInfoDocumento)
                canvas.drawText("$$montoDescu", startX + 359, startY, paintInfoDocumento)
                canvas.drawText("$$ventaNoSuj", startX + 415, startY, paintInfoDocumento)
                canvas.drawText("$$ventaExenta", startX + 469, startY, paintInfoDocumento)
                canvas.drawText("$$ventaGravada", startX + 506, startY, paintInfoDocumento)
                // Mover al siguiente renglón
                startY += rowHeight
            }



            // RESUMEN
            // Info sobre todas las ventas que se realizaron
            canvas.drawText("SUMA DE VENTAS:", startX + 325, startY, paintTITULO)
            val resumen = jsonObject.getJSONObject("resumen")
            val totalNoSuj = resumen.getString("totalNoSuj")
            val totalExenta = resumen.getString("totalExenta")
            val totalGravada = resumen.getString("totalGravada")
            canvas.drawText("$$totalNoSuj", startX + 414, startY, paintInfoDocumento)
            canvas.drawText("$$totalExenta", startX + 468, startY, paintInfoDocumento)
            canvas.drawText("$$totalGravada", startX + 509, startY, paintInfoDocumento)

            // LLama a los demás datos de DETALLES

            // Para mover a la derecha: Incrementa finalPosition (por ejemplo, startX + 500).
            // Para mover a la izquierda: Decrementa finalPosition (por ejemplo, startX + 350).
            val finalPosition1 = startX + 495
            val finalPosition2 = startX + 530
            // Info de Suma Total de Operaciones con su respectivo monto
            val subTotalVentas = resumen.getString("subTotalVentas")
            canvas.drawText("Suma Total de Operaciones:", finalPosition1 - paintTITULO.measureText("Suma Total de Operaciones:"), startY + 15, paintTITULO)
            canvas.drawText("$$subTotalVentas", finalPosition2 - paintTITULO.measureText(subTotalVentas), startY + 15, paintInfoDocumento)

            // Info de Descuento para Ventas no sujetas con su respectivo monto
            val descuNoSuj = resumen.getString("descuNoSuj")
            canvas.drawText("Monto global Desc., Rebajas y otros a ventas no sujetas:", finalPosition1 - paintTITULO.measureText("Monto global Desc., Rebajas y otros a ventas no sujetas:"), startY + 26, paintTITULO)
            canvas.drawText("$$descuNoSuj", finalPosition2 - paintTITULO.measureText(descuNoSuj), startY + 26, paintInfoDocumento)

            // Info de Descuento para Ventas exentas con su respectivo monto
            val descuExenta = resumen.getString("descuExenta")
            canvas.drawText("Monto global Desc., Rebajas y otros a ventas exentas:", finalPosition1 - paintTITULO.measureText("Monto global Desc., Rebajas y otros a ventas exentas:"), startY + 37, paintTITULO)
            canvas.drawText("$$descuExenta", finalPosition2 - paintTITULO.measureText(descuExenta), startY + 37, paintInfoDocumento)

            // Info de Descuento para Ventas gravadas con su respectivo monto
            val descuGravada = resumen.getString("descuGravada")
            canvas.drawText("Monto global Desc., Rebajas y otros a ventas gravadas:", finalPosition1 - paintTITULO.measureText("Monto global Desc., Rebajas y otros a ventas gravadas:"), startY + 48, paintTITULO)
            canvas.drawText("$$descuGravada", finalPosition2 - paintTITULO.measureText(descuGravada), startY + 48, paintInfoDocumento)

            // Obtener el array de tributos
            val tributos: JSONArray? = if (resumen.isNull("tributos")) null else resumen.getJSONArray("tributos")



            // Variables para almacenar la descripción y el valor
            var descripcion20 = ""
            var valor20 = 0.0
            // Buscar y almacenar la descripción y el valor del tributo con código "20"
            if (tributos != null) {
                for (i in 0 until tributos.length()) {
                    val tributo = tributos.getJSONObject(i)
                    if (tributo.getString("codigo") == "20") {
                        descripcion20 = tributo.getString("descripcion")
                        valor20 = tributo.getDouble("valor")
                        break  // Suponiendo que solo hay un tributo con código "20"
                    }
                }
            }
            // Dibuja lo que es el monto de IVA (13%) sobre el total de la venta
            canvas.drawText(descripcion20, finalPosition1 - paintTITULO.measureText(descripcion20), startY + 59, paintTITULO)
            canvas.drawText("$$valor20", finalPosition2 - paintTITULO.measureText(valor20.toString()), startY + 59, paintInfoDocumento)

            // Muesta Info sobre el Sub-Total
            val subTotal = resumen.getString("subTotal")
            canvas.drawText("Sub-Total:", finalPosition1 - paintTITULO.measureText("Sub-Total:"), startY + 70, paintTITULO)
            canvas.drawText("$$subTotal", finalPosition2 - paintTITULO.measureText(subTotal), startY + 70, paintInfoDocumento)

            // Muesta Info sobre el IVA Percibido
            /*val ivaPerci1 = resumen.getString("ivaPerci1")
            canvas.drawText("IVA Percibido:", finalPosition1 - paintTITULO.measureText("IVA Percibido:"), startY + 81, paintTITULO)
            canvas.drawText("$$ivaPerci1", finalPosition2 - paintTITULO.measureText(ivaPerci1), startY + 81, paintInfoDocumento)*/

            // Muesta Info sobre el IVA Retenido
            val ivaRete1 = resumen.getString("ivaRete1")
            canvas.drawText("IVA Retenido:", finalPosition1 - paintTITULO.measureText("IVA Retenido:"), startY + 92, paintTITULO)
            canvas.drawText("$$ivaRete1", finalPosition2 - paintTITULO.measureText(ivaRete1), startY + 92, paintInfoDocumento)

            // Muesta Info sobre Retención Renta
            val reteRenta = resumen.getString("reteRenta")
            canvas.drawText("Retención Renta:", finalPosition1 - paintTITULO.measureText("Retención Renta:"), startY + 103, paintTITULO)
            canvas.drawText("$$reteRenta", finalPosition2 - paintTITULO.measureText(reteRenta), startY + 103, paintInfoDocumento)

            // Muesta Info sobre el Monto Total de la Operación
            val montoTotalOperacion = resumen.getString("montoTotalOperacion")
            canvas.drawText("Monto Total de la Operación:", finalPosition1 - paintTITULO.measureText("Monto Total de la Operación:"), startY + 114, paintTITULO)
            canvas.drawText("$$montoTotalOperacion", finalPosition2 - paintTITULO.measureText(montoTotalOperacion), startY + 114, paintInfoDocumento)

            // Muesta Info sobre Otros montos posibles no afectos
            val totalNoGravado = resumen.getString("totalNoGravado")
            canvas.drawText("Total Otros montos no afectos:", finalPosition1 - paintTITULO.measureText("Total Otros montos no afectos:"), startY + 125, paintTITULO)
            canvas.drawText("$$totalNoGravado", finalPosition2 - paintTITULO.measureText(totalNoGravado), startY + 125, paintInfoDocumento)

            // Muesta Info sobre el Total a Pagar
            val totalPagar = resumen.getString("totalPagar")
            canvas.drawText("Total a Pagar:", finalPosition1 - paintTITULO.measureText("Total a Pagar:"), startY + 136, paintTITULO)
            canvas.drawText("$$totalPagar", finalPosition2 - paintTITULO.measureText(totalPagar), startY + 136, paintInfoDocumento)

            // Principal 1 - Coordenadas del rectángulo que encierra valor en letras y demás información
            val rectanguloLeft1 = startX - 10
            val rectanguloTop1 = startY - 14
            val rectanguloRight1 = 315f
            val rectanguloBottom1 = startY + 145
            // Dibujar rectángulo
            canvas.drawRect(rectanguloLeft1, rectanguloTop1, rectanguloRight1, rectanguloBottom1, paintRect1)

            // Secundario 1.1 - Encierra valor en letras con tipo de condición de operación
            val rectanguloLeft11 = startX - 10
            val rectanguloTop11 = startY - 14
            val rectanguloRight11 = 315f
            val rectanguloBottom11 = startY + 31
            // Dibujar rectángulo
            canvas.drawRect(rectanguloLeft11, rectanguloTop11, rectanguloRight11, rectanguloBottom11, paintRect2)

            // Secundario 1.2 - Encierra Emisor Responsable
            val rectanguloLeft12 = startX - 10
            val rectanguloTop12 = startY + 48
            val rectanguloRight12 = 315f
            val rectanguloBottom12 = startY + 78
            // Dibujar rectángulo
            canvas.drawRect(rectanguloLeft12, rectanguloTop12, rectanguloRight12, rectanguloBottom12, paintRect2)

            // Secundario 1.3 - Encierra Receptor Responsable
            val rectanguloLeft13 = startX - 10
            val rectanguloTop13 = startY + 78
            val rectanguloRight13 = 315f
            val rectanguloBottom13 = startY + 108
            // Dibujar rectángulo
            canvas.drawRect(rectanguloLeft13, rectanguloTop13, rectanguloRight13, rectanguloBottom13, paintRect2)

            // Secundario 1.3 - Encierra Receptor Responsable
            val rectanguloLeft14 = startX - 10
            val rectanguloTop14 = startY + 78
            val rectanguloRight14 = 315f
            val rectanguloBottom14 = startY + 108
            // Dibujar rectángulo
            canvas.drawRect(rectanguloLeft14, rectanguloTop14, rectanguloRight14, rectanguloBottom13, paintRect2)

            // Coordenadas del rectángulo que encierra info de las Ventas
            val rectanguloLeft2 = 315f
            val rectanguloTop2 = startY - 14
            val rectanguloRight2 = 585f
            val rectanguloBottom2 = startY + 145
            // Dibujar rectángulo
            canvas.drawRect(rectanguloLeft2, rectanguloTop2, rectanguloRight2, rectanguloBottom2, paintRect1)

            // Muestra Info de Valor en Letras
            val totalLetras = resumen.getString("totalLetras")
            canvas.drawText("Valor en Letras: $totalLetras", startX, startY + 5, paintTITULO)

            // Muestra Info de Condición de la Operación
            val condicionOperacion = resumen.getInt("condicionOperacion")
            if (condicionOperacion == 1) {
                // Si la operación fue al Contado
                canvas.drawText("Condición de la Operación: $condicionOperacion - Contado", startX, startY + 20, paintTITULO)
            } else if (condicionOperacion == 2) {
                // Si la operación fue al Crédito
                canvas.drawText("Condición de la Operación: $condicionOperacion - A crédito", startX, startY + 20, paintTITULO)
            } else if (condicionOperacion == 3){
                // Si la operación fue otra
                canvas.drawText("Condición de la Operación: $condicionOperacion - Otro", startX, startY + 20, paintTITULO)
            }



            // EXTENSIÓN
            // Muestra información extra que requiere hacienda
            canvas.drawText("EXTENSIÓN", startX + 125, startY + 42, paintTITULO)

            canvas.drawText("Emisor Responsable:", startX, startY + 60, paintTITULO)
            canvas.drawText("No. documento:", startX, startY + 70, paintTITULO)

            canvas.drawText("Receptor Responsable:", startX, startY + 90, paintTITULO)
            canvas.drawText("No. documento:", startX, startY + 100, paintTITULO)

            canvas.drawText("Observaciones:", startX, startY + 120, paintTITULO)



            // Agregar el número de página
            canvas.drawText("Página 1 de 1", 550f, 775f, paintInfoDocumento)



            // Valida si hubo algún problema para poder validar el archivo json
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error al procesar el JSON: ${e.message}", Toast.LENGTH_LONG).show()
        }

        // Aquí dejan de generarse páginas del pdf
        pdfDocument.finishPage(pagina1)
/*
        // Accede al directorio de descargas del dispositivo, ya sea virtual o físico
        // El acceso lo hace através del directorio de la descargas
        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        // Con ese nombre se le va a guardar el PDF
        val outputFilePath = File(downloadsDir, "Comprobante de Consumidor Final.pdf")

        // Valida si el PDF no tuvo errores para generarse
        try {
            pdfDocument.writeTo(FileOutputStream(outputFilePath))
            Toast.makeText(this, "Se creó el PDF correctamente en: ${outputFilePath.absolutePath}", Toast.LENGTH_LONG).show()

            val intentCF = Intent(applicationContext, VerPdfCF::class.java)
            startActivity(intentCF)

        } catch (e: Exception) {
            // En caso de que los haya habido muestra un mensaje
            e.printStackTrace()
            Toast.makeText(this, "Error al crear el PDF: ${e.message}", Toast.LENGTH_LONG).show()
        }*/

        // Aquí finaliza la generación del documento pdf
       // pdfDocument.close()
        return pdfDocument

    }


    // Función para poder leer el archivo json
    private fun leerJsonDesdeAssets(fileName: String): String {
        var json = ""
        try {
            val inputStream: InputStream = assets.open(fileName)
            json = inputStream.bufferedReader().use { it.readText() }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return json
    }

    // Función para verificar si los permisos están otorgados dentro del dispositivo
    private fun checkPermission(): Boolean {
        // El permiso de escritura
        val permission1 = ContextCompat.checkSelfPermission(applicationContext,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        // El permiso de lectura
        val permission2 = ContextCompat.checkSelfPermission(applicationContext,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )
        return permission1 == PackageManager.PERMISSION_GRANTED && permission2 == PackageManager.PERMISSION_GRANTED
    }

    // Función para pedirle los permisos al usuario
    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this,
            // Pide permisos de escritura y lectura
            arrayOf(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ),
            200
        )
    }
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun contarDocumentosConfEmisor() {
        val query = QueryBuilder.select(SelectResult.all())
            .from(DataSource.database(database))
            .where(Expression.property("tipo").equalTo(Expression.string("DUI")))

        try {
            val result = query.execute()
            val count = result.allResults().size
            Log.d("ReClienteActivity", "Número de documentos de tipo 'ConfEmisor': $count")
        } catch (e: CouchbaseLiteException) {
            Log.e("ReClienteActivity", "Error al contar los documentos de tipo 'ConfEmisor': ${e.message}", e)
        }
    }
    private fun isTablet(): Boolean {
        return (resources.configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE
    }

}