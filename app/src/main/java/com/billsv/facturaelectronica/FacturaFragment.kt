package com.billsv.facturaelectronica

import android.app.Dialog
import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
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
import androidx.core.net.toUri
import com.billsv.facturaelectronica.databinding.ActivityVerPdfCfBinding
import com.couchbase.lite.CouchbaseLiteException
import com.couchbase.lite.Database
import com.couchbase.lite.Ordering
import com.google.zxing.BarcodeFormat
import com.journeyapps.barcodescanner.BarcodeEncoder
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.lang.Exception
import java.math.RoundingMode


class FacturaFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var facturaAdapter: FacturaAdapter
    private lateinit var btnLoadMore: ImageButton
    private lateinit var btnLoadPrevious: ImageButton
    private lateinit var database: Database
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
        val view = inflater.inflate(R.layout.fragment_factura, container, false)

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
            val input = etDui.text.toString().trim()

            if (input.isNotEmpty()) {
                // Verificar si es un número (DUI) o un texto (nombre)
                if (input.matches(Regex("\\d+"))) {
                    if (input.length >= 10) {
                        buscarPorDui(input)
                    } else {
                        Toast.makeText(context, "Ingrese un DUI válido", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    buscarPorNombre(input)
                }
            } else {
                Toast.makeText(context, "Ingrese un DUI o nombre", Toast.LENGTH_SHORT).show()
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

        facturaAdapter = FacturaAdapter(requireContext(), mutableListOf()) { factura ->
            // Usamos ViewBinding para inflar el layout
            val binding = ActivityVerPdfCfBinding.inflate(LayoutInflater.from(requireContext()))
            val dialogoGenerar = Dialog(requireContext())

            // Asignamos el layout al diálogo
            dialogoGenerar.setContentView(binding.root)

            // Configurar el tamaño del diálogo
            val width = (resources.displayMetrics.widthPixels * 0.98).toInt() // 90% del ancho de la pantalla
            val height = (resources.displayMetrics.heightPixels * 0.6).toInt() // 80% del alto de la pantalla
            dialogoGenerar.window?.setLayout(width, height)
            dialogoGenerar.setCanceledOnTouchOutside(true)

            // Generar el PDF
            val pdfDocument = generarPdf(factura)

            // Guardar el PDF en caché
            val pdfFile = savePdfToCache(requireContext(), pdfDocument)

            // Mostrar el PDF en PDFView utilizando el ViewBinding
            binding.VistaPdfCF.fromFile(pdfFile)
            binding.VistaPdfCF.isZoomEnabled = true
            binding.VistaPdfCF.show()

            // Mostrar el diálogo
            dialogoGenerar.show()
        }

        recyclerView.adapter = facturaAdapter

        // Cargar la primera página de datos
        loadMoreItems()

        return view
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

    private fun loadMoreItems() {
        val newData = obtenerDatosPaginados(currentPage * pageSize, pageSize)
        if (newData.isNotEmpty()) {
            currentData = newData
            facturaAdapter.setFacturas(currentData)
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
            facturaAdapter.setFacturas(currentData)
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

        // Ordenar las facturas por numeroControl de forma descendente para CF
        val query = QueryBuilder.select(SelectResult.all())
            .from(DataSource.database(database))
            .where(Expression.property("tipoD").equalTo(Expression.string("Factura Consumidor Final")))
            .orderBy(Ordering.property("numeroControl").descending())  // Ordenar de mayor a menor
            .limit(Expression.intValue(limit), Expression.intValue(offset))

        val result = query.execute()
        val dataList = mutableListOf<Factura>()

        result.allResults().forEach { results ->
            val dict = results.getDictionary(database.name)
            val nombre = dict?.getString("nombre") ?: ""
            val telefono = dict?.getString("telefono") ?: ""
            val numeroControl = dict?.getString("numeroControl") ?: ""
            val dui = dict?.getString("dui") ?: ""
            val nit = dict?.getString("nit") ?: ""
            val nrc = dict?.getString("nrc") ?: ""
            val fecha = dict?.getString("fechaEmi") ?: ""
            val codActividad = dict?.getString("codAcEco") ?: ""
            val desAcEco = dict?.getString("desAcEco") ?: ""
            val correo = dict?.getString("correo") ?: ""
            val departamento = dict?.getString("departamento") ?: ""
            val municipio = dict?.getString("municipio") ?: ""
            val complemento = dict?.getString("complemento") ?: ""
            val sello = dict?.getString("selloRecibido") ?: ""
            val articulos = dict?.getString("articulos")
            val codigoG = dict?.getString("codigoGeneracion") ?: ""
            val totalNosuj = dict?.getDouble("totalNoSuj")?.toDouble() ?: 0.00
            val totalExenta = dict?.getDouble("totalExenta")?.toDouble() ?: 0.00
            val totalGravada = dict?.getDouble("totalGravada")?.toDouble() ?: 0.00
            val total = dict?.getDouble("total")?.toDouble() ?: 0.00
            val iva = dict?.getDouble("iva")?.toDouble() ?: 0.00
            val condicion = dict?.getString("condicionOp") ?: ""
            val factura = Factura(nombre, numeroControl, dui, nit, nrc, fecha, codActividad, desAcEco, correo, departamento, municipio, complemento, sello, articulos, codigoG, telefono, totalNosuj, totalExenta, totalGravada, total, iva, condicion)
            dataList.add(factura)
            Log.e("Articulos", articulos.toString())
        }

        // Actualizar el total de resultados
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

        result.allResults().forEach { results ->
            val dict = results.getDictionary(database.name)
            val nombre = dict?.getString("nombre") ?: ""
            val telefono = dict?.getString("telefono") ?: ""
            val numeroControl = dict?.getString("numeroControl") ?: ""
            val duiResult = dict?.getString("dui") ?: ""
            val nit = dict?.getString("nit") ?: ""
            val nrc = dict?.getString("nrc") ?: ""
            val fecha = dict?.getString("fechaEmi") ?: ""
            val codActividad = dict?.getString("codAcEco") ?: ""
            val desAcEco = dict?.getString("desAcEco") ?: ""
            val correo = dict?.getString("correo") ?: ""
            val departamento = dict?.getString("departamento") ?: ""
            val municipio = dict?.getString("municipio") ?: ""
            val complemento = dict?.getString("complemento") ?: ""
            val sello = dict?.getString("selloRecibido") ?: ""
            val articulos = dict?.getString("articulos")
            val codigoG = dict?.getString("codigoGeneracion") ?: ""
            val totalNosuj = dict?.getDouble("totalNoSuj")?: 0.00
            val totalExenta = dict?.getDouble("totalExenta")?: 0.00
            val totalGravada = dict?.getDouble("totalGravada")?: 0.00
            val total = dict?.getDouble("total")?: 0.00
            val iva = dict?.getDouble("iva")?: 0.00
            val condicion = dict?.getString("condicionOp") ?: ""
            val factura = Factura(nombre, numeroControl, dui, nit, nrc, fecha, codActividad, desAcEco, correo, departamento, municipio, complemento, sello, articulos, codigoG, telefono, totalNosuj, totalExenta, totalGravada, total, iva, condicion)
            dataList.add(factura)
            Log.e("Articulos", articulos.toString())
        }

        if (dataList.isNotEmpty()) {
            currentData = dataList
            facturaAdapter.setFacturas(currentData)
            btnClearFilter.visibility = View.VISIBLE // Mostrar el botón de limpiar filtro
            btnBuscar.visibility = View.GONE // Ocultar el botón de buscar
        } else {
            Toast.makeText(context, "No se encontraron facturas con ese DUI", Toast.LENGTH_SHORT).show()
        }
    }

    private fun buscarPorNombre(nombre: String) {
        val app = requireActivity().application as MyApp
        val database = app.database

        val query = QueryBuilder.select(SelectResult.all())
            .from(DataSource.database(database))
            .where(Expression.property("nombre").like(Expression.string("%$nombre%"))) // Buscar nombre similar

        val result = query.execute()
        val dataList = mutableListOf<Factura>()

        result.allResults().forEach { result ->
            val dict = result.getDictionary(database.name)
            val nombre = dict?.getString("nombre") ?: ""
            val telefono = dict?.getString("telefono") ?: ""
            val numeroControl = dict?.getString("numeroControl") ?: ""
            val dui = dict?.getString("dui") ?: ""
            val nit = dict?.getString("nit") ?: ""
            val nrc = dict?.getString("nrc") ?: ""
            val fecha = dict?.getString("fechaEmi") ?: ""
            val codActividad = dict?.getString("codAcEco") ?: ""
            val desAcEco = dict?.getString("desAcEco") ?: ""
            val correo = dict?.getString("correo") ?: ""
            val departamento = dict?.getString("departamento") ?: ""
            val municipio = dict?.getString("municipio") ?: ""
            val complemento = dict?.getString("complemento") ?: ""
            val sello = dict?.getString("selloRecibido") ?: ""
            val articulos = dict?.getString("articulos")
            val codigoG = dict?.getString("codigoGeneracion") ?: ""
            val totalNosuj = dict?.getDouble("totalNoSuj")?: 0.00
            val totalExenta = dict?.getDouble("totalExenta")?: 0.00
            val totalGravada = dict?.getDouble("totalGravada")?: 0.00
            val total = dict?.getDouble("total")?: 0.00
            val iva = dict?.getDouble("iva")?: 0.00
            val condicion = dict?.getString("condicionOp") ?: ""
            val factura = Factura(nombre, numeroControl, dui, nit, nrc, fecha, codActividad, desAcEco, correo, departamento, municipio, complemento, sello, articulos, codigoG, telefono, totalNosuj, totalExenta, totalGravada, total, iva, condicion)
            dataList.add(factura)
        }

        if (dataList.isNotEmpty()) {
            currentData = dataList
            facturaAdapter.setFacturas(currentData)
            btnClearFilter.visibility = View.VISIBLE // Mostrar el botón de limpiar filtro
        } else {
            Toast.makeText(context, "No se encontraron facturas con ese nombre", Toast.LENGTH_SHORT).show()
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
            .where(Expression.property("tipoD").equalTo(Expression.string("Factura Consumidor Final")))

        try {
            val result = query.execute()
            totalResults = result.allResults().size
            Log.d("ReClienteActivity", "Número de documentos de tipo 'cf': $totalResults")
        } catch (e: CouchbaseLiteException) {
            Log.e("ReClienteActivity", "Error al contar los documentos de tipo 'ConfEmisor': ${e.message}", e)
        }

        return totalResults
    }
    private fun generarPdf(factura:Factura):PdfDocument {
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
        // Estilo de Letra 3.1 - Para los títulos
        val paintTITULO = Paint().apply {
            color = Color.BLACK
            textSize = 8f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        // Estilo de Letra 3.2 - Para los títulos sin Negrita
        val paintTITULO2 = Paint().apply {
            color = Color.BLACK
            textSize = 8f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        }
        // Estilo de Letra 4 - Para la info del emisor y receptor
        val paintInfoContribuyentes = Paint().apply {
            color = Color.BLACK
            textSize = 7f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        }
        // Estilo de Letra 5 - Para la info del emisor y receptor - TÍTULOS
        val paintInfoContribuyentes2 = Paint().apply {
            color = Color.BLACK
            textSize = 7f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
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
            // Dibujar el Encabezado
            canvas.drawText("DOCUMENTO TRIBUTARIO ELECTRÓNICO", 205f, 25f, paintEncabezado)
            canvas.drawText("FACTURA", 290f, 40f, paintEncabezado)
            //imagen
            // Obtener la imagen desde la URI
            val imageUri = obtenerUriGuardada()?.toUri()
            Log.e("imageUri","$imageUri")
            val contentResolver: ContentResolver = requireContext().contentResolver
            val imageStream: InputStream? = imageUri?.let { contentResolver.openInputStream(it) }
            val bitmap = BitmapFactory.decodeStream(imageStream)

            // Dibuja la imagen en el PDF
            if (bitmap != null) {
                val scaledBitmap = Bitmap.createScaledBitmap(bitmap, 150, 112, false)
                canvas.drawBitmap(scaledBitmap, 40f, 50f, null)
            }

            // IDENTIFICACIÓN
            /*   Lado izquierdo   */
            val codigoGeneracion = factura.codigoG
            val numeroControl = factura.numeroControl
            // Dibujar el texto en el PDF

            // Info Código de Generación
            val styledTextI1 = "Código de Generación: "
            val styledTextWidthI1 = paintInfoContribuyentes2.measureText(styledTextI1)
            canvas.drawText(styledTextI1, 25f, 175f, paintInfoContribuyentes2)
            canvas.drawText(codigoGeneracion, 25f + styledTextWidthI1, 175f, paintInfoContribuyentes)
            //canvas.drawText("Código de Generación: $codigoGeneracion", 25f, 175f, paintInfoDocumento)

            // Info Número de Control
            val styledTextI2 = "Número de Control: "
            val styledTextWidthI2 = paintInfoContribuyentes2.measureText(styledTextI2)
            canvas.drawText(styledTextI2, 25f, 185f, paintInfoContribuyentes2)
            canvas.drawText(numeroControl, 25f + styledTextWidthI2, 185f, paintInfoContribuyentes)
            //canvas.drawText("Número de Control: $numeroControl", 25f, 185f, paintInfoDocumento)

            /*   Lado derecho   */
            val tipoModelo = 1
            val tipoOperacion = 1

            // Dibujar el texto en el PDF
            // Info Modelo de Facturación
            if (tipoModelo == 1) {
                val respuestaTipoModelo = "Modelo Facturación Previo"
                val styledTextI3 = "Modelo de Facturación: "
                val styledTextWidthI3 = paintInfoContribuyentes2.measureText(styledTextI3)
                canvas.drawText(styledTextI3, 375f, 175f, paintInfoContribuyentes2)
                canvas.drawText(respuestaTipoModelo, 375f + styledTextWidthI3, 175f, paintInfoContribuyentes)
                //canvas.drawText("Modelo de Facturación: Modelo Facturación Previo", 375f, 175f, paintInfoDocumento)

            } else if (tipoModelo == 2) {
                val respuestaTipoModelo = "Modelo Facturación Diferido"
                val styledTextI3 = "Modelo de Facturación: "
                val styledTextWidthI3 = paintInfoContribuyentes2.measureText(styledTextI3)
                canvas.drawText(styledTextI3, 375f, 175f, paintInfoContribuyentes2)
                canvas.drawText(respuestaTipoModelo, 375f + styledTextWidthI3, 175f, paintInfoContribuyentes)
                //canvas.drawText("Modelo de Facturación: Modelo Facturación Diferido", 375f, 175f, paintInfoDocumento)
            }

            // Info Tipo de Transmisión
            if (tipoOperacion == 1) {
                val respuestaTipoTransmision = "Transmisión Normal"
                val styledTextI4 = "Tipo de Transmisión: "
                val styledTextWidthI4 = paintInfoContribuyentes2.measureText(styledTextI4)
                canvas.drawText(styledTextI4, 375f, 185f, paintInfoContribuyentes2)
                canvas.drawText(respuestaTipoTransmision, 375f + styledTextWidthI4, 185f, paintInfoContribuyentes)
                //canvas.drawText("Tipo de Transmisión: Transmisión Normal", 375f, 185f, paintInfoDocumento)

            } else if (tipoOperacion == 2) {
                val respuestaTipoTransmision = "Transmisión por Contingencia"
                val styledTextI4 = "Tipo de Transmisión: "
                val styledTextWidthI4 = paintInfoContribuyentes2.measureText(styledTextI4)
                canvas.drawText(styledTextI4, 375f, 185f, paintInfoContribuyentes2)
                canvas.drawText(respuestaTipoTransmision, 375f + styledTextWidthI4, 185f, paintInfoContribuyentes)
                //canvas.drawText("Tipo de Transmisión: Transmisión por Contingencia", 375f, 185f, paintInfoDocumento)
            }
            val fechaYhora = factura.fecha
            val fecha = fechaYhora.split(" ")[0]
            val styledTextI5 = "Fecha y Hora de Generación: "
            val styledTextWidthI5 = paintInfoContribuyentes2.measureText(styledTextI5)
            canvas.drawText(styledTextI5, 375f, 195f, paintInfoContribuyentes2)
            canvas.drawText(fechaYhora, 375f + styledTextWidthI5, 195f, paintInfoContribuyentes)
            val app = requireActivity().application as MyApp
            val ambiente = app.ambiente
            //CODIGO QR
            val qrCodeBitmap = generateQRCode(ambiente,codigoGeneracion,fecha)
            qrCodeBitmap?.let {
                canvas.drawBitmap(it, 250f, 45f, null)  // Dibuja el QR en la posición deseada
            }
            val selloRecibido = factura.sello
            //Este es el sello de recibido otrogado por el Ministerio de Hacienda
            val styledTextI6 = "Sello de Recepción: "
            val styledTextWidthI6 = paintInfoContribuyentes2.measureText(styledTextI6)
            canvas.drawText(styledTextI6, 25f, 195f, paintInfoContribuyentes2)
            if (selloRecibido != null) {
                canvas.drawText(selloRecibido, 25f + styledTextWidthI6, 195f, paintInfoContribuyentes)
            }
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
                    departamentoE = datos[11]
                    municipioE = datos[12]
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

            // Nombre o razón social
            val styledTextE1 = "Nombre o Razón Social: "
            val styledTextWidthE1 = paintInfoContribuyentes2.measureText(styledTextE1)
            canvas.drawText(styledTextE1, 40f, 250f, paintInfoContribuyentes2)
            if (nombre1 != null) {
                canvas.drawText(nombre1, 40f + styledTextWidthE1, 250f, paintInfoContribuyentes)
            }
            //canvas.drawText("Nombre o razón social: $nombre1", 40f, 250f, paintInfoContribuyentes)

            // NIT
            val styledTextE2 = "NIT: "
            val styledTextWidthE2 = paintInfoContribuyentes2.measureText(styledTextE2)
            canvas.drawText(styledTextE2, 40f, 260f, paintInfoContribuyentes2)
            if (nit1 != null) {
                canvas.drawText(nit1, 40f + styledTextWidthE2, 260f, paintInfoContribuyentes)
            }
            //canvas.drawText("NIT: $nit1", 40f, 260f, paintInfoContribuyentes)

            // NRC
            val styledTextE3 = "NRC: "
            val styledTextWidthE3 = paintInfoContribuyentes2.measureText(styledTextE3)
            canvas.drawText(styledTextE3, 40f, 270f, paintInfoContribuyentes2)
            if (nrc1 != null) {
                canvas.drawText(nrc1, 40f + styledTextWidthE3, 270f, paintInfoContribuyentes)
            }
            //canvas.drawText("NRC: $nrc1", 40f, 270f, paintInfoContribuyentes)

            // Actividad Económica
            val styledTextE4 = "Actividad Económica: "
            val styledTextWidthE4 = paintInfoContribuyentes2.measureText(styledTextE4)
            canvas.drawText(styledTextE4, 40f, 280f, paintInfoContribuyentes2)
            if (descActividad1 != null) {
                canvas.drawText(descActividad1, 40f + styledTextWidthE4, 280f, paintInfoContribuyentes)
            }
            //canvas.drawText("Actividad Económica: $descActividad1", 40f, 280f, paintInfoContribuyentes)

            // Municipio
            val styledTextE5 = "Municipio: "
            val styledTextWidthE5 = paintInfoContribuyentes2.measureText(styledTextE5)
            canvas.drawText(styledTextE5, 40f, 290f, paintInfoContribuyentes2)
            if (municipio1 != null) {
                canvas.drawText(municipio1, 40f + styledTextWidthE5, 290f, paintInfoContribuyentes)
            }
            //canvas.drawText("Municipio: $municipio1", 40f, 290f, paintInfoContribuyentes)

            // Departamento
            val styledTextE6 = "Departamento: "
            val styledTextWidthE6 = paintInfoContribuyentes2.measureText(styledTextE6)
            canvas.drawText(styledTextE6, 40f, 300f, paintInfoContribuyentes2)
            if (departamento1 != null) {
                canvas.drawText(departamento1, 40f + styledTextWidthE6, 300f, paintInfoContribuyentes)
            }
            //canvas.drawText("Departamento: $departamento1", 40f, 300f, paintInfoContribuyentes)

            // Dirección
            val styledTextE7 = "Dirección: "
            val styledTextWidthE7 = paintInfoContribuyentes2.measureText(styledTextE7)
            canvas.drawText(styledTextE7, 40f, 310f, paintInfoContribuyentes2)
            if (complemento1 != null) {
                canvas.drawText(complemento1, 40f + styledTextWidthE7, 310f, paintInfoContribuyentes)
            }
            //canvas.drawText("Dirección: $complemento1", 40f, 310f, paintInfoContribuyentes)

            // Número de Teléfono
            val styledTextE8 = "Número de Teléfono: "
            val styledTextWidthE8 = paintInfoContribuyentes2.measureText(styledTextE8)
            canvas.drawText(styledTextE8, 40f, 320f, paintInfoContribuyentes2)
            if (telefono1 != null) {
                canvas.drawText(telefono1, 40f + styledTextWidthE8, 320f, paintInfoContribuyentes)
            }
            //canvas.drawText("Número de Teléfono: $telefono1", 40f, 320f, paintInfoContribuyentes)

            // Correo Electrónico
            val styledTextE9 = "Correo Electrónico: "
            val styledTextWidthE9 = paintInfoContribuyentes2.measureText(styledTextE9)
            canvas.drawText(styledTextE9, 40f, 330f, paintInfoContribuyentes2)
            if (correo1 != null) {
                canvas.drawText(correo1, 40f + styledTextWidthE9, 330f, paintInfoContribuyentes)
            }
            //canvas.drawText("Correo Electrónico: $correo1", 40f, 330f, paintInfoContribuyentes)

            // RECEPTOR
            var nombre2 = factura.nombre
            var nit2 = factura.nit
            var nrc2 = factura.nrc
            var descActividad2 = factura.desAcEco
            var municipio2 = factura.municipio
            var departamento2 = factura.departamento
            var complemento2 = factura.complemento
            var telefono2 = factura.telefono
            var correo2 = factura.correo
            var dui2 = factura.dui
            // Coordenadas del rectángulo del RECEPTOR
            val emisorLeftReceptor = 321f
            val emisorTopReceptor = 215f
            val emisorRightReceptor = 587f
            val emisorBottomReceptor = 340f

            // Dibujar rectángulo del RECEPTOR
            canvas.drawRoundRect(emisorLeftReceptor, emisorTopReceptor, emisorRightReceptor, emisorBottomReceptor, cornerRadius1, cornerRadius1, paintRect1)

            // Dibujar el texto del RECEPTOR
            canvas.drawText("RECEPTOR", 425f, 230f, paintTITULO) // Dibuja que es la info del Receptor

            val styledText1 = "Nombre: "
            val styledTextWidth1 = paintInfoContribuyentes2.measureText(styledText1)
            canvas.drawText(styledText1, 336f, 250f, paintInfoContribuyentes2)
            canvas.drawText(nombre2, 336f + styledTextWidth1, 250f, paintInfoContribuyentes)
            //canvas.drawText("Nombre: $nombre2", 336f, 250f, paintInfoContribuyentes)

            val styledText2 = "DUI: "
            val styledTextWidth2 = paintInfoContribuyentes2.measureText(styledText2)
            canvas.drawText(styledText2, 336f, 260f, paintInfoContribuyentes2)
            canvas.drawText(dui2, 336f + styledTextWidth2, 260f, paintInfoContribuyentes)
            //canvas.drawText("DUI: $dui2", 336f, 260f, paintInfoContribuyentes)

            val styledText3 = "Teléfono: "
            val styledTextWidth3 = paintInfoContribuyentes2.measureText(styledText3)
            canvas.drawText(styledText3, 336f, 270f, paintInfoContribuyentes2)
            canvas.drawText(telefono2, 336f + styledTextWidth3, 270f, paintInfoContribuyentes)
            //canvas.drawText("Teléfono: $telefono2", 336f, 270f, paintInfoContribuyentes)

            val styledText4 = "Correo Electrónico: "
            val styledTextWidth4 = paintInfoContribuyentes2.measureText(styledText4)
            canvas.drawText(styledText4, 336f, 280f, paintInfoContribuyentes2)
            canvas.drawText(correo2, 336f + styledTextWidth4, 280f, paintInfoContribuyentes)
            //canvas.drawText("Correo Electrónico: $correo2", 336f, 280f, paintInfoContribuyentes)

            val styledText5 = "Municipio: "
            val styledTextWidth5 = paintInfoContribuyentes2.measureText(styledText5)
            canvas.drawText(styledText5, 336f, 290f, paintInfoContribuyentes2)
            canvas.drawText(municipio2, 336f + styledTextWidth5, 290f, paintInfoContribuyentes)
            //canvas.drawText("Municipio: $municipio2", 336f, 290f, paintInfoContribuyentes)

            val styledText6 = "Departamento: "
            val styledTextWidth6 = paintInfoContribuyentes2.measureText(styledText6)
            canvas.drawText(styledText6, 336f, 300f, paintInfoContribuyentes2)
            canvas.drawText(departamento2, 336f + styledTextWidth6, 300f, paintInfoContribuyentes)
            //canvas.drawText("Departamento: $departamento2", 336f, 300f, paintInfoContribuyentes)

            val styledText7 = "Dirección: "
            val styledTextWidth7 = paintInfoContribuyentes2.measureText(styledText7)
            canvas.drawText(styledText7, 336f, 310f, paintInfoContribuyentes2)
            canvas.drawText(complemento2, 336f + styledTextWidth7, 310f, paintInfoContribuyentes)
            //canvas.drawText("Dirección: $complemento2", 336f, 310f, paintInfoContribuyentes)

            //canvas.drawText("Teléfono: $telefono2", 336f, 320f, paintInfoContribuyentes)
            //canvas.drawText("Correo Electrónico: $correo2", 336f, 330f, paintInfoContribuyentes)

            // CUERPO
            // Tabla de ítems

            val startX = 40f // Posición X de inicio de la tabla
            var startY = 370f // Posición Y de inicio de la tabla
            val rowHeight = 15f // Altura de cada fila de la tabla
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
            startY += 25
            // Ejecuta la consulta
            val articulosString = factura.articulos // tu cadena de entrada

            val cleanedString = articulosString?.replace("[\\[\\]]".toRegex(), "") // Eliminar corchetes
            val articulosArray = cleanedString?.split(",")?.map { it.trim() } // Ajusta el delimitador según necesites

            articulosArray?.forEach { articuloString ->
                // Dividir cada artículo en sus datos individuales
                val data = articuloString.split("\n")

                    val numItem = data[16].toInt()
                    val cantidad = data[1].toInt()
                    val uniMedida = data[2].toInt()
                    val descripcion = data[3]
                    val precioUni = data[5]
                    val montoDescu = 0.0 // Si tienes descuentos, asigna el valor correspondiente
                    val ventaNoSuj = data[8]
                    val ventaExenta = data[7]
                    val ventaGravada = data[6]

                // Dibujar cada celda de la fila
                canvas.drawText(numItem.toString(), startX + 2, startY, paintInfoDocumento)
                canvas.drawText(cantidad.toString(), startX + 45, startY, paintInfoDocumento)
                canvas.drawText(uniMedida.toString(), startX + 90, startY, paintInfoDocumento)
                canvas.drawText(descripcion, startX + 128, startY, paintInfoDocumento)


                // Los precios de estas categorías se dibujan con los 4 decimales

                val precioUnishow = precioUni.toDouble().toBigDecimal()
                    .setScale(4, RoundingMode.HALF_UP)
                canvas.drawText("$$precioUnishow", startX + 301, startY, paintInfoDocumento)
                //canvas.drawText("$$precioUni", startX + 301, startY, paintInfoDocumento)

                val montoDescuhow = montoDescu.toBigDecimal()
                    .setScale(4, RoundingMode.HALF_UP)
                canvas.drawText("$$montoDescuhow", startX + 351, startY, paintInfoDocumento)
                //canvas.drawText("$$montoDescu", startX + 351, startY, paintInfoDocumento)

                val ventaNoSujshow = ventaNoSuj.toDouble().toBigDecimal()
                    .setScale(4, RoundingMode.HALF_UP)
                canvas.drawText("$$ventaNoSujshow", startX + 406, startY, paintInfoDocumento)
                //canvas.drawText("$$ventaNoSuj", startX + 406, startY, paintInfoDocumento)

                val ventaExentashow = ventaExenta.toDouble().toBigDecimal()
                    .setScale(4, RoundingMode.HALF_UP)
                canvas.drawText("$$ventaExentashow", startX + 461, startY, paintInfoDocumento)
                //canvas.drawText("$$ventaExenta", startX + 461, startY, paintInfoDocumento)

                val ventaGravadashow = ventaGravada.toDouble().toBigDecimal()
                    .setScale(4, RoundingMode.HALF_UP)
                canvas.drawText("$$ventaGravadashow", startX + 506, startY, paintInfoDocumento)
                //canvas.drawText("$$ventaGravada", startX + 506, startY, paintInfoDocumento)

                // Mover al siguiente renglón
                startY += rowHeight
            }
            startY += 20



            // RESUMEN
            // Info sobre todas las ventas que se realizaron
            canvas.drawText("SUMA DE VENTAS:", startX + 325, startY, paintTITULO)

            val totalNoSuj = factura.totalNosuj
            val totalExenta = factura.totalExenta
            val totalGravada = factura.totalGravada

            val totalNoSujshow = totalNoSuj.toBigDecimal()
                .setScale(2, RoundingMode.HALF_UP).toString()
            val totalExentashow = totalExenta.toBigDecimal()
                .setScale(2, RoundingMode.HALF_UP).toString()
            val totalGravadashow = totalGravada.toBigDecimal()
                .setScale(2, RoundingMode.HALF_UP).toString()

            canvas.drawText("$$totalNoSujshow", startX + 406, startY, paintInfoDocumento)
            canvas.drawText("$$totalExentashow", startX + 461, startY, paintInfoDocumento)
            canvas.drawText("$$totalGravadashow", startX + 506, startY, paintInfoDocumento)

            // LLama a los demás datos de DETALLES

            // Para mover a la derecha: Incrementa finalPosition (por ejemplo, startX + 500).
            // Para mover a la izquierda: Decrementa finalPosition (por ejemplo, startX + 350).
            val finalPosition1 = startX + 495
            val finalPosition2 = startX + 530
            // Info de Suma Total de Operaciones con su respectivo monto

            val subTotalVentas = factura.total.toBigDecimal()
                .setScale(2, RoundingMode.HALF_UP).toString()

            canvas.drawText("Suma Total de Operaciones:", finalPosition1 - paintTITULO.measureText("Suma Total de Operaciones:"), startY + 15, paintTITULO)
            canvas.drawText("$$subTotalVentas", finalPosition2 - 24, startY + 15, paintInfoDocumento)

            // Info de Descuento para Ventas no sujetas con su respectivo monto



            // Convertir los valores a cadenas, o asignar "0.0" si son nulos

            val totalNoSujStr = totalNoSujshow.toString() ?: "0.00"
            val totalExentaStr = totalExentashow.toString() ?: "0.00"
            val totalGravadaStr = totalGravadashow.toString() ?: "0.00"



            // Dibujar las ventas no sujetas, exentas y gravadas
            canvas.drawText("Ventas No Sujetas:", finalPosition1 - paintTITULO.measureText("Ventas No Sujetas:"), startY + 26, paintTITULO)
            canvas.drawText("$$totalNoSujshow", finalPosition2 - 24, startY + 26, paintInfoDocumento)

            canvas.drawText("Ventas Exentas:", finalPosition1 - paintTITULO.measureText("Ventas Exentas:"), startY + 37, paintTITULO)
            canvas.drawText("$$totalExentashow", finalPosition2 - 24, startY + 37, paintInfoDocumento)

            canvas.drawText("Ventas Gravadas:", finalPosition1 - paintTITULO.measureText("Ventas Gravadas:"), startY + 48, paintTITULO)
            canvas.drawText("$$totalGravadashow", finalPosition2 - 24, startY + 48, paintInfoDocumento)

            // Obtener el array de tributos
            var tributos: String? = null
            tributos = null
            // Variables para almacenar la descripción y el valor
            var descripcion20 = ""
            var valor20: String = ""

// Suponiendo que el valor del IVA ya está calculado y almacenado en "totalIva" como el 10% del total
            val totalIva = factura.iva.toBigDecimal().setScale(2, RoundingMode.HALF_UP)

// Buscar y almacenar la descripción y el valor del tributo con código "20"
            if (tributos != null && totalIva != null) {
                descripcion20 = "Impuesto al Valor Agregado 13%" // Cambia la descripción si corresponde
                valor20 = totalIva.toString() // Usa el valor dinámico calculado para el IVA
            } else {
                descripcion20 = ""
                valor20 = ""
            }
            // Muesta Info sobre el Sub-Total
            val subTotal =  factura.total
            val subTotalshow = subTotal.toBigDecimal()
                .setScale(2, RoundingMode.HALF_UP).toString()
            canvas.drawText("Sub-Total:", finalPosition1 - paintTITULO.measureText("Sub-Total:"), startY + 59, paintTITULO)
            canvas.drawText("$$subTotalshow", finalPosition2 - 24, startY + 59, paintInfoDocumento)
            // Muesta Info sobre el IVA Retenido
            val ivaRete1 = 0.00
            val ivaRete1show = ivaRete1?.toBigDecimal()
                ?.setScale(2, RoundingMode.HALF_UP).toString()

            canvas.drawText("IVA Retenido:", finalPosition1 - paintTITULO.measureText("IVA Retenido:"), startY + 70, paintTITULO)
            canvas.drawText("$$ivaRete1show", finalPosition2 - 24, startY + 70, paintInfoDocumento)

            // Muesta Info sobre Retención Renta
            val reteRenta = 0.00
            val reteRentashow = reteRenta.toBigDecimal()
                .setScale(2, RoundingMode.HALF_UP).toString()

            canvas.drawText("Retención Renta:", finalPosition1 - paintTITULO.measureText("Retención Renta:"), startY + 81, paintTITULO)
            canvas.drawText("$$reteRentashow", finalPosition2 - 24, startY + 81, paintInfoDocumento)

            var montoTotalOperacion = "0.00"


            montoTotalOperacion = factura.total.toBigDecimal()
                .setScale(2, RoundingMode.HALF_UP).toString()

            canvas.drawText("Monto Total de la Operación:", finalPosition1 - paintTITULO.measureText("Monto Total de la Operación:"), startY + 92, paintTITULO)
            canvas.drawText("$$montoTotalOperacion", finalPosition2 - 24, startY + 92, paintInfoDocumento)


            // Muesta Info sobre Otros montos posibles no afectos
            val totalNoGravado = 0.00
            val totalNoGravadoShow = totalNoGravado?.toBigDecimal()
                ?.setScale(2, RoundingMode.HALF_UP).toString()

            canvas.drawText("Total Otros montos no afectos:", finalPosition1 - paintTITULO.measureText("Total Otros montos no afectos:"), startY + 103, paintTITULO)
            canvas.drawText("$$totalNoGravadoShow", finalPosition2 - 24, startY + 103, paintInfoDocumento)
            // Muesta Info sobre el Total a Pagar
            var totalPagar = "0.00"
            totalPagar = factura.total.toBigDecimal()
                .setScale(2, RoundingMode.HALF_UP).toString()

            canvas.drawText("Total a Pagar:", finalPosition1 - paintTITULO.measureText("Total a Pagar:"), startY + 114, paintTITULO)
            canvas.drawText("$$totalPagar", finalPosition2 - 24, startY + 114, paintInfoDocumento)

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

            //val totalLetras =precioEnLetras(intent.getStringExtra("total")?.toDouble())
            val totalLetras = precioEnLetras(totalPagar.toDouble())

            val styledTextL1 = "Valor en Letras: "
            val styledTextWidthL1 = paintTITULO.measureText(styledTextL1)
            canvas.drawText(styledTextL1, startX, startY + 5, paintTITULO)
            canvas.drawText(totalLetras, startX + styledTextWidthL1, startY + 5, paintTITULO2)
            //canvas.drawText("Valor en Letras: $totalLetras", startX, startY + 5, paintTITULO)

            // Muestra Info de Condición de la Operación
            val condicionOperacion = factura.condicion.toInt()
            if (condicionOperacion == 1) {
                // Si la operación fue al Contado
                val styledTextL2 = "Condición de la Operación: "
                val styledTextWidthL2 = paintTITULO.measureText(styledTextL2)
                canvas.drawText(styledTextL2, startX, startY + 20, paintTITULO)
                canvas.drawText("1 - Contado", startX + styledTextWidthL2, startY + 20, paintTITULO2)
                //canvas.drawText("Condición de la Operación: $condicionOperacion - Contado", startX, startY + 20, paintTITULO)

            } else if (condicionOperacion == 2) {
                // Si la operación fue al Crédito
                val styledTextL3 = "Condición de la Operación: "
                val styledTextWidthL3 = paintTITULO.measureText(styledTextL3)
                canvas.drawText(styledTextL3, startX, startY + 20, paintTITULO)
                canvas.drawText("2 - Crédito", startX + styledTextWidthL3, startY + 20, paintTITULO2)
                //canvas.drawText("Condición de la Operación: $condicionOperacion - A crédito", startX, startY + 20, paintTITULO)

            } else if (condicionOperacion == 3){
                // Si la operación fue otra
                val styledTextL4 = "Condición de la Operación: "
                val styledTextWidthL4 = paintTITULO.measureText(styledTextL4)
                canvas.drawText(styledTextL4, startX, startY + 20, paintTITULO)
                canvas.drawText("3 - Otro", startX + styledTextWidthL4, startY + 20, paintTITULO2)
                //canvas.drawText("Condición de la Operación: $condicionOperacion - Otro", startX, startY + 20, paintTITULO)
            }



            // EXTENSIÓN
            // Muestra información extra que requiere hacienda
            canvas.drawText("EXTENSIÓN", startX + 105, startY + 42, paintTITULO)

            canvas.drawText("Emisor Responsable: ", startX, startY + 60, paintTITULO)
            //canvas.drawText("$nombreE", startX+80, startY + 60, paintTITULO)
            canvas.drawText("No. documento: ", startX, startY + 70, paintTITULO)
            //canvas.drawText("$nitE", startX+60, startY + 70, paintTITULO)


            canvas.drawText("Receptor Responsable: ", startX, startY + 90, paintTITULO)
            canvas.drawText("$nombre2", startX+87, startY + 90, paintTITULO2)
            canvas.drawText("No. documento: ", startX, startY + 100, paintTITULO)
            canvas.drawText("$nit2", startX+60, startY + 100, paintTITULO2)
            canvas.drawText("Observaciones:", startX, startY + 120, paintTITULO)



            // Agregar el número de página
            canvas.drawText("Página 1 de 1", 550f, 775f, paintInfoDocumento)



            // Valida si hubo algún problema para poder validar el archivo json
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(requireContext(), "Error al procesar el JSON: ${e.message}", Toast.LENGTH_LONG).show()
        }

        // Aquí dejan de generarse páginas del pdf
        pdfDocument.finishPage(pagina1)
        return pdfDocument

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
    private fun obtenerEmisor(): List<String> {
        // Obtén la instancia de la base de datos desde la aplicación
        val app = requireActivity().application as MyApp
        database = app.database

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
            val deptext = dict?.getString("deptext")
            val muntext = dict?.getString("muntext")

            // Formatea los datos como una cadena y la agrega a la lista
            val dataString = "$nombre\n$nombrec\n$nit\n$nrc\n$AcEco\n$direccion\n$telefono\n$correo\n$dui\n$departamento\n$municipio\n$deptext\n$muntext"
            dataList.add(dataString)
        }

        // Devuelve la lista de datos
        return dataList
    }
    private fun obtenerUriGuardada(): String? {
        val app = requireActivity().application as MyApp
        database = app.database

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
    fun generateQRCode(ambiente: String, codgeneracion: String?, fechaEmi: String): Bitmap? {
        val url = "https://admin.factura.gob.sv/consultaPublica?ambiente=$ambiente&codGen=$codgeneracion&fechaEmi=$fechaEmi"
        return try {
            val barcodeEncoder = BarcodeEncoder()
            barcodeEncoder.encodeBitmap(url, BarcodeFormat.QR_CODE, 120, 120)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}