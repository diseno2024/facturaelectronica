package com.billsv.firmador.utils

class ResponseBody {
    companion object {
        const val status_ok = "OK"
        const val status_error = "ERROR"
    }

    var status: String = ""
    var body: Any? = null

    constructor()

    constructor(status: String, body: Any?) {
        this.status = status
        this.body = body
    }
}