package com.billsv.facturaelectronica

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.DocumentsContract
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.documentfile.provider.DocumentFile
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

class RestauracionActivity : AppCompatActivity() {

    private val WRITE_EXTERNAL_STORAGE_REQUEST_CODE = 101
    private val READ_EXTERNAL_STORAGE_REQUEST_CODE = 102
    private val PICK_DIRECTORY_REQUEST_CODE = 2
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
        setContentView(R.layout.activity_restauracion)

        val btnAtras: ImageButton = findViewById(R.id.atras)
        btnAtras.setOnClickListener {
            val intent = Intent(this, MenuActivity::class.java)
            startActivity(intent)
            finish()
        }

        val btnRealizarRecuperacion: Button = findViewById(R.id.buttonSelectTime)
        btnRealizarRecuperacion.setOnClickListener {
            requestPermissions()
        }
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
        val backupUri = getBackupDirectoryUri()
        if (backupUri != null) {
            restoreDataFromUri(backupUri)
        } else {
            openDirectoryPicker()
        }
    }

    private fun getBackupDirectoryUri(): Uri? {
        val sharedPref = getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        val uriString = sharedPref.getString("backup_directory_uri", null)
        return uriString?.let { Uri.parse(it) }
    }

    private fun restoreDataFromUri(directoryUri: Uri) {
        val documentFile = DocumentFile.fromTreeUri(this, directoryUri)
        if (documentFile != null && documentFile.isDirectory) {
            try {
                for (file in documentFile.listFiles()) {
                    if (file.isFile && file.name?.endsWith(".zip") == true) {
                        val restorationDirectory = File(filesDir, "restauracion_factura")
                        if (restorationDirectory.exists()) {
                            restorationDirectory.deleteRecursively()
                        }
                        restorationDirectory.mkdirs()
                        unzipFile(file.uri, restorationDirectory)
                        Log.d("RestauracionActivity", "Datos restaurados con éxito en: ${restorationDirectory.absolutePath}")
                        Toast.makeText(this, "Datos restaurados con éxito", Toast.LENGTH_SHORT).show()
                        return
                    }
                }
                Toast.makeText(this, "No se encontró un archivo de respaldo", Toast.LENGTH_SHORT).show()
            } catch (e: IOException) {
                e.printStackTrace()
                Log.e("RestauracionActivity", "Error al restaurar datos: ${e.message}")
                Toast.makeText(this, "Error al restaurar datos", Toast.LENGTH_SHORT).show()
            }
        } else {
            Log.e("RestauracionActivity", "El directorio de respaldo no es válido")
            Toast.makeText(this, "El directorio de respaldo no es válido", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openDirectoryPicker() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).apply {
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        }
        startActivityForResult(intent, PICK_DIRECTORY_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_DIRECTORY_REQUEST_CODE && resultCode == RESULT_OK) {
            data?.data?.let { uri ->
                val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                contentResolver.takePersistableUriPermission(uri, takeFlags)
                saveDirectoryUri(uri)
                restoreDataFromUri(uri)
            }
        }
    }

    private fun saveDirectoryUri(uri: Uri) {
        val sharedPref = getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putString("restore_directory_uri", uri.toString())
            apply()
        }
    }

    @Throws(IOException::class)
    private fun unzipFile(zipUri: Uri, targetDirectory: File) {
        contentResolver.openInputStream(zipUri)?.use { inputStream ->
            ZipInputStream(inputStream).use { zis ->
                var zipEntry: ZipEntry? = zis.nextEntry
                while (zipEntry != null) {
                    val newFile = createFile(targetDirectory, zipEntry)
                    if (zipEntry.isDirectory) {
                        if (!newFile.isDirectory && !newFile.mkdirs()) {
                            throw IOException("Failed to create directory: ${newFile.absolutePath}")
                        }
                    } else {
                        FileOutputStream(newFile).use { fos ->
                            zis.copyTo(fos)
                        }
                    }
                    zipEntry = zis.nextEntry
                }
            }
        }
    }

    @Throws(IOException::class)
    private fun createFile(targetDirectory: File, zipEntry: ZipEntry): File {
        val destFile = File(targetDirectory, zipEntry.name)
        val destDirPath = targetDirectory.canonicalPath
        val destFilePath = destFile.canonicalPath
        if (!destFilePath.startsWith(destDirPath + File.separator)) {
            throw IOException("Entry is outside of the target dir: ${zipEntry.name}")
        }
        return destFile
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == WRITE_EXTERNAL_STORAGE_REQUEST_CODE || requestCode == READ_EXTERNAL_STORAGE_REQUEST_CODE) {
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
}
