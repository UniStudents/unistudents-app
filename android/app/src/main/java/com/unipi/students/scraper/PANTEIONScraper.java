package com.unipi.students.scraper;

import android.content.Context;

import com.datadog.android.log.Logger;
import com.unipi.students.common.UserAgentGenerator;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketTimeoutException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.Map;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

public class PANTEIONScraper {

    private String username;
    private String password;
    private boolean authorized;
    private boolean connected;
    private Document[] infoAndGradesPages = null;
    private SSLSocketFactory sslSocketFactory;
    private Logger logger = new Logger.Builder()
            .setNetworkInfoEnabled(true)
            .setServiceName("com.unipi.students.parser.PANTEIONScraper")
            .setLogcatLogsEnabled(true)
            .setDatadogLogsEnabled(true)
            .setLoggerName("PANTEIONScraper")
            .build();

    public PANTEIONScraper(String username, String password, Context context) {
        this.username = username.trim().replace(" ", "");
        this.password = password.trim().replace(" ", "");
        this.authorized = true;
        this.connected = true;
        this.sslSocketFactory = buildSSLSocketFactory(context);
        this.getHtmlPages();
    }

    private void getHtmlPages() {
        String VIEWSTATE;
        String EVENTVALIDATION;
        String EVENTTARGET;
        String EVENTARGUMENT;
        String PREVIOUSPAGE;
        int pages;
        Map<String, String> cookies;
        boolean authorized;

        Connection.Response response;

        Document[] responses = new Document[5];

        String userAgent = UserAgentGenerator.generate();

        // Getting all the needed ids
        try {
            response = Jsoup.connect("https://foit.panteion.gr/declare/Login.aspx?ReturnUrl=%2fdeclare%2fDefault.aspx")
                    .method(Connection.Method.GET)
                    .header("User-Agent", userAgent)
                    .timeout(60 * 1000)
                    .sslSocketFactory(this.sslSocketFactory)
                    .execute();

            cookies = response.cookies();

            Document homePage = response.parse();

            VIEWSTATE = homePage.select("#__VIEWSTATE").attr("value");
            EVENTVALIDATION = homePage.select("#__EVENTVALIDATION").attr("value");
            EVENTTARGET = homePage.select("#__EVENTTARGET").attr("value");
            EVENTARGUMENT = homePage.select("#__EVENTARGUMENT").attr("value");
        } catch (SocketTimeoutException connException) {
            connected = false;
            logger.e("Error: " + connException.getMessage(), connException);
            return;
        } catch (IOException e) {
            logger.e("Error: " + e.getMessage(), e);
            return;
        }

        // First attempt to login and gather all the wanted cookies
        try {
            response = Jsoup.connect("https://foit.panteion.gr/declare/Login.aspx?ReturnUrl=%2fdeclare%2fDefault.aspx")
                    .data("__VIEWSTATE", VIEWSTATE)
                    .data("__EVENTVALIDATION", EVENTVALIDATION)
                    .data("__EVENTTARGET", EVENTTARGET)
                    .data("__EVENTARGUMENT", EVENTARGUMENT)
                    .data("btnRegister", "Εγγραφή")
                    .data("txtUserName", this.username)
                    .data("txtPsw", this.password)
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
                    .header("Accept-Encoding", "gzip, deflate, br")
                    .header("Accept-Language", "en-US,en;q=0.9,el-GR;q=0.8,el;q=0.7")
                    .header("Connection", "keep-alive")
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .header("Host", "foit.panteion.gr")
                    .header("Origin", "https://foit.panteion.gr")
                    .header("Referer", "https://foit.panteion.gr/declare/Login.aspx?ReturnUrl=%2Fdeclare%2FDefault.aspx")
                    .header("Sec-Fetch-Dest", "document")
                    .header("Sec-Fetch-Mode", "navigate")
                    .header("Sec-Fetch-Site", "origin")
                    .header("Sec-Fetch-User", "?1")
                    .header("Upgrade-Insecure-Requests", "1")
                    .header("User-Agent", userAgent)
                    .followRedirects(false)
                    .method(Connection.Method.POST)
                    .cookies(cookies)
                    .timeout(60 * 1000)
                    .sslSocketFactory(this.sslSocketFactory)
                    .execute();

            cookies.putAll(response.cookies());

            // Check if authorized
            authorized = authorizationCheck(response.parse());
        } catch (SocketTimeoutException connException) {
            connected = false;
            logger.e("Error: " + connException.getMessage(), connException);
            return;
        } catch (IOException e) {
            logger.e("Error: " + e.getMessage(), e);
            return;
        }

        // Return if not authorized
        if (!authorized) {
            this.authorized = false;
            return;
        }

        // Login and Grades
        try {
            response = Jsoup.connect("https://foit.panteion.gr/declare/StudentLessons.list.aspx")
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
                    .header("Accept-Encoding", "gzip, deflate, br")
                    .header("Accept-Language", "en-US,en;q=0.9,el-GR;q=0.8,el;q=0.7")
                    .header("Connection", "keep-alive")
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .header("Host", "foit.panteion.gr")
                    .header("Referer", "https://foit.panteion.gr/declare/Login.aspx?ReturnUrl=%2Fdeclare%2FDefault.aspx")
                    .header("Sec-Fetch-Dest", "document")
                    .header("Sec-Fetch-Mode", "navigate")
                    .header("Sec-Fetch-Site", "origin")
                    .header("Sec-Fetch-User", "?1")
                    .header("Upgrade-Insecure-Requests", "1")
                    .header("User-Agent", userAgent)
                    .method(Connection.Method.GET)
                    .cookies(cookies)
                    .timeout(60 * 1000)
                    .sslSocketFactory(this.sslSocketFactory)
                    .execute();

            Document gradesDOM = response.parse();

            // Save first 20 grades
            responses[0] = gradesDOM;

            VIEWSTATE = gradesDOM.select("#__VIEWSTATE").attr("value");
            EVENTVALIDATION = gradesDOM.select("#__EVENTVALIDATION").attr("value");
            PREVIOUSPAGE = gradesDOM.select("#__PREVIOUSPAGE").attr("value");

            pages = gradesDOM.select("#ctl00_ContentData_grdStudLess").select(".gvPagerStyle > td:nth-child(1) > table:nth-child(1) > tbody:nth-child(1) > tr:nth-child(1)").select("td").toArray().length;
        } catch (SocketTimeoutException connException) {
            connected = false;
            logger.e("Error: " + connException.getMessage(), connException);
            return;
        } catch (IOException e) {
            logger.e("Error: " + e.getMessage(), e);
            return;
        }

        if (pages > 1) {
            for (int i = 2; i < (pages + 1); i++) {
                try {
                    response = Jsoup.connect("https://foit.panteion.gr/declare/StudentLessons.list.aspx")
                            .data("ctl00$ContentLeft$ScriptManager", "ctl00$ContentData$upLess|ctl00$ContentData$grdStudLess")
                            .data("__VIEWSTATE", VIEWSTATE)
                            .data("__EVENTTARGET", "ctl00$ContentData$grdStudLess")
                            .data("__EVENTARGUMENT", "Page$" + i)
                            .data("ctl00$ContentLeft$ddlListType", "DET")
                            .data("ctl00$ContentLeft$ddlPassed", "ALL")
                            .data("ctl00$ContentLeft$txtAcadYear", "")
                            .data("ctl00$ContentLeft$ddlTerm", "-32768")
                            .data("ctl00$ContentLeft$ddlTermType", "")
                            .data("ctl00$ContentData$hdnTerm", "-32768")
                            .data("ctl00$ContentData$ddlRows", "20")
                            .data("ctl00$ContentData$dlgPopup$hiddenIsVisible", "")
                            .data("__EVENTVALIDATION", EVENTVALIDATION)
                            .data("__PREVIOUSPAGE", PREVIOUSPAGE)
                            .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
                            .header("Accept-Encoding", "gzip, deflate, br")
                            .header("Accept-Language", "en-US,en;q=0.9,el-GR;q=0.8,el;q=0.7")
                            .header("Connection", "keep-alive")
                            .header("Content-Type", "application/x-www-form-urlencoded")
                            .header("Host", "foit.panteion.gr")
                            .header("Referer", "https://foit.panteion.gr/declare/Login.aspx?ReturnUrl=%2Fdeclare%2FDefault.aspx")
                            .header("Sec-Fetch-Dest", "document")
                            .header("Sec-Fetch-Mode", "navigate")
                            .header("Sec-Fetch-Site", "origin")
                            .header("Sec-Fetch-User", "?1")
                            .header("Upgrade-Insecure-Requests", "1")
                            .header("User-Agent", userAgent)
                            .method(Connection.Method.POST)
                            .cookies(cookies)
                            .timeout(60 * 1000)
                            .sslSocketFactory(this.sslSocketFactory)
                            .execute();

                    Document gradesDOM = response.parse();

                    // Save another 20 grades
                    responses[i - 1] = gradesDOM;

                } catch (SocketTimeoutException connException) {
                    connected = false;
                    logger.e("Error: " + connException.getMessage(), connException);
                    return;
                } catch (IOException e) {
                    logger.e("Error: " + e.getMessage(), e);
                    return;
                }
            }
        }

        setInfoAndGradesPages(responses);
    }

