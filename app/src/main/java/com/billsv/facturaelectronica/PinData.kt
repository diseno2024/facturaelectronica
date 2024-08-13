package com.billsv.facturaelectronica

data class PinData(val pins: MutableList<String>) {
    fun addPin(pin: String) {
        pins.add(pin)
    }

    fun removePin(pin: String) {
        pins.remove(pin)
    }

    fun clearPins() {
        pins.clear()
    }
}