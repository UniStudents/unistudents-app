package com.unipi.students.scraper;

import com.datadog.android.log.Logger;
import com.unipi.students.common.UserAgentGenerator;
import com.unipi.students.model.LoginForm;

import org.jsoup.Connection;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

public class UPATRASScraper {
    private final String USER_AGENT;
    private boolean connected;
    private boolean authorized;
    private Document infoAndGradesPage;
    private Map<String, String> cookies;
    private Logger logger = new Logger.Builder()
            .setNetworkInfoEnabled(true)
            .setServiceName("com.unipi.students.scraper.UPATRASScraper")
            .setLogcatLogsEnabled(true)
            .setDatadogLogsEnabled(true)
            .setLoggerName("UPATRASScraper")
            .build();


    public UPATRASScraper(LoginForm loginForm) {
        this.authorized = true;
        this.connected = true;
        USER_AGENT = UserAgentGenerator.generate();
        getDocuments(loginForm.getUsername(), loginForm.getPassword(), loginForm.getCookies());
    }

    private void getDocuments(String username, String password, Map<String, String> cookies) {
        if (cookies == null) {
            System.out.println("======> DEBUG: Normal request");
            getHtmlPages(username, password);
        } else {
            System.out.println("======> DEBUG: Cookies request");
            getHtmlPages(cookies);
            if (infoAndGradesPage == null) {
                System.out.println("======> DEBUG: Normal request after cookies request");
                getHtmlPages(username, password);
            }
        }
    }

