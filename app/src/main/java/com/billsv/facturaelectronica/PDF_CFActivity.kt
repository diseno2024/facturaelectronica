package com.billsv.facturaelectronica

import android.app.Dialog
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.content.Intent
import android.os.Environment
import android.util.Log
import android.widget.Button
import android.widget.Toast
import com.couchbase.lite.CouchbaseLiteException
import com.couchbase.lite.DataSource
import com.couchbase.lite.Expression
import com.couchbase.lite.MutableDocument
import com.couchbase.lite.QueryBuilder
import com.couchbase.lite.SelectResult
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.io.FileWriter
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.UUID

class PDF_CFActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_pdf_cfactivity)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val generar: Button = findViewById(R.id.Generar)
        generar.setOnClickListener {
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
                totalIva = 2.6,
                porcentajeDescuento = 0.0,
                ivaRete1 = 0.0,
                reteRenta = 0.0,
                totalNoGravado = 0.0,
                totalPagar = 22.6,
                saldoFavor = 0.0,
                condicionOperacion = 1,
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
                totalNoSuj = 0.0,
                totalExenta = 0.0,
                totalGravada = 20.0,
                subTotalVentas = 20.0,
                descuNoSuj = 0.0,
                descuExenta = 0.0,
                descuGravada = 0.0,
                totalDescu = 0.0,
                tributos = null,
                subTotal = 20.0,
                montoTotalOperacion = 22.6,
                totalLetras = precioEnLetras(42.37)
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
            numero < 20 -> especiales[numero - 11]
            numero < 100 -> {
                val decena = numero / 10
                val unidad = numero % 10
                if (unidad == 0) decenas[decena] else "${decenas[decena]} y ${unidades[unidad]}"
            }
            numero < 1000 -> {
                val centena = numero / 100
                val resto = numero % 100
                if (resto == 0) "${unidades[centena]}cientos" else "${unidades[centena]}cientos ${numeroALetras(resto)}"
            }
            else -> throw IllegalArgumentException("Número fuera de rango")
        }
    }

    fun precioEnLetras(precio: Double): String {
        val partes = precio.toString().split(".")
        val parteEntera = partes[0].toInt()
        val parteDecimal = if (partes.size > 1) partes[1].padEnd(2, '0').take(2).toInt() else 0

        val letrasEntera = if (parteEntera == 1) "un dólar" else "${numeroALetras(parteEntera)} dólares"
        val letrasDecimal = when (parteDecimal) {
            0 -> ""
            1 -> "con un centavo"
            else -> "con ${numeroALetras(parteDecimal)} centavos"
        }

        return "$letrasEntera $letrasDecimal".trim()
    }

}