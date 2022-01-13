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

public class ECEScraper {
    private final String USER_AGENT;
    private boolean connected;
    private boolean authorized;
    private Document studentInfoAndGradesPage;
    private Map<String, String> cookies;
    private Logger logger = new Logger.Builder()
            .setNetworkInfoEnabled(true)
            .setServiceName("com.unipi.students.scraper.ECEScraper")
            .setLogcatLogsEnabled(true)
            .setDatadogLogsEnabled(true)
            .setLoggerName("ECEScraper")
            .build();

    public ECEScraper(LoginForm loginForm) {
        this.connected = true;
        this.authorized = true;
        USER_AGENT = UserAgentGenerator.generate();
        this.getDocuments(loginForm.getUsername(), loginForm.getPassword(), loginForm.getCookies());
    }

    private void getDocuments(String username, String password, Map<String, String> cookies) {
        if (cookies == null) {
            System.out.println("======> DEBUG: Normal request");
            getHtmlPages(username, password);
        } else {
            System.out.println("======> DEBUG: Cookies request");
            getHtmlPages(cookies);
            if (studentInfoAndGradesPage == null) {
                System.out.println("======> DEBUG: Normal request after cookies request");
                getHtmlPages(username, password);
            }
        }
    }

    private void getHtmlPages(String username, String password) {
        username = username.trim();
        password = password.trim();
        Connection.Response response;

        Map<String, String> cookies;

        //
        // Get /login page
        //

        try {
            response = Jsoup.connect("https://students.ece.ntua.gr/login")
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
                    .header("Accept-Encoding", "gzip, deflate. br")
                    .header("Accept-Language", "en-US,en;q=0.9,el-GR;q=0.8,el;q=0.7")
                    .header("Connection", "keep-alive")
                    .header("Host", "students.ece.ntua.gr")
                    .header("Sec-Fetch-Dest", "document")
                    .header("Sec-Fetch-Mode", "navigate")
                    .header("Sec-Fetch-Site", "none")
                    .header("Sec-Fetch-User", "?1")
                    .header("Upgrade-Insecure-Request", "1")
                    .header("User-Agent", USER_AGENT)
                    .method(Connection.Method.GET)
                    .execute();
        } catch (SocketTimeoutException | UnknownHostException | HttpStatusException | ConnectException connException) {
            authorized = false;
            logger.w("[NTUA.ECE] Warning: " + connException.getMessage(), connException);
            return;
        } catch (IOException e) {
            logger.e("[NTUA.ECE] Error: " + e.getMessage(), e);
            return;
        }

        cookies = response.cookies();


        //
        // Get /Shibboleth.sso/Login?target=https://students.ece.ntua.gr/authorize
        //

        try {
            response = Jsoup.connect("https://students.ece.ntua.gr/Shibboleth.sso/Login?target=https://students.ece.ntua.gr/authorize")
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
                    .header("Accept-Encoding", "gzip, deflate. br")
                    .header("Accept-Language", "en-US,en;q=0.9,el-GR;q=0.8,el;q=0.7")
                    .header("Connection", "keep-alive")
                    .header("Host", "students.ece.ntua.gr")
                    .header("Referer", "https://students.ece.ntua.gr/login")
                    .header("Sec-Fetch-Dest", "document")
                    .header("Sec-Fetch-Mode", "navigate")
                    .header("Sec-Fetch-Site", "same-origin")
                    .header("Sec-Fetch-User", "?1")
                    .header("Upgrade-Insecure-Request", "1")
                    .header("User-Agent", USER_AGENT)
                    .followRedirects(false)
                    .cookies(cookies)
                    .method(Connection.Method.GET)
                    .execute();
        } catch (SocketTimeoutException | UnknownHostException | HttpStatusException | ConnectException connException) {
            authorized = false;
            logger.w("[NTUA.ECE] Warning: " + connException.getMessage(), connException);
            return;
        } catch (IOException e) {
            logger.e("[NTUA.ECE] Error: " + e.getMessage(), e);
            return;
        }

        String location = response.header("location");


        //
        // Redirect login.ntua.gr/idp/profile/SAML2/Redirect/SSO?SAMLRequest
        //

        try {
            response = Jsoup.connect(location)
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
                    .header("Accept-Encoding", "gzip, deflate. br")
                    .header("Accept-Language", "en-US,en;q=0.9,el-GR;q=0.8,el;q=0.7")
                    .header("Connection", "keep-alive")
                    .header("Host", "login.ntua.gr")
                    .header("Referer", "https://students.ece.ntua.gr/")
                    .header("Sec-Fetch-Dest", "document")
                    .header("Sec-Fetch-Mode", "navigate")
                    .header("Sec-Fetch-Site", "same-origin")
                    .header("Sec-Fetch-User", "?1")
                    .header("Upgrade-Insecure-Request", "1")
                    .header("User-Agent", USER_AGENT)
                    .followRedirects(false)
                    .method(Connection.Method.GET)
                    .execute();
        } catch (SocketTimeoutException | UnknownHostException | HttpStatusException | ConnectException connException) {
            authorized = false;
            logger.w("[NTUA.ECE] Warning: " + connException.getMessage(), connException);
            return;
        } catch (IOException e) {
            logger.e("[NTUA.ECE] Error: " + e.getMessage(), e);
            return;
        }


        location = response.header("location");
        Map<String, String> cookies2 = response.cookies();


        //
        // Redirect /idp/profile/SAML2/Redirect/SSO;jsessionid
        //

        try {
            response = Jsoup.connect("https://login.ntua.gr" + location)
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
                    .header("Accept-Encoding", "gzip, deflate. br")
                    .header("Accept-Language", "en-US,en;q=0.9,el-GR;q=0.8,el;q=0.7")
                    .header("Connection", "keep-alive")
                    .header("Host", "login.ntua.gr")
                    .header("Referer", "https://students.ece.ntua.gr/")
                    .header("Sec-Fetch-Dest", "document")
                    .header("Sec-Fetch-Mode", "navigate")
                    .header("Sec-Fetch-Site", "same-site")
                    .header("Sec-Fetch-User", "?1")
                    .header("Upgrade-Insecure-Request", "1")
                    .header("User-Agent", USER_AGENT)
                    .followRedirects(false)
                    .cookies(cookies2)
                    .method(Connection.Method.GET)
                    .execute();
        } catch (SocketTimeoutException | UnknownHostException | HttpStatusException | ConnectException connException) {
            authorized = false;
            logger.w("[NTUA.ECE] Warning: " + connException.getMessage(), connException);
            return;
        } catch (IOException e) {
            logger.e("[NTUA.ECE] Error: " + e.getMessage(), e);
            return;
        }


        //
        // POST login
        //

        try {
            response = Jsoup.connect("https://login.ntua.gr" + location)
                    .data("j_username", username)
                    .data("j_password", password)
                    .data("_eventId_proceed", "Login")
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
                    .header("Accept-Encoding", "gzip, deflate. br")
                    .header("Accept-Language", "en-US,en;q=0.9,el-GR;q=0.8,el;q=0.7")
                    .header("Connection", "keep-alive")
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .header("Host", "login.ntua.gr")
                    .header("Origin", "https://login.ntua.gr")
                    .header("Referer", location)
                    .header("Sec-Fetch-Dest", "document")
                    .header("Sec-Fetch-Mode", "navigate")
                    .header("Sec-Fetch-Site", "same-origin")
                    .header("Sec-Fetch-User", "?1")
                    .header("Upgrade-Insecure-Request", "1")
                    .header("User-Agent", USER_AGENT)
                    .followRedirects(false)
                    .cookies(cookies2)
                    .method(Connection.Method.POST)
                    .execute();
        } catch (SocketTimeoutException | UnknownHostException | HttpStatusException | ConnectException connException) {
            authorized = false;
            logger.w("[NTUA.ECE] Warning: " + connException.getMessage(), connException);
            return;
        } catch (IOException e) {
            logger.e("[NTUA.ECE] Error: " + e.getMessage(), e);
            return;
        }

        String location2 = response.header("location");
        Map<String, String> cookies3 = response.cookies();
        cookies3.putAll(cookies2);


        //
        // Redirect /idp/profile/SAML2/Redirect/SSO;jsessionid
        //

        try {
            response = Jsoup.connect("https://login.ntua.gr" + location2)
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
                    .header("Accept-Encoding", "gzip, deflate. br")
                    .header("Accept-Language", "en-US,en;q=0.9,el-GR;q=0.8,el;q=0.7")
                    .header("Connection", "keep-alive")
                    .header("Host", "login.ntua.gr")
                    .header("Referer", location)
                    .header("Sec-Fetch-Dest", "document")
                    .header("Sec-Fetch-Mode", "navigate")
                    .header("Sec-Fetch-Site", "same-origin")
                    .header("Sec-Fetch-User", "?1")
                    .header("Upgrade-Insecure-Request", "1")
                    .header("User-Agent", USER_AGENT)
                    .followRedirects(false)
                    .cookies(cookies3)
                    .method(Connection.Method.GET)
                    .execute();
        } catch (SocketTimeoutException | UnknownHostException | HttpStatusException | ConnectException connException) {
            authorized = false;
            logger.w("[NTUA.ECE] Warning: " + connException.getMessage(), connException);
            return;
        } catch (IOException e) {
            logger.e("[NTUA.ECE] Error: " + e.getMessage(), e);
            return;
        }

        // authorization check
        if (!isAuthorized(response)) return;


        //
        // Post /idp/profile/SAML2/Redirect/SSO;jsessionid
        //

        try {
            response = Jsoup.connect("https://login.ntua.gr" + location2)
                    .data("_shib_idp_consentIds", "businessCategory")
                    .data("_shib_idp_consentIds", "commonName")
                    .data("_shib_idp_consentIds", "commonName-el")
                    .data("_shib_idp_consentIds", "eduPersonAffiliation")
                    .data("_shib_idp_consentIds", "eduPersonEntitlement")
                    .data("_shib_idp_consentIds", "eduPersonOrgUnitDN")
                    .data("_shib_idp_consentIds", "eduPersonPrimaryAffiliation")
                    .data("_shib_idp_consentIds", "eduPersonPrimaryOrgUnitDN")
                    .data("_shib_idp_consentIds", "eduPersonPrincipalName")
                    .data("_shib_idp_consentIds", "eduPersonScopedAffiliation")
                    .data("_shib_idp_consentIds", "email")
                    .data("_shib_idp_consentIds", "givenName")
                    .data("_shib_idp_consentIds", "givenName-el")
                    .data("_shib_idp_consentIds", "organizationalUnit")
                    .data("_shib_idp_consentIds", "surname")
                    .data("_shib_idp_consentIds", "surname-el")
                    .data("_shib_idp_consentOptions", "_shib_idp_rememberConsent")
                    .data("_eventId_proceed", "Accept")
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
                    .header("Accept-Encoding", "gzip, deflate. br")
                    .header("Accept-Language", "en-US,en;q=0.9,el-GR;q=0.8,el;q=0.7")
                    .header("Connection", "keep-alive")
                    .header("Host", "login.ntua.gr")
                    .header("Origin", "https://login.ntua.gr")
                    .header("Referer", location2)
                    .header("Sec-Fetch-Dest", "document")
                    .header("Sec-Fetch-Mode", "navigate")
                    .header("Sec-Fetch-Site", "same-origin")
                    .header("Sec-Fetch-User", "?1")
                    .header("Upgrade-Insecure-Request", "1")
                    .header("User-Agent", USER_AGENT)
                    .followRedirects(false)
                    .cookies(cookies3)
                    .method(Connection.Method.POST)
                    .execute();
        } catch (SocketTimeoutException | UnknownHostException | HttpStatusException | ConnectException connException) {
            authorized = false;
            logger.w("[NTUA.ECE] Warning: " + connException.getMessage(), connException);
            return;
        } catch (IOException e) {
            logger.e("[NTUA.ECE] Error: " + e.getMessage(), e);
            return;
        }

        Document doc;
        try {
            doc = response.parse();
        } catch (IOException e) {
            logger.e("[NTUA.ECE] Error: " + e.getMessage(), e);
            return;
        }

        Elements firstElement = doc.getElementsByAttributeValue("name", "RelayState");
        Elements secondElement = doc.getElementsByAttributeValue("name", "SAMLResponse");

        String RelayState = firstElement.first().attributes().get("value");
        String SAMLResponse = secondElement.first().attributes().get("value");

        //
        // POST /Shibboleth.sso/SAML2/POST
        //

        try {
            response = Jsoup.connect("https://students.ece.ntua.gr/Shibboleth.sso/SAML2/POST")
                    .data("RelayState", RelayState)
                    .data("SAMLResponse", SAMLResponse)
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
                    .header("Accept-Encoding", "gzip, deflate. br")
                    .header("Accept-Language", "en-US,en;q=0.9,el-GR;q=0.8,el;q=0.7")
                    .header("Connection", "keep-alive")
                    .header("Host", "students.ece.ntua.gr")
                    .header("Origin", "https://login.ntua.gr")
                    .header("Referer", "https://login.ntua.gr/")
                    .header("Sec-Fetch-Dest", "document")
                    .header("Sec-Fetch-Mode", "navigate")
                    .header("Sec-Fetch-Site", "same-site")
                    .header("Sec-Fetch-User", "?1")
                    .header("Upgrade-Insecure-Request", "1")
                    .header("User-Agent", USER_AGENT)
                    .followRedirects(false)
                    .cookies(cookies)
                    .method(Connection.Method.POST)
                    .execute();
        } catch (SocketTimeoutException | UnknownHostException | HttpStatusException | ConnectException connException) {
            authorized = false;
            logger.w("[NTUA.ECE] Warning: " + connException.getMessage(), connException);
            return;
        } catch (IOException e) {
            logger.e("[NTUA.ECE] Error: " + e.getMessage(), e);
            return;
        }

        location = response.header("location");
        if (location == null) return;
        Map<String, String> cookies5 = response.cookies();
        cookies5.putAll(response.cookies());


        //
        // Redirect /authorize
        //

        try {
            response = Jsoup.connect(location)
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
                    .header("Accept-Encoding", "gzip, deflate. br")
                    .header("Accept-Language", "en-US,en;q=0.9,el-GR;q=0.8,el;q=0.7")
                    .header("Connection", "keep-alive")
                    .header("Host", "students.ece.ntua.gr")
                    .header("Referer", "https://login.ntua.gr/")
                    .header("Sec-Fetch-Dest", "document")
                    .header("Sec-Fetch-Mode", "navigate")
                    .header("Sec-Fetch-Site", "same-site")
                    .header("Upgrade-Insecure-Request", "1")
                    .header("User-Agent", USER_AGENT)
                    .followRedirects(false)
                    .cookies(cookies5)
                    .method(Connection.Method.GET)
                    .execute();
        } catch (SocketTimeoutException | UnknownHostException | HttpStatusException | ConnectException connException) {
            authorized = false;
            logger.w("[NTUA.ECE] Warning: " + connException.getMessage(), connException);
            return;
        } catch (IOException e) {
            logger.e("[NTUA.ECE] Error: " + e.getMessage(), e);
            return;
        }

        location = response.header("location");
        cookies5.putAll(response.cookies());


        //
        // Redirect /courses
        //

        try {
            response = Jsoup.connect(location)
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
                    .header("Accept-Encoding", "gzip, deflate. br")
                    .header("Accept-Language", "en-US,en;q=0.9,el-GR;q=0.8,el;q=0.7")
                    .header("Connection", "keep-alive")
                    .header("Host", "students.ece.ntua.gr")
                    .header("Referer", "https://login.ntua.gr/")
                    .header("Sec-Fetch-Dest", "document")
                    .header("Sec-Fetch-Mode", "navigate")
                    .header("Sec-Fetch-Site", "same-site")
                    .header("Upgrade-Insecure-Request", "1")
                    .header("User-Agent", USER_AGENT)
                    .followRedirects(false)
                    .cookies(cookies5)
                    .method(Connection.Method.GET)
                    .execute();
        } catch (SocketTimeoutException | UnknownHostException | HttpStatusException | ConnectException connException) {
            authorized = false;
            logger.w("[NTUA.ECE] Warning: " + connException.getMessage(), connException);
            return;
        } catch (IOException e) {
            logger.e("[NTUA.ECE] Error: " + e.getMessage(), e);
            return;
        }

        cookies5.putAll(response.cookies());

        try {
            Document document = response.parse();
            setStudentInfoAndGradesPage(document);
            setCookies(cookies5);
        } catch (IOException e) {
            logger.e("[NTUA.ECE] Error: " + e.getMessage(), e);
        }
    }


