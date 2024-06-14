package com.billsv.firmador.models.minec


class SignatureAlgorithm {
    var algorithm: String
    var parameters: String?

    constructor() : super() {
        algorithm = "Sha256WithRSAEncryption"
        parameters = null
    }

    constructor(algorithm: String, parameters: String?) : super() {
        this.algorithm = algorithm
        this.parameters = parameters
    }
}

