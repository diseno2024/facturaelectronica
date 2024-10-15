package com.billsv.facturaelectronica
import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import android.util.Log

class PinManager(context: Context) {

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(PIN_PREFS_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()

    companion object {
        private const val PIN_PREFS_NAME = "PinPrefs"
        private const val PINS_KEY = "pins"
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