    private void getHtmlPages(String username, String password) {
        Connection.Response response;
        String location;
        Map<String, String> cookiesSession;

        //
        //  Get access to login page
        //

        try {
            response = Jsoup.connect("https://progress.upatras.gr/redirectapp/index.jsp?schema=https&host=progress.upatras.gr&port=443&path=%2firj%2fportal")
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
                    .header("Accept-Encoding", "gzip, deflate, br")
                    .header("Accept-Language", "en-US,en;q=0.9")
                    .header("Connection", "keep-alive")
                    .header("Host", "progress.upatras.gr")
                    .header("Sec-Fetch-Dest", "document")
                    .header("Sec-Fetch-Mode", "navigate")
                    .header("Sec-Fetch-Site", "none")
                    .header("Sec-Fetch-User", "?1")
                    .header("Upgrade-Insecure-Requests", "1")
                    .header("User-Agent", USER_AGENT)
                    .followRedirects(false)
                    .method(Connection.Method.GET)
                    .timeout(60 * 1000)
                    .execute();
        } catch (SocketTimeoutException | UnknownHostException | HttpStatusException | ConnectException connException) {
            connected = false;
            logger.w("[UPATRAS] Warning: " + connException.getMessage(), connException);
            return;
        } catch (IOException e) {
            logger.e("[UPATRAS] Error: " + e.getMessage(), e);
            return;
        }

        location = response.header("location");
        cookiesSession = response.cookies();

        //
        //  Get redirect
        //

        try {
            response = Jsoup.connect(location)
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
                    .header("Accept-Encoding", "gzip, deflate, br")
                    .header("Accept-Language", "en-US,en;q=0.9")
                    .header("Connection", "keep-alive")
                    .header("Host", "idp.upnet.gr")
                    .header("Sec-Fetch-Dest", "document")
                    .header("Sec-Fetch-Mode", "navigate")
                    .header("Sec-Fetch-Site", "none")
                    .header("Sec-Fetch-User", "?1")
                    .header("Upgrade-Insecure-Requests", "1")
                    .header("User-Agent", USER_AGENT)
                    .followRedirects(false)
                    .method(Connection.Method.GET)
                    .timeout(60 * 1000)
                    .execute();
        } catch (SocketTimeoutException | UnknownHostException | HttpStatusException | ConnectException connException) {
            connected = false;
            logger.w("[UPATRAS] Warning: " + connException.getMessage(), connException);
            return;
        } catch (IOException e) {
            logger.e("[UPATRAS] Error: " + e.getMessage(), e);
            return;
        }

        location = response.header("location");
        Map<String, String> JSESSIONID = new HashMap<>(response.cookies());

        //
        // Get login page
        //

        try {
            response = Jsoup.connect("https://idp.upnet.gr" + location)
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
                    .header("Accept-Encoding", "gzip, deflate, br")
                    .header("Accept-Language", "en-US,en;q=0.9")
                    .header("Connection", "keep-alive")
                    .header("Host", "idp.upnet.gr")
                    .header("Sec-Fetch-Dest", "document")
                    .header("Sec-Fetch-Mode", "navigate")
                    .header("Sec-Fetch-Site", "none")
                    .header("Sec-Fetch-User", "?1")
                    .header("Upgrade-Insecure-Requests", "1")
                    .header("User-Agent", USER_AGENT)
                    .cookies(JSESSIONID)
                    .followRedirects(false)
                    .method(Connection.Method.GET)
                    .timeout(60 * 1000)
                    .execute();
        } catch (SocketTimeoutException | UnknownHostException | HttpStatusException | ConnectException connException) {
            connected = false;
            logger.w("[UPATRAS] Warning: " + connException.getMessage(), connException);
            return;
        } catch (IOException e) {
            logger.e("[UPATRAS] Error: " + e.getMessage(), e);
            return;
        }


        //
        // Try to Login
        //

        try {
            response = Jsoup.connect("https://idp.upnet.gr" + location)
                    .data("j_username", username)
                    .data("j_password", password)
                    .data("_eventId_proceed", "")
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
                    .header("Accept-Encoding", "gzip, deflate, br")
                    .header("Accept-Language", "en-US,en;q=0.9")
                    .header("Cache-Control", "max-age=0")
                    .header("Connection", "keep-alive")
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .header("Host", "idp.upnet.gr")
                    .header("Origin", "https://idp.upnet.gr")
                    .header("Referer", "https://idp.upnet.gr" + location)
                    .header("Sec-Fetch-Dest", "document")
                    .header("Sec-Fetch-Mode", "navigate")
                    .header("Sec-Fetch-Site", "none")
                    .header("Sec-Fetch-User", "?1")
                    .header("Upgrade-Insecure-Requests", "1")
                    .header("User-Agent", USER_AGENT)
                    .cookies(JSESSIONID)
                    .followRedirects(false)
                    .method(Connection.Method.POST)
                    .timeout(60 * 1000)
                    .execute();
        } catch (SocketTimeoutException | UnknownHostException | HttpStatusException | ConnectException connException) {
            connected = false;
            logger.w("[UPATRAS] Warning: " + connException.getMessage(), connException);
            return;
        } catch (IOException e) {
            logger.e("[UPATRAS] Error: " + e.getMessage(), e);
            return;
        }

        // authorization check
        if (!isAuthorized(response, JSESSIONID)) {
            return;
        }

        Document doc;
        try {
            doc = response.parse();
        } catch (IOException e) {
            logger.e("[UPATRAS] Error: " + e.getMessage(), e);
            return;
        }

        Elements firstElement = doc.getElementsByAttributeValue("name", "RelayState");
        Elements secondElement = doc.getElementsByAttributeValue("name", "SAMLResponse");

        String RelayState = firstElement.first().attributes().get("value");
        String SAMLResponse = secondElement.first().attributes().get("value");

        //
        //  Post request acs
        //

        try {
            response = Jsoup.connect("https://progress.upatras.gr/saml2/sp/acs")
                    .data("RelayState", RelayState)
                    .data("SAMLResponse", SAMLResponse)
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
                    .header("Accept-Encoding", "gzip, deflate, br")
                    .header("Accept-Language", "en-US,en;q=0.9")
                    .header("Cache-Control", "max-age=0")
                    .header("Connection", "keep-alive")
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .header("Host", "progress.upatras.gr")
                    .header("Origin", "https://idp.upnet.gr")
                    .header("Referer", "https://idp.upnet.gr/")
                    .header("Sec-Fetch-Dest", "document")
                    .header("Sec-Fetch-Mode", "navigate")
                    .header("Sec-Fetch-Site", "cross-site")
                    .header("Upgrade-Insecure-Requests", "1")
                    .header("User-Agent", USER_AGENT)
                    .cookies(cookiesSession)
                    .followRedirects(false)
                    .method(Connection.Method.POST)
                    .timeout(60 * 1000)
                    .execute();
        } catch (SocketTimeoutException | UnknownHostException | HttpStatusException | ConnectException connException) {
            connected = false;
            logger.w("[UPATRAS] Warning: " + connException.getMessage(), connException);
            return;
        } catch (IOException e) {
            logger.e("[UPATRAS] Error: " + e.getMessage(), e);
            return;
        }

        //
        //  POST request to /redirectapp/index.jsp
        //

        try {
            response = Jsoup.connect("https://progress.upatras.gr/redirectapp/index.jsp?schema=https&host=progress.upatras.gr&port=443&path=%2firj%2fportal")
                    .data("RelayState", RelayState)
                    .data("SAMLResponse", SAMLResponse)
                    .data("saml2post", "false")
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
                    .header("Accept-Encoding", "gzip, deflate, br")
                    .header("Accept-Language", "en-US,en;q=0.9")
                    .header("Cache-Control", "max-age=0")
                    .header("Connection", "keep-alive")
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .header("Host", "progress.upatras.gr")
                    .header("Origin", "https://progress.upatras.gr")
                    .header("Referer", "https://progress.upatras.gr/saml2/sp/acs")
                    .header("Sec-Fetch-Dest", "document")
                    .header("Sec-Fetch-Mode", "navigate")
                    .header("Sec-Fetch-Site", "same-origin")
                    .header("Upgrade-Insecure-Requests", "1")
                    .header("User-Agent", USER_AGENT)
                    .cookies(cookiesSession)
                    .followRedirects(false)
                    .method(Connection.Method.POST)
                    .timeout(60 * 1000)
                    .execute();
        } catch (SocketTimeoutException | UnknownHostException | HttpStatusException | ConnectException connException) {
            connected = false;
            logger.w("[UPATRAS] Warning: " + connException.getMessage(), connException);
            return;
        } catch (IOException e) {
            logger.e("[UPATRAS] Error: " + e.getMessage(), e);
            return;
        }

        location = response.header("location");
        Map<String, String> newSessionCookies = new HashMap<>(response.cookies());
        newSessionCookies.put("saplb_*", cookiesSession.get("saplb_*"));

        try {
            response = Jsoup.connect(location)
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
                    .header("Accept-Encoding", "gzip, deflate, br")
                    .header("Accept-Language", "en-US,en;q=0.9")
                    .header("Cache-Control", "max-age=0")
                    .header("Connection", "keep-alive")
                    .header("Host", "progress.upatras.gr")
                    .header("Referer", "https://progress.upatras.gr/saml2/sp/acs")
                    .header("Sec-Fetch-Dest", "document")
                    .header("Sec-Fetch-Mode", "navigate")
                    .header("Sec-Fetch-Site", "same-origin")
                    .header("Upgrade-Insecure-Requests", "1")
                    .header("User-Agent", USER_AGENT)
                    .cookies(newSessionCookies)
                    .followRedirects(false)
                    .method(Connection.Method.GET)
                    .timeout(60 * 1000)
                    .execute();
        } catch (SocketTimeoutException | UnknownHostException | HttpStatusException | ConnectException connException) {
            connected = false;
            logger.w("[UPATRAS] Warning: " + connException.getMessage(), connException);
            return;
        } catch (IOException e) {
            logger.e("[UPATRAS] Error: " + e.getMessage(), e);
            return;
        }

        location = response.header("location");

        try {
            response = Jsoup.connect(location)
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
                    .header("Accept-Encoding", "gzip, deflate, br")
                    .header("Accept-Language", "en-US,en;q=0.9")
                    .header("Cache-Control", "max-age=0")
                    .header("Connection", "keep-alive")
                    .header("Host", "progress.upatras.gr")
                    .header("Referer", "https://progress.upatras.gr/saml2/sp/acs")
                    .header("Sec-Fetch-Dest", "document")
                    .header("Sec-Fetch-Mode", "navigate")
                    .header("Sec-Fetch-Site", "same-origin")
                    .header("Upgrade-Insecure-Requests", "1")
                    .header("User-Agent", USER_AGENT)
                    .cookies(newSessionCookies)
                    .followRedirects(false)
                    .method(Connection.Method.GET)
                    .timeout(60 * 1000)
                    .execute();
        } catch (SocketTimeoutException | UnknownHostException | HttpStatusException | ConnectException connException) {
            connected = false;
            logger.w("[UPATRAS] Warning: " + connException.getMessage(), connException);
            return;
        } catch (IOException e) {
            logger.e("[UPATRAS] Error: " + e.getMessage(), e);
            return;
        }

        for (Map.Entry<String, String> entry : response.cookies().entrySet()) {
            newSessionCookies.put(entry.getKey(), entry.getValue());
        }

        //
        //  First Document
        //

        try {
            response = Jsoup.connect("https://progress.upatras.gr/irj/servlet/prt/portal/prteventname/Navigate/prtroot/pcd!3aportal_content!2fevery_user!2fgeneral!2fdefaultAjaxframeworkContent!2fcom.sap.portal.contentarea?ExecuteLocally=true&CurrentWindowId=WID1602677333160&sapDocumentRenderingMode=Edge&windowId=WID1602677333160&NavMode=0&PrevNavTarget=navurl%3A%2F%2F7bc3de75b9454e902dc1352222572126")
                    .data("NavigationTarget", "navurl://0e5de93cbd00668b7e693a0eb4348811")
                    .data("Command", "SUSPEND")
                    .data("SerPropString", "")
                    .data("SerKeyString", "")
                    .data("SerAttrKeyString", "")
                    .data("SerWinIdString", "")
                    .data("DebugSet", "")
                    .data("Embedded", "true")
                    .data("SessionKeysAvailable", "true")
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
                    .header("Accept-Encoding", "gzip, deflate, br")
                    .header("Accept-Language", "en-US,en;q=0.9")
                    .header("Cache-Control", "max-age=0")
                    .header("Connection", "keep-alive")
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .header("Host", "progress.upatras.gr")
                    .header("Origin", "https://progress.upatras.gr")
                    .header("Referer", "https://progress.upatras.gr/irj/portal")
                    .header("Sec-Fetch-Dest", "iframe")
                    .header("Sec-Fetch-Mode", "navigate")
                    .header("Sec-Fetch-Site", "same-origin")
                    .header("Sec-Fetch-User", "?1")
                    .header("Upgrade-Insecure-Requests", "1")
                    .header("User-Agent", USER_AGENT)
                    .cookies(newSessionCookies)
                    .followRedirects(false)
                    .method(Connection.Method.POST)
                    .timeout(60 * 1000)
                    .execute();
        } catch (SocketTimeoutException | UnknownHostException | HttpStatusException | ConnectException connException) {
            connected = false;
            logger.w("[UPATRAS] Warning: " + connException.getMessage(), connException);
            return;
        } catch (IOException e) {
            logger.e("[UPATRAS] Error: " + e.getMessage(), e);
            return;
        }

        Document document;
        try {
             document = response.parse();
        } catch (IOException e) {
            logger.e("[UPATRAS] Error: " + e.getMessage(), e);
            return;
        }

        String url = document.getElementsByAttributeValue("id", "isolatedWorkAreaForm").first().attributes().get("action");
        String sap_ep_iviewhandle = document.getElementsByAttributeValue("name", "sap-ep-iviewhandle").first().attributes().get("value");
        String sap_wd_tstamp = document.getElementsByAttributeValue("name", "sap-wd-tstamp").first().attributes().get("value");

        Map<String, String> lastCookies = new HashMap<>();
        lastCookies.put("MYSAPSSO2", newSessionCookies.get("MYSAPSSO2"));
        lastCookies.put("SAPWP_active", "1");

        //
        //  Second Doc
        //

        try {
            response = Jsoup.connect(url)
                    .data("NavigationTarget", "navurl://0e5de93cbd00668b7e693a0eb4348811")
                    .data("sap-ep-iviewhandle", sap_ep_iviewhandle)
                    .data("sap-wd-configId", "")
                    .data("sap-ep-iviewid", "pcdshort:/fUVR4SVaE8DV6B jvmPeTNgnsL0=")
                    .data("sap-ep-pcdunit", "pcdshort:/Z8qOo1ha2xbew219e77v 7A4xUY=")
                    .data("sap-client", "400")
                    .data("sap-language", "EL")
                    .data("sap-accessibility", "")
                    .data("sap-rtl", "")
                    .data("sap-ep-version", "7.31.201711280844")
                    .data("sap-wd-tstamp", sap_wd_tstamp)
                    .data("sap-explanation", "null")
                    .data("sap-cssurl", "https://progress.upatras.gr:443/com.sap.portal.design.urdesigndata/themes/portal/PATRAS_U/ls/ls_sf3.css?v=10.30.7.303325.1553675478892")
                    .data("sap-theme", "PATRAS_U@https://progress.upatras.gr:443/com.sap.portal.theming.webdav.themeswebdavlistener/GUID:92e3ae3cc5302e7b5cc1e769be468f39")
                    .data("sap-cssversion", "10.30.7.303325.0")
                    .data("sap-tray-type", "PLAIN")
                    .data("sap-tray-padding", "X")
                    .data("sap-ie", "Edge")
                    .data("sap-browserhistory", "disabled")
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
                    .header("Accept-Encoding", "gzip, deflate, br")
                    .header("Accept-Language", "en-US,en;q=0.9")
                    .header("Cache-Control", "max-age=0")
                    .header("Connection", "keep-alive")
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .header("Host", "matrix.upatras.gr")
                    .header("Origin", "https://progress.upatras.gr")
                    .header("Referer", "https://progress.upatras.gr/")
                    .header("Sec-Fetch-Dest", "iframe")
                    .header("Sec-Fetch-Mode", "navigate")
                    .header("Sec-Fetch-Site", "same-site")
                    .header("Sec-Fetch-User", "?1")
                    .header("Upgrade-Insecure-Requests", "1")
                    .header("User-Agent", USER_AGENT)
                    .cookies(lastCookies)
                    .followRedirects(false)
                    .method(Connection.Method.POST)
                    .timeout(60 * 1000)
                    .execute();
        } catch (SocketTimeoutException | UnknownHostException | HttpStatusException | ConnectException connException) {
            connected = false;
            logger.w("[UPATRAS] Warning: " + connException.getMessage(), connException);
            return;
        } catch (IOException e) {
            logger.e("[UPATRAS] Error: " + e.getMessage(), e);
            return;
        }


        Document document2;
        try {
            document2 = response.parse();
        } catch (IOException e) {
            logger.e("[UPATRAS] Error: " + e.getMessage(), e);
            return;
        }

        String url2 = document2.getElementsByAttributeValue("name", "sap.client.SsrClient.form").first().attributes().get("action");
        String sap_wd_secure_id = document2.getElementsByAttributeValue("name", "sap-wd-secure-id").first().attributes().get("value");
        int start = url2.indexOf('=');
        int end = url2.indexOf('?');
        String sap_ext_sid = url2.substring(start+1, end);
        String SAPEVENTQUEUE = "ClientInspector_Notify~E002Id~E004WD01~E005Data~E004ClientWidth~003A1190px~003BClientHeight~003A1014px~003BScreenWidth~003A2560px~003BScreenHeight~003A1440px~003BScreenOrientation~003Alandscape~003BThemedTableRowHeight~003A21px~003BThemedFormLayoutRowHeight~003A25px~003BDeviceType~003ADESKTOP~E003~E002ResponseData~E004delta~E005EnqueueCardinality~E004single~E003~E002~E003~E001Custom_ClientInfos~E002Id~E004WD01~E005WindowOpenerExists~E004false~E005ClientURL~E004https~003A~002F~002Fmatrix.upatras.gr~002Fsap~002Fbc~002Fwebdynpro~002FSAP~002FPIQ_ST_ACAD_WORK_OV~003Bsap-ext-sid~003D" + sap_ext_sid + "~E005ClientWidth~E0041190~E005ClientHeight~E0041014~E003~E002ClientAction~E004enqueue~E005ResponseData~E004delta~E003~E002~E003~E001LoadingPlaceHolder_Load~E002Id~E004_loadingPlaceholder_~E003~E002ResponseData~E004delta~E005ClientAction~E004submit~E003~E002~E003~E001Form_Request~E002Id~E004sap.client.SsrClient.form~E005Async~E004false~E005FocusInfo~E004~E005Hash~E004~E005DomChanged~E004false~E005IsDirty~E004false~E003~E002ResponseData~E004delta~E003~E002~E003";
        lastCookies.put("sap-usercontext", "sap-language=EL&sap-client=400");

        //
        //  Third & final Doc
        //

        try {
            response = Jsoup.connect("https://matrix.upatras.gr/sap/bc/webdynpro/SAP/" + url2)
                    .data("sap-charset", "utf-8")
                    .data("sap-wd-secure-id", sap_wd_secure_id)
                    .data("SAPEVENTQUEUE", SAPEVENTQUEUE)
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
                    .header("Accept-Encoding", "gzip, deflate, br")
                    .header("Accept-Language", "en-US,en;q=0.9")
                    .header("Connection", "keep-alive")
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .header("Host", "matrix.upatras.gr")
                    .header("Origin", "https://progress.upatras.gr")
                    .header("Referer", "https://progress.upatras.gr/")
                    .header("Sec-Fetch-Dest", "empty")
                    .header("Sec-Fetch-Mode", "cors")
                    .header("Sec-Fetch-Site", "same-origin")
                    .header("X-Requested-With", "XMLHttpRequest")
                    .header("X-XHR-Logon", "accept")
                    .header("User-Agent", USER_AGENT)
                    .cookies(lastCookies)
                    .followRedirects(false)
                    .method(Connection.Method.POST)
                    .timeout(60 * 1000)
                    .maxBodySize(0)
                    .execute();
        } catch (SocketTimeoutException | UnknownHostException | HttpStatusException | ConnectException connException) {
            connected = false;
            logger.w("[UPATRAS] Warning: " + connException.getMessage(), connException);
            return;
        } catch (IOException e) {
            logger.e("[UPATRAS] Error: " + e.getMessage(), e);
            return;
        }

        Document document3;
        try {
            document3 = response.parse();
        } catch (IOException e) {
            logger.e("[UPATRAS] Error: " + e.getMessage(), e);
            return;
        }

        setInfoAndGradesPage(document3);
        setCookies(lastCookies, url, sap_ep_iviewhandle, sap_wd_tstamp, SAPEVENTQUEUE);
    }

