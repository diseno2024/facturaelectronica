package com.billsv.facturaelectronica

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import android.util.Log
import com.couchbase.lite.*

class PinManager(context: Context) {

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(PIN_PREFS_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()
    private lateinit var database: Database

    companion object {
        private const val PIN_PREFS_NAME = "PinPrefs"
        private const val PINS_KEY = "pins"
    }

    init {
        // Inicializar la base de datos aquí
        val app = context.applicationContext as MyApp
        database = app.database
    }

    fun loadPins(): MutableList<String> {
        val pinsString = sharedPreferences.getString(PINS_KEY, null)

        return if (pinsString != null) {
            // Define el tipo de lista usando TypeToken
            val type = object : TypeToken<MutableList<String>>() {}.type
            val pins: MutableList<String> = gson.fromJson(pinsString, type) ?: mutableListOf()
            Log.d("PinManager", "PINs cargados: $pins") // Log para los PINs cargados
            pins
        } else {
            Log.d("PinManager", "No se encontraron PINs guardados.")
            mutableListOf()
        }
    }

    fun removePin(pin: String) {
        val pinsList = loadPins().toMutableList()

        // Eliminar el PIN si existe en la lista
        if (pinsList.contains(pin)) {
            pinsList.remove(pin)
            savePins(pinsList)
        }
    }

    fun updatePin(oldPin: String, newPin: String) {
        val pinsList = loadPins().toMutableList()

        // Verifica si el PIN antiguo existe en la lista
        if (pinsList.contains(oldPin)) {
            // Encuentra el índice del PIN antiguo y lo reemplaza con el nuevo
            val index = pinsList.indexOf(oldPin)
            pinsList[index] = newPin

            // Guarda la lista actualizada en SharedPreferences
            savePins(pinsList)
            Log.d("PinManager", "PIN actualizado correctamente de $oldPin a $newPin")
        } else {
            Log.e("PinManager", "El PIN '$oldPin' no se encontró en la lista.")
        }
    }


    fun addPin(pin: String) {
        val pins = loadPins()
        pins.add(pin)
        savePins(pins)
        Log.d("PinManager", "Se ha guardado el PIN: $pin. Lista actual: $pins") // Log para el nuevo PIN guardado
    }

    private fun savePins(pins: List<String>) {
        val pinsString = gson.toJson(pins)
        sharedPreferences.edit().putString(PINS_KEY, pinsString).apply()
        Log.d("PinManager", "PINs guardados: $pins") // Log para los PINs guardados
    }
}