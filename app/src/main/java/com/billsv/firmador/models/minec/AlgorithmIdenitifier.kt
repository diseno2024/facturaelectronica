package com.billsv.firmador.models.minec

class AlgorithmIdenitifier {
    var algorithm: String
    var parameters: String?

    constructor() : super() {
        algorithm = "RSA"
        parameters = null
    }

    constructor(algorithm: String, parameters: String?) : super() {
        this.algorithm = algorithm
        this.parameters = parameters
    }
}

