package com.billsv.firmador.models.minec

import java.util.LinkedList


class CertificatePolicies {
    private var policyInformations: MutableList<PolicyInformation>

    constructor() : super() {
        policyInformations = LinkedList()
    }

    constructor(policyInformations: MutableList<PolicyInformation>) : super() {
        this.policyInformations = policyInformations
    }

    fun add(policyInformation: PolicyInformation) {
        policyInformations.add(policyInformation)
    }

    fun getPolicyInformations(): List<PolicyInformation> {
        return policyInformations
    }

    fun setPolicyInformations(policyInformations: MutableList<PolicyInformation>) {
        this.policyInformations = policyInformations
    }
}