    private void getHtmlPages(Map<String, String> cookies) {
        Connection.Response response;
        Document document;

        Map<String, String> cookiesCopy = new HashMap<>(cookies);
        cookiesCopy.remove("department");
        cookiesCopy.remove("category");
        cookiesCopy.remove("username");
        cookiesCopy.remove("token");

        //
        // Redirect /courses
        //

        try {
            response = Jsoup.connect("https://students.ece.ntua.gr/courses")
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
                    .header("Accept-Encoding", "gzip, deflate. br")
                    .header("Accept-Language", "en-US,en;q=0.9,el-GR;q=0.8,el;q=0.7")
                    .header("Connection", "keep-alive")
                    .header("Host", "students.ece.ntua.gr")
                    .header("Referer", "https://login.ntua.gr/")
                    .header("Sec-Fetch-Dest", "document")
                    .header("Sec-Fetch-Mode", "navigate")
                    .header("Sec-Fetch-Site", "same-site")
                    .header("Upgrade-Insecure-Request", "1")
                    .header("User-Agent", USER_AGENT)
                    .followRedirects(false)
                    .cookies(cookiesCopy)
                    .method(Connection.Method.GET)
                    .execute();
        } catch (SocketTimeoutException | UnknownHostException | HttpStatusException | ConnectException connException) {
            authorized = false;
            logger.w("[NTUA.ECE] Warning: " + connException.getMessage(), connException);
            return;
        } catch (IOException e) {
            logger.e("[NTUA.ECE] Error: " + e.getMessage(), e);
            return;
        }

        cookies.putAll(response.cookies());

        try {
            document = response.parse();
            if (document.outerHtml().contains("<title>Redirecting to")) return;
            setStudentInfoAndGradesPage(document);
            setCookies(cookies);
        } catch (IOException e) {
            logger.e("[NTUA.ECE] Error: " + e.getMessage(), e);
        }
    }

    public boolean isConnected() {
        return connected;
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
    }

    public boolean isAuthorized() {
        return authorized;
    }

    private boolean isAuthorized(Connection.Response response) {
        try {
            Document document = response.parse();
            String html = document.outerHtml();
            if (html.contains("Authentication failed as there is no such username") ||
                    html.contains("Incorrect password, please retype it paying attention to capitals and input language") ||
                    html.contains("warning_sign")) {
                authorized = false;
                return false;
            }
            return true;
        } catch (IOException e) {
            logger.e("[NTUA.ECE] Error: " + e.getMessage(), e);
            return false;
        }
    }

    public void setAuthorized(boolean authorized) {
        this.authorized = authorized;
    }

    public Document getStudentInfoAndGradesPage() {
        return studentInfoAndGradesPage;
    }

    public void setStudentInfoAndGradesPage(Document studentInfoAndGradesPage) {
        this.studentInfoAndGradesPage = studentInfoAndGradesPage;
    }

    public Map<String, String> getCookies() {
        return cookies;
    }

    public void setCookies(Map<String, String> cookies) {
        this.cookies = cookies;
    }
}
