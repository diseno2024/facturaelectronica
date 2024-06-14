package com.billsv.firmador.models

import java.io.Serializable


abstract class AbsDocumentos : Serializable {
    private var _id: String? = null
    fun get_id(): String? {
        return this._id
    }

    fun set_id(_id: String?) {
        this._id = _id
    }
}

