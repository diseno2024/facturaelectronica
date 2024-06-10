package com.billsv.facturaelectronica

import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.UUID

class ConfHacienda : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_conf_hacienda)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val uuid: TextView = findViewById(R.id.textView55)
        val uuid2: TextView = findViewById(R.id.textView57)
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        // Formatea la fecha y la hora
        val formattedDate = dateFormat.format(calendar.time)
        uuid.text = formattedDate
        val formattedTime = timeFormat.format(calendar.time)
        uuid2.text = formattedTime
        //https://apitest.dtes.mh.gob.sv/seguridad/auth
        //https://api.dtes.mh.gob.sv/seguridad/authw
    }
    private fun generarUUIDv4(): String {
        val uuid = UUID.randomUUID()
        return uuid.toString().toUpperCase()
    }

}