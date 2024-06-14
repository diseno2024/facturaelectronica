package com.billsv.firmador.models.minec


class TempDate {
    var nano: String? = null
    var epochSecond: String? = null

    constructor(nano: String?, epochSecond: String?) : super() {
        this.nano = nano
        this.epochSecond = epochSecond
    }

    constructor() : super()
}

