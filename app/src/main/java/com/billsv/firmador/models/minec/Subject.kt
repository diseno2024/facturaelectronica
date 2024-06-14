package com.billsv.firmador.models.minec


class Subject {
    var countryName: String
    var organizationName: String? = null
    var organizationUnitName: String? = null
    var organizationIdentifier: String? = null
    var surname: String? = null
    var givenName: String? = null
    var commonName: String? = null
    var description: String? = null

    constructor(
        countryName: String,
        organizationName: String?,
        organizationUnitName: String?,
        organizationIdentifier: String?,
        surname: String?,
        givenName: String?,
        commonName: String?,
        description: String?
    ) : super() {
        this.countryName = countryName
        this.organizationName = organizationName
        this.organizationUnitName = organizationUnitName
        this.organizationIdentifier = organizationIdentifier
        this.surname = surname
        this.givenName = givenName
        this.commonName = commonName
        this.description = description
    }

    constructor() : super() {
        countryName = "EL SALVADOR"
    }
}

