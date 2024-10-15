package com.billsv.facturaelectronica
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class LoginActivity : AppCompatActivity() {

    private lateinit var pinEditText: EditText
    private lateinit var loginButton: Button
    private lateinit var pinManager: PinManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        pinManager = PinManager(this)

        pinEditText = findViewById(R.id.pinEditText)
        loginButton = findViewById(R.id.loginButton)

        // Limitar la longitud del PIN a 6 dígitos
        pinEditText.filters = arrayOf(android.text.InputFilter.LengthFilter(6))

        // Aplicar máscara de contraseña (números ocultos)
        pinEditText.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_PASSWORD

        // Configurar el botón de inicio de sesión
        loginButton.setOnClickListener {
            val enteredPin = pinEditText.text.toString()

            // Verificar si el campo de PIN está vacío
            if (enteredPin.isEmpty()) {
                Toast.makeText(this, "Por favor, ingrese el PIN", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Cargar la lista de PINs guardados en PinManager
            val savedPins = pinManager.loadPins()

            if (savedPins.isEmpty()) {
                Toast.makeText(this, "No hay PIN guardado", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Verificar si el PIN ingresado está en la lista de PINs guardados
            if (savedPins.contains(enteredPin)) {
                val intent = Intent(this, MenuActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "PIN incorrecto", Toast.LENGTH_SHORT).show()
            }
        }
    }
}