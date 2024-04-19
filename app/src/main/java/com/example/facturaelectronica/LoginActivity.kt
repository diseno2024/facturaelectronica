package com.example.facturaelectronica


import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class LoginActivity : AppCompatActivity() {

    private lateinit var pinEditText: EditText
    private lateinit var loginButton: Button

    private val DEFAULT_PIN = "123456" // PIN predeterminado
    private val PIN_FILE_NAME = "pins.json" // Nombre del archivo para guardar los pines

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        pinEditText = findViewById(R.id.pinEditText)
        loginButton = findViewById(R.id.loginButton)

        loginButton.setOnClickListener {
            val enteredPin = pinEditText.text.toString()
            val savedPins = loadPinsFromMenuActivity()

            if (enteredPin == DEFAULT_PIN || savedPins.contains(enteredPin)) {
                // PIN correcto, iniciar MenuActivity
                val intent = Intent(this@LoginActivity, MenuActivity::class.java)
                startActivity(intent)
                finish() // Finalizar LoginActivity para evitar que el usuario regrese presionando el botón Atrás
            } else {
                // PIN incorrecto, mostrar mensaje de error
                Toast.makeText(this@LoginActivity, "PIN incorrecto", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadPinsFromMenuActivity(): MutableList<String> {
        val sharedPreferences = getSharedPreferences("pins_prefs", Context.MODE_PRIVATE)
        val pinsString = sharedPreferences.getString("pins", null)
        return if (pinsString != null) {
            Gson().fromJson(pinsString, object : TypeToken<List<String>>() {}.type)
        } else {
            mutableListOf()
        }
    }
}

