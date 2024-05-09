package com.example.facturaelectronica

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.File
import android.widget.Button

class RestauracionActivity : AppCompatActivity() {

    private val WRITE_EXTERNAL_STORAGE_REQUEST_CODE = 101

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
            requestWriteExternalStoragePermission()
        }
    }



    private fun requestWriteExternalStoragePermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                WRITE_EXTERNAL_STORAGE_REQUEST_CODE
            )
        } else {
            createRestorationFolder()
        }
    }

    private fun createRestorationFolder() {
        val parentDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
        val backupDirectoryName = "respaldo_factura2024"
        val restorationDirectoryName = "restauracion_factura"

        val backupDirectory = File(parentDir, backupDirectoryName)
        val restorationDirectory = File(backupDirectory, restorationDirectoryName)

        if (backupDirectory.exists()) { // Verificar si la carpeta de respaldo existe
            if (!restorationDirectory.exists()) { // Verificar si la carpeta de restauración no existe
                if (restorationDirectory.mkdirs()) { // Intentar crear la carpeta de restauración
                    Log.d("RestauracionActivity", "Directorio de restauración creado en: ${restorationDirectory.absolutePath}")
                    Toast.makeText(this, "Directorio de restauración creado exitosamente", Toast.LENGTH_SHORT).show()
                } else {
                    Log.e("RestauracionActivity", "Error al crear el directorio de restauración")
                    Toast.makeText(this, "Error al crear el directorio de restauración", Toast.LENGTH_SHORT).show()
                }
            } else {
                Log.d("RestauracionActivity", "El directorio de restauración ya existe en: ${restorationDirectory.absolutePath}")
                Toast.makeText(this, "El directorio de restauración ya existe", Toast.LENGTH_SHORT).show()
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
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                createRestorationFolder()
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


