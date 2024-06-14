package com.billsv.firmador.models.minec


class SubjectAlternativeNames {
    var rfc822Name: String

    constructor() : super() {
        rfc822Name = ""
    }

    constructor(rfc822Name: String) : super() {
        this.rfc822Name = rfc822Name
    }
}

