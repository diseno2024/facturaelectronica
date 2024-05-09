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
import android.provider.MediaStore
import android.content.ContentValues
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
        val values = ContentValues().apply {
            put(MediaStore.Files.FileColumns.DISPLAY_NAME, "backup_directory")
            put(MediaStore.Files.FileColumns.MIME_TYPE, "application/vnd.android.package-archive")
        }
        val resolver = applicationContext.contentResolver
        val uri = resolver.insert(MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY), values)
        uri?.let { uri ->
            // El directorio se creó exitosamente
            Log.d("BackupActivity", "Directorio de respaldo creado exitosamente en: $uri")
            Toast.makeText(this, "Directorio de respaldo creado exitosamente", Toast.LENGTH_SHORT).show()
        } ?: run {
            // Error al crear el directorio
            Log.e("BackupActivity", "Error al crear el directorio de respaldo")
            Toast.makeText(this, "Error al crear el directorio de respaldo", Toast.LENGTH_SHORT).show()
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