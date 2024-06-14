package com.billsv.firmador.models.minec


class AccessDescription {
    var accessMethod: String? = null
    private var accessLocation: MutableList<String>

    constructor() : super() {
        accessMethod = ""
        accessLocation = ArrayList()
    }

    constructor(accessLocation: List<String?>?) : super() {
        this.accessLocation = accessLocation as MutableList<String>
    }

    constructor(accessMethod: String?, accessLocation: MutableList<String>) : super() {
        this.accessMethod = accessMethod
        this.accessLocation = accessLocation
    }

    fun addAccessLocation(accessLocation: String) {
        this.accessLocation.add(accessLocation)
    }

    fun getAccessLocation(): List<String> {
        return accessLocation
    }

    fun setAccessLocation(accessLocation: MutableList<String>) {
        this.accessLocation = accessLocation
    }
}

