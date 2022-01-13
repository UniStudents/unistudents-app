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
import java.util.Map;
import java.util.Random;

public class HUAScraper {
    private final String USER_AGENT;
    private final String PRE_LOG;
    private boolean connected;
    private boolean authorized;
    private Document studentInfoAndGradesPage;
    private Map<String, String> cookies;
    private Logger logger = new Logger.Builder()
            .setNetworkInfoEnabled(true)
            .setServiceName("com.unipi.students.scraper.HUAScraper")
            .setLogcatLogsEnabled(true)
            .setDatadogLogsEnabled(true)
            .setLoggerName("HUAScraper")
            .build();

    public HUAScraper(LoginForm loginForm) {
        this.connected = true;
        this.authorized = true;
        USER_AGENT = UserAgentGenerator.generate();
        PRE_LOG = "HUA";
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
        Map<String, String> lastTARGET;
        String location;

        //
        // Get login page
        //

        try {
            response = Jsoup.connect("https://e-studies.hua.gr/unistudent/")
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
                    .header("Accept-Encoding", "gzip, deflate, br")
                    .header("Sec-Fetch-Dest", "document")
                    .header("Sec-Fetch-Mode", "navigate")
                    .header("Sec-Fetch-Site", "none")
                    .header("Upgrade-Insecure-Requests", "1")
                    .header("User-Agent", USER_AGENT)
                    .method(Connection.Method.GET)
                    .execute();
        } catch (SocketTimeoutException | UnknownHostException | HttpStatusException | ConnectException connException) {
            connected = false;
            logger.w("[" + PRE_LOG + "] Warning: " + connException.getMessage(), connException);
            return;
        } catch (IOException e) {
            logger.e("[" + PRE_LOG + "] Error: " + e.getMessage(), e);
            return;
        }

        Document doc;
        try {
            doc = response.parse();
        } catch (IOException e) {
            logger.e("[" + PRE_LOG + "] Error: " + e.getMessage(), e);
            return;
        }

        Elements el = doc.getElementsByAttributeValue("name", "lt");
        String lt = el.first().attributes().get("value");
        Elements exec = doc.getElementsByAttributeValue("name", "execution");
        String execution = exec.first().attributes().get("value");
        String loginUrl = doc.select("form").first().attributes().get("action");
        String loginPageUrl = response.url().toString();

        //
        // Submit Login
        //

        try {
            response = Jsoup.connect("https://sso.hua.gr" + loginUrl)
                    .data("username", username)
                    .data("password", password)
                    .data("lt", lt)
                    .data("execution", execution)
                    .data("_eventId", "submit")
                    .data("submitForm", "Είσοδος")
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
                    .header("Accept-Encoding", "gzip, deflate, br")
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .header("Host", "sso.hua.gr")
                    .header("Origin", "https://sso.hua.gr")
                    .header("Referer", loginPageUrl)
                    .header("Sec-Fetch-Dest", "document")
                    .header("Sec-Fetch-Mode", "navigate")
                    .header("Sec-Fetch-Site", "same-origin")
                    .header("Sec-Fetch-User", "?1")
                    .header("Upgrade-Insecure-Requests", "1")
                    .header("User-Agent", USER_AGENT)
                    .method(Connection.Method.POST)
                    .followRedirects(false)
                    .cookies(response.cookies())
                    .execute();
        } catch (SocketTimeoutException | UnknownHostException | HttpStatusException | ConnectException connException) {
            connected = false;
            logger.w("[" + PRE_LOG + "] Warning: " + connException.getMessage(), connException);
            return;
        } catch (IOException e) {
            logger.e("[" + PRE_LOG + "] Error: " + e.getMessage(), e);
            return;
        }

        // authorization check
        if (!isAuthorized(response)) return;

        location = response.header("location");

        //
        //  Redirect login?TARGET
        //

        try {
            response = Jsoup.connect(location)
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
                    .header("Accept-Encoding", "gzip, deflate")
                    .header("Host", "e-studies.hua.gr")
                    .header("Referer", "https://sso.hua.gr/")
                    .header("Sec-Fetch-Dest", "document")
                    .header("Sec-Fetch-Mode", "navigate")
                    .header("Sec-Fetch-Site", "same-origin")
                    .header("Sec-Fetch-User", "?1")
                    .header("Upgrade-Insecure-Requests", "1")
                    .header("User-Agent", USER_AGENT)
                    .method(Connection.Method.GET)
                    .followRedirects(false)
                    .execute();
        } catch (SocketTimeoutException | UnknownHostException | HttpStatusException | ConnectException connException) {
            connected = false;
            logger.w("[" + PRE_LOG + "] Warning: " + connException.getMessage(), connException);
            return;
        } catch (IOException e) {
            logger.e("[" + PRE_LOG + "] Error: " + e.getMessage(), e);
            return;
        }

        location = response.header("location");
        lastTARGET = response.cookies();
        cookies = response.cookies();

        // SSN value on query parameters is the same as JSESSIONID value
        String ssnValue = getSSNValue(location);

        //
        // Redirect login?TARGET
        //

        try {
            response = Jsoup.connect(location)
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
                    .header("Accept-Encoding", "gzip, deflate, br")
                    .header("Host", "e-studies.hua.gr")
                    .header("Sec-Fetch-Dest", "document")
                    .header("Sec-Fetch-Mode", "navigate")
                    .header("Sec-Fetch-Site", "cross-site")
                    .header("Sec-Fetch-User", "?1")
                    .header("Upgrade-Insecure-Requests", "1")
                    .header("User-Agent", USER_AGENT)
                    .method(Connection.Method.GET)
                    .followRedirects(false)
                    .cookies(cookies)
                    .execute();
        } catch (SocketTimeoutException | UnknownHostException | HttpStatusException | ConnectException connException) {
            connected = false;
            logger.w("[" + PRE_LOG + "] Warning: " + connException.getMessage(), connException);
            return;
        } catch (IOException e) {
            logger.e("[" + PRE_LOG + "] Error: " + e.getMessage(), e);
            return;
        }

        lastTARGET.putAll(response.cookies());
        cookies = response.cookies();
        if (ssnValue != null) {
            cookies.put("JSESSIONID", ssnValue);
        }
        location = response.header("location");

        //
        // Redirect unistudent/
        //

        try {
            response = Jsoup.connect(location)
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
                    .header("Accept-Encoding", "gzip, deflate")
                    .header("Host", "e-studies.hua.gr")
                    .header("Upgrade-Insecure-Requests", "1")
                    .header("User-Agent", USER_AGENT)
                    .method(Connection.Method.GET)
                    .followRedirects(false)
                    .cookies(cookies)
                    .execute();
        } catch (SocketTimeoutException | UnknownHostException | HttpStatusException | ConnectException connException) {
            connected = false;
            logger.w("[" + PRE_LOG + "] Warning: " + connException.getMessage(), connException);
            return;
        } catch (IOException e) {
            logger.e("[" + PRE_LOG + "] Error: " + e.getMessage(), e);
            return;
        }

        location = response.header("location");

        //
        // Redirect unistudent/
        //

        try {
            response = Jsoup.connect(location)
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
                    .header("Accept-Encoding", "gzip, deflate, br")
                    .header("Host", "e-studies.hua.gr")
                    .header("Sec-Fetch-Dest", "document")
                    .header("Sec-Fetch-Mode", "navigate")
                    .header("Sec-Fetch-Site", "cross-site")
                    .header("Sec-Fetch-User", "?1")
                    .header("Upgrade-Insecure-Requests", "1")
                    .header("User-Agent", USER_AGENT)
                    .method(Connection.Method.GET)
                    .cookies(cookies)
                    .execute();
        } catch (SocketTimeoutException | UnknownHostException | HttpStatusException | ConnectException connException) {
            connected = false;
            logger.w("[" + PRE_LOG + "] Warning: " + connException.getMessage(), connException);
            return;
        } catch (IOException e) {
            logger.e("[" + PRE_LOG + "] Error: " + e.getMessage(), e);
            return;
        }

        // replogin.js doesn't need JSESSIONID cookie
        cookies.remove("JSESSIONID");
        String scriptUrl;
        try {
            doc = response.parse();
            scriptUrl = doc.getElementsByTag("script").first().attributes().get("src");
        } catch (IOException e) {
            logger.e("[" + PRE_LOG + "] Error: " + e.getMessage(), e);
            return;
        }

        //
        // replogin.js
        //

        try {
            response = Jsoup.connect("https://e-studies.hua.gr" + scriptUrl)
                    .header("Accept", "*/*")
                    .header("Accept-Encoding", "gzip, deflate")
                    .header("Host", "e-studies.hua.gr")
                    .header("Upgrade-Insecure-Requests", "1")
                    .header("Sec-Fetch-Dest", "script")
                    .header("Sec-Fetch-Mode", "no-cors")
                    .header("Sec-Fetch-Site", "same-origin")
                    .header("User-Agent", USER_AGENT)
                    .method(Connection.Method.GET)
                    .followRedirects(false)
                    .cookies(cookies)
                    .ignoreContentType(true)
                    .execute();
        } catch (SocketTimeoutException | UnknownHostException | HttpStatusException | ConnectException connException) {
            connected = false;
            logger.w("[" + PRE_LOG + "] Warning: " + connException.getMessage(), connException);
            return;
        } catch (IOException e) {
            logger.e("[" + PRE_LOG + "] Error: " + e.getMessage(), e);
            return;
        }

        location = response.header("location");
        cookies.putAll(response.cookies());

        //
        // Last redirect login?TARGET
        //

        try {
            response = Jsoup.connect("https://e-studies.hua.gr/" + location)
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
                    .header("Accept-Encoding", "gzip, deflate, br")
                    .header("Host", "e-studies.hua.gr")
                    .header("Sec-Fetch-Dest", "document")
                    .header("Sec-Fetch-Mode", "navigate")
                    .header("Sec-Fetch-Site", "cross-site")
                    .header("Sec-Fetch-User", "?1")
                    .header("Upgrade-Insecure-Requests", "1")
                    .header("User-Agent", USER_AGENT)
                    .method(Connection.Method.GET)
                    .followRedirects(false)
                    .cookies(lastTARGET)
                    .ignoreContentType(true)
                    .execute();
        } catch (SocketTimeoutException | UnknownHostException | HttpStatusException | ConnectException connException) {
            connected = false;
            logger.w("[" + PRE_LOG + "] Warning: " + connException.getMessage(), connException);
            return;
        } catch (IOException e) {
            logger.e("[" + PRE_LOG + "] Error: " + e.getMessage(), e);
            return;
        }

        cookies.putAll(response.cookies());
        int an1 = getRandom();
        cookies.put("a.0n1", String.valueOf(an1));

        //
        // Try fetch final Document
        //

        try {
            response = Jsoup.connect("https://e-studies.hua.gr/a/srv/uniStu?a.0n1=" + an1 + "&a=PreviewGenDataSelf")
                    .header("Accept", "*/*")
                    .header("Accept-Encoding", "gzip, deflate, br")
                    .header("Host", "e-studies.hua.gr")
                    .header("Referer", "https://e-studies.hua.gr/unistudent/")
                    .header("Sec-Fetch-Dest", "empty")
                    .header("Sec-Fetch-Mode", "cors")
                    .header("Sec-Fetch-Site", "same-origin")
                    .header("User-Agent", USER_AGENT)
                    .method(Connection.Method.GET)
                    .cookies(cookies)
                    .ignoreContentType(true)
                    .execute();
        } catch (SocketTimeoutException | UnknownHostException | HttpStatusException | ConnectException connException) {
            connected = false;
            logger.w("[" + PRE_LOG + "] Warning: " + connException.getMessage(), connException);
            return;
        } catch (IOException e) {
            logger.e("[" + PRE_LOG + "] Error: " + e.getMessage(), e);
            return;
        }

        try {
            doc = response.parse();
            setStudentInfoAndGradesPage(doc);
            setCookies(cookies);
        } catch (IOException e) {
            logger.e("[" + PRE_LOG + "] Error: " + e.getMessage(), e);
        }
    }

