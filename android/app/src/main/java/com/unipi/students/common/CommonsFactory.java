package com.unipi.students.common;

import android.content.Context;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.Enumeration;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

public class CommonsFactory {

    public static SSLSocketFactory sslSocketFactory;

    public static SSLSocketFactory buildSSLSocketFactory(Context context) {
        // Add support for self-signed (local) SSL certificates
        try {
            String[] certs = new String[]{"uoi.crt", "uoiclass.crt", "panteion.crt", "cm.crt",
                    "teiemt.crt", "USERTrust-AUA.crt", "GEANT-AUA.crt", "estudent-aua.crt", "student-aua.crt", "teiwest.crt"};

            // Create a KeyStore containing our trusted CAs
            String keyStoreType = KeyStore.getDefaultType();
            KeyStore keyStore = KeyStore.getInstance(keyStoreType);
            keyStore.load(null, null);

            // Load CAs from an InputStream
            CertificateFactory cf = CertificateFactory.getInstance("X.509");

            for (String cert : certs) {
                InputStream is = context.getResources().getAssets().open(cert);
                try (InputStream caInput = new BufferedInputStream(is)) {
                    Certificate ca;
                    ca = cf.generateCertificate(caInput);
                    keyStore.setCertificateEntry(cert, ca);
                }
            }

            // add default CAs
            KeyStore defaultCAs = KeyStore.getInstance("AndroidCAStore");
            if (defaultCAs != null) {
                defaultCAs.load(null,null);
                Enumeration<String> keyAliases = defaultCAs.aliases();
                while (keyAliases.hasMoreElements()) {
                    String alias = keyAliases.nextElement();
                    Certificate cert = defaultCAs.getCertificate(alias);
                    try {
                        if (!keyStore.containsAlias(alias))
                            keyStore.setCertificateEntry(alias, cert);
                    }
                    catch(Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
            tmf.init(keyStore);

            // Create an SSLContext that uses our TrustManager
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, tmf.getTrustManagers(), null);
            sslSocketFactory = sslContext.getSocketFactory();
            return sslSocketFactory;
        } catch (NoSuchAlgorithmException | KeyStoreException | KeyManagementException | CertificateException | IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
