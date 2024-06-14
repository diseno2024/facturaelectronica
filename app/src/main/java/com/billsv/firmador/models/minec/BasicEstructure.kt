package com.billsv.firmador.models.minec

import com.billsv.firmador.models.Llave


class BasicEstructure {
    var version: String? = null
    var serial: String? = null
    var signatureAlgorithm: SignatureAlgorithm? = null
    var issuer: Issuer? = null
    var validity: Validity? = null
    var subject: Subject? = null
    var subjectPublicKeyInfo: SubjectPublicKeyInfo? = null

    constructor() : super()
    constructor(subject: Subject?, llave: Llave) : super() {
        version = "2"
        serial = "1.2.840.113549.1.1.11"
        signatureAlgorithm = SignatureAlgorithm()
        issuer = Issuer()
        validity = Validity()
        this.subject = subject
        subjectPublicKeyInfo = SubjectPublicKeyInfo(
            AlgorithmIdenitifier(llave.algorithm!!, null),
            llave.encodied
        )
    }

    constructor(
        version: String?, serial: String?, signatureAlgorithm: SignatureAlgorithm?, issuer: Issuer?,
        validity: Validity?, subject: Subject?, subjectPublicKeyInfo: SubjectPublicKeyInfo?
    ) : super() {
        this.version = version
        this.serial = serial
        this.signatureAlgorithm = signatureAlgorithm
        this.issuer = issuer
        this.validity = validity
        this.subject = subject
        this.subjectPublicKeyInfo = subjectPublicKeyInfo
    }
}