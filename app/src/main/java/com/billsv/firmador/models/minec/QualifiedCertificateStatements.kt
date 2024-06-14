package com.billsv.firmador.models.minec


class QualifiedCertificateStatements {
    var qcCompliance: String? = null
    var qcEuRetentionPeriod: String? = null
    var qcPDS: QcPDS? = null
    var qcType: String? = null

    constructor(qcPDS: QcPDS?) : super() {
        qcCompliance = ""
        qcEuRetentionPeriod = "10"
        this.qcPDS = qcPDS
        qcType = "id-etsi-qct-esign"
    }

    constructor() : super()
    constructor(
        qcCompliance: String?,
        qcEuRetentionPeriod: String?,
        qcPDS: QcPDS?,
        qcType: String?
    ) : super() {
        this.qcCompliance = qcCompliance
        this.qcEuRetentionPeriod = qcEuRetentionPeriod
        this.qcPDS = qcPDS
        this.qcType = qcType
    }
}

