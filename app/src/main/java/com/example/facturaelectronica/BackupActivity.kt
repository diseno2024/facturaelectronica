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
import android.widget.Button
import android.widget.EditText
import android.util.Log
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import java.util.*



class BackupActivity : AppCompatActivity() {

    private lateinit var buttonSelectTime: Button
    private lateinit var editTextSelectedTime: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_backup) // Asegúrate de que este sea el nombre correcto de tu layout

        val switchEncendido = findViewById<Switch>(R.id.switchEncendido)
        val textEstado = findViewById<TextView>(R.id.textEstado)

        switchEncendido.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                textEstado.text = "Encendido"
            } else {
                textEstado.text = "Apagado"
            }
        }

        val spinnerFrecuencia = findViewById<Spinner>(R.id.spinnerFrecuencia)

        // Obtén las opciones del StringArray en strings.xml
        val opciones = resources.getStringArray(R.array.frecuencia_options)

        // Crea un adaptador usando las opciones y un diseño predeterminado para los elementos del Spinner
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, opciones)

        // Especifica el diseño para los elementos desplegables
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        // Establece el adaptador en el Spinner
        spinnerFrecuencia.adapter = adapter

        // Implementa un listener si necesitas manejar las selecciones del usuario
        spinnerFrecuencia.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                // Aquí puedes realizar acciones basadas en la opción seleccionada
                val opcionSeleccionada = opciones[position]
                // Por ejemplo, puedes mostrar un mensaje con la opción seleccionada
                Toast.makeText(applicationContext, "Seleccionaste: $opcionSeleccionada", Toast.LENGTH_SHORT).show()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Implementa acciones si no se selecciona nada, si es necesario
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
        val buttonAtras = findViewById<Button>(R.id.button1)
        buttonAtras.setOnClickListener {
            // Iniciar la actividad MenuActivity
            val intent = Intent(this, MenuActivity::class.java)
            startActivity(intent)
        }
    }
}