    private void getHtmlPages(Map<String, String> cookies) {
        Connection.Response response;
        Document doc;
        int an1 = getRandom();
        cookies.put("a.0n1", String.valueOf(an1));

        try {
            response = Jsoup.connect("https://e-studies.hua.gr/a/srv/uniStu?a.0n1=" + an1 + "&a=PreviewGenDataSelf")
                    .header("Accept", "*/*")
                    .header("Accept-Encoding", "gzip, deflate, br")
                    .header("Host", "e-studies.hua.gr")
                    .header("Referer", "https://e-studies.hua.gr/unistudent/")
                    .header("Sec-Fetch-Dest", "empty")
                    .header("Sec-Fetch-Mode", "cors")
                    .header("Sec-Fetch-Site", "same-origin")
                    .header("User-Agent", USER_AGENT)
                    .method(Connection.Method.GET)
                    .cookies(cookies)
                    .execute();
        } catch (SocketTimeoutException | UnknownHostException | HttpStatusException | ConnectException connException) {
            logger.w("[" + PRE_LOG + "] Warning: " + connException.getMessage(), connException);
            return;
        } catch (IOException e) {
            logger.e("[" + PRE_LOG + "] Error: " + e.getMessage(), e);
            return;
        }

        try {
            doc = response.parse();
            if (doc.toString().contains("<title>Κεντρική Υπηρεσία Πιστοποίησης</title>")) return;
            setStudentInfoAndGradesPage(doc);
            setCookies(cookies);
        } catch (IOException e) {
            logger.e("[" + PRE_LOG + "] Error: " + e.getMessage(), e);
        }
    }

    private boolean isAuthorized(Connection.Response response) {
        if (response.statusCode() == 200) {
            this.authorized = false;
            return false;
        } else {
            this.authorized = true;
            return true;
        }
    }

    private int getRandom() {
        Random rand = new Random();
        return rand.nextInt((999999 - 15250) + 1) + 15250;
    }

    private String getSSNValue(String url) {
        for (String element : url.split("&")) {
            if (element.contains("ssn")) {
                return element.replace("ssn=", "");
            }
        }
        return null;
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
