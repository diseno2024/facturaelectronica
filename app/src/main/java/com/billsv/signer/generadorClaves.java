package com.billsv.signer;

import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;

import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.cert.X509Certificate;
import java.util.Calendar;
import java.util.Date;

public class generadorClaves {

    public static KeyPair generarParClaves() throws Exception {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(4096);
        return keyPairGenerator.generateKeyPair();
    }

    public static X509Certificate generarCertificado(KeyPair keyPair, infoEmisor issuerInfo) throws Exception {
        String issuerDN = String.format(
                "C=%s, ST=%s, L=%s, O=%s, OU=%s, CN=%s",
                issuerInfo.getPais(),
                issuerInfo.getDepartamento(),
                issuerInfo.getMunicipio(),
                issuerInfo.getActividadEco(),
                issuerInfo.getNombre(),
                issuerInfo.getNombrec()
        );

        X500Name issuer = new X500Name(issuerDN);
        X500Name subject = new X500Name(issuerDN);  // Para un certificado autofirmado, el emisor y el sujeto son los mismos
        BigInteger serial = BigInteger.valueOf(System.currentTimeMillis());
        Date notBefore = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(notBefore);
        calendar.add(Calendar.YEAR, 1);
        Date notAfter = calendar.getTime();

        X509v3CertificateBuilder certificateBuilder = new JcaX509v3CertificateBuilder(
                issuer,
                serial,
                notBefore,
                notAfter,
                subject,
                keyPair.getPublic()
        );

        ContentSigner contentSigner = new JcaContentSignerBuilder("SHA512WithRSA").build(keyPair.getPrivate());
        return new JcaX509CertificateConverter().getCertificate(
                certificateBuilder.build(contentSigner)
        );
    }
}