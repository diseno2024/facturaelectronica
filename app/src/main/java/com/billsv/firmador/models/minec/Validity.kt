package com.billsv.firmador.models.minec

import java.util.Calendar
import java.util.Date


class Validity {
    var notBefore: Date
    var notAfter: Date

    constructor(notBefore: Date, notAfter: Date) {
        this.notBefore = notBefore
        this.notAfter = notAfter
    }

    constructor() {
        notBefore = Date()
        val calendar = Calendar.getInstance()
        calendar.time = notBefore
        calendar.add(Calendar.DAY_OF_YEAR, 1825)
        notAfter = calendar.time
    }
}