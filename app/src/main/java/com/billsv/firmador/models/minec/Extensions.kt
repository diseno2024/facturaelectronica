package com.billsv.firmador.models.minec


class Extensions {
    var authorityKeyIdentifier: AuthorityKeyIdentifier? = null
    var subjectKeyIdentifier: SubjectKeyIdentifier? = null
    var keyUsage: KeyUsage? = null
    var certificatePolicies: CertificatePolicies? = null
    var subjectAlternativeNames: SubjectAlternativeNames? = null
    var extendedKeyUsage: ExtendedKeyUsage? = null
    var crlDistributionPoint: CrlDistributionPoint? = null
    var authorityInfoAccess: AuthorityInfoAccess? = null
    var qualifiedCertificateStatements: QualifiedCertificateStatements? = null
    var basicConstraints: BasicConstraints? = null

    constructor(
        rfc822Name: String?,
        crlDistributionPoint: List<String?>?,
        accessLocation: List<String?>?
    ) : super() {
        authorityKeyIdentifier = AuthorityKeyIdentifier()
        subjectKeyIdentifier = SubjectKeyIdentifier()
        keyUsage = KeyUsage()
        certificatePolicies = CertificatePolicies()
        subjectAlternativeNames = rfc822Name?.let { SubjectAlternativeNames(it) }
        extendedKeyUsage = ExtendedKeyUsage()
        this.crlDistributionPoint = CrlDistributionPoint(crlDistributionPoint)
        //		this.crlDistributionPoint.add("http://www2.mh.gob.sv/crl");
//		this.crlDistributionPoint.add("http://www2.mh.gob.sv/crl2");
        authorityInfoAccess = AuthorityInfoAccess()
        authorityInfoAccess!!.addAccessDescription(AccessDescription(accessLocation))
        //		accessDescription.addAccessLocation("https://www.minec.gob.sv/ca/public/donwload/subordinadal.crt");
        qualifiedCertificateStatements = QualifiedCertificateStatements(QcPDS())
        basicConstraints = BasicConstraints()
    }

    constructor() : super()
    constructor(
        authorityKeyIdentifier: AuthorityKeyIdentifier?,
        subjectKeyIdentifier: SubjectKeyIdentifier?,
        keyUsage: KeyUsage?,
        certificatePolicies: CertificatePolicies?,
        subjectAlternativeNames: SubjectAlternativeNames?,
        extendedKeyUsage: ExtendedKeyUsage?,
        crlDistributionPoint: CrlDistributionPoint?,
        authorityInfoAccess: AuthorityInfoAccess?,
        qualifiedCertificateStatements: QualifiedCertificateStatements?,
        basicConstraints: BasicConstraints?
    ) : super() {
        this.authorityKeyIdentifier = authorityKeyIdentifier
        this.subjectKeyIdentifier = subjectKeyIdentifier
        this.keyUsage = keyUsage
        this.certificatePolicies = certificatePolicies
        this.subjectAlternativeNames = subjectAlternativeNames
        this.extendedKeyUsage = extendedKeyUsage
        this.crlDistributionPoint = crlDistributionPoint
        this.authorityInfoAccess = authorityInfoAccess
        this.qualifiedCertificateStatements = qualifiedCertificateStatements
        this.basicConstraints = basicConstraints
    }
}

