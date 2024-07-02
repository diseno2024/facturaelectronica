package com.billsv.signer

import android.util.Log
import com.couchbase.lite.*

public fun obtenerInfoEmisor(database: Database): infoEmisor? {
    val query = QueryBuilder.select(
        SelectResult.property("nombre"),
        SelectResult.property("nombreC"),
        SelectResult.property("dui"),
        SelectResult.property("nit"),
        SelectResult.property("nrc"),
        SelectResult.property("ActividadEco"),
        SelectResult.property("departamento"),
        SelectResult.property("municipio"),
        SelectResult.property("direccion"),
        SelectResult.property("telefono"),
        SelectResult.property("correo")
    )
        .from(DataSource.database(database))
        .where(Expression.property("tipo").equalTo(Expression.string("ConfEmisor")))

    return try {
        val resultSet = query.execute()
        val result = resultSet.next()
        if (result != null) {
            infoEmisor(
                result.getString("nombre") ?: "",
                result.getString("nombreC") ?: "",
                result.getString("dui") ?: "",
                result.getString("nit") ?: "",
                result.getString("nrc") ?: "",
                result.getString("ActividadEco") ?: "",
                result.getString("departamento") ?: "",
                result.getString("municipio") ?: "",
                result.getString("direccion") ?: "",
                result.getString("telefono") ?: "",
                result.getString("correo") ?: "",

            )
        } else {
            null
        }
    } catch (e: CouchbaseLiteException) {
        Log.e("Error", "Error al obtener la información del emisor", e)
        null
    }
}