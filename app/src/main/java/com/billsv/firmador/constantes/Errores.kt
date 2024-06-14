package com.billsv.firmador.constantes

enum class Errores(val code: String, val text: String) {
    COD_801_CERT_ERROR_NO_ENCOTRADO("801", "No existe certificado activo"),
    COD_802_NO_VALIDO("802", "No valido"),
    COD_803_ERROR_LLAVE_PRUBLICA("803", "No existe llave publica para este nit"),
    COD_804_ERROR_NO_CATALOGADO("804", "Error no catalogado"),
    COD_805_ERROR_CERTIFCADO_DUPLICADO("805", "Ya existe una certificado activo"),
    COD_806_ERROR_GENERACION_CERTIFICADO("806", "Generación de certificados satisfactoria"),
    COD_807_ERROR_DESCARGAR_ARCHIVO("807", "Error en la descarga de archivo"),
    COD_808_ERROR_SUBUR_ARCHIVO("808", "Error en al subir el archivo"),
    COD_809_DATOS_REQUERIDOS("809", "Son datos requeridos"),
    COD_810_CONVERTIR_JSON_A_STRING("810", "Problemas al convertir Json a String"),
    COD_811_CONVERTIR_STRING_A_JSON("811", "Problemas al convertir String a Json"),
    COD_812_NO_FILE("812", "No se encontro el archivo");

    object errores {
        const val COD_801_CERT_ERROR_NO_ENCOTRADO = "801"
        const val COD_802_NO_VALIDO = "802"
        const val COD_803_ERROR_LLAVE_PRUBLICA = "803"
        const val COD_804_ERROR_NO_CATALOGADO = "804"
        const val COD_805_ERROR_CERTIFCADO_DUPLICADO = "805"
        const val COD_806_ERROR_GENERACION_CERTIFICADO = "806"
        const val COD_807_ERROR_DESCARGAR_ARCHIVO = "807"
        const val COD_808_ERROR_SUBUR_ARCHIVO = "808"
        const val COD_809_DATOS_REQUERIDOS = "809"
        const val COD_810_CONVERTIR_JSON_A_STRING = "810"
        const val COD_811_CONVERTIR_STRING_A_JSON = "811"
        const val COD_812_NO_FILE = "812"
    }
}