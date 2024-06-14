package com.billsv.firmador.utils

class ResponseBody {
    var status: String? = null
    var body: Any? = null

    companion object {
        var status_ok = "OK"
        var status_error = "ERROR"
    }
}