    private void getHtmlPages(Map<String, String> cookies) {
        Connection.Response response;

        String url = cookies.remove("url");
        String sap_ep_iviewhandle = cookies.remove("sap_ep_iviewhandle");
        String sap_wd_tstamp = cookies.remove("sap_wd_tstamp");
        String SAPEVENTQUEUE = cookies.remove("SAPEVENTQUEUE");
        if (url == null ||
            sap_ep_iviewhandle == null ||
            sap_wd_tstamp == null ||
            SAPEVENTQUEUE == null) return;


        //
        //  Second Doc
        //

        try {
            response = Jsoup.connect(url)
                    .data("sap-ep-iviewhandle", sap_ep_iviewhandle)
                    .data("sap-wd-configId", "")
                    .data("sap-ep-iviewid", "pcdshort:/fUVR4SVaE8DV6B jvmPeTNgnsL0=")
                    .data("sap-ep-pcdunit", "pcdshort:/Z8qOo1ha2xbew219e77v 7A4xUY=")
                    .data("sap-client", "400")
                    .data("sap-language", "EL")
                    .data("sap-accessibility", "")
                    .data("sap-rtl", "")
                    .data("sap-ep-version", "7.31.201711280844")
                    .data("sap-wd-tstamp", sap_wd_tstamp)
                    .data("sap-explanation", "null")
                    .data("sap-cssurl", "https://progress.upatras.gr:443/com.sap.portal.design.urdesigndata/themes/portal/PATRAS_U/ls/ls_sf3.css?v=10.30.7.303325.1553675478892")
                    .data("sap-theme", "PATRAS_U@https://progress.upatras.gr:443/com.sap.portal.theming.webdav.themeswebdavlistener/GUID:92e3ae3cc5302e7b5cc1e769be468f39")
                    .data("sap-cssversion", "10.30.7.303325.0")
                    .data("sap-tray-type", "PLAIN")
                    .data("sap-tray-padding", "X")
                    .data("sap-ie", "Edge")
                    .data("sap-browserhistory", "disabled")
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
                    .header("Accept-Encoding", "gzip, deflate, br")
                    .header("Accept-Language", "en-US,en;q=0.9")
                    .header("Cache-Control", "max-age=0")
                    .header("Connection", "keep-alive")
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .header("Host", "matrix.upatras.gr")
                    .header("Origin", "https://progress.upatras.gr")
                    .header("Referer", "https://progress.upatras.gr/")
                    .header("Sec-Fetch-Dest", "iframe")
                    .header("Sec-Fetch-Mode", "navigate")
                    .header("Sec-Fetch-Site", "same-site")
                    .header("Sec-Fetch-User", "?1")
                    .header("Upgrade-Insecure-Requests", "1")
                    .header("User-Agent", USER_AGENT)
                    .cookies(cookies)
                    .followRedirects(false)
                    .method(Connection.Method.POST)
                    .timeout(60 * 1000)
                    .execute();
        } catch (SocketTimeoutException | UnknownHostException | HttpStatusException | ConnectException connException) {
            connected = false;
            logger.w("[UPATRAS] Warning: " + connException.getMessage(), connException);
            return;
        } catch (IOException e) {
            logger.e("[UPATRAS] Error: " + e.getMessage(), e);
            return;
        }

        Document document;
        try {
            document = response.parse();
            if (document.outerHtml().contains("Σελίδα δεν βρέθηκε. Ανανεώστε την σελίδα ή δοκιμάστε αργότερα. Εάν επιμένει το πρόβλημα, επικοινωνήστε με τον διαχειριστή Πύλης για υποστήριξη."))
                return;
        } catch (IOException e) {
            logger.e("[UPATRAS] Error: " + e.getMessage(), e);
            return;
        }

        if (document.outerHtml().contains("<title>Είσοδος στο σύστημα</title>")) return;
        String url2 = document.getElementsByAttributeValue("name", "sap.client.SsrClient.form").first().attributes().get("action");
        String sap_wd_secure_id = document.getElementsByAttributeValue("name", "sap-wd-secure-id").first().attributes().get("value");

        //
        //  Third & final Doc
        //

        try {
            response = Jsoup.connect("https://matrix.upatras.gr/sap/bc/webdynpro/SAP/" + url2)
                    .data("sap-charset", "utf-8")
                    .data("sap-wd-secure-id", sap_wd_secure_id)
                    .data("SAPEVENTQUEUE", SAPEVENTQUEUE)
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
                    .header("Accept-Encoding", "gzip, deflate, br")
                    .header("Accept-Language", "en-US,en;q=0.9")
                    .header("Connection", "keep-alive")
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .header("Host", "matrix.upatras.gr")
                    .header("Origin", "https://progress.upatras.gr")
                    .header("Referer", "https://progress.upatras.gr/")
                    .header("Sec-Fetch-Dest", "empty")
                    .header("Sec-Fetch-Mode", "cors")
                    .header("Sec-Fetch-Site", "same-origin")
                    .header("X-Requested-With", "XMLHttpRequest")
                    .header("X-XHR-Logon", "accept")
                    .header("User-Agent", USER_AGENT)
                    .cookies(cookies)
                    .followRedirects(false)
                    .method(Connection.Method.POST)
                    .timeout(60 * 1000)
                    .execute();
        } catch (SocketTimeoutException | UnknownHostException | HttpStatusException | ConnectException connException) {
            connected = false;
            logger.w("[UPATRAS] Warning: " + connException.getMessage(), connException);
            return;
        } catch (IOException e) {
            logger.e("[UPATRAS] Error: " + e.getMessage(), e);
            return;
        }

        Document document3;
        try {
            document3 = response.parse();
        } catch (IOException e) {
            logger.e("[UPATRAS] Error: " + e.getMessage(), e);
            return;
        }

        setInfoAndGradesPage(document3);
        setCookies(cookies, url, sap_ep_iviewhandle, sap_wd_tstamp, SAPEVENTQUEUE);
    }

    public boolean isConnected() {
        return connected;
    }

    private boolean isAuthorized(Connection.Response response, Map<String, String> JSESSIONID) {
        try {
            if (response.statusCode() != 200) {
                String location = response.header("location");
                response = Jsoup.connect("https://idp.upnet.gr" + location)
                        .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
                        .header("Accept-Encoding", "gzip, deflate, br")
                        .header("Accept-Language", "en-US,en;q=0.9")
                        .header("Cache-Control", "max-age=0")
                        .header("Connection", "keep-alive")
                        .header("Content-Type", "application/x-www-form-urlencoded")
                        .header("Host", "idp.upnet.gr")
                        .header("Referer", response.url().toString())
                        .header("Sec-Fetch-Dest", "document")
                        .header("Sec-Fetch-Mode", "navigate")
                        .header("Sec-Fetch-Site", "same-origin")
                        .header("Sec-Fetch-User", "?1")
                        .header("Upgrade-Insecure-Requests", "1")
                        .header("User-Agent", USER_AGENT)
                        .cookies(JSESSIONID)
                        .followRedirects(false)
                        .method(Connection.Method.GET)
                        .timeout(60 * 1000)
                        .execute();

                String html = response.parse().outerHtml();
                if (html.contains("Λανθασμένος κωδικός χρήστη.") ||
                    html.contains("Άγνωστο όνομα χρήστη.")) {
                    this.authorized = false;
                    return false;
                }
            }

            this.authorized = true;
            return true;
        } catch (SocketTimeoutException | UnknownHostException | HttpStatusException | ConnectException connException) {
            connected = false;
            logger.w("[UPATRAS] Warning: " + connException.getMessage(), connException);
            return false;
        } catch (IOException e) {
            logger.e("[UPATRAS] Error: " + e.getMessage(), e);
            return false;
        }
    }

    public boolean isAuthorized() {
        return authorized;
    }

    public Document getInfoAndGradesPage() {
        return infoAndGradesPage;
    }

    private void setInfoAndGradesPage(Document infoAndGradesPage) {
        String nestedHTML = infoAndGradesPage.outerHtml();
        this.infoAndGradesPage = Jsoup.parse(nestedHTML.substring(nestedHTML.indexOf("<![CDATA[")+9, nestedHTML.indexOf("]]>")));
    }

    public Map<String, String> getCookies() {
        return cookies;
    }

    public void setCookies(Map<String, String> cookies, String url, String sap_ep_iviewhandle, String sap_wd_tstamp, String SAPEVENTQUEUE) {
        this.cookies = cookies;
        this.cookies.put("url", url);
        this.cookies.put("sap_ep_iviewhandle", sap_ep_iviewhandle);
        this.cookies.put("sap_wd_tstamp", sap_wd_tstamp);
        this.cookies.put("SAPEVENTQUEUE", SAPEVENTQUEUE);
    }
}
