package com.billsv.facturaelectronica
data class Identificacion(
    val version: Int,
    val ambiente: String?,
    val tipoDte: String?,
    val numeroControl: String?,
    val codigoGeneracion: String?,
    val tipoModelo: Int,
    val tipoOperacion: Int,
    val tipoContingencia: String?,
    val motivoContin: String?,
    val fecEmi: String?,
    val horEmi: String?,
    val tipoMoneda: String?
)

data class Direccion(
    val departamento: String?,
    val municipio: String?,
    val complemento: String?
)

data class Emisor(
    val nit: String?,
    val nrc: String?,
    val nombre: String?,
    val codActividad: String?,
    val descActividad: String?,
    val nombreComercial: String?,
    val tipoEstablecimiento: String?,
    val direccion: Direccion,
    val telefono: String?,
    val correo: String?,
    val codEstableMH: String?,
    val codEstable: String?,
    val codPuntoVentaMH: String?,
    val codPuntoVenta: String?,
)

data class Receptor(
    val tipoDocumento: String?,
    val numDocumento: String?,
    val nrc: String?,
    val nombre: String?,
    val codActividad: String?,
    val descActividad: String?,
    val direccion: Direccion?,
    val telefono: String?,
    val correo: String?
)

data class CuerpoDocumento(
    val numItem: Int?,
    val tipoItem: Int?,
    val numeroDocumento: String?,
    val cantidad: Double?,
    val codigo: String?,
    val codTributo: String?,
    val uniMedida: Int?,
    val descripcion: String?,
    val precioUni: Double?,
    val montoDescu: Double?,
    val ventaNoSuj: Double?,
    val ventaExenta: Double?,
    val ventaGravada: Double?,
    val tributos: String?,
    val psv: Double?,
    val noGravado: Double?,
    val ivaItem: Double?,
)

data class Pago(
    val codigo: String?,
    val montoPago: Double?,
    val referencia: String?,
    val plazo: String?,
    val periodo: Int?
)

data class Resumen(
    val totalNoSuj: Double?,
    val totalExenta: Double?,
    val totalGravada: Double?,
    val subTotalVentas: Double?,
    val descuNoSuj: Double?,
    val descuExenta: Double?,
    val descuGravada: Double?,
    val porcentajeDescuento: Double?,
    val totalDescu: Double?,
    val tributos: String?,
    val subTotal: Double?,
    val ivaRete1: Double?,
    val reteRenta: Double?,
    val montoTotalOperacion: Double?,
    val totalNoGravado: Double?,
    val totalPagar: Double?,
    val totalLetras: String?,
    val totalIva: Double?,
    val saldoFavor: Double?,
    val condicionOperacion: Int?,
    val pagos: List<Pago>?,
    val numPagoElectronico: String?
)

data class Extension(
    val nombEntrega: String?,
    val docuEntrega: String?,
    val nombRecibe: String?,
    val docuRecibe: String?,
    val observaciones: String?,
    val placaVehiculo: String?,
)

data class Documento(
    val identificacion: Identificacion,
    val documentoRelacionado: String?,
    val emisor: Emisor,
    val receptor: Receptor,
    val otrosDocumentos: String?,
    val ventaTercero: String?,
    var cuerpoDocumento: List<CuerpoDocumento>,
    val resumen: Resumen,
    val extension: Extension,
    val apendice: String?,
    val selloRecibido: String?,
    val firmaElectronica: String?
)