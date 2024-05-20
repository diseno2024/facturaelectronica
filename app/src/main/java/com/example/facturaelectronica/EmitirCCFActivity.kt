package com.example.facturaelectronica

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.widget.ImageButton
import android.widget.TextView
import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.os.Environment
import android.widget.Button
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.lang.Exception

class EmitirCCFActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_emitir_ccf)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val buttonAtras = findViewById<ImageButton>(R.id.atras)
        buttonAtras.setOnClickListener {
            val intent = Intent(this, MenuActivity::class.java)
            startActivity(intent)
            finish()
        }
        // Recupera los datos pasados desde la otra actividad
        val datosGuardados = intent.getStringExtra("Contribuyente")
        val Nombre: TextView = findViewById(R.id.nombreR)
        val NRC: TextView = findViewById(R.id.nrcR)
        // Aquí puedes usar los datos como necesites
        datosGuardados?.let{
            val datos = it.split("\n")
            Nombre.text = datos[0]
            NRC.text = datos[4]
        }


        // Botón para poder crear el documento PDF
        val btnGenerarPdf = findViewById<Button>(R.id.btnCrearPdf)
        // Verifica los permisos si están aceptados cuando vamos a hacer la generación
        if (checkPermission()) {
            // El permiso está aceptado
            Toast.makeText(this, "Permiso Aceptado", Toast.LENGTH_LONG).show()
        } else {
            // Vuelve a pedir el permiso
            requestPermissions()
        }
        // Llama la función para poder generar el archivo PDF
        btnGenerarPdf.setOnClickListener {
            generarPdf()
        }
    }
    override fun onBackPressed() {
        super.onBackPressed() // Llama al método onBackPressed() de la clase base
        val intent = Intent(this, MenuActivity::class.java)
        startActivity(intent)
        finish()
    }
    fun DataReceptor(view: View) {
        val intent = Intent(this, InfoReceptoresActivity::class.java)
        startActivity(intent)
        finish()
    }
    fun DataArticulo(view: View) {
        val intent = Intent(this, AgregarArticuloActivity::class.java)
        startActivity(intent)
        finish()
    }
    // Función para poder generar el PDF
    private fun generarPdf() {
        // Variable para poder almacenar el contenido del json através de una función
        val jsonData = leerJsonDesdeAssets("archivo.json")
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
        // Estilo de Rectángulo 1 - Estilo de los rectángulo del emisor y receptor
        val paintRect1 = Paint().apply {
            style = Paint.Style.STROKE // Solo dibujar el contorno
            color = Color.BLACK // Color del contorno
            strokeWidth = 2f // Ancho del contorno
        }
        // Estilo de Borde 1 - Para el rectángulo del emisor y receptor
        val cornerRadius1 = 5f

        // Extraer y mostrar información del JSON
        try {
            val jsonObject = JSONObject(jsonData)

            // Aquí se empieza a generar el PDF en base a toda la info

            // Dibujar el Encabezado
            canvas.drawText("DOCUMENTO TRIBUTARIO ELECTRÓNICO", 210f, 25f, paintEncabezado)
            canvas.drawText("COMPROBANTE DE CREDITO FISCAL", 220f, 40f, paintEncabezado)

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
            val respuestaHacienda = jsonObject.getJSONObject("respuestaHacienda")
            val selloRecibido = respuestaHacienda.getString("selloRecibido")
            // Este es el sello de recibido otrogado por el Ministerio de Hacienda
            canvas.drawText("Sello de Recepción: $selloRecibido", 25f, 195f, paintInfoDocumento)



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
            val nit2 = receptor.getString("nit")
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
            val tributos = resumen.getJSONArray("tributos")
            // Variables para almacenar la descripción y el valor
            var descripcion20 = ""
            var valor20 = 0.0
            // Buscar y almacenar la descripción y el valor del tributo con código "20"
            for (i in 0 until tributos.length()) {
                val tributo = tributos.getJSONObject(i)
                if (tributo.getString("codigo") == "20") {
                    descripcion20 = tributo.getString("descripcion")
                    valor20 = tributo.getDouble("valor")
                    break  // Suponiendo que solo hay un tributo con código "20"
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
            val ivaPerci1 = resumen.getString("ivaPerci1")
            canvas.drawText("IVA Percibido:", finalPosition1 - paintTITULO.measureText("IVA Percibido:"), startY + 81, paintTITULO)
            canvas.drawText("$$ivaPerci1", finalPosition2 - paintTITULO.measureText(ivaPerci1), startY + 81, paintInfoDocumento)

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

            // Muestra Info de Valor en Letras
            val totalLetras = resumen.getString("totalLetras")
            canvas.drawText("Valor en Letras: $totalLetras", startX, startY + 15, paintTITULO)

            // Muestra Info de Condición de la Operación
            val condicionOperacion = resumen.getInt("condicionOperacion")
            if (condicionOperacion == 1) {
                // Si la operación fue al Contado
                canvas.drawText("Condición de la Operación: $condicionOperacion - Contado", startX, startY + 30, paintTITULO)
            } else if (condicionOperacion == 2) {
                // Si la operación fue al Crédito
                canvas.drawText("Condición de la Operación: $condicionOperacion - A crédito", startX, startY + 30, paintTITULO)
            } else if (condicionOperacion == 3){
                // Si la operación fue otra
                canvas.drawText("Condición de la Operación: $condicionOperacion - Otro", startX, startY + 30, paintTITULO)
            }



            // EXTENSIÓN
            // Muestra información extra que requiere hacienda
            canvas.drawText("EXTENSIÓN", startX + 125, startY + 45, paintTITULO)

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

        // Accede al directorio de descargas del dispositivo, ya sea virtual o físico
        // El acceso lo hace através del directorio de la descargas
        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        // Con ese nombre se le va a guardar el PDF
        val outputFilePath = File(downloadsDir, "midocumento.pdf")

        // Valida si el PDF no tuvo errores para generarse
        try {
            pdfDocument.writeTo(FileOutputStream(outputFilePath))
            Toast.makeText(this, "Se creó el PDF correctamente en: ${outputFilePath.absolutePath}", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            // En caso de que los haya habido muestra un mensaje
            e.printStackTrace()
            Toast.makeText(this, "Error al crear el PDF: ${e.message}", Toast.LENGTH_LONG).show()
        }

        // Aquí finaliza la generación del documento pdf
        pdfDocument.close()
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
        val permission1 = ContextCompat.checkSelfPermission(applicationContext, WRITE_EXTERNAL_STORAGE)
        // El permiso de lectura
        val permission2 = ContextCompat.checkSelfPermission(applicationContext, READ_EXTERNAL_STORAGE)
        return permission1 == PackageManager.PERMISSION_GRANTED && permission2 == PackageManager.PERMISSION_GRANTED
    }

    // Función para pedirle los permisos al usuario
    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this,
            // Pide permisos de escritura y lectura
            arrayOf(WRITE_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE),
            200
        )
    }
}