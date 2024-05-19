package com.example.facturaelectronica

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.couchbase.lite.CouchbaseLiteException
import com.couchbase.lite.MutableDocument
import com.couchbase.lite.Database

class PrinReClienteActivity : AppCompatActivity() {
    private lateinit var spinnerDep: Spinner
    private lateinit var spinnerMun: Spinner
    private lateinit var nombre: EditText
    private lateinit var nit: EditText
    private lateinit var email: EditText
    private lateinit var direccion: EditText
    private lateinit var telefono: EditText
    private lateinit var departamento: Spinner
    private lateinit var municipio: Spinner
    private lateinit var agregarButton: Button
    private lateinit var cancelarButton: Button
    private lateinit var database: Database


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_prin_re_cliente)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Inicializa el Spinner de departamento
        spinnerDep = findViewById(R.id.departamento)
        val departamentos = arrayOf("Ahuachapán", "Cabañas", "Chalatenango", "Cuscatlán", "La Libertad", "La paz", "La Unión", "Morazán", "San Miguel", "San Vicente", "Santa Ana", "Sonsonate", "Usulután")

        // Configura el adaptador para el Spinner de departamento
        val adapterDep = ArrayAdapter(this, R.layout.spinner_personalizado, departamentos)
        adapterDep.setDropDownViewResource(R.layout.spinner_dropdown_per)
        spinnerDep.adapter = adapterDep

        // Inicializa el Spinner de municipio
        spinnerMun = findViewById(R.id.municipio)
        val municipios = arrayOf("Municipios", "de", "cada", "departamento")

        // Configura el adaptador para el Spinner de municipio
        val adapterMun = ArrayAdapter(this, R.layout.spinner_personalizado, municipios)
        adapterMun.setDropDownViewResource(R.layout.spinner_dropdown_per)
        spinnerMun.adapter = adapterMun

        // Inicializar vistas
        nombre = findViewById(R.id.nombre)
        nit = findViewById(R.id.nit)
        email = findViewById(R.id.email)
        direccion = findViewById(R.id.direccion)
        departamento = findViewById(R.id.departamento)
        municipio = findViewById(R.id.municipio)
        telefono = findViewById(R.id.telefono)
        agregarButton = findViewById(R.id.btnAgregar)
        cancelarButton = findViewById(R.id.btnCancelar)

        // Agregar TextWatcher para el campo de teléfono
        telefono.addTextChangedListener(object : TextWatcher {
            private var isUpdating = false
            private val mask = "####-####"

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (isUpdating) return

                isUpdating = true
                val formatted = formatPhoneNumber(s.toString())
                telefono.setText(formatted)
                telefono.setSelection(formatted.length)
                isUpdating = false
            }

            private fun formatPhoneNumber(phone: String): String {
                val digits = phone.replace(Regex("\\D"), "")
                val formatted = StringBuilder()

                var i = 0
                for (m in mask.toCharArray()) {
                    if (m != '#') {
                        formatted.append(m)
                        continue
                    }
                    if (i >= digits.length) break
                    formatted.append(digits[i])
                    i++
                }
                return formatted.toString()
            }

        })
        nit.addTextChangedListener(object : TextWatcher {
            private var isUpdating = false
            private val mask = "####-######-###-#" // La máscara del NIT

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (isUpdating) return

                isUpdating = true
                val formatted = formatNIT(s.toString())
                nit.setText(formatted)
                nit.setSelection(formatted.length)
                isUpdating = false
            }

            private fun formatNIT(nit: String): String {
                // Eliminar todos los caracteres no numéricos del NIT
                val digits = nit.replace(Regex("\\D"), "")
                val formatted = StringBuilder()

                var i = 0
                for (m in mask.toCharArray()) {
                    if (m != '#') {
                        formatted.append(m)
                        continue
                    }
                    if (i >= digits.length) break
                    formatted.append(digits[i])
                    i++
                }
                return formatted.toString()
            }
        })


        // Configurar evento de clic para el botón "Agregar"
        agregarButton.setOnClickListener {
            if (validarEntradas()) {
                guardarInformacion()
                // Limpiar los EditText
                nombre.text.clear()
                nit.text.clear()
                email.text.clear()
                direccion.text.clear()
                telefono.text.clear()

                // Iniciar otra actividad
                val intent = Intent(this, EmitirCFActivity::class.java)
                startActivity(intent)
            }
        }

        cancelarButton.setOnClickListener {
            // Iniciar otra actividad
            val intent = Intent(this, EmitirCFActivity::class.java)
            startActivity(intent)
        }

        // Inicializar la base de datos
        val app = application as MyApp
        database = app.database
    }

    private fun validarEntradas(): Boolean {
        val nombreText = nombre.text.toString()
        val nitText = nit.text.toString().replace("-", "")
        val emailText = email.text.toString()
        val direccionText = direccion.text.toString()
        val telefonoText = telefono.text.toString().replace("-", "")

        // Verifica que todos los campos estén llenos
        if (nombreText.isEmpty() || nitText.isEmpty() || emailText.isEmpty() ||  telefonoText.isEmpty()) {
            Toast.makeText(this, "Todos los campos son obligatorios", Toast.LENGTH_SHORT).show()
            return false
        }

        // Verifica que el correo electrónico tenga un formato válido
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(emailText).matches()) {
            Toast.makeText(this, "Correo electrónico no válido", Toast.LENGTH_SHORT).show()
            return false
        }

        // Verifica que el NIT sea un número válido
        if (!nitText.matches(Regex("\\d{14}"))) {
            Toast.makeText(this, "NIT debe ser un número válido", Toast.LENGTH_SHORT).show()
            return false
        }

        // Verifica que el teléfono sea un número válido de 8 dígitos
        if (!telefonoText.matches(Regex("\\d{8}"))) {
            Toast.makeText(this, "Teléfono debe ser un número válido de 8 dígitos", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    private fun guardarInformacion() {
        val nombreText = nombre.text.toString()
        val nitText = nit.text.toString()
        val emailText = email.text.toString()
        val direccionText = direccion.text.toString()
        val departamentoText = departamento.selectedItem.toString()
        val municipioText = municipio.selectedItem.toString()
        val telefonoText = telefono.text.toString().replace("-", "")


        // Crear un documento mutable para guardar en la base de datos
        val document = MutableDocument()
            .setString("nombre", nombreText)
            .setString("nit", nitText)
            .setString("email", emailText)
            .setString("direccion", direccionText)
            .setString("departamento", departamentoText)
            .setString("municipio", municipioText)
            .setString("telefono", telefonoText)
            .setString("tipo", "cliente")

        try {
            // Guardar el documento en la base de datos
            database.save(document)
            Log.d("ReClienteActivity", "Datos guardados correctamente: \n $document")
            Toast.makeText(this, "Datos guardados correctamente", Toast.LENGTH_SHORT).show()
        } catch (e: CouchbaseLiteException) {
            Log.e("ReClienteActivity", "Error al guardar los datos en la base de datos: ${e.message}", e)
            Toast.makeText(this, "Error al guardar los datos", Toast.LENGTH_SHORT).show()
        }
    }
}