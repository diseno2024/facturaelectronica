package com.example.facturaelectronica
import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.Switch
import android.widget.TextView
import android.widget.Spinner
import android.widget.ArrayAdapter
import android.widget.AdapterView
import android.view.View
import android.widget.Toast
import android.widget.ImageButton
import android.widget.Button
import android.widget.EditText
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import java.util.*

import android.os.Environment
import android.util.Log
import java.io.File
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat




class BackupActivity : AppCompatActivity() {

    private lateinit var buttonSelectTime: Button
    private lateinit var editTextSelectedTime: EditText
    private val WRITE_EXTERNAL_STORAGE_REQUEST_CODE = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_backup)

        val switchEncendido = findViewById<Switch>(R.id.switchEncendido)
        val textEstado = findViewById<TextView>(R.id.textEstado)

        switchEncendido.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                textEstado.text = "Encendido"
            } else {
                textEstado.text = "Apagado"
            }
        }

        val btnRegistrar = findViewById<Button>(R.id.btnRegistrar)
        btnRegistrar.setOnClickListener {
            requestWriteExternalStoragePermission()
        }

        val spinnerFrecuencia = findViewById<Spinner>(R.id.spinnerFrecuencia)
        val opciones = resources.getStringArray(R.array.frecuencia_options)
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, opciones)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerFrecuencia.adapter = adapter

        spinnerFrecuencia.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val opcionSeleccionada = opciones[position]
                Toast.makeText(applicationContext, "Seleccionaste: $opcionSeleccionada", Toast.LENGTH_SHORT).show()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
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
                    val selectedTime = String.format("%02d:%02d %s", hour12Format, selectedMinute, period)
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
            createBackupFolder()
        }
    }

    private fun createBackupFolder() {
        // Obtener el directorio de documentos públicos
        val parentDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)

// Nombre del directorio de respaldo dentro del directorio de documentos
        val backupDirectoryName = "respaldo_factura2024"

// Crear el objeto File para el directorio de respaldo
        val backupDirectory = File(parentDir, backupDirectoryName)

// Verificar si el directorio ya existe
        if (!backupDirectory.exists()) {
            // Intentar crear el directorio si no existe
            if (backupDirectory.mkdirs()) {
                // El directorio se creó exitosamente
                Log.d("BackupActivity", "Directorio de respaldo creado exitosamente en: ${backupDirectory.absolutePath}")
                Toast.makeText(this, "Directorio de respaldo creado exitosamente", Toast.LENGTH_SHORT).show()
            } else {
                // Error al crear el directorio
                Log.e("BackupActivity", "Error al crear el directorio de respaldo")
                Toast.makeText(this, "Error al crear el directorio de respaldo", Toast.LENGTH_SHORT).show()
            }
        } else {
            // El directorio ya existe
            Log.d("BackupActivity", "El directorio de respaldo ya existe en: ${backupDirectory.absolutePath}")
            Toast.makeText(this, "El directorio de respaldo ya existe", Toast.LENGTH_SHORT).show()
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
}