package com.billsv.firmador.models.minec


class CrlDistributionPoint {
    private var distributionPoint: MutableList<String>

    constructor() : super() {
        distributionPoint = ArrayList()
    }

    constructor(distributionPoint: List<String?>?) : super() {
        this.distributionPoint = distributionPoint as MutableList<String>
    }

    fun add(distributionPoint: String) {
        this.distributionPoint.add(distributionPoint)
    }

    fun getDistributionPoint(): List<String> {
        return distributionPoint
    }

    fun setDistributionPoint(distributionPoint: MutableList<String>) {
        this.distributionPoint = distributionPoint
    }
}