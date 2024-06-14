package com.billsv.firmador.models.minec


class KeyUsage {
    var digitalSignature: Short
    var contentCommintment: Short
    var dataEncipherment: Short
    var keyAgreement: Short
    var keyCertificateSignature: Short
    var crlSignature: Short
    var encipherOnly: Short
    var decipherOnly: Short

    constructor() : super() {
        digitalSignature = 1
        contentCommintment = 1
        dataEncipherment = 0
        keyAgreement = 0
        keyCertificateSignature = 0
        crlSignature = 0
        encipherOnly = 0
        decipherOnly = 0
    }

    constructor(
        digitalSignature: Short,
        contentCommintment: Short,
        dataEncipherment: Short,
        keyAgreement: Short,
        keyCertificateSignature: Short,
        crlSignature: Short,
        encipherOnly: Short,
        decipherOnly: Short
    ) : super() {
        this.digitalSignature = digitalSignature
        this.contentCommintment = contentCommintment
        this.dataEncipherment = dataEncipherment
        this.keyAgreement = keyAgreement
        this.keyCertificateSignature = keyCertificateSignature
        this.crlSignature = crlSignature
        this.encipherOnly = encipherOnly
        this.decipherOnly = decipherOnly
    }
}

