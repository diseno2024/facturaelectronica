package com.billsv.firmador.models.minec


class ExtendedKeyUsage {
    var clientAuth: String
    var emailProtection: String

    constructor() : super() {
        clientAuth = ""
        emailProtection = ""
    }

    constructor(clientAuth: String, emailProtection: String) : super() {
        this.clientAuth = clientAuth
        this.emailProtection = emailProtection
    }
}