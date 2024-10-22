package com.billsv.facturaelectronica.appintro

import android.os.Bundle
import android.view.View
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.billsv.facturaelectronica.R
import com.billsv.facturaelectronica.PinManager
import android.text.InputFilter
import android.text.InputType

class PIN : Fragment() {

    private lateinit var pinEditText: EditText
    private lateinit var confirmPinEditText: EditText
    private var isPinSaved = false // Variable para controlar si el PIN ya ha sido guardado

    // Variable para verificar si el PIN es correcto
    private var PinIngresadoCorrecto: Boolean = false

    // Variables para controlar si los mensajes se han mostrado
    private var pinEmptyShown = false
    private var pinLengthErrorShown = false
    private var pinMismatchErrorShown = false
    private var pinCreatedShown = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.activity_pin, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        pinEditText = view.findViewById(R.id.pinEditText)
        confirmPinEditText = view.findViewById(R.id.confirmPinEditText)

        // Configuración para que solo se ingresen 6 dígitos numéricos con la máscara de contraseña
        pinEditText.filters = arrayOf<InputFilter>(InputFilter.LengthFilter(6))
        confirmPinEditText.filters = arrayOf<InputFilter>(InputFilter.LengthFilter(6))
        pinEditText.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_PASSWORD
        confirmPinEditText.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_PASSWORD

    }

    // Esta función se encargará de validar y guardar el PIN al cambiar a la siguiente diapositiva
    fun validarYGuardarPin() {
        val pin = pinEditText.text.toString()
        val confirmPin = confirmPinEditText.text.toString()

        // Verificar si alguno de los campos está vacío y mostrar el mensaje solo una vez
        if (pin.isEmpty() || confirmPin.isEmpty()) {
            if (!pinEmptyShown) {
                Toast.makeText(context, "Ingrese y confirme el PIN", Toast.LENGTH_SHORT).show()
                pinEmptyShown = true  // Marcar el mensaje como mostrado
            }
            return
        }

        // Verificar si la longitud del PIN es incorrecta y mostrar el mensaje solo una vez
        if (pin.length != 6 || confirmPin.length != 6) {
            if (!pinLengthErrorShown) {
                Toast.makeText(context, "El PIN debe tener exactamente 6 dígitos", Toast.LENGTH_SHORT).show()
                pinLengthErrorShown = true  // Marcar el mensaje como mostrado
            }
            return
        }

        // Verificar si los PINs no coinciden y mostrar el mensaje solo una vez
        if (pin != confirmPin) {
            if (!pinMismatchErrorShown) {
                Toast.makeText(context, "Los PINs no coinciden", Toast.LENGTH_SHORT).show()
                pinMismatchErrorShown = true  // Marcar el mensaje como mostrado
            }
            return
        }

        // Si todo es correcto, guardar el PIN
        val pinManager = PinManager(requireContext())
        pinManager.addPin(pin)

        // Mostrar el mensaje de éxito solo una vez
        if (!pinCreatedShown) {
            Toast.makeText(context, "¡PIN creado correctamente!", Toast.LENGTH_SHORT).show()
            pinCreatedShown = true  // Marcar el mensaje como mostrado
        }

        PinIngresadoCorrecto = true
        isPinSaved = true
    }

    fun PinCorrecto(): Boolean {
        return PinIngresadoCorrecto
    }

    companion object {
        fun newInstance(): PIN {
            return PIN()
        }
    }
}