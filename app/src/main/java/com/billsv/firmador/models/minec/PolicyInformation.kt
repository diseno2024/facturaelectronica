package com.billsv.firmador.models.minec


class PolicyInformation {
    var policyIdentifier: String
    var policyQualifiers: PolicyQualifiers

    constructor() : super() {
        policyIdentifier = ""
        policyQualifiers = PolicyQualifiers()
    }

    constructor(policyIdentifier: String, policyQualifiers: PolicyQualifiers) : super() {
        this.policyIdentifier = policyIdentifier
        this.policyQualifiers = policyQualifiers
    }
}

