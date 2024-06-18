package com.billsv.facturaelectronica

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.graphics.pdf.PdfRenderer
import android.os.Environment
import android.os.ParcelFileDescriptor
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import com.billsv.facturaelectronica.databinding.ActivityPdfCfactivityBinding
import com.couchbase.lite.CouchbaseLiteException
import com.couchbase.lite.DataSource
import com.couchbase.lite.Expression
import com.couchbase.lite.MutableDocument
import com.couchbase.lite.QueryBuilder
import com.couchbase.lite.SelectResult
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.FileWriter
import java.io.IOException
import java.io.InputStream
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.UUID
import com.pdfview.PDFView

class PDF_CFActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPdfCfactivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityPdfCfactivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.Generar.setOnClickListener {
            val dialogoGenerar = Dialog(this)
            dialogoGenerar.setContentView(R.layout.layout_generar) // R.layout.layout_custom_dialog es tu diseño personalizado
            val width = (resources.displayMetrics.widthPixels * 0.9).toInt() // 80% del ancho de la pantalla
            val height = (resources.displayMetrics.heightPixels * 0.45).toInt() // 60% del alto de la pantalla
            dialogoGenerar.window?.setLayout(width, height)
            dialogoGenerar.setCanceledOnTouchOutside(false)
            val btnYes = dialogoGenerar.findViewById<Button>(R.id.btnYes)
            val btnNo = dialogoGenerar.findViewById<Button>(R.id.btnNo)
            btnYes.setOnClickListener {
                json()
            }

            btnNo.setOnClickListener {
                // Acción al hacer clic en el botón "Cancelar"
                dialogoGenerar.dismiss()
            }

