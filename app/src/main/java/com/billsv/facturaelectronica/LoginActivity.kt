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
        val newPinEditText: EditText = dialogView.findViewById(R.id.newPinEditText)
        val newPinLabel: TextView = dialogView.findViewById(R.id.newPinLabel)

        // Inicialmente ocultamos el campo para crear el nuevo PIN
        newPinEditText.visibility = View.GONE
        newPinLabel.visibility = View.GONE

        val dialog = AlertDialog.Builder(this)
            .setTitle("Recuperación de PIN")
            .setView(dialogView)
            .setCancelable(false)
            .setNegativeButton("Cancelar") { dialogInterface: DialogInterface, _ ->
                dialogInterface.dismiss()
            }
            .setPositiveButton("Comprobar", null) // Usamos null para no cerrar automáticamente el diálogo
            .create()

        dialog.show()

        // Configuramos el botón de "Comprobar"
        val positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
        positiveButton.setOnClickListener {
            val nameCommercial = nameCommercialEditText.text.toString()
            val nrc = nrcEditText.text.toString()

            // Validar los datos introducidos
            if (validateData(nameCommercial, nrc)) {
                // Si los datos son correctos, mostrar el campo para el nuevo PIN
                newPinEditText.visibility = View.VISIBLE
                newPinLabel.visibility = View.VISIBLE

                // Cambiar el botón "Comprobar" por "Guardar PIN" y actualizamos su funcionalidad
                positiveButton.setText("Guardar PIN")
                positiveButton.setOnClickListener {
                    val newPin = newPinEditText.text.toString()

                    // Validar el nuevo PIN
                    if (!TextUtils.isEmpty(newPin) && newPin.length == 6) {
                        pinManager.addPin(newPin)
                        Toast.makeText(this, "Nuevo PIN guardado correctamente", Toast.LENGTH_SHORT).show()

                        // Cerrar el diálogo después de guardar el PIN
                        dialog.dismiss()
                    } else {
                        Toast.makeText(this, "El PIN debe tener exactamente 6 dígitos", Toast.LENGTH_SHORT).show()
                    }
                }
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
}