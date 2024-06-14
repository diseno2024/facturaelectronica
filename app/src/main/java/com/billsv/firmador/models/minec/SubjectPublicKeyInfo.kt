package com.billsv.firmador.models.minec


class SubjectPublicKeyInfo {
    var algorithmIdenitifier: AlgorithmIdenitifier
    var subjectPublicKey: ByteArray?

    constructor() : super() {
        algorithmIdenitifier = AlgorithmIdenitifier()
        subjectPublicKey = null
    }

    constructor(subjectPublicKey: ByteArray?) : super() {
        algorithmIdenitifier = AlgorithmIdenitifier()
        this.subjectPublicKey = subjectPublicKey
    }

    constructor(
        algorithmIdenitifier: AlgorithmIdenitifier,
        subjectPublicKey: ByteArray?
    ) : super() {
        this.algorithmIdenitifier = algorithmIdenitifier
        this.subjectPublicKey = subjectPublicKey
    }
}

