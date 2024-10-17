package com.billsv.facturaelectronica

data class Factura(
    val nombre: String,
    val numeroControl: String,
    val dui: String,
    val nit: String,
    val nrc: String,
    val fecha: String,
    val codActividad: String,
    val desAcEco: String,
    val correo: String,
    val departamento: String,
    val municipio: String,
    val complemento: String,
    val sello: String,
    val articulos: String?,
    val codigoG: String,
    val telefono: String,
    val totalNosuj: Double,
    val totalExenta: Double,
    val totalGravada: Double,
    val total: Double,
    val iva : Double,
    val condicion : String
)
