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

public class AUEBScraper {
    private final String USER_AGENT;
    private final String PRE_LOG;
    private boolean connected;
    private boolean authorized;
    private Document studentInfoAndGradesPage;
    private Map<String, String> cookies;
    private Logger logger = new Logger.Builder()
            .setNetworkInfoEnabled(true)
            .setServiceName("com.unipi.students.scraper.AUEBScraper")
            .setLogcatLogsEnabled(true)
            .setDatadogLogsEnabled(true)
            .setLoggerName("AUEBScraper")
            .build();


    public AUEBScraper(LoginForm loginForm) {
        this.connected = true;
        this.authorized = true;
        USER_AGENT = UserAgentGenerator.generate();
        PRE_LOG = "AUEB";
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
        Map<String, String> cookies2;
        Map<String, String> cookiesFinal;
        Map<String, String> finalDocCookies;

        //
        // Get Login Page
        //

        try {
            response = Jsoup.connect("https://e-grammateia.aueb.gr/unistudent/")
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
                    .header("Accept-Encoding", "gzip, deflate, br")
                    .header("Sec-Fetch-Dest", "document")
                    .header("Sec-Fetch-Mode", "navigate")
                    .header("Sec-Fetch-Site", "none")
                    .header("Sec-Fetch-User", "?1")
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

        cookies = response.cookies();

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
            response = Jsoup.connect("https://sso.aueb.gr" + loginUrl)
                    .data("username", username)
                    .data("password", password)
                    .data("lt", lt)
                    .data("execution", execution)
                    .data("_eventId", "submit")
                    .data("submitForm", "Είσοδος")
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
                    .header("Accept-Encoding", "gzip, deflate, br")
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .header("Host", "sso.aueb.gr")
                    .header("Origin", "https://sso.aueb.gr")
                    .header("Referer", loginPageUrl)
                    .header("Sec-Fetch-Dest", "document")
                    .header("Sec-Fetch-Mode", "navigate")
                    .header("Sec-Fetch-Site", "same-origin")
                    .header("Sec-Fetch-User", "?1")
                    .header("Upgrade-Insecure-Requests", "1")
                    .header("User-Agent", USER_AGENT)
                    .method(Connection.Method.POST)
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

        // authorization check
        if (!isAuthorized(response)) return;

        String location = response.header("location");

        //
        //  Redirect login?TARGET
        //

        try {
            response = Jsoup.connect(location)
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
                    .header("Accept-Encoding", "gzip, deflate")
                    .header("Host", "e-grammateia.aueb.gr")
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
        String jsessionid = response.cookie("JSESSIONID");
        cookies2 = response.cookies();

        //
        // Redirect login;jsessionid
        //

        try {
            response = Jsoup.connect(location)
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
                    .header("Accept-Encoding", "gzip, deflate")
                    .header("Host", "e-grammateia.aueb.gr")
                    .header("Upgrade-Insecure-Requests", "1")
                    .header("User-Agent", USER_AGENT)
                    .method(Connection.Method.GET)
                    .followRedirects(false)
                    .cookies(cookies2)
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
        cookies2.putAll(response.cookies());
        cookies = response.cookies();
        cookiesFinal = response.cookies();
        finalDocCookies = response.cookies();

        //
        // Redirect unistdent/
        //

        try {
            response = Jsoup.connect(location)
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
                    .header("Accept-Encoding", "gzip, deflate, br")
                    .header("Host", "e-grammateia.aueb.gr")
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

        location = response.header("location");
        cookiesFinal.putAll(response.cookies());

        //
        // Redirect login?TARGET
        //

        try {
            response = Jsoup.connect("https://e-grammateia.aueb.gr" + location)
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
                    .header("Accept-Encoding", "gzip, deflate, br")
                    .header("Host", "e-grammateia.aueb.gr")
                    .header("Sec-Fetch-Dest", "document")
                    .header("Sec-Fetch-Mode", "navigate")
                    .header("Sec-Fetch-Site", "cross-site")
                    .header("Sec-Fetch-User", "?1")
                    .header("Upgrade-Insecure-Requests", "1")
                    .header("User-Agent", USER_AGENT)
                    .method(Connection.Method.GET)
                    .followRedirects(false)
                    .cookies(cookies2)
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
        finalDocCookies.putAll(response.cookies());
        cookiesFinal.putAll(response.cookies());

        //
        // Redirect unistudent/
        //

        try {
            response = Jsoup.connect(location)
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
                    .header("Accept-Encoding", "gzip, deflate, br")
                    .header("Host", "e-grammateia.aueb.gr")
                    .header("Sec-Fetch-Dest", "document")
                    .header("Sec-Fetch-Mode", "navigate")
                    .header("Sec-Fetch-Site", "cross-site")
                    .header("Sec-Fetch-User", "?1")
                    .header("Upgrade-Insecure-Requests", "1")
                    .header("User-Agent", USER_AGENT)
                    .method(Connection.Method.GET)
                    .cookies(cookiesFinal)
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
        } catch (IOException e) {
            logger.e("[" + PRE_LOG + "] Error: " + e.getMessage(), e);
            return;
        }

        String scriptUrl = doc.getElementsByTag("script").first().attributes().get("src");
        Map<String, String> newCookies = cookiesFinal;
        newCookies.remove("JSESSIONID");

        //
        // Request replogin.js?app=unistu
        //

        try {
            response = Jsoup.connect("https://e-grammateia.aueb.gr" + scriptUrl)
                    .header("Accept", "*/*")
                    .header("Accept-Encoding", "gzip, deflate, br")
                    .header("Host", "e-grammateia.aueb.gr")
                    .header("Referer", "https://e-grammateia.aueb.gr/unistudent/")
                    .header("Sec-Fetch-Dest", "script")
                    .header("Sec-Fetch-Mode", "no-cors")
                    .header("Sec-Fetch-Site", "same-origin")
                    .header("User-Agent", USER_AGENT)
                    .method(Connection.Method.GET)
                    .followRedirects(false)
                    .cookies(newCookies)
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
        finalDocCookies.putAll(response.cookies());
        String JSESS = response.cookie("JSESSIONID");
        newCookies.put("JSESSIONID", jsessionid);

        //
        // Redirect login?TARGET=
        //

        try {
            response = Jsoup.connect("https://e-grammateia.aueb.gr" + location)
                    .header("Accept", "*/*")
                    .header("Accept-Encoding", "gzip, deflate, br")
                    .header("Host", "e-grammateia.aueb.gr")
                    .header("Referer", "https://e-grammateia.aueb.gr/unistudent/")
                    .header("Sec-Fetch-Dest", "script")
                    .header("Sec-Fetch-Mode", "no-cors")
                    .header("Sec-Fetch-Site", "same-origin")
                    .header("User-Agent", USER_AGENT)
                    .method(Connection.Method.GET)
                    .followRedirects(false)
                    .cookies(newCookies)
                    .execute();
        } catch (SocketTimeoutException | UnknownHostException | HttpStatusException | ConnectException connException) {
            connected = false;
            logger.w("[" + PRE_LOG + "] Warning: " + connException.getMessage(), connException);
            return;
        } catch (IOException e) {
            logger.e("[" + PRE_LOG + "] Error: " + e.getMessage(), e);
            return;
        }

        finalDocCookies.putAll(response.cookies());
        cookiesFinal.putAll(response.cookies());
        finalDocCookies.put("JSESSIONID", JSESS);

        int an1 = getRandom();
        finalDocCookies.put("a.0n1", String.valueOf(an1));

        //
        // Try fetch final Document
        //

        try {
            response = Jsoup.connect("https://e-grammateia.aueb.gr/a/srv/uniStu?a.0n1=" + an1 + "&a=PreviewGenDataSelf")
                    .header("Accept", "*/*")
                    .header("Accept-Encoding", "gzip, deflate, br")
                    .header("Host", "e-grammateia.aueb.gr")
                    .header("Referer", "https://e-grammateia.aueb.gr/unistudent/")
                    .header("Sec-Fetch-Dest", "empty")
                    .header("Sec-Fetch-Mode", "cors")
                    .header("Sec-Fetch-Site", "same-origin")
                    .header("User-Agent", USER_AGENT)
                    .method(Connection.Method.GET)
                    .cookies(finalDocCookies)
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
            setCookies(finalDocCookies);
        } catch (IOException e) {
            logger.e("[" + PRE_LOG + "] Error: " + e.getMessage(), e);
        }
    }

    private void getHtmlPages(Map<String, String> cookies) {
        if (cookies.size() == 0) return;
        Connection.Response response;
        Document doc;
        int an1 = getRandom();
        cookies.put("a.0n1", String.valueOf(an1));

        try {
            response = Jsoup.connect("https://e-grammateia.aueb.gr/a/srv/uniStu?a.0n1=" + an1 + "&a=PreviewGenDataSelf")
                    .header("Accept", "*/*")
                    .header("Accept-Encoding", "gzip, deflate, br")
                    .header("Host", "e-grammateia.aueb.gr")
                    .header("Referer", "https://e-grammateia.aueb.gr/unistudent/")
                    .header("Sec-Fetch-Dest", "empty")
                    .header("Sec-Fetch-Mode", "cors")
                    .header("Sec-Fetch-Site", "same-origin")
                    .header("User-Agent", USER_AGENT)
                    .method(Connection.Method.GET)
                    .cookies(cookies)
                    .execute();
        } catch (SocketTimeoutException | UnknownHostException | HttpStatusException | ConnectException connException) {
            return;
        } catch (IOException e) {
            logger.e("[" + PRE_LOG + "] Error: " + e.getMessage(), e);
            return;
        }

        if (response.statusCode() != 200) return;

        try {
            doc = response.parse();
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

    private void setStudentInfoAndGradesPage(Document studentInfoAndGradesPage) {
        this.studentInfoAndGradesPage = studentInfoAndGradesPage;
    }

    public Map<String, String> getCookies() {
        return cookies;
    }

    public void setCookies(Map<String, String> cookies) {
        this.cookies = cookies;
    }
}
