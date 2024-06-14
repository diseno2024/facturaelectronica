package com.billsv.firmador.models.minec


class PolicyQualifiers {
    var cpsUri: String
    var userNotice: String

    constructor(cpsUri: String, userNotice: String) : super() {
        this.cpsUri = cpsUri
        this.userNotice = userNotice
    }

    constructor() : super() {
        cpsUri = "www2.mh.gob.sv/dpc"
        userNotice = "CERTIFICADO PARA FACTURACIÓN ELECTRÓNICA"
    }
}

