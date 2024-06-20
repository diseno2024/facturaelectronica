package com.billsv.facturaelectronica

import android.app.Dialog
import android.content.ContentResolver
import android.content.Context
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
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
import androidx.core.net.toUri
import com.billsv.facturaelectronica.databinding.ActivityPdfCfactivityBinding
import com.couchbase.lite.CouchbaseLiteException
import com.couchbase.lite.DataSource
import com.couchbase.lite.Expression
import com.couchbase.lite.Meta
import com.couchbase.lite.MutableDocument
import com.couchbase.lite.QueryBuilder
import com.couchbase.lite.SelectResult
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
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
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PDF_CFActivity : AppCompatActivity() {
    private lateinit var apiService: ApiService
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
        val TIPO = intent.getStringExtra("JSON")
        if(TIPO=="Factura"){
            binding.Generar.text = "Generar Factura"
        }else{
            binding.Generar.text = "Generar Crédito Fiscal"
        }
        binding.Generar.setOnClickListener {
            val dialogoGenerar = Dialog(this)
            if(TIPO=="Factura"){
                dialogoGenerar.setContentView(R.layout.layout_generar) // R.layout.layout_custom_dialog es tu diseño personalizado
            }else{
                dialogoGenerar.setContentView(R.layout.layout_generar2) // R.layout.layout_custom_dialog es tu diseño personalizado
            }
            val width = (resources.displayMetrics.widthPixels * 0.9).toInt() // 80% del ancho de la pantalla
            val height = (resources.displayMetrics.heightPixels * 0.45).toInt() // 60% del alto de la pantalla
            dialogoGenerar.window?.setLayout(width, height)
            dialogoGenerar.setCanceledOnTouchOutside(false)
            val btnYes = dialogoGenerar.findViewById<Button>(R.id.btnYes)
            val btnNo = dialogoGenerar.findViewById<Button>(R.id.btnNo)
            btnYes.setOnClickListener {
                val JSON = intent.getStringExtra("JSON")
                if(JSON!=null) {
                    json()
                    guardarDatosF(JSON)
                    borrararticulos(JSON)
                    borrarDui(JSON)
                    borrarClienteTemporal()
                    borrarNCCG(JSON)
                    if(autentificar()){
                        val credenciales = obtenerUsuarioAPI()
                        var usuarioapi = ""
                        var contraseñaapi = ""
                        credenciales.forEach { data ->
                            val datosapi = data.split("\n")
                            usuarioapi = datosapi[0]
                            contraseñaapi = datosapi[1]
                            if(usuarioapi!="") {
                                apiService = RetrofitClient.instance.create(ApiService::class.java)
                                apiService = RetrofitClient2.instance.create(ApiService::class.java)
                                val authRequest = AuthRequest(usuarioapi, contraseñaapi)

                                Log.d("API_REQUEST", "Enviando solicitud a la API")

                                apiService.authenticate(authRequest).enqueue(object : Callback<AuthResponse> {
                                    override fun onResponse(call: Call<AuthResponse>, response: Response<AuthResponse>) {
                                        if (response.isSuccessful) {
                                            response.body()?.let { authResponse ->
                                                if (authResponse.status == "OK") {
                                                    val authBody = authResponse.body
                                                    Toast.makeText(this@PDF_CFActivity, "Token: ${authBody?.token}", Toast.LENGTH_LONG).show()
                                                    Log.d("API_RESPONSE", "Token: ${authBody?.token}")
                                                } else {
                                                    handleErrorResponse(response)
                                                }
                                            }
                                        } else {
                                            handleErrorResponse(response)
                                        }
                                    }

                                    override fun onFailure(call: Call<AuthResponse>, t: Throwable) {
                                        Toast.makeText(this@PDF_CFActivity, "Error: ${t.message}", Toast.LENGTH_LONG).show()
                                        Log.e("API_ERROR", "Error: ${t.message}", t)
                                    }
                                })
                            }
                        }
                    }
                }
                dialogoGenerar.dismiss()
                val intent = Intent(this, EmitirCFActivity::class.java)
                startActivity(intent)
                finish()
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

    private fun autentificar(): Boolean {
        return false
    }

    private fun obtenerUsuarioAPI(): List<String> {
        // Obtén la instancia de la base de datos desde la aplicación
        val app = application as MyApp
        val database = app.database

        // Crea una consulta para seleccionar todos los documentos con tipo = "cliente"
        val query = QueryBuilder.select(SelectResult.all())
            .from(DataSource.database(database))
            .where(Expression.property("tipo").equalTo(Expression.string("Autentificacion")))

        // Ejecuta la consulta
        val result = query.execute()

        // Lista para almacenar los datos obtenidos
        val dataList = mutableListOf<String>()

        // Itera sobre todos los resultados de la consulta
        result.allResults().forEach { result ->
            // Obtiene el diccionario del documento del resultado actual
            val dict = result.getDictionary(database.name)

            // Extrae los valores de los campos del documento
            val usuario = dict?.getString("usuario")
            val contraseña = dict?.getString("contraseña")

            // Formatea los datos como una cadena y la agrega a la lista
            val dataString = "$usuario\n$contraseña"
            dataList.add(dataString)
        }

        // Devuelve la lista de datos
        return dataList
    }

    private fun handleErrorResponse(response: Response<AuthResponse>) {
        try {
            val errorResponse = Gson().fromJson(response.errorBody()?.charStream(), ErrorResponse::class.java)
            Toast.makeText(this, "Error: ${errorResponse.message}", Toast.LENGTH_LONG).show()
            Log.e("API_ERROR_RESPONSE", "Error: ${errorResponse.message}")
        } catch (e: JsonSyntaxException) {
            Toast.makeText(this, "Error desconocido", Toast.LENGTH_LONG).show()
            Log.e("API_ERROR_RESPONSE", "Error desconocido", e)
        }
    }
    override fun onBackPressed() {
        super.onBackPressed() // Llama al método onBackPressed() de la clase base
        val control = intent.getStringExtra("numeroControl")
        val codigo = intent.getStringExtra("codGeneracion")
        val JSON = intent.getStringExtra("JSON")
        if(JSON=="Factura"){
            val intent = Intent(this, EmitirCFActivity::class.java)
            intent.putExtra("numeroControl", control)
            intent.putExtra("codigoGeneracion", codigo)
            startActivity(intent)
            finish()
        }else{
            val intent = Intent(this, EmitirCCFActivity::class.java)
            intent.putExtra("numeroControl", control)
            intent.putExtra("codigoGeneracion", codigo)
            startActivity(intent)
            finish()
        }
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
        //ccf
        val valorIva = intent.getStringExtra("Iva")?.toDouble()
        val totalF= valorIva?.plus(total!!)
        val fechayHora = FyH_emicion()
        if (fechayHora.isNotEmpty()) {
            fechayHora.let {
                val datos = it.split("\n")
                fecEmi = datos[0]
                horEmi = datos[1]
            }
        }
        val app = application as MyApp
        val ambiente = app.ambiente
        val documento = Documento(
            identificacion = Identificacion(
                version = 1,
                ambiente = ambiente,
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
                pagos = null,
                numPagoElectronico = null,
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
        val documento2 = DocumentoC(
            identificacion = IdentificacionC(
                version = 1,
                ambiente = ambiente,
                tipoDte = "03",
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
            emisor = EmisorC(
                nit = nitE,
                nrc = nrcE,
                nombre = nombreE,
                codActividad = codAcEcoE,
                descActividad = desAcEcoE,
                nombreComercial = nombrecE,
                tipoEstablecimiento = "Oficina",
                direccion = DireccionC(
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
            receptor = ReceptorC(
                nit = nit,
                nrc = nrc,
                nombre = nombre,
                codActividad = codAcEco,
                descActividad = desAcEco,
                nombreComercial = nombre,
                direccion = DireccionC(
                    departamento = departamento,
                    municipio = municipio,
                    complemento = complemento
                ),
                telefono = telefono,
                correo = correo
            ),
            otrosDocumentos = null,
            ventaTercero = null,
            cuerpoDocumento = emptyList(),
            resumen = ResumenC(
                totalNoSuj = totalNoSujeto,
                totalExenta = totalExenta,
                totalGravada = totalGravada,
                subTotalVentas = total,
                descuNoSuj = 0.0,
                descuExenta = 0.0,
                descuGravada = 0.0,
                porcentajeDescuento = 0.0,
                totalDescu = 0.0,
                tributos =  tributosC(
                    codigo = "20",
                    descripcion = "Impuesto al Valor Agregado 13%",
                    valor = valorIva
                ),
                subTotal = total,
                ivaPercil = 0.0,
                ivaRete1 = 0.0,
                reteRenta = 0.0,
                montoTotalOperacion = totalF,
                totalNoGravado = 0.0,
                totalPagar = totalF,
                totalLetras = precioEnLetras(totalF),
                saldoFavor = 0.0,
                condicionOperacion = condicionOperacion,
                pagos = null,
                numPagoElectronico = null,
            ),
            extension = ExtensionC(
                placaVehiculo = null,
                docuEntrega = null,
                nombEntrega = null,
                docuRecibe = nit,
                nombRecibe = nombre,
                observaciones = null
            ),
            apendice = null,
            selloRecibido = "Sello de recibido",
            firmaElectronica = "Firma electrónica"
        )
        val JSON = intent.getStringExtra("JSON")
        if(JSON=="Factura"){
            val articulos = obtenerDatosGuardados("F")
            val cuerpoDocumentos = createCuerpoDocumento(articulos)

            documento.cuerpoDocumento = cuerpoDocumentos

            // Crear una instancia de ObjectMapper
            val mapper = ObjectMapper().registerModule(KotlinModule())
            mapper.configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true)

            // Convertir la instancia de Documento a JSON
            val json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(documento)

            saveJsonToExternalStorage(json,numeroContol)
        }else if(JSON=="CreditoFiscal"){
            val articulos = obtenerDatosGuardados("CF")
            val cuerpoDocumentosC = createCuerpoDocumentoC(articulos)

            documento2.cuerpoDocumento = cuerpoDocumentosC

            // Crear una instancia de ObjectMapper
            val mapper = ObjectMapper().registerModule(KotlinModule())
            mapper.configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true)

            // Convertir la instancia de Documento a JSON
            val json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(documento2)

            saveJsonToExternalStorage(json,numeroContol)
        }
    }

    private fun createCuerpoDocumentoC(dataList: List<String>): List<CuerpoDocumentoC> {
        return dataList.mapIndexed { index, dataString ->
            val data = dataString.split("\n")
            CuerpoDocumentoC(
                ivaItem = data[9].toDouble(),
                psv = data[13].toDouble(),
                noGravado = data[14].toDouble(),
                numItem = index + 1,
                tipoItem = data[0].toInt(),
                numeroDocumento = null,
                cantidad = data[1].toDouble(),
                codigo = null,
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
    private fun obtenerDatosGuardados(letra:String): List<String> {
        // Obtén la instancia de la base de datos desde la aplicación
        val app = application as MyApp
        val database = app.database
        var nombre: String = ""
        if(letra=="F"){
            nombre = "Articulocf"
        }else if(letra=="CF"){
            nombre = "Articuloccf"
        }
        // Crea una consulta para seleccionar todos los documentos con tipo = "cliente"
        val query = QueryBuilder.select(SelectResult.all())
            .from(DataSource.database(database))
            .where(Expression.property("tipo").equalTo(Expression.string(nombre)))

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
                codigo = null,
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
            val TIPO = intent.getStringExtra("JSON")
            // Dibujar el Encabezado
            canvas.drawText("DOCUMENTO TRIBUTARIO ELECTRÓNICO", 210f, 25f, paintEncabezado)
            if(TIPO=="Factura"){
                canvas.drawText("FACTURA DE CONSUMIDOR FINAL", 230f, 40f, paintEncabezado)
            }else{
                canvas.drawText("COMPROBANTE DE CRÉDITO FISCAL", 220f, 40f, paintEncabezado)
            }
            //imagen
            // Obtener la imagen desde la URI
            val imageUri = obtenerUriGuardada()?.toUri()
            Log.e("imageUri","$imageUri")
            val contentResolver: ContentResolver = contentResolver
            val imageStream: InputStream? = imageUri?.let { contentResolver.openInputStream(it) }
            val bitmap = BitmapFactory.decodeStream(imageStream)

            // Dibuja la imagen en el PDF
            if (bitmap != null) {
                val scaledBitmap = Bitmap.createScaledBitmap(bitmap, 150, 112, false)
                canvas.drawBitmap(scaledBitmap, 40f, 50f, null)
            }

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
            var nombre2 = nombre
            if (nombre2==null){
                nombre2=""
            }
            var nit2 = nit
            if (nit2==null){
                nit2=""
            }
            var nrc2 = nrc
            if (nrc2==null){
                nrc2=""
            }
            var descActividad2 = desAcEco
            if (descActividad2==null){
                descActividad2=""
            }
            var municipio2 = municipio
            if (municipio2==null){
                municipio2=""
            }
            var departamento2 = departamento
            if (departamento2==null){
                departamento2=""
            }
            var complemento2 = complemento
            if (complemento2==null){
                complemento2=""
            }
            var telefono2 = telefono
            if (telefono2==null){
                telefono2=""
            }
            var correo2 = correo
            if (correo2==null){
                correo2=""
            }
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
            val JSON = intent.getStringExtra("JSON")
            var Articulo:String=""
            if(JSON=="Factura"){
                Articulo = "Articulocf"
            }else{
                Articulo = "Articuloccf"
            }
            // Crea una consulta para seleccionar todos los documentos con tipo = "cliente"
            val query = QueryBuilder.select(SelectResult.all())
                .from(DataSource.database(database))
                .where(Expression.property("tipo").equalTo(Expression.string(Articulo)))

            // Ejecuta la consulta
            val result = query.execute()

            // Itera sobre todos los resultados de la consulta
            result.allResults().forEach { results ->
                // Obtiene el diccionario del documento del resultado actual
                val dict = results.getDictionary(database.name)

                // Dibujar cada fila de la tabla
                val numItem = dict?.getString("numItem")
                val cantidad = dict?.getString("Cantidad")
                val uniMedida = dict?.getString("Unidad")
                val descripcion = dict?.getString("Producto")
                val precioUni = dict?.getString("Precio")
                val montoDescu = dict?.getString("montoDesc")
                val ventaNoSuj = dict?.getString("ventaNS")
                val ventaExenta = dict?.getString("ventaE")
                val ventaGravada = dict?.getString("ventaG")
                // Dibujar cada celda de la fila
                if (numItem != null) {
                    canvas.drawText(numItem, startX + 2, startY, paintInfoDocumento)
                }
                if (cantidad != null) {
                    canvas.drawText(cantidad, startX + 33, startY, paintInfoDocumento)
                }
                if (uniMedida != null) {
                    canvas.drawText(uniMedida, startX + 86, startY, paintInfoDocumento)
                }
                if (descripcion != null) {
                    canvas.drawText(descripcion, startX + 128, startY, paintInfoDocumento)
                }
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

            val totalNoSuj = intent.getStringExtra("totalNoSuj")?.toDouble()
            val totalExenta = intent.getStringExtra("totalExenta")?.toDouble()
            val totalGravada = intent.getStringExtra("totalGravada")?.toDouble()
            canvas.drawText("$$totalNoSuj", startX + 414, startY, paintInfoDocumento)
            canvas.drawText("$$totalExenta", startX + 468, startY, paintInfoDocumento)
            canvas.drawText("$$totalGravada", startX + 509, startY, paintInfoDocumento)

            // LLama a los demás datos de DETALLES

            // Para mover a la derecha: Incrementa finalPosition (por ejemplo, startX + 500).
            // Para mover a la izquierda: Decrementa finalPosition (por ejemplo, startX + 350).
            val finalPosition1 = startX + 495
            val finalPosition2 = startX + 530
            // Info de Suma Total de Operaciones con su respectivo monto

            val subTotalVentas = intent.getStringExtra("total")

            canvas.drawText("Suma Total de Operaciones:", finalPosition1 - paintTITULO.measureText("Suma Total de Operaciones:"), startY + 15, paintTITULO)
            canvas.drawText("$$subTotalVentas", finalPosition2 - paintTITULO.measureText(subTotalVentas), startY + 15, paintInfoDocumento)

            // Info de Descuento para Ventas no sujetas con su respectivo monto
            val descuNoSuj = "0.0"
            canvas.drawText("Monto global Desc., Rebajas y otros a ventas no sujetas:", finalPosition1 - paintTITULO.measureText("Monto global Desc., Rebajas y otros a ventas no sujetas:"), startY + 26, paintTITULO)
            canvas.drawText("$$descuNoSuj", finalPosition2 - paintTITULO.measureText(descuNoSuj), startY + 26, paintInfoDocumento)

            // Info de Descuento para Ventas exentas con su respectivo monto
            val descuExenta = "0.0"
            canvas.drawText("Monto global Desc., Rebajas y otros a ventas exentas:", finalPosition1 - paintTITULO.measureText("Monto global Desc., Rebajas y otros a ventas exentas:"), startY + 37, paintTITULO)
            canvas.drawText("$$descuExenta", finalPosition2 - paintTITULO.measureText(descuExenta), startY + 37, paintInfoDocumento)

            // Info de Descuento para Ventas gravadas con su respectivo monto
            val descuGravada = "0.0"
            canvas.drawText("Monto global Desc., Rebajas y otros a ventas gravadas:", finalPosition1 - paintTITULO.measureText("Monto global Desc., Rebajas y otros a ventas gravadas:"), startY + 48, paintTITULO)
            canvas.drawText("$$descuGravada", finalPosition2 - paintTITULO.measureText(descuGravada), startY + 48, paintInfoDocumento)

            // Obtener el array de tributos
            val tributos: JSONArray? = null



            // Variables para almacenar la descripción y el valor
            var descripcion20 = ""
            var valor20 = 0.0
            // Buscar y almacenar la descripción y el valor del tributo con código "20"
            if (tributos != null) {
                for (i in 0 until tributos.length()) {
                    val tributo = tributos.getJSONObject(i)
                    if (tributo.getString("codigo") == "20") {
                        descripcion20 = tributo.getString("descripcion")
                        valor20 = tributo.getDouble("Iva")
                        break  // Suponiendo que solo hay un tributo con código "20"
                    }
                }
            }
            // Dibuja lo que es el monto de IVA (13%) sobre el total de la venta
            canvas.drawText(descripcion20, finalPosition1 - paintTITULO.measureText(descripcion20), startY + 59, paintTITULO)
            canvas.drawText("$$valor20", finalPosition2 - paintTITULO.measureText(valor20.toString()), startY + 59, paintInfoDocumento)

            // Muesta Info sobre el Sub-Total
            val subTotal =  intent.getStringExtra("total")
            canvas.drawText("Sub-Total:", finalPosition1 - paintTITULO.measureText("Sub-Total:"), startY + 70, paintTITULO)
            canvas.drawText("$$subTotal", finalPosition2 - paintTITULO.measureText(subTotal), startY + 70, paintInfoDocumento)

            // Muesta Info sobre el IVA Percibido
            /*val ivaPerci1 = resumen.getString("ivaPerci1")
            canvas.drawText("IVA Percibido:", finalPosition1 - paintTITULO.measureText("IVA Percibido:"), startY + 81, paintTITULO)
            canvas.drawText("$$ivaPerci1", finalPosition2 - paintTITULO.measureText(ivaPerci1), startY + 81, paintInfoDocumento)*/

            // Muesta Info sobre el IVA Retenido
            val ivaRete1 = "0.0"
            canvas.drawText("IVA Retenido:", finalPosition1 - paintTITULO.measureText("IVA Retenido:"), startY + 92, paintTITULO)
            canvas.drawText("$$ivaRete1", finalPosition2 - paintTITULO.measureText(ivaRete1), startY + 92, paintInfoDocumento)

            // Muesta Info sobre Retención Renta
            val reteRenta = "0.0"
            canvas.drawText("Retención Renta:", finalPosition1 - paintTITULO.measureText("Retención Renta:"), startY + 103, paintTITULO)
            canvas.drawText("$$reteRenta", finalPosition2 - paintTITULO.measureText(reteRenta), startY + 103, paintInfoDocumento)

            var montoTotalOperacion = "0.0"
            val tF = intent.getStringExtra("JSON")
            if (tF=="Factura"){
                montoTotalOperacion = intent.getStringExtra("total").toString()
            }else if(tF=="CreditoFiscal"){
                val total = intent.getStringExtra("total")?.toDouble()
                val iva= intent.getStringExtra("Iva")?.toDouble()
                val monto= total!! + iva!!
                montoTotalOperacion = monto.toString()
            }
            // Muesta Info sobre el Monto Total de la Operación
            canvas.drawText("Monto Total de la Operación:", finalPosition1 - paintTITULO.measureText("Monto Total de la Operación:"), startY + 114, paintTITULO)
            canvas.drawText("$$montoTotalOperacion", finalPosition2 - paintTITULO.measureText(montoTotalOperacion), startY + 114, paintInfoDocumento)

            // Muesta Info sobre Otros montos posibles no afectos
            val totalNoGravado = "0.0"
            canvas.drawText("Total Otros montos no afectos:", finalPosition1 - paintTITULO.measureText("Total Otros montos no afectos:"), startY + 125, paintTITULO)
            canvas.drawText("$$totalNoGravado", finalPosition2 - paintTITULO.measureText(totalNoGravado), startY + 125, paintInfoDocumento)

            // Muesta Info sobre el Total a Pagar
            var totalPagar = "0.0"
            if (tF=="Factura"){
                totalPagar = intent.getStringExtra("total").toString()
            }else if(tF=="CreditoFiscal"){
                val total = intent.getStringExtra("total")?.toDouble()
                val iva= intent.getStringExtra("Iva")?.toDouble()
                val totalm= total!! + iva!!
                totalPagar = totalm.toString()
            }
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

            val totalLetras =precioEnLetras(intent.getStringExtra("total")?.toDouble())
            canvas.drawText("Valor en Letras: $totalLetras", startX, startY + 5, paintTITULO)

            // Muestra Info de Condición de la Operación
            val condicionOperacion = intent.getStringExtra("condicionOperacion")?.toInt()
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

            canvas.drawText("Emisor Responsable: ", startX, startY + 60, paintTITULO)
            //canvas.drawText("$nombreE", startX+80, startY + 60, paintTITULO)
            canvas.drawText("No. documento: ", startX, startY + 70, paintTITULO)
            //canvas.drawText("$nitE", startX+60, startY + 70, paintTITULO)


            canvas.drawText("Receptor Responsable: ", startX, startY + 90, paintTITULO)
            canvas.drawText("$nombre2", startX+87, startY + 90, paintTITULO)
            canvas.drawText("No. documento: ", startX, startY + 100, paintTITULO)
            canvas.drawText("$nit2", startX+60, startY + 100, paintTITULO)
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
    fun savePdfToCache(context: Context, document: PdfDocument): File {
        val cacheDir = context.cacheDir
        val file = File(cacheDir, "example.pdf")
        try {
            val fos = FileOutputStream(file)
            document.writeTo(fos)
            fos.close()
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            document.close()
        }
        return file
    }
    fun renderPdfToImageView(pdfFile: File, imageView: ImageView) {
        try {
            val fileDescriptor = ParcelFileDescriptor.open(pdfFile, ParcelFileDescriptor.MODE_READ_ONLY)
            val pdfRenderer = PdfRenderer(fileDescriptor)
            val page = pdfRenderer.openPage(0)

            val bitmap = Bitmap.createBitmap(page.width, page.height, Bitmap.Config.ARGB_8888)
            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
            imageView.setImageBitmap(bitmap)

            page.close()
            pdfRenderer.close()
            fileDescriptor.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
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
    private fun borrararticulos(l:String) {
        val app = application as MyApp
        val database = app.database
        var articulos:String=""
        if(l=="Factura"){
            articulos = "Articulocf"
        }else{
            articulos = "Articuloccf"
        }
        val query = QueryBuilder.select(SelectResult.expression(Meta.id))
            .from(DataSource.database(database))
            .where(Expression.property("tipo").equalTo(Expression.string(articulos)))

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
            } else {
                Log.d("Prin_Re_Cliente", "No se encontraron artículos")
            }
        } catch (e: CouchbaseLiteException) {
            Log.e("Prin_Re_Cliente", "Error al eliminar los artículos: ${e.message}", e)
        }
    }
    private fun borrarDui(l:String) {
        val app = application as MyApp
        val database = app.database
        var TYPE:String=""
        if(l=="Factura"){
            TYPE = "DUI"
        }else{
            TYPE = "NRC"
        }
        val query = QueryBuilder.select(SelectResult.expression(Meta.id))
            .from(DataSource.database(database))
            .where(Expression.property("tipo").equalTo(Expression.string(TYPE)))

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
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
    private fun borrarNCCG(l:String) {
        val app = application as MyApp
        val database = app.database
        var NCCG:String=""
        if(l=="Factura"){
            NCCG = "NCCG"
        }else{
            NCCG = "NCCGCCF"
        }
        val query = QueryBuilder.select(SelectResult.expression(Meta.id))
            .from(DataSource.database(database))
            .where(Expression.property("tipo").equalTo(Expression.string(NCCG)))

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
    private fun guardarDatosF(l:String) {
        var tipoDC: String = ""
        val app = application as MyApp
        val database = app.database
        val numeroControl = intent.getStringExtra("numeroControl")
        val codigoG =intent.getStringExtra("codGeneracion")
        var nombre=""
        var nit=""
        var dui=""
        var nrc = ""
        val totalExenta= intent.getStringExtra("totalExenta")?.toDouble()
        val totalGravada= intent.getStringExtra("totalGravada")?.toDouble()
        val totalNoSuj= intent.getStringExtra("totalNoSuj")?.toDouble()
        val fechayhora=FyH_emicion()
        var fecEmi=""
        if (fechayhora.isNotEmpty()) {
            fechayhora.let {
                val datos = it.split("\n")
                fecEmi = datos[0]
            }
        }
        if(l=="Factura"){
            tipoDC = "Factura Consumidor Final"
        }else{
            tipoDC = "Comprobante Crédito Fiscal"
        }
        val subTotalVentas= totalExenta!! + totalGravada!! + totalNoSuj!!
        val cliente=intent.getStringExtra("Cliente")
        Log.e("Cliente","$cliente")
        if (cliente != "") {
            val datos = cliente?.split("\n")

            if (datos != null) {
                if (datos.isNotEmpty()) {
                    nombre = datos[0]
                    nit = datos[11]
                    dui = datos[12]
                    nrc = datos[9]
                } else {
                    // Maneja el caso donde los datos no están completos o el formato no es el esperado
                    println("Los datos del cliente no están en el formato esperado.")
                }
            }
        } else {
            // Maneja el caso donde cliente es null
            println("No se recibió información del cliente.")
        }


        val document = MutableDocument()
            .setString("nombre",nombre)
            .setString("nit",nit)
            .setString("dui",dui)
            .setDouble("totalNoSuj", totalNoSuj)
            .setDouble("totalExenta", totalExenta)
            .setDouble("totalGravada", totalGravada)
            .setDouble("total",subTotalVentas)
            .setString("tipo","factura")
            .setString("numeroControl",numeroControl)
            .setString("fechaEmi",fecEmi)
            .setString("tipoD",tipoDC)
            .setString("nrc",nrc)
            .setString("codigoGeneracion",codigoG)
        try {
            // Guardar el documento en la base de datos
            database.save(document)
            Log.d("TuClase", "Datos guardados correctamente: \n $document")
        } catch (e: CouchbaseLiteException) {
            Log.e(
                "TuClase",
                "Error al guardar los datos en la base de datos: ${e.message}",
                e
            )
            Toast.makeText(this, "Error al guardar los datos", Toast.LENGTH_SHORT).show()
        }
    }
    private fun obtenerUriGuardada(): String? {
        val app = application as MyApp
        val database = app.database

        val query = QueryBuilder.select(SelectResult.property("URI"))
            .from(DataSource.database(database))
            .where(Expression.property("tipo").equalTo(Expression.string("Imagen")))

        return try {
            val resultSet = query.execute()
            val result = resultSet.next()

            result?.getString("URI")
        } catch (e: CouchbaseLiteException) {
            Log.e("Prin_Re_Cliente", "Error al obtener la URI de la base de datos: ${e.message}", e)
            null
        }
    }

}