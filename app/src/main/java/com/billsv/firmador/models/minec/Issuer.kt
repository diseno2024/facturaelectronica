package com.billsv.firmador.models.minec
class Issuer {
    var countryName: String
    var localilyName: String
    var organizationalUnit: String
    var organizationalName: String
    var commonName: String
    var organizationIdentifier: String

    constructor() : super() {
        countryName = "SV"
        localilyName = "SAN SALVADOR"
        organizationalUnit = "MINISTERIO DE HACIENDA"
        organizationalName = "DIRECCIÓN GENERAL DE IMPUESTOS INTERNOS"
        commonName =
            "UNIDAD COORDINADORA DEL PROGAMA FORTALECIMIENTO A LA ADMINISTRACIÓN TRIBUTARÍA"
        organizationIdentifier = "VATSV-0614-010111-003-2"
    }

    constructor(
        countryName: String,
        localilyName: String,
        organizationalUnit: String,
        organizationalName: String,
        commonName: String,
        organizationIdentifier: String
    ) : super() {
        this.countryName = countryName
        this.localilyName = localilyName
        this.organizationalUnit = organizationalUnit
        this.organizationalName = organizationalName
        this.commonName = commonName
        this.organizationIdentifier = organizationIdentifier
    }
}

