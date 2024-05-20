package com.example.facturaelectronica

import android.content.Intent
import android.Manifest
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
            restoreDatabase()
        }
    }

    private fun restoreDatabase() {
        val backupDirectoryName = "respaldo_factura2024"
        val restorationDirectoryName = "restauracion_factura"

        val backupDirectory = File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), backupDirectoryName)
        val restorationDirectory = File(backupDirectory, restorationDirectoryName)

        if (restorationDirectory.exists()) {
            val databaseDirectory = File(getDatabasePath("my_database").parent)

            try {
                if (databaseDirectory.exists()) {
                    // Eliminar la base de datos actual si existe
                    databaseDirectory.deleteRecursively()
                }

                // Copiar los archivos de restauración al directorio de la base de datos actual
                restorationDirectory.copyRecursively(databaseDirectory, overwrite = true)

                Toast.makeText(this, "Restauración realizada exitosamente", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Log.e("RestauracionActivity", "Error al restaurar la base de datos: ${e.message}")
                Toast.makeText(this, "Error al restaurar la base de datos", Toast.LENGTH_SHORT).show()
            }
        } else {
            Log.e("RestauracionActivity", "El directorio de restauración no existe")
            Toast.makeText(this, "El directorio de restauración no existe", Toast.LENGTH_SHORT).show()
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
                restoreDatabase()
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
