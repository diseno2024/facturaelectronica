package com.example.facturaelectronica

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import org.apache.commons.io.FileUtils
import java.io.File

class RestauracionActivity : AppCompatActivity() {

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
        val parentDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
        val backupDirectoryName = "respaldo_factura2024"
        val restorationDirectoryName = "restauracion_factura"
        val additionalBackupDirectoryName = "additional_backup"

        val backupDirectory = File(parentDir, backupDirectoryName)
        val restorationDirectory = File(backupDirectory, restorationDirectoryName)
        val additionalBackupDirectory = File(parentDir, additionalBackupDirectoryName)

        val sourceDirectory = if (restorationDirectory.exists()) restorationDirectory else additionalBackupDirectory

        if (sourceDirectory.exists()) {
            try {
<<<<<<< HEAD
                // Copiar los archivos desde la carpeta de respaldo o la ubicación adicional a la carpeta de restauración
                copyDirectory(sourceDirectory, parentDir)
=======
                // Copiar los archivos desde la carpeta de respaldo a la carpeta de restauración
                FileUtils.copyDirectory(restorationDirectory, parentDir)
>>>>>>> a48ea32 (Arreglo Restauracion)
                Log.d("RestauracionActivity", "Datos restaurados con éxito")
                Toast.makeText(this, "Datos restaurados con éxito", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("RestauracionActivity", "Error al restaurar datos: ${e.message}")
                Toast.makeText(this, "Error al restaurar datos", Toast.LENGTH_SHORT).show()
            }
        } else {
            Log.e("RestauracionActivity", "No se pudo encontrar la carpeta de respaldo ni la ubicación adicional")
            Toast.makeText(this, "No se pudo encontrar la carpeta de respaldo ni la ubicación adicional", Toast.LENGTH_SHORT).show()
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
<<<<<<< HEAD

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
}
=======
}
>>>>>>> a48ea32 (Arreglo Restauracion)
