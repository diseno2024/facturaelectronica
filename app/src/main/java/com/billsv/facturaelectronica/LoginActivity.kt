package com.billsv.facturaelectronica
import android.Manifest
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.InputFilter
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class LoginActivity : AppCompatActivity() {

    private lateinit var pinEditText: EditText
    private lateinit var loginButton: Button
    private lateinit var linkTextView: TextView

    private val PIN_FILE_NAME = "pins.json" // Nombre del archivo para guardar los pines
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
        setContentView(R.layout.activity_login)

        pinEditText = findViewById(R.id.pinEditText)
        loginButton = findViewById(R.id.loginButton)
        linkTextView = findViewById(R.id.linkTextView)

        // Verificar si es la primera vez que se instala la aplicación
        val sharedPreferences = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val isFirstTime = sharedPreferences.getBoolean("is_first_time", true)

        if (isFirstTime) {
            // Mostrar un cuadro de diálogo para crear un nuevo PIN
            showCreatePinDialog()
        } else {
            // Verificar si hay un PIN guardado
            val savedPins = loadPinsFromMenuActivity()
            if (savedPins.isEmpty()) {
                // Si no hay PIN guardado, mostrar mensaje para crear uno
                Toast.makeText(this@LoginActivity, "Por favor, crea un nuevo PIN", Toast.LENGTH_SHORT).show()
            }
        }

        loginButton.setOnClickListener {
            val enteredPin = pinEditText.text.toString()
            val savedPins = loadPinsFromMenuActivity()
            if (savedPins.contains(enteredPin)) {
                // PIN correcto, iniciar MenuActivity
                val intent = Intent(this@LoginActivity, MenuActivity::class.java)
                startActivity(intent)
                requestPermissions()
                finish() // Finalizar LoginActivity para evitar que el usuario regrese presionando el botón Atrás
            } else {
                // PIN incorrecto, mostrar mensaje de error
                Toast.makeText(this@LoginActivity, "PIN incorrecto", Toast.LENGTH_SHORT).show()
            }
        }

        setupClickableLink()
    }

    private fun showCreatePinDialog() {
        val dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder.setTitle("Crear PIN")
        dialogBuilder.setMessage("Por favor, crea un nuevo PIN (máximo 6 dígitos)")
        val input = EditText(this)
        input.filters = arrayOf<InputFilter>(InputFilter.LengthFilter(6)) // Máximo 6 dígitos
        input.inputType = android.text.InputType.TYPE_CLASS_NUMBER // Solo números
        dialogBuilder.setView(input)

        dialogBuilder.setPositiveButton("Guardar", DialogInterface.OnClickListener { dialog, which ->
            val enteredPin = input.text.toString()
            savePin(enteredPin)
            Toast.makeText(this@LoginActivity, "PIN creado correctamente", Toast.LENGTH_SHORT).show()

            // Guardar que ya no es la primera vez que se instala la aplicación
            val sharedPreferences = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.putBoolean("is_first_time", false)
            editor.apply()
        })

        dialogBuilder.setCancelable(false)
        dialogBuilder.show()
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

    private fun savePin(pin: String) {
        val sharedPreferences = getSharedPreferences("pins_prefs", Context.MODE_PRIVATE)
        val editor: SharedPreferences.Editor = sharedPreferences.edit()
        val savedPins = loadPinsFromMenuActivity()
        savedPins.add(pin)
        val pinsString = Gson().toJson(savedPins)
        editor.putString("pins", pinsString)
        editor.apply()
    }

    private fun setupClickableLink() {
        val text = "Leer mas sobre nosotros aquí"
        val spannableString = SpannableString(text)
        val clickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                val url = "https://bill-sv.netlify.app"
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                startActivity(intent)
            }
        }

        val start = text.indexOf("Leer mas sobre nosotros aquí")
        val end = start + "Leer mas sobre nosotros aquí".length
        spannableString.setSpan(clickableSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        linkTextView.text = spannableString
        linkTextView.movementMethod = LinkMovementMethod.getInstance()
    }

    private fun requestPermissions() {
        val permissionsToRequest = mutableListOf<String>()

        for (permission in permissionList) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    permission
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                permissionsToRequest.add(permission)
            }
        }

        if (permissionsToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                permissionsToRequest.toTypedArray(),
                WRITE_EXTERNAL_STORAGE_REQUEST_CODE
            )
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
                Toast.makeText(
                    this,
                    "Permisos Concedidos",
                    Toast.LENGTH_SHORT
                ).show()
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