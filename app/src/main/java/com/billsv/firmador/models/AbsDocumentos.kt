package com.billsv.firmador.models

import java.io.Serializable

abstract class AbsDocumentos : Serializable {
    var _id: String? = null
    fun get_id(): String? {
        return _id
    }

    fun set_id(_id: String?) {
        this._id = _id
    }
}


