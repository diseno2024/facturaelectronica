package com.billsv.facturaelectronica

import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import android.widget.Button
import android.widget.TextView
import android.content.Intent
import android.util.Log
import com.couchbase.lite.*
import android.text.InputType
import android.text.TextUtils
import android.text.Editable
import android.text.TextWatcher
import com.billsv.facturaelectronica.ImportarClientes
import android.text.InputFilter

class LoginActivity : AppCompatActivity() {

    private lateinit var pinEditText: EditText
    private lateinit var loginButton: Button
    private lateinit var recoverPinButton: Button
    private lateinit var pinManager: PinManager
    private lateinit var database: Database

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        pinManager = PinManager(this)
        val app = application as MyApp
        database = app.database
        pinEditText = findViewById(R.id.pinEditText)
        loginButton = findViewById(R.id.loginButton)
        recoverPinButton = findViewById(R.id.recoverPinButton)

        // Configuramos el campo de PIN para que muestre los caracteres como contraseña
        pinEditText.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_PASSWORD

        // Configurar el botón de inicio de sesión
        loginButton.setOnClickListener {
            val enteredPin = pinEditText.text.toString()
            if (enteredPin.isEmpty()) {
                Toast.makeText(this, "Por favor, ingrese el PIN", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val savedPins = pinManager.loadPins()
            if (savedPins.isEmpty()) {
                Toast.makeText(this, "No hay PIN guardado", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (savedPins.contains(enteredPin)) {
                val intent = Intent(this, MenuActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "PIN incorrecto", Toast.LENGTH_SHORT).show()
            }
        }

        // Configurar el botón de recuperación de PIN
        recoverPinButton.setOnClickListener {
            showRecoverPinDialog()
        }
    }

    // Mostrar cuadro de diálogo para la recuperación del PIN
    private fun showRecoverPinDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_recover_pin, null)
        val nameCommercialEditText: EditText = dialogView.findViewById(R.id.nameCommercialEditText)
        val nrcEditText: EditText = dialogView.findViewById(R.id.nrcEditText)

        // Limitar el número de caracteres en el EditText a 9
        val maxDigits = 9
        nrcEditText.filters = arrayOf(InputFilter.LengthFilter(maxDigits))

        // Instancia de ImportarClientes para llamar a las funciones
        val importarClientes = ImportarClientes()

        val dialog = AlertDialog.Builder(this)
            .setTitle("Recuperación de PIN")
            .setView(dialogView)
            .setCancelable(false)
            .setNegativeButton("Cancelar") { dialogInterface: DialogInterface, _ ->
                dialogInterface.dismiss()
            }
            .setPositiveButton("Comprobar", null) // Usamos null para manejar el botón más adelante
            .create()

        dialog.show()

        // Agregar el TextWatcher al campo NRC para aplicar el formateo
        nrcEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                s?.let {
                    val formattedNrc = importarClientes.formatearNRC(it.toString())
                    // Si el NRC formateado es diferente al texto actual, actualizar el campo
                    if (formattedNrc != it.toString() && formattedNrc.length <= maxDigits) {
                        nrcEditText.removeTextChangedListener(this)
                        nrcEditText.setText(formattedNrc)
                        nrcEditText.setSelection(formattedNrc.length)
                        nrcEditText.addTextChangedListener(this)
                    }
                }
            }
        })

        // Configurar el botón de "Comprobar"
        val positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
        positiveButton.setOnClickListener {
            val nameCommercial = nameCommercialEditText.text.toString()
            val nrc = nrcEditText.text.toString()

            // Validar los datos introducidos
            if (validateData(nameCommercial, nrc)) {
                // Si los datos son correctos, mostrar el diálogo para crear el nuevo PIN
                dialog.dismiss() // Cerramos el diálogo actual
                showCreatePinDialog1() // Llamar al diálogo de creación de PIN
            } else {
                // Mostrar mensaje de error si los datos son incorrectos
                Toast.makeText(this, "Datos incorrectos. Inténtelo nuevamente.", Toast.LENGTH_SHORT).show()
            }
        }
    }


    // Validar los datos introducidos contra los almacenados en la base de datos
    private fun validateData(nameCommercial: String, nrc: String): Boolean {
        val query = QueryBuilder.select(SelectResult.expression(Meta.id))
            .from(DataSource.database(database))
            .where(
                Expression.property("tipo").equalTo(Expression.string("ConfEmisor"))
                    .and(Expression.property("nombreC").equalTo(Expression.string(nameCommercial)))
                    .and(Expression.property("nrc").equalTo(Expression.string(nrc)))
            )

        try {
            val resultSet = query.execute()
            return resultSet.count() > 0 // Si se encuentra el documento, los datos son correctos
        } catch (e: CouchbaseLiteException) {
            Log.e("LoginActivity", "Error al validar los datos de recuperación de PIN", e)
            return false
        }
    }

    private fun showCreatePinDialog1() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Crear nuevo PIN")

        // Crear una vista para el diálogo con un EditText
        val input = EditText(this)
        input.filters = arrayOf<InputFilter>(InputFilter.LengthFilter(6))
        input.inputType = InputType.TYPE_CLASS_NUMBER
        builder.setView(input)

        // Botón para confirmar la creación
        builder.setPositiveButton("Crear", null)

        // Botón de cancelar
        builder.setNegativeButton("Cancelar") { dialog, _ -> dialog.dismiss() }

        // Mostrar el diálogo
        val dialog = builder.create()
        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val newPin = input.text.toString()

                // Validar si el nuevo PIN tiene exactamente 6 dígitos
                if (newPin.length == 6) {
                    // Agregar el nuevo PIN en la base de datos
                    pinManager.addPin(newPin)

                    // Notificar al usuario que el PIN fue creado correctamente
                    Toast.makeText(this, "Nuevo PIN creado correctamente", Toast.LENGTH_SHORT).show()

                    // Cerrar el diálogo de creación
                    dialog.dismiss()

                    // Volver a mostrar la lista de PINs actualizada
                } else {
                    // Mostrar un mensaje de error si el PIN no es válido
                    Toast.makeText(this, "El PIN debe tener exactamente 6 dígitos. Inténtelo nuevamente.", Toast.LENGTH_SHORT).show()
                }
            }
        }

        dialog.show()
    }

}