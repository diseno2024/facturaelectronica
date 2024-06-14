package com.billsv.firmador.models.minec


class AuthorityInfoAccess {
    private var accessDescription: MutableList<AccessDescription>

    constructor() : super() {
        accessDescription = ArrayList()
    }

    constructor(accessDescription: MutableList<AccessDescription>) : super() {
        this.accessDescription = accessDescription
    }

    fun addAccessDescription(accessDescription: AccessDescription) {
        this.accessDescription.add(accessDescription)
    }

    fun getAccessDescription(): List<AccessDescription> {
        return accessDescription
    }

    fun setAccessDescription(accessDescription: MutableList<AccessDescription>) {
        this.accessDescription = accessDescription
    }
}