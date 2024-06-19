package com.billsv.facturaelectronica
data class IdentificacionC(
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

data class DireccionC(
    val departamento: String?,
    val municipio: String?,
    val complemento: String?
)

data class EmisorC(
    val nit: String?,
    val nrc: String?,
    val nombre: String?,
    val codActividad: String?,
    val descActividad: String?,
    val nombreComercial: String?,
    val tipoEstablecimiento: String?,
    val direccion: DireccionC,
    val telefono: String?,
    val correo: String?,
    val codEstableMH: String?,
    val codEstable: String?,
    val codPuntoVentaMH: String?,
    val codPuntoVenta: String?,
)

data class ReceptorC(
    val nit: String?,
    val nrc: String?,
    val nombre: String?,
    val codActividad: String?,
    val descActividad: String?,
    val nombreComercial: String?,
    val direccion: DireccionC?,
    val telefono: String?,
    val correo: String?
)

data class CuerpoDocumentoC(
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

data class PagoC(
    val codigo: String?,
    val montoPago: Double?,
    val referencia: String?,
    val plazo: String?,
    val periodo: Int?
)
data class tributosC(
    val codigo: String?,
    val descripcion: String?,
    val valor: Double?,
)

data class ResumenC(
    val totalNoSuj: Double?,
    val totalExenta: Double?,
    val totalGravada: Double?,
    val subTotalVentas: Double?,
    val descuNoSuj: Double?,
    val descuExenta: Double?,
    val descuGravada: Double?,
    val porcentajeDescuento: Double?,
    val totalDescu: Double?,
    val tributos: tributosC,
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
    val pagos: List<PagoC>?,
    val numPagoElectronico: String?
)

data class ExtensionC(
    val nombEntrega: String?,
    val docuEntrega: String?,
    val nombRecibe: String?,
    val docuRecibe: String?,
    val observaciones: String?,
    val placaVehiculo: String?,
)

data class DocumentoC(
    val identificacion: IdentificacionC,
    val documentoRelacionado: String?,
    val emisor: EmisorC,
    val receptor: ReceptorC,
    val otrosDocumentos: String?,
    val ventaTercero: String?,
    var cuerpoDocumento: List<CuerpoDocumentoC>,
    val resumen: ResumenC,
    val extension: ExtensionC,
    val apendice: String?,
    val selloRecibido: String?,
    val firmaElectronica: String?
)