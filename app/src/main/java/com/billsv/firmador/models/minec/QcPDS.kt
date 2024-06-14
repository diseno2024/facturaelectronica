package com.billsv.firmador.models.minec


class QcPDS {
    var pdsLocation: String?
    var url: String
    var language: String

    constructor() : super() {
        pdsLocation = null
        url = "https://www2.mh.gob.sv/pds"
        language = "ES"
    }

    constructor(pdsLocation: String?, url: String, language: String) : super() {
        this.pdsLocation = pdsLocation
        this.url = url
        this.language = language
    }
}

