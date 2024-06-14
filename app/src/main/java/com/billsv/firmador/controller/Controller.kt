package com.billsv.firmador.controller


import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.billsv.firmador.utils.Mensaje

class Controller : AppCompatActivity() {

    // Instancia de Mensaje
    private lateinit var mensaje: Mensaje

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicializa el mensaje
        mensaje = Mensaje()

        // Usa el mensaje
        val respuesta = mensaje.ok("Mensaje de éxito")
        println(respuesta)
    }
}