    private boolean authorizationCheck(Document document) {
        String html = document.toString();

        return !(html.contains("Δεν υπάρχει μέλος με αυτά τα στοιχεία. Επικοινωνήστε με την διαχειριστή της εφαρμογής.") ||
                html.contains("SL: LdapException: (49) Invalid Credentials LdapException: Matched DN:") || html.contains("SL: LdapException: (91) Connect Error System.IO.IOException: The authentication or decryption has failed. ---> Mono.Security.Protocol.Tls.TlsException: The authentication or decryption has failed. at Mono.Security.Protocol.Tls.RecordProtocol.ProcessAlert (AlertLevel alertLevel, AlertDescription alertDesc) [0x00000] in :0 at Mono.Security.Protocol.Tls.RecordProtocol.InternalReceiveRecordCallback (IAsyncResult asyncResult) [0x00000] in :0 --- End of inner exception stack trace --- at Mono.Security.Protocol.Tls.SslStreamBase.Read (System.Byte[] buffer, Int32 offset, Int32 count) [0x00000] in :0 at System.IO.Stream.ReadByte () [0x00007] in /usr/src/packages/BUILD/mono-2.8/mcs/class/corlib/System.IO/Stream.cs:149 at Novell.Directory.Ldap.Asn1.Asn1Identifier..ctor (System.IO.Stream in_Renamed) [0x00000] in :0 at Novell.Directory.Ldap.Connection+ReaderThread.Run () [0x00000] in :0"));
    }

    public boolean isConnected() {
        return connected;
    }

    public boolean isAuthorized() {
        return authorized;
    }

    public Document[] getInfoAndGradesPages() {
        return infoAndGradesPages;
    }

    private void setInfoAndGradesPages(Document[] infoAndGradesPages) {
        if (infoAndGradesPages[0] == null)
            this.infoAndGradesPages = null;
        else
            this.infoAndGradesPages = infoAndGradesPages;
    }

    private SSLSocketFactory buildSSLSocketFactory(Context context) {
        // Add support for self-signed (local) SSL certificates
        try {

            // Load CAs from an InputStream
            // (could be from a resource or ByteArrayInputStream or ...)
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            // From https://www.washington.edu/itconnect/security/ca/load-der.crt
            InputStream is = context.getResources().getAssets().open("panteion.crt");
            InputStream caInput = new BufferedInputStream(is);
            Certificate ca;
            try {
                ca = cf.generateCertificate(caInput);
            } finally {
                caInput.close();
            }

            // Create a KeyStore containing our trusted CAs
            String keyStoreType = KeyStore.getDefaultType();
            KeyStore keyStore = KeyStore.getInstance(keyStoreType);
            keyStore.load(null, null);
            keyStore.setCertificateEntry("ca", ca);

            // Create a TrustManager that trusts the CAs in our KeyStore
            String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
            tmf.init(keyStore);

            // Create an SSLContext that uses our TrustManager
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, tmf.getTrustManagers(), null);
            return sslContext.getSocketFactory();

        } catch (NoSuchAlgorithmException | KeyStoreException | KeyManagementException | CertificateException | IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}