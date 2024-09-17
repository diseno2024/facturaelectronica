package com.billsv.facturaelectronica

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.InputFilter
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class LoginActivity : AppCompatActivity() {

    private lateinit var pinEditText: EditText
    private lateinit var loginButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        pinEditText = findViewById(R.id.pinEditText)
        loginButton = findViewById(R.id.loginButton)

        // Limitar la longitud del PIN a 6 dígitos
        pinEditText.filters = arrayOf(InputFilter.LengthFilter(6))

        // Configurar el botón de inicio de sesión
        loginButton.setOnClickListener {
            val enteredPin = pinEditText.text.toString()

            // Verificar si el campo de PIN está vacío
            if (enteredPin.isEmpty()) {
                Toast.makeText(this, "Por favor, ingrese el PIN", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Cargar el PIN guardado en SharedPreferences
            val savedPin = getSavedPin()

            if (savedPin.isNullOrEmpty()) {
                Toast.makeText(this, "No hay PIN guardado", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Verificar si el PIN ingresado coincide con el PIN guardado
            if (enteredPin == savedPin) {
                // Redirigir al usuario a la pantalla principal si el PIN es correcto
                val intent = Intent(this, MenuActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "PIN incorrecto", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getSavedPin(): String? {
        // Obtener el PIN guardado desde SharedPreferences
        val sharedPreferences = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        return sharedPreferences.getString("user_pin", null)
    }
}
