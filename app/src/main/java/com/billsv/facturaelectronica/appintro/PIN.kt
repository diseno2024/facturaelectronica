package com.billsv.facturaelectronica.appintro

import android.content.Context
import android.os.Bundle
import android.text.InputFilter
import android.view.View
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.billsv.facturaelectronica.R

class PIN : Fragment() {

    private lateinit var pinEditText: EditText
    private lateinit var confirmPinEditText: EditText
    private lateinit var saveButton: Button

    // Declaramos la variable PinIngresadoCorrecto a nivel de la clase
    private var PinIngresadoCorrecto: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.activity_pin, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        pinEditText = view.findViewById(R.id.pinEditText)
        confirmPinEditText = view.findViewById(R.id.confirmPinEditText)
        saveButton = view.findViewById(R.id.saveButton)

        // Limitar la longitud del PIN a 6 dígitos
        //pinEditText.filters = arrayOf(InputFilter.LengthFilter(6))
        //confirmPinEditText.filters = arrayOf(InputFilter.LengthFilter(6))

        // Configurar el botón de guardar
        saveButton.setOnClickListener {
            val pin = pinEditText.text.toString()
            val confirmPin = confirmPinEditText.text.toString()

            // Verificar que ambos campos no estén vacíos
            if (pin.isEmpty() || confirmPin.isEmpty()) {
                Toast.makeText(context, "Ingrese y confirme el PIN", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Verificar que ambos pin no sean mayores a  6 dígitos
            if (pin.length > 6 || confirmPin.length > 6) {
                Toast.makeText(context, "El PIN debe tener a lo mucho 6 dígitos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Verificar que ambos PINs coincidan
            if (pin != confirmPin) {
                Toast.makeText(context, "Los PINs no coinciden", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Guardar si todo está bien
            savePin(pin)

            // Mostrar mensaje de éxito
            Toast.makeText(context, "¡PIN creado correctamente!", Toast.LENGTH_SHORT).show()

            // Verifica en la variable global que el PIN está correcto
            PinIngresadoCorrecto = true

        }
    }

    // Función para verificar si el PIN es correcto
    fun PinCorrecto(): Boolean {
        return PinIngresadoCorrecto
    }

    private fun savePin(pin: String) {
        // Guardar el PIN en SharedPreferences
        val sharedPreferences = activity?.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences?.edit()
        editor?.putString("user_pin", pin)
        editor?.apply()
    }

    companion object {
        fun newInstance(): PIN {
            return PIN()
        }
    }
}
