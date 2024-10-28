package com.billsv.facturaelectronica

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import java.util.Locale
import java.util.Date
import java.text.SimpleDateFormat
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import com.couchbase.lite.Database
import com.couchbase.lite.MutableDocument
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.*


class RestauracionActivity : AppCompatActivity() {

    private val WRITE_EXTERNAL_STORAGE_REQUEST_CODE = 101
    private val FILE_SELECT_CODE = 102  // Código para el selector de archivos
    private lateinit var database: Database // Inicializa Couchbase Lite y tu base de datos
    private val permissionList: List<String> = if (Build.VERSION.SDK_INT >= 33) {
        listOf(
            Manifest.permission.READ_MEDIA_AUDIO,
            Manifest.permission.READ_MEDIA_VIDEO,
            Manifest.permission.READ_MEDIA_IMAGES
        )
    } else {
        listOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
    }
    // Definición de las clases necesarias dentro de RestauracionActivity.kt
    data class DocumentWrapper(
        val docId: String,
        val my_database: JsonObject
    )
    data class ComprobanteCreditoFiscal(
        val totalNoSuj: Double,
        val numeroControl: String,
        val tipoD: String,
        val articulos: String,
        val municipio: String,
        val fechaEmi: String,
        val nombre: String,
        val condicionOp: String,
        val codigoGeneracion: String,
        val total: Double,
        val complemento: String,
        val iva: Double,
        val totalGravada: Double,
        val correo: String,
        val desAcEco: String,
        val nit: String,
        val selloRecibido: String,
        val departamento: String,
        val dui: String,
        val codAcEco: String,
        val totalExenta: Double,
        val telefono: String,
        val nrc: String?
    )
    data class Cliente(
        val municipioT: String,
        val tipo: String,
        val municipio: String,
        val nitM: String,
        val direccion: String,
        val nombre: String,
        val actividadEconomica: String,
        val tipoCliente: String,
        val nit: String,
        val departamentoT: String,
        val departamento: String,
        val dui: String,
        val duiM: String,
        val telefono: String,
        val telefonoM: String,
        val nrc: String,
        val email: String
    )


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_restauracion)

        database = Database("my_database")

        val btnAtras: ImageButton = findViewById(R.id.atras)
        btnAtras.setOnClickListener {
            val intent = Intent(this, MenuActivity::class.java)
            startActivity(intent)
            finish()
        }

        val btnRealizarRecuperacion: Button = findViewById(R.id.buttonSelectTime)
        btnRealizarRecuperacion.setOnClickListener {
            requestPermissions()
            openFileSelector()
        }

        // Mostrar la fecha del último respaldo y el rango de fechas de restauración
        mostrarFechas()
    }

    // Función para mostrar la fecha del último respaldo y el rango de fechas de restauración
    private fun mostrarFechas() {
        val fechaUltimoRespaldo = leerFechaUltimoRespaldo()
        val textFecha: TextView = findViewById(R.id.textFecha)
        val textFecha1: TextView = findViewById(R.id.textFecha1)
        val textFecha2: TextView = findViewById(R.id.textFecha2)
        val dateFormat = android.icu.text.SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

        if (fechaUltimoRespaldo != null) {
            textFecha.text = dateFormat.format(fechaUltimoRespaldo)

            // Obtener el rango de fechas de restauración
            val rangoRestauracion = obtenerRangoRestauracion()
            if (rangoRestauracion != null) {
                textFecha1.text = dateFormat.format(rangoRestauracion.first)
                textFecha2.text = dateFormat.format(rangoRestauracion.second)
            } else {
                textFecha1.text = "N/A"
                textFecha2.text = "N/A"
            }
        } else {
            textFecha.text = "Ninguna"
            textFecha1.text = "N/A"
            textFecha2.text = "N/A"
        }
    }

    // Función para leer la fecha del último respaldo
    private fun leerFechaUltimoRespaldo(): Date? {
        // Aquí implementa la lógica para leer la fecha del último respaldo
        // Por ejemplo, supongamos que obtienes la fecha desde SharedPreferences
        val sharedPrefs = getSharedPreferences("BackupPrefs", MODE_PRIVATE)
        val timestamp = sharedPrefs.getLong("last_backup_timestamp", -1)
        return if (timestamp != -1L) Date(timestamp) else null
    }

    private fun getFriendlyPath(absolutePath: String): String {
        val rootPath = "/storage/emulated/0"
        return if (absolutePath.startsWith(rootPath)) {
            "Storage" + absolutePath.substring(rootPath.length)
        } else {
            absolutePath
        }
    }
    // Función para obtener el rango de fechas de restauración
    private fun obtenerRangoRestauracion(): Pair<Date, Date>? {
        // Aquí deberías implementar la lógica para obtener el rango de fechas de restauración
        // Por ejemplo, podrías obtenerlas desde SharedPreferences o cualquier otra fuente de datos
        // Aquí se muestra un ejemplo simple de cómo podrías obtener un rango de fechas ficticio

        val fechaInicio = Calendar.getInstance()
        fechaInicio.set(2024, Calendar.APRIL, 10) // Fecha de inicio ficticia

        val fechaFin = Calendar.getInstance()
        fechaFin.set(2024, Calendar.APRIL, 12) // Fecha de fin ficticia

        return fechaInicio.time to fechaFin.time
    }

    private fun requestPermissions() {
        val permissionsToRequest = mutableListOf<String>()

        for (permission in permissionList) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(permission)
            }
        }

        if (permissionsToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                permissionsToRequest.toTypedArray(),
                WRITE_EXTERNAL_STORAGE_REQUEST_CODE
            )
        } else {
            restoreData()
        }
    }

    private fun restoreData() {
        val parentDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
        val backupDirectoryName = "respaldo_factura2024"
        val restorationDirectoryName = "restauracion_factura"

        val backupDirectory = File(parentDir, backupDirectoryName)
        val restorationDirectory = File(parentDir, restorationDirectoryName)

        if (backupDirectory.exists() && backupDirectory.isDirectory) {
            try {
                // Verificar si el directorio de restauración ya existe y eliminarlo si es necesario
                if (restorationDirectory.exists()) {
                    restorationDirectory.deleteRecursively()
                }
                // Crear el directorio de restauración
                if (restorationDirectory.mkdirs()) {
                    // Copiar todos los archivos del directorio de respaldo al directorio de restauración
                    copyDirectory(backupDirectory, restorationDirectory)
                    val restorationDirectoryPath = getFriendlyPath(restorationDirectory.absolutePath)
                    Log.d("RestauracionActivity", "Datos restaurados con éxito en: $restorationDirectoryPath")
                    Toast.makeText(this, "Datos restaurados con éxito", Toast.LENGTH_SHORT).show()

                    // Crear archivo ZIP con los datos restaurados
                    val zipFile = File(parentDir, "$restorationDirectoryName.zip")
                    zipDirectory(restorationDirectory, zipFile)
                    val zipFilePath = getFriendlyPath(zipFile.absolutePath)
                    Log.d("RestauracionActivity", "Datos comprimidos con éxito en: $zipFilePath")
                    Toast.makeText(this, "Datos comprimidos con éxito en: $zipFilePath", Toast.LENGTH_LONG).show()
                } else {
                    Log.e("RestauracionActivity", "Error al crear el directorio de restauración")
                    Toast.makeText(this, "Error al crear el directorio de restauración", Toast.LENGTH_SHORT).show()
                }
            } catch (e: IOException) {
                e.printStackTrace()
                Log.e("RestauracionActivity", "Error al restaurar datos: ${e.message}")
                Toast.makeText(this, "Error al restaurar datos", Toast.LENGTH_SHORT).show()
            }
        } else {
            Log.e("RestauracionActivity", "La carpeta de respaldo no existe")
            Toast.makeText(this, "La carpeta de respaldo no existe", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == WRITE_EXTERNAL_STORAGE_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                restoreData()
            } else {
                Toast.makeText(
                    this,
                    "Los permisos son necesarios para realizar la restauración",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    @Throws(IOException::class)
    private fun copyDirectory(srcDir: File, destDir: File) {
        if (srcDir.isDirectory) {
            if (!destDir.exists()) {
                destDir.mkdirs()
            }

            val children = srcDir.list()
            if (children != null) {
                for (child in children) {
                    copyDirectory(File(srcDir, child), File(destDir, child))
                }
            }
        } else {
            FileInputStream(srcDir).use { input ->
                FileOutputStream(destDir).use { output ->
                    input.copyTo(output)
                }
            }
        }
    }

    @Throws(IOException::class)
    private fun zipDirectory(srcDir: File, zipFile: File) {
        ZipOutputStream(FileOutputStream(zipFile)).use { zos ->
            zipFile(srcDir, srcDir.name, zos)
        }
    }

    @Throws(IOException::class)
    private fun zipFile(srcFile: File, fileName: String, zos: ZipOutputStream) {
        if (srcFile.isDirectory) {
            val children = srcFile.list()
            if (children != null) {
                for (child in children) {
                    zipFile(File(srcFile, child), "$fileName/$child", zos)
                }
            }
        } else {
            FileInputStream(srcFile).use { fis ->
                val zipEntry = ZipEntry(fileName)
                zos.putNextEntry(zipEntry)
                fis.copyTo(zos)
                zos.closeEntry()
            }
        }
    }
    // Función para abrir el selector de archivos
    private fun openFileSelector() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            type = "application/json"
            addCategory(Intent.CATEGORY_OPENABLE)
        }
        startActivityForResult(intent, FILE_SELECT_CODE)
    }
    // Método para manejar el resultado del selector de archivos
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == FILE_SELECT_CODE && resultCode == Activity.RESULT_OK) {
            data?.data?.let { uri ->
                restoreDataFromJson(uri)
            }
        }
    }
    private fun restoreDataFromJson(uri: Uri) {
        try {
            val inputStream = contentResolver.openInputStream(uri)
            val reader = BufferedReader(InputStreamReader(inputStream))
            val jsonString = reader.readText()
            reader.close()

            Log.d("RestauracionActivity", "Contenido del JSON: $jsonString") // Verificar el contenido del JSON

            val gson = Gson()
            val documentListType = object : TypeToken<List<DocumentWrapper>>() {}.type
            val documents: List<DocumentWrapper> = gson.fromJson(jsonString, documentListType)

            for (document in documents) {
                Log.d("RestauracionActivity", "Procesando documento con ID: ${document.docId}") // Log para cada documento

                // Verifica si es un documento de tipo ComprobanteCreditoFiscal
                if (document.my_database.has("tipoD")) {
                    val comprobante = gson.fromJson(document.my_database, ComprobanteCreditoFiscal::class.java)

                    val mutableDoc = MutableDocument(document.docId)
                    mutableDoc.setValue("totalNoSuj", comprobante.totalNoSuj)
                    mutableDoc.setValue("numeroControl", comprobante.numeroControl)
                    mutableDoc.setValue("tipoD", comprobante.tipoD)
                    mutableDoc.setValue("articulos", comprobante.articulos)
                    mutableDoc.setValue("municipio", comprobante.municipio)
                    mutableDoc.setValue("fechaEmi", comprobante.fechaEmi)
                    mutableDoc.setValue("nombre", comprobante.nombre)
                    mutableDoc.setValue("condicionOp", comprobante.condicionOp)
                    mutableDoc.setValue("codigoGeneracion", comprobante.codigoGeneracion)
                    mutableDoc.setValue("total", comprobante.total)
                    mutableDoc.setValue("complemento", comprobante.complemento)
                    mutableDoc.setValue("iva", comprobante.iva)
                    mutableDoc.setValue("totalGravada", comprobante.totalGravada)
                    mutableDoc.setValue("correo", comprobante.correo)
                    mutableDoc.setValue("desAcEco", comprobante.desAcEco)
                    mutableDoc.setValue("nit", comprobante.nit)
                    mutableDoc.setValue("selloRecibido", comprobante.selloRecibido)
                    mutableDoc.setValue("departamento", comprobante.departamento)
                    mutableDoc.setValue("dui", comprobante.dui)
                    mutableDoc.setValue("codAcEco", comprobante.codAcEco)
                    mutableDoc.setValue("totalExenta", comprobante.totalExenta)
                    mutableDoc.setValue("telefono", comprobante.telefono)
                    mutableDoc.setValue("nrc", comprobante.nrc)

                    database.save(mutableDoc)
                    Log.d("RestauracionActivity", "Documento Comprobante guardado con ID: ${document.docId}") // Confirmación de guardado

                    // Verifica si es un documento de tipo Cliente
                } else if (document.my_database.has("tipoCliente")) {
                    val cliente = gson.fromJson(document.my_database, Cliente::class.java)

                    val mutableDoc = MutableDocument(document.docId)
                    mutableDoc.setValue("municipioT", cliente.municipioT)
                    mutableDoc.setValue("tipo", cliente.tipo)
                    mutableDoc.setValue("municipio", cliente.municipio)
                    mutableDoc.setValue("nitM", cliente.nitM)
                    mutableDoc.setValue("direccion", cliente.direccion)
                    mutableDoc.setValue("nombre", cliente.nombre)
                    mutableDoc.setValue("actividadEconomica", cliente.actividadEconomica)
                    mutableDoc.setValue("tipoCliente", cliente.tipoCliente)
                    mutableDoc.setValue("nit", cliente.nit)
                    mutableDoc.setValue("departamentoT", cliente.departamentoT)
                    mutableDoc.setValue("departamento", cliente.departamento)
                    mutableDoc.setValue("dui", cliente.dui)
                    mutableDoc.setValue("duiM", cliente.duiM)
                    mutableDoc.setValue("telefono", cliente.telefono)
                    mutableDoc.setValue("telefonoM", cliente.telefonoM)
                    mutableDoc.setValue("nrc", cliente.nrc)
                    mutableDoc.setValue("email", cliente.email)

                    database.save(mutableDoc)
                    Log.d("RestauracionActivity", "Documento Cliente guardado con ID: ${document.docId}")

                } else {
                    Log.d("RestauracionActivity", "Documento sin tipoD o tipoCliente, ID: ${document.docId}")
                }
            }
            Toast.makeText(this, "Datos restaurados con éxito desde JSON", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("RestauracionActivity", "Error al restaurar datos: ${e.message}")
            Toast.makeText(this, "Error al restaurar datos: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }



    override fun onDestroy() {
        super.onDestroy()
        database.close()  // Cierra la base de datos al salir de la actividad
    }

}