            dialogoGenerar.show()
        }
        // Crear y guardar el PDF en un archivo temporal
        val pdfDocument = generarPdf()
        val pdfFile = savePdfToCache(this, pdfDocument)

        // Mostrar el PDF utilizando PDFView
        binding.VistaPdf.fromFile(pdfFile)
        binding.VistaPdf.isZoomEnabled = true
        binding.VistaPdf.show()



    }
    override fun onBackPressed() {
        super.onBackPressed() // Llama al método onBackPressed() de la clase base
        val intent = Intent(this, EmitirCFActivity::class.java)
        startActivity(intent)
        finish()
    }
    private fun json(){
        var dui: String? = null
        var nombre: String? = null
        var telefono: String? = null
        var departamento: String? = null
        var municipio: String? = null
        var complemento: String? = null
        var correo: String? = null
        var nrc: String? = null
        var nit: String? = null
        var codAcEco: String? = null
        var desAcEco: String? = null

        val datosGuardados = intent.getStringExtra("Cliente")
        if (datosGuardados != null) {
            if(datosGuardados.isNotEmpty()) {
                datosGuardados.let {
                    val datos = it.split("\n")
                    dui = datos[8]
                    nombre = datos[0]
                    telefono = datos[6]
                    departamento = datos[4]
                    municipio = datos[5]
                    complemento = datos[3]
                    correo = datos[2]
                    nit = datos[1]
                    if(datos[9]=="null"){
                        nrc = null
                        codAcEco = null
                        desAcEco = null
                    }else{
                        nrc = datos[9]
                        codAcEco = datos[10]
                        desAcEco = datos[10]
                    }
                }
            }
        }
        var duiE: String? = null
        var nombreE: String? = null
        var nombrecE: String? = null
        var telefonoE: String? = null
        var departamentoE: String? = null
        var municipioE: String? = null
        var complementoE: String? = null
        var correoE: String? = null
        var nrcE: String? = null
        var nitE: String? = null
        var codAcEcoE: String? = null
        var desAcEcoE: String? = null

        val infoemisor = obtenerEmisor()
        if (infoemisor.isNotEmpty()) {
            infoemisor.forEach{ data->
                val datos = data.split("\n")
                // "$nombre\n$nombrec\n$nit\n$nrc\n$AcEco\n$direccion\n$telefono\n$correo\n$dui\n$departamento\n$municipio"
                duiE = datos[8]
                nombreE = datos[0]
                nombrecE = datos[1]
                telefonoE = datos[6]
                departamentoE = datos[9]
                municipioE = datos[10]
                complementoE = datos[5]
                correoE = datos[7]
                nitE = datos[2].replace("-","")
                nrcE = datos[3]
                codAcEcoE = datos[4]
                desAcEcoE = datos[4]

            }
        }
        var fecEmi: String? = null
        var horEmi: String? = null
        val numeroContol = intent.getStringExtra("numeroControl")
        val codigoGeneracion = intent.getStringExtra("codGeneracion")
        val totalNoSujeto = intent.getStringExtra("totalNoSuj")?.toDouble()
        val totalExenta= intent.getStringExtra("totalExenta")?.toDouble()
        val totalGravada= intent.getStringExtra("totalGravada")?.toDouble()
        val total= intent.getStringExtra("total")?.toDouble()
        val totalIva= intent.getStringExtra("totalIva")?.toDouble()
        val condicionOperacion= intent.getStringExtra("condicionOperacion")?.toInt()
        val fechayHora = FyH_emicion()
        if (fechayHora.isNotEmpty()) {
            fechayHora.let {
                val datos = it.split("\n")
                fecEmi = datos[0]
                horEmi = datos[1]
            }
        }
        val documento = Documento(
            identificacion = Identificacion(
                version = 1,
                ambiente = "00",
                tipoDte = "01",
                numeroControl = numeroContol,
                codigoGeneracion = codigoGeneracion,
                tipoModelo = 1,
                tipoOperacion = 1,
                tipoContingencia = null,
                motivoContin = null,
                fecEmi = fecEmi,
                horEmi = horEmi,
                tipoMoneda = "USD"
            ),
            documentoRelacionado = null,
            emisor = Emisor(
                nit = nitE,
                nrc = nrcE,
                nombre = nombreE,
                codActividad = codAcEcoE,
                descActividad = desAcEcoE,
                nombreComercial = nombrecE,
                tipoEstablecimiento = "Oficina",
                direccion = Direccion(
                    departamento = departamentoE,
                    municipio = municipioE,
                    complemento = complementoE
                ),
                telefono = telefonoE,
                correo = correoE,
                codEstableMH = null,
                codEstable = "1",
                codPuntoVentaMH = null,
                codPuntoVenta = "1"
            ),
            receptor = Receptor(
                tipoDocumento = "13",
                numDocumento = dui,
                nrc = nrc,
                nombre = nombre,
                codActividad = codAcEco,
                descActividad = desAcEco,
                telefono = telefono,
                direccion = Direccion(
                    departamento = departamento,
                    municipio = municipio,
                    complemento = complemento
                ),
                correo = correo
            ),
            otrosDocumentos = null,
            ventaTercero = null,
            cuerpoDocumento = emptyList(),
            resumen = Resumen(
                totalIva = totalIva,
                porcentajeDescuento = 0.0,
                ivaRete1 = 0.0,
                reteRenta = 0.0,
                totalNoGravado = 0.0,
                totalPagar = total,
                saldoFavor = 0.0,
                condicionOperacion = condicionOperacion,
                pagos = listOf(
                    Pago(
                        codigo = "01",
                        montoPago = 22.6,
                        referencia = "Pago en efectivo",
                        plazo = "Inmediato",
                        periodo = 0
                    )
                ),
                numPagoElectronico = "0001",
                totalNoSuj = totalNoSujeto,
                totalExenta = totalExenta,
                totalGravada = totalGravada,
                subTotalVentas = total,
                descuNoSuj = 0.0,
                descuExenta = 0.0,
                descuGravada = 0.0,
                totalDescu = 0.0,
                tributos = null,
                subTotal = total,
                montoTotalOperacion = total,
                totalLetras = precioEnLetras(total)
            ),
            extension = Extension(
                placaVehiculo = null,
                docuEntrega = null,
                nombEntrega = null,
                docuRecibe = null,
                nombRecibe = null,
                observaciones = null
            ),
            apendice = null,
            selloRecibido = "Sello de recibido",
            firmaElectronica = "Firma electrónica"
        )
        val articulos = obtenerDatosGuardados()
        val cuerpoDocumentos = createCuerpoDocumento(articulos)

        documento.cuerpoDocumento = cuerpoDocumentos

        // Crear una instancia de ObjectMapper
        val mapper = ObjectMapper().registerModule(KotlinModule())
        mapper.configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true)

        // Convertir la instancia de Documento a JSON
        val json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(documento)

        saveJsonToExternalStorage(json,numeroContol)
    }
    private fun FyH_emicion(): String{
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        // Formatea la fecha y la hora
        val formattedDate = dateFormat.format(calendar.time)
        val formattedTime = timeFormat.format(calendar.time)
        val tiempo = "$formattedDate\n$formattedTime"
        return tiempo
    }
    private fun saveJsonToExternalStorage(jsonData: String,numeroControl:String?) {
        val fileName = "$numeroControl.json"
        val directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val file = File(directory, fileName)

        try {
            FileWriter(file).use {
                it.write(jsonData)
            }
            Toast.makeText(this, "Archivo JSON guardado en: ${file.absolutePath}", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error al guardar el archivo JSON", Toast.LENGTH_SHORT).show()
        }
    }
    private fun obtenerEmisor(): List<String> {
        // Obtén la instancia de la base de datos desde la aplicación
        val app = application as MyApp
        val database = app.database

        // Crea una consulta para seleccionar todos los documentos con tipo = "cliente"
        val query = QueryBuilder.select(SelectResult.all())
            .from(DataSource.database(database))
            .where(Expression.property("tipo").equalTo(Expression.string("ConfEmisor")))

        // Ejecuta la consulta
        val result = query.execute()

        // Lista para almacenar los datos obtenidos
        val dataList = mutableListOf<String>()

        // Itera sobre todos los resultados de la consulta
        result.allResults().forEach { results ->
            // Obtiene el diccionario del documento del resultado actual
            val dict = results.getDictionary(database.name)

            // Extrae los valores de los campos del documento
            val nombre = dict?.getString("nombre")
            val nombrec = dict?.getString("nombreC")
            val dui = dict?.getString("dui")
            val nit = dict?.getString("nit")
            val nrc = dict?.getString("nrc")
            val AcEco = dict?.getString("ActividadEco")
            val departamento = dict?.getString("departamento")
            val municipio = dict?.getString("municipio")
            val direccion = dict?.getString("direccion")
            val telefono = dict?.getString("telefono")
            val correo = dict?.getString("correo")

            // Formatea los datos como una cadena y la agrega a la lista
            val dataString = "$nombre\n$nombrec\n$nit\n$nrc\n$AcEco\n$direccion\n$telefono\n$correo\n$dui\n$departamento\n$municipio"
            dataList.add(dataString)
        }

        // Devuelve la lista de datos
        return dataList
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
            val codigoP = dict?.getString("codigoP")
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
    fun createCuerpoDocumento(dataList: List<String>): List<CuerpoDocumento> {
        return dataList.mapIndexed { index, dataString ->
            val data = dataString.split("\n")
            CuerpoDocumento(
                ivaItem = data[9].toDouble(),
                psv = data[13].toDouble(),
                noGravado = data[14].toDouble(),
                numItem = index + 1,
                tipoItem = data[0].toInt(),
                numeroDocumento = null,
                cantidad = data[1].toDouble(),
                codigo = data[10],
                codTributo = null,
                uniMedida = data[2].toInt(),
                descripcion = data[3],
                precioUni = data[5].toDouble(),
                montoDescu = 0.0,
                ventaNoSuj = data[8].toDouble(),
                ventaExenta = data[7].toDouble(),
                ventaGravada = data[6].toDouble(),
                tributos = null
            )
        }
    }
    fun numeroALetras(numero: Int): String {
        val unidades = arrayOf("", "uno", "dos", "tres", "cuatro", "cinco", "seis", "siete", "ocho", "nueve")
        val decenas = arrayOf("", "diez", "veinte", "treinta", "cuarenta", "cincuenta", "sesenta", "setenta", "ochenta", "noventa")
        val especiales = arrayOf("once", "doce", "trece", "catorce", "quince", "dieciséis", "diecisiete", "dieciocho", "diecinueve")

        return when {
            numero == 0 -> "cero"
            numero < 10 -> unidades[numero]
            numero in 11..19 -> especiales[numero - 11]
            numero < 100 -> {
                val decena = numero / 10
                val unidad = numero % 10
                if (unidad == 0) decenas[decena] else "${decenas[decena]} y ${unidades[unidad]}"
            }
            numero < 1000 -> {
                val centena = numero / 100
                val resto = numero % 100
                val centenaStr = when (centena) {
                    1 -> if (resto == 0) "cien" else "ciento"
                    5 -> "quinientos"
                    7 -> "setecientos"
                    9 -> "novecientos"
                    else -> "${unidades[centena]}cientos"
                }
                if (resto == 0) centenaStr else "$centenaStr ${numeroALetras(resto)}"
            }
            numero < 1000000 -> {
                val miles = numero / 1000
                val resto = numero % 1000
                val milesStr = if (miles == 1) "mil" else "${numeroALetras(miles)} mil"
                if (resto == 0) milesStr else "$milesStr ${numeroALetras(resto)}"
            }
            else -> throw IllegalArgumentException("Número fuera de rango")
        }
    }

    fun precioEnLetras(precio: Double?): String {
        if (precio == null) {
            return "Precio no especificado"
        }

        val parteEntera = precio.toInt()
        val parteDecimal = (precio * 100 % 100).toInt()

        val letrasEntera = if (parteEntera == 1) "un dólar" else "${numeroALetras(parteEntera)} dólares"
        val letrasDecimal = when (parteDecimal) {
            0 -> ""
            1 -> "con un centavo"
            else -> "con ${numeroALetras(parteDecimal)} centavos"
        }

        return "$letrasEntera $letrasDecimal".trim()
    }
    private fun generarPdf():PdfDocument {
        // Variable para poder almacenar el contenido del json através de una función
        //val jsonData = leerJsonDesdeAssets("DTE-01-OFIC0001-000000000000012.json")

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

            // Aquí se empieza a generar el PDF en base a toda la info

            // Dibujar el Encabezado
            canvas.drawText("DOCUMENTO TRIBUTARIO ELECTRÓNICO", 210f, 25f, paintEncabezado)
            canvas.drawText("COMPROBANTE DE CONSUMIDOR FINAL", 220f, 40f, paintEncabezado)

            // IDENTIFICACIÓN
            /*   Lado izquierdo   */
            val codigoGeneracion = intent.getStringExtra("codGeneracion")
            val numeroControl = intent.getStringExtra("numeroControl")
            // Dibujar el texto en el PDF
            // Info Código de Generación
            canvas.drawText("Código de Generación: $codigoGeneracion", 25f, 175f, paintInfoDocumento)
            // Info Número de Control
            canvas.drawText("Número de Control: $numeroControl", 25f, 185f, paintInfoDocumento)
            /*   Lado derecho   */
            val tipoModelo = 1
            val tipoOperacion = 1
            val fecEmi = ""
            val horEmi = ""
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

            var duiE: String? = null
            var nombreE: String? = null
            var nombrecE: String? = null
            var telefonoE: String? = null
            var departamentoE: String? = null
            var municipioE: String? = null
            var complementoE: String? = null
            var correoE: String? = null
            var nrcE: String? = null
            var nitE: String? = null
            var codAcEcoE: String? = null
            var desAcEcoE: String? = null

            val infoemisor = obtenerEmisor()
            if (infoemisor.isNotEmpty()) {
                infoemisor.forEach{ data->
                    val datos = data.split("\n")
                    // "$nombre\n$nombrec\n$nit\n$nrc\n$AcEco\n$direccion\n$telefono\n$correo\n$dui\n$departamento\n$municipio"
                    duiE = datos[8]
                    nombreE = datos[0]
                    nombrecE = datos[1]
                    telefonoE = datos[6]
                    departamentoE = datos[9]
                    municipioE = datos[10]
                    complementoE = datos[5]
                    correoE = datos[7]
                    nitE = datos[2].replace("-","")
                    nrcE = datos[3]
                    codAcEcoE = datos[4]
                    desAcEcoE = datos[4]

                }
            }
            // EMISOR
            val nombre1 = nombreE
            val nit1 = nitE
            val nrc1 = nrcE
            val descActividad1 = desAcEcoE
            val municipio1 = municipioE
            val departamento1 = departamentoE
            val complemento1 = complementoE
            val telefono1 = telefonoE
            val correo1 = correoE
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
            canvas.drawText("Municipio: $municipio1", 40f, 290f, paintInfoContribuyentes)
            canvas.drawText("Departamento: $departamento1", 40f, 300f, paintInfoContribuyentes)
            canvas.drawText("Dirección: $complemento1", 40f, 310f, paintInfoContribuyentes)
            canvas.drawText("Número de Teléfono: $telefono1", 40f, 320f, paintInfoContribuyentes)
            canvas.drawText("Correo Electrónico: $correo1", 40f, 330f, paintInfoContribuyentes)



            var dui: String? = null
            var nombre: String? = null
            var telefono: String? = null
            var departamento: String? = null
            var municipio: String? = null
            var complemento: String? = null
            var correo: String? = null
            var nrc: String? = null
            var nit: String? = null
            var codAcEco: String? = null
            var desAcEco: String? = null

            val datosGuardados = intent.getStringExtra("Cliente")
            if (datosGuardados != null) {
                if(datosGuardados.isNotEmpty()) {
                    datosGuardados.let {
                        val datos = it.split("\n")
                        dui = datos[8]
                        nombre = datos[0]
                        telefono = datos[6]
                        departamento = datos[4]
                        municipio = datos[5]
                        complemento = datos[3]
                        correo = datos[2]
                        nit = datos[1]
                        if(datos[9]=="null"){
                            nrc = null
                            codAcEco = null
                            desAcEco = null
                        }else{
                            nrc = datos[9]
                            codAcEco = datos[10]
                            desAcEco = datos[10]
                        }
                    }
                }
            }
            // RECEPTOR
            val nombre2 = nombre
            val nit2 = nit
            val nrc2 = nrc
            val descActividad2 = desAcEco
            val municipio2 = municipio
            val departamento2 = departamento
            val complemento2 = complemento
            val telefono2 = telefono
            val correo2 = correo
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
            canvas.drawText("Municipio: $municipio2", 336f, 290f, paintInfoContribuyentes)
            canvas.drawText("Departamento: $departamento2", 336f, 300f, paintInfoContribuyentes)
            canvas.drawText("Dirección: $complemento2", 336f, 310f, paintInfoContribuyentes)
            canvas.drawText("Número de Teléfono: $telefono2", 336f, 320f, paintInfoContribuyentes)
            canvas.drawText("Correo Electrónico: $correo2", 336f, 330f, paintInfoContribuyentes)



            // CUERPO
            // Tabla de ítems

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

            val app = application as MyApp
            val database = app.database

            // Crea una consulta para seleccionar todos los documentos con tipo = "cliente"
            val query = QueryBuilder.select(SelectResult.all())
                .from(DataSource.database(database))
                .where(Expression.property("tipo").equalTo(Expression.string("Articulocf")))

            // Ejecuta la consulta
            val result = query.execute()