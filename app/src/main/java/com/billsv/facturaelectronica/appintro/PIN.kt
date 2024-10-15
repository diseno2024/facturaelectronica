package com.billsv.facturaelectronica.appintro

import android.os.Bundle
import android.view.View
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.billsv.facturaelectronica.R
import com.billsv.facturaelectronica.PinManager

class PIN : Fragment() {

    private lateinit var pinEditText: EditText
    private lateinit var confirmPinEditText: EditText
    private lateinit var saveButton: Button
    private var isPinSaved = false // Variable para controlar si el PIN ya ha sido guardado

    // Variable para verificar si el PIN es correcto
    private var PinIngresadoCorrecto: Boolean = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.activity_pin, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        pinEditText = view.findViewById(R.id.pinEditText)
        confirmPinEditText = view.findViewById(R.id.confirmPinEditText)
        saveButton = view.findViewById(R.id.saveButton)

        // Configurar el botón de guardar
        saveButton.setOnClickListener {
            if (isPinSaved) {
                // Mostrar mensaje si ya se ha creado un PIN
                Toast.makeText(context, "¡Ya tienes un PIN configurado! Si necesitas crear uno nuevo, puedes hacerlo desde el menú.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val pin = pinEditText.text.toString()
            val confirmPin = confirmPinEditText.text.toString()

            if (pin.isEmpty() || confirmPin.isEmpty()) {
                Toast.makeText(context, "Ingrese y confirme el PIN", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (pin.length != 6 || confirmPin.length != 6) {
                Toast.makeText(context, "El PIN debe tener exactamente 6 dígitos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (pin != confirmPin) {
                Toast.makeText(context, "Los PINs no coinciden", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Guardar el PIN
            val pinManager = PinManager(requireContext())
            pinManager.addPin(pin)

            Toast.makeText(context, "¡PIN creado correctamente!", Toast.LENGTH_SHORT).show()
            PinIngresadoCorrecto = true
            isPinSaved = true // Establecer que el PIN ya ha sido guardado
        }
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