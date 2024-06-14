package com.billsv.firmador.constantes
enum class Errores(private val codigo: String, private val texto: String) {
    COD_801_CERT_ERROR_NO_ENCOTRADO("801", "No existe certificado activo"),
    COD_802_NO_VALIDO("802", "No válido"),
    COD_803_ERROR_LLAVE_PUBLICA("803", "No existe llave pública para este NIT"),
    COD_804_ERROR_NO_CATALOGADO("804", "Error no catalogado"),
    COD_805_ERROR_CERTIFICADO_DUPLICADO("805", "Ya existe una certificado activo"),
    COD_806_ERROR_GENERACION_CERTIFICADO("806", "Generación de certificados satisfactoria"),
    COD_807_ERROR_DESCARGAR_ARCHIVO("807", "Error en la descarga de archivo"),
    COD_808_ERROR_SUBIR_ARCHIVO("808", "Error al subir el archivo"),
    COD_809_DATOS_REQUERIDOS("809", "Son datos requeridos"),
    COD_810_CONVERTIR_JSON_A_STRING("810", "Problemas al convertir JSON a String"),
    COD_811_CONVERTIR_STRING_A_JSON("811", "Problemas al convertir String a JSON"),
    COD_812_NO_FILE("812", "No se encontró el archivo");

    fun getTexto(): String {
        return texto
    }

    fun getCodigo(): String {
        return codigo
    }

    companion object {
        const val COD_801_CERT_ERROR_NO_ENCONTRADO = "801"
        const val COD_802_NO_VALIDO = "802"
        const val COD_803_ERROR_LLAVE_PUBLICA = "803"
        const val COD_804_ERROR_NO_CATALOGADO = "804"
        const val COD_805_ERROR_CERTIFICADO_DUPLICADO = "805"
        const val COD_806_ERROR_GENERACION_CERTIFICADO = "806"
        const val COD_807_ERROR_DESCARGAR_ARCHIVO = "807"
        const val COD_808_ERROR_SUBIR_ARCHIVO = "808"
        const val COD_809_DATOS_REQUERIDOS = "809"
        const val COD_810_CONVERTIR_JSON_A_STRING = "810"
        const val COD_811_CONVERTIR_STRING_A_JSON = "811"
        const val COD_812_NO_FILE = "812"
    }
}