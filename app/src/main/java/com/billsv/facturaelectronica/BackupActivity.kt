package com.billsv.facturaelectronica

import android.Manifest
import android.app.TimePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.couchbase.lite.Database
import java.io.*
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class BackupActivity : AppCompatActivity() {

    private lateinit var buttonSelectTime: Button
    private lateinit var editTextSelectedTime: EditText
    private val WRITE_EXTERNAL_STORAGE_REQUEST_CODE = 101
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
            requestPermissions()
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

    private fun requestPermissions() {
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
            createBackupFolder()
        }
    }

    private fun createBackupFolder() {
        val parentDir =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
        val backupDirectoryName = "respaldo_factura2024"
        val backupDirectory = File(parentDir, backupDirectoryName)

        // Verificar si el directorio de respaldo existe y manejar errores de creación
        if (!backupDirectory.exists()) {
            try {
                if (backupDirectory.mkdirs()) {
                    Log.d(
                        "BackupActivity",
                        "Directorio de respaldo creado en: ${backupDirectory.absolutePath}"
                    )
                    Toast.makeText(
                        this,
                        "Directorio de respaldo creado en: ${getFriendlyPath(backupDirectory.absolutePath)}",
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    Log.e("BackupActivity", "Error al crear el directorio de respaldo")
                    Toast.makeText(this, "Error al crear el directorio de respaldo", Toast.LENGTH_SHORT)
                        .show()
                    return
                }
            } catch (e: Exception) {
                Log.e("BackupActivity", "Excepción al crear el directorio de respaldo: ${e.message}")
                Toast.makeText(this, "Excepción al crear el directorio de respaldo", Toast.LENGTH_SHORT)
                    .show()
                return
            }
        } else {
            Log.d(
                "BackupActivity",
                "El directorio de respaldo ya existe en: ${backupDirectory.absolutePath}"
            )
            Toast.makeText(this, "El directorio de respaldo ya existe en: ${getFriendlyPath(backupDirectory.absolutePath)}", Toast.LENGTH_LONG).show()
        }

        // Realizar respaldo de la base de datos después de crear el directorio
        backupDatabase(backupDirectory)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == WRITE_EXTERNAL_STORAGE_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                createBackupFolder()
            } else {
                Toast.makeText(
                    this,
                    "Los permisos son necesarios para realizar el respaldo",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    // Método para realizar el respaldo de la base de datos
    private fun backupDatabase(backupDir: File) {
        // Supongamos que tienes una instancia de la base de datos de Couchbase Lite llamada `database`
        val database = Database("my_database")  // Corrige el nombre de la base de datos

        val dbDir = File(database.path)
        val backupDirDb = File(backupDir, dbDir.name)

        try {
            // Verificar si el archivo de destino ya existe y eliminarlo si es necesario
            if (backupDirDb.exists()) {
                backupDirDb.deleteRecursively()  // Utiliza deleteRecursively para eliminar directorios
            }
            // Copiar todos los archivos al directorio de respaldo
            copyDirectory(dbDir, backupDirDb)
            Log.d("BackupActivity", "Respaldo realizado con éxito: ${backupDirDb.absolutePath}")
            Toast.makeText(this, "Respaldo realizado con éxito", Toast.LENGTH_SHORT).show()

            // Crear archivo zip
            val zipFile = File(backupDir, "${backupDirDb.name}.zip")
            zipDirectory(backupDirDb, zipFile)
            Log.d("BackupActivity", "Archivos comprimidos con éxito: ${zipFile.absolutePath}")
            Toast.makeText(this, "Archivos comprimidos con éxito en: ${getFriendlyPath(zipFile.absolutePath)}", Toast.LENGTH_LONG).show()
        } catch (e: IOException) {
            e.printStackTrace()
            Log.e("BackupActivity", "Error al realizar el respaldo: ${e.message}")
            Toast.makeText(this, "Error al realizar el respaldo", Toast.LENGTH_SHORT).show()
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
                for (i in children.indices) {
                    copyDirectory(File(srcDir, children[i]), File(destDir, children[i]))
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

    private fun getFriendlyPath(absolutePath: String): String {
        return absolutePath.replace("/storage/emulated/0", "Internal Storage")
    }
}
