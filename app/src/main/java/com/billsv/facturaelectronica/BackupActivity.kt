package com.billsv.facturaelectronica

import android.Manifest
import android.app.TimePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import java.text.SimpleDateFormat
import android.content.SharedPreferences
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.documentfile.provider.DocumentFile
import com.couchbase.lite.Database
import java.io.*
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class BackupActivity : AppCompatActivity() {

    private lateinit var buttonSelectTime: Button
    private lateinit var editTextSelectedTime: EditText
    private val WRITE_EXTERNAL_STORAGE_REQUEST_CODE = 101
    private val CREATE_FILE_REQUEST_CODE = 1
    private val preferenceFileKey = "com.billsv.facturaelectronica.PREFERENCE_FILE_KEY"
    private val backupUriKey = "backupUri"

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_backup)

        val switchEncendido = findViewById<Switch>(R.id.switchEncendido)
        val textEstado = findViewById<TextView>(R.id.textEstado)

        switchEncendido.setOnCheckedChangeListener { _, isChecked ->
            textEstado.text = if (isChecked) "Encendido" else "Apagado"
        }

        val btnRegistrar = findViewById<Button>(R.id.btnRegistrar)
        btnRegistrar.setOnClickListener {
            checkPermissionsAndBackup()
        }

        val spinnerFrecuencia = findViewById<Spinner>(R.id.spinnerFrecuencia)
        val opciones = resources.getStringArray(R.array.frecuencia_options)
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, opciones)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerFrecuencia.adapter = adapter

        spinnerFrecuencia.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val opcionSeleccionada = opciones[position]
                Toast.makeText(
                    applicationContext,
                    "Seleccionaste: $opcionSeleccionada",
                    Toast.LENGTH_SHORT
                ).show()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        buttonSelectTime = findViewById(R.id.buttonSelectTime)
        editTextSelectedTime = findViewById(R.id.editTextSelectedTime)

        buttonSelectTime.setOnClickListener {
            val cal = Calendar.getInstance()
            val hour = cal.get(Calendar.HOUR_OF_DAY)
            val minute = cal.get(Calendar.MINUTE)

            val timePickerDialog = TimePickerDialog(
                this,
                TimePickerDialog.OnTimeSetListener { _, selectedHour, selectedMinute ->
                    val period = if (selectedHour < 12) "AM" else "PM"
                    val hour12Format = if (selectedHour > 12) selectedHour - 12 else selectedHour
                    val selectedTime =
                        String.format("%02d:%02d %s", hour12Format, selectedMinute, period)
                    editTextSelectedTime.setText("Hora seleccionada: $selectedTime")
                },
                hour,
                minute,
                true
            )
            timePickerDialog.show()
        }

        val buttonAtras = findViewById<ImageButton>(R.id.atras)
        buttonAtras.setOnClickListener {
            val intent = Intent(this, MenuActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(this, MenuActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun checkPermissionsAndBackup() {
        val permissionsToRequest = mutableListOf<String>()

        for (permission in permissionList) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    permission
                ) != PackageManager.PERMISSION_GRANTED
            ) {
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
            performBackup()
        }
    }

    private fun performBackup() {
        val sharedPreferences = getSharedPreferences(preferenceFileKey, MODE_PRIVATE)
        val backupUriString = sharedPreferences.getString(backupUriKey, null)

        if (backupUriString != null) {
            val backupUri = Uri.parse(backupUriString)
            backupDatabase(backupUri)
        } else {
            openDirectoryPicker()
        }
    }

    private fun openDirectoryPicker() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
        startActivityForResult(intent, CREATE_FILE_REQUEST_CODE)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == WRITE_EXTERNAL_STORAGE_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                performBackup()
            } else {
                Toast.makeText(
                    this,
                    "Los permisos son necesarios para realizar el respaldo",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CREATE_FILE_REQUEST_CODE && resultCode == RESULT_OK) {
            data?.data?.let { uri ->
                val sharedPreferences = getSharedPreferences(preferenceFileKey, MODE_PRIVATE)
                with(sharedPreferences.edit()) {
                    putString(backupUriKey, uri.toString())
                    apply()
                }
                backupDatabase(uri)
            }
        }
    }

    private fun backupDatabase(uri: Uri) {
        val database = Database("my_database")
        val dbDir = File(database.path)
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val backupFileName = "respaldo_factura2024.zip"

        try {
            // Crear un nuevo archivo ZIP o abrir el existente si ya existe
            val backupZipFile = File(cacheDir, backupFileName)
            val zipOutputStream = if (backupZipFile.exists()) {
                ZipOutputStream(FileOutputStream(backupZipFile, true)) // Modo de append
            } else {
                ZipOutputStream(FileOutputStream(backupZipFile))
            }

            // Agregar la base de datos actual al archivo ZIP
            val databaseEntry = ZipEntry("my_database.cblite2")
            zipOutputStream.putNextEntry(databaseEntry)
            FileInputStream(File(dbDir, "my_database.cblite2")).use { input ->
                input.copyTo(zipOutputStream)
            }
            zipOutputStream.closeEntry()

            // Resto del código para guardar el archivo ZIP en la ubicación seleccionada...
            if (DocumentFile.fromTreeUri(this, uri)?.isDirectory == true) {
                val documentFile = DocumentFile.fromTreeUri(this, uri)
                val backupFile = documentFile?.createFile("application/zip", backupFileName)
                backupFile?.uri?.let { backupUri ->
                    contentResolver.openOutputStream(backupUri)?.use { outputStream ->
                        FileInputStream(backupZipFile).use { inputStream ->
                            inputStream.copyTo(outputStream)
                        }
                    }
                    Log.d("BackupActivity", "Archivos comprimidos con éxito: ${backupZipFile.absolutePath}")
                    Toast.makeText(this, "Archivos comprimidos con éxito en la ubicación seleccionada", Toast.LENGTH_LONG).show()
                }
            } else {
                Log.e("BackupActivity", "La URI seleccionada no es un directorio")
                Toast.makeText(this, "Error: la URI seleccionada no es un directorio", Toast.LENGTH_SHORT).show()
            }
        } catch (e: IOException) {
            e.printStackTrace()
            Log.e("BackupActivity", "Error al realizar el respaldo: ${e.message}")
            Toast.makeText(this, "Error al realizar el respaldo", Toast.LENGTH_SHORT).show()
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
}
