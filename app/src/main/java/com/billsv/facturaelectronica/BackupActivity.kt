package com.billsv.facturaelectronica

import android.Manifest
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.*
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.couchbase.lite.Database
import java.io.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class BackupActivity : AppCompatActivity() {

    private val WRITE_EXTERNAL_STORAGE_REQUEST_CODE = 101
    private val permissionList: List<String> = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
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

    private lateinit var buttonSelectTime: Button
    private lateinit var switchEncendido: Switch
    private lateinit var textEstado: TextView
    private lateinit var textFecha: TextView
    private lateinit var editTextSelectedTime: EditText
    private lateinit var alarmManager: AlarmManager
    private lateinit var pendingIntent: PendingIntent
    private lateinit var sharedPrefs: SharedPreferences
    private lateinit var buttonManualBackup: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_backup)

        // Inicializar vistas y componentes
        switchEncendido = findViewById(R.id.switchEncendido)
        textEstado = findViewById(R.id.textEstado)
        textFecha = findViewById(R.id.textFecha)
        editTextSelectedTime = findViewById(R.id.editTextSelectedTime)
        buttonManualBackup = findViewById(R.id.btnRegistrar)

        // Obtener SharedPreferences para guardar la configuración
        sharedPrefs = getSharedPreferences("BackupPrefs", Context.MODE_PRIVATE)

        switchEncendido.isChecked = sharedPrefs.getBoolean("automatic_backup_enabled", false)
        textEstado.text = if (switchEncendido.isChecked) "Encendido" else "Apagado"

        val savedHour = sharedPrefs.getInt("automatic_backup_hour", -1)
        val savedMinute = sharedPrefs.getInt("automatic_backup_minute", -1)
        if (savedHour != -1 && savedMinute != -1) {
            val period = if (savedHour < 12) "AM" else "PM"
            val hour12Format = if (savedHour > 12) savedHour - 12 else savedHour
            val selectedTime = String.format("%02d:%02d %s", hour12Format, savedMinute, period)
            editTextSelectedTime.setText("Hora seleccionada: $selectedTime")
        }

        val savedFrequency = sharedPrefs.getString("automatic_backup_frequency", null)
        if (savedFrequency != null) {
            val spinnerFrecuencia = findViewById<Spinner>(R.id.spinnerFrecuencia)
            val opciones = resources.getStringArray(R.array.frecuencia_options)
            val position = opciones.indexOf(savedFrequency)
            if (position != -1) {
                spinnerFrecuencia.setSelection(position)
            }
        }

        switchEncendido.setOnCheckedChangeListener { _, isChecked ->
            textEstado.text = if (isChecked) "Encendido" else "Apagado"

            with(sharedPrefs.edit()) {
                putBoolean("automatic_backup_enabled", isChecked)
                apply()
            }

            if (isChecked) {
                val spinnerFrecuencia = findViewById<Spinner>(R.id.spinnerFrecuencia)
                val opcionSeleccionada = spinnerFrecuencia.selectedItem as String
                scheduleAutomaticBackup(opcionSeleccionada)
            } else {
                cancelAutomaticBackup()
            }
        }

        // Configurar listener para el spinner de frecuencia
        val spinnerFrecuencia = findViewById<Spinner>(R.id.spinnerFrecuencia)
        val opciones = resources.getStringArray(R.array.frecuencia_options)
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, opciones)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerFrecuencia.adapter = adapter

        spinnerFrecuencia.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val opcionSeleccionada = opciones[position]

                saveAutomaticBackupFrequency(opcionSeleccionada)

                if (switchEncendido.isChecked) {
                    scheduleAutomaticBackup(opcionSeleccionada)
                }
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
                    saveAutomaticBackupTime(selectedHour, selectedMinute)
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


        // Cargar la fecha del último respaldo si existe
        val fechaUltimoRespaldo = leerFechaUltimoRespaldo()
        if (fechaUltimoRespaldo != null) {
            textFecha.text =
                SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(fechaUltimoRespaldo)
        } else {
            textFecha.text = "Ninguna"
        }

        alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(this, BackupActivity::class.java)
        pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        buttonManualBackup.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                createBackupFolder()
            } else {
                requestPermissions()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        switchEncendido.isChecked = sharedPrefs.getBoolean("automatic_backup_enabled", false)
        textEstado.text = if (switchEncendido.isChecked) "Encendido" else "Apagado"

        val savedHour = sharedPrefs.getInt("automatic_backup_hour", -1)
        val savedMinute = sharedPrefs.getInt("automatic_backup_minute", -1)
        if (savedHour != -1 && savedMinute != -1) {
            val period = if (savedHour < 12) "AM" else "PM"
            val hour12Format = if (savedHour > 12) savedHour - 12 else savedHour
            val selectedTime = String.format("%02d:%02d %s", hour12Format, savedMinute, period)
            editTextSelectedTime.setText("Hora seleccionada: $selectedTime")
        }

        val savedFrequency = sharedPrefs.getString("automatic_backup_frequency", null)
        if (savedFrequency != null) {
            val spinnerFrecuencia = findViewById<Spinner>(R.id.spinnerFrecuencia)
            val opciones = resources.getStringArray(R.array.frecuencia_options)
            val position = opciones.indexOf(savedFrequency)
            if (position != -1) {
                spinnerFrecuencia.setSelection(position)
            }
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
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(permission)
            }
        }

        if (permissionsToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, permissionsToRequest.toTypedArray(), WRITE_EXTERNAL_STORAGE_REQUEST_CODE)
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
                        "Directorio de respaldo creado exitosamente",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Log.e("BackupActivity", "Error al crear el directorio de respaldo")
                    Toast.makeText(
                        this,
                        "Error al crear el directorio de respaldo",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                    return
                }
            } catch (e: Exception) {
                Log.e(
                    "BackupActivity",
                    "Excepción al crear el directorio de respaldo: ${e.message}"
                )
                Toast.makeText(
                    this,
                    "Excepción al crear el directorio de respaldo",
                    Toast.LENGTH_SHORT
                )
                    .show()
                return
            }
        } else {
            Log.d(
                "BackupActivity",
                "El directorio de respaldo ya existe en: ${backupDirectory.absolutePath}"
            )
            Toast.makeText(this, "El directorio de respaldo ya existe", Toast.LENGTH_SHORT).show()
        }

        // Realizar respaldo de la base de datos después de crear el directorio
        backupDatabase(backupDirectory)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == WRITE_EXTERNAL_STORAGE_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                createBackupFolder()
            } else {
                Toast.makeText(this, "Permisos denegados para escribir en el almacenamiento externo", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Método para realizar el respaldo de la base de datos
    private fun backupDatabase(backupDir: File) {
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
            Toast.makeText(this, "Archivos comprimidos con éxito", Toast.LENGTH_SHORT).show()

            // Actualizar la fecha del último respaldo
            actualizarFechaUltimoRespaldo(Date())
            textFecha.text = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
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

    private fun leerFechaUltimoRespaldo(): Date? {
        // Aquí implementa la lógica para leer la fecha del último respaldo
        // Por ejemplo, supongamos que obtienes la fecha desde SharedPreferences
        val sharedPrefs = getSharedPreferences("BackupPrefs", Context.MODE_PRIVATE)
        val timestamp = sharedPrefs.getLong("last_backup_timestamp", -1)
        return if (timestamp != -1L) Date(timestamp) else null
    }

    private fun actualizarFechaUltimoRespaldo(fecha: Date) {
        // Aquí implementa la lógica para actualizar la fecha del último respaldo
        // Por ejemplo, guardando la fecha en SharedPreferences
        val sharedPrefs = getSharedPreferences("BackupPrefs", Context.MODE_PRIVATE)
        with(sharedPrefs.edit()) {
            putLong("last_backup_timestamp", fecha.time)
            apply()
        }
    }

    private fun showTimePickerDialog() {
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

                // Guardar la hora seleccionada para el respaldo automático
                saveAutomaticBackupTime(selectedHour, selectedMinute)
            },
            hour,
            minute,
            true
        )
        timePickerDialog.show()
    }

    private val PREFS_FILENAME = "BackupPrefs"
    private val PREF_AUTOMATIC_BACKUP_HOUR = "automatic_backup_hour"
    private val PREF_AUTOMATIC_BACKUP_MINUTE = "automatic_backup_minute"
    private val PREF_AUTOMATIC_BACKUP_FREQUENCY = "automatic_backup_frequency"

    // Método para guardar la hora seleccionada en SharedPreferences
    private fun saveAutomaticBackupTime(hour: Int, minute: Int) {
        // Obtener el SharedPreferences
        with(sharedPrefs.edit()) {
            // Guardar la hora y los minutos seleccionados
            putInt("automatic_backup_hour", hour)
            putInt("automatic_backup_minute", minute)
            apply()
        }

        // Informar al usuario que la hora de respaldo automático se ha guardado
        Toast.makeText(
            this,
            "Hora de respaldo automático guardada: ${formatTime(hour, minute)}",
            Toast.LENGTH_SHORT
        ).show()
    }

    // Método para guardar la frecuencia seleccionada en SharedPreferences
    private fun saveAutomaticBackupFrequency(frequency: String) {
        with(sharedPrefs.edit()) {
            putString("automatic_backup_frequency", frequency)
            apply()
        }
    }


    private fun formatTime(hour: Int, minute: Int): String {
        val period = if (hour < 12) "AM" else "PM"
        val hour12Format = if (hour > 12) hour - 12 else hour
        return String.format("%02d:%02d %s", hour12Format, minute, period)
    }

    private fun scheduleAutomaticBackup(frequency: String) {
        val calendar = Calendar.getInstance()
        calendar.set(
            Calendar.HOUR_OF_DAY,
            0
        )  // Ajusta la hora de acuerdo a la selección del usuario
        calendar.set(
            Calendar.MINUTE,
            0
        )      // Ajusta los minutos de acuerdo a la selección del usuario
        calendar.set(Calendar.SECOND, 0)

        // PendingIntent para iniciar el servicio que realizará el respaldo automático
        val intent = Intent(this, BackupActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Cancelar cualquier PendingIntent anterior
        alarmManager.cancel(pendingIntent)

        // Programar el respaldo automático según la frecuencia seleccionada
        when (frequency) {
            "Diario" -> {
                alarmManager.setRepeating(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    AlarmManager.INTERVAL_DAY,
                    pendingIntent
                )
            }

            "Semanal" -> {
                calendar.set(
                    Calendar.DAY_OF_WEEK,
                    Calendar.MONDAY
                ) // Ajusta el día de la semana según la selección del usuario
                alarmManager.setRepeating(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    AlarmManager.INTERVAL_DAY * 7,
                    pendingIntent
                )
            }

            "Mensual" -> {
                calendar.set(
                    Calendar.DAY_OF_MONTH,
                    1
                ) // Ajusta el día del mes según la selección del usuario
                alarmManager.setRepeating(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    AlarmManager.INTERVAL_DAY * 30, // Aproximadamente un mes
                    pendingIntent
                )
            }
        }

        Toast.makeText(
            this,
            "Respaldo automático programado correctamente: $frequency",
            Toast.LENGTH_SHORT
        ).show()
    }


    private fun cancelAutomaticBackup() {
        if (::pendingIntent.isInitialized) {  // Verifica si está inicializada antes de usarla
            // Cancelar el respaldo automático pendiente
            alarmManager.cancel(pendingIntent)
            Toast.makeText(
                this,
                "Respaldo automático cancelado",
                Toast.LENGTH_SHORT
            ).show()
        } else {
            Log.e(
                "BackupActivity",
                "PendingIntent no inicializado al intentar cancelar el respaldo automático"
            )
            Toast.makeText(
                this,
                "Error al cancelar el respaldo automático",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun restoreAutomaticBackupSettings() {
        val sharedPrefs = getSharedPreferences(PREFS_FILENAME, Context.MODE_PRIVATE)
        val hour = sharedPrefs.getInt(PREF_AUTOMATIC_BACKUP_HOUR, -1)
        val minute = sharedPrefs.getInt(PREF_AUTOMATIC_BACKUP_MINUTE, -1)

        if (hour != -1 && minute != -1) {
            // Aplicar la configuración de respaldo automático guardada
            val selectedTime = formatTime(hour, minute)
            editTextSelectedTime.setText("Hora seleccionada: $selectedTime")
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "BackupChannel"
            val descriptionText = "Canal para notificaciones de respaldo automático"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("backup_channel", name, importance).apply {
                description = descriptionText
            }
            // Registrar el canal en el sistema
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}
