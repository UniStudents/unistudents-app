package com.unipi.students.scraper;

import com.datadog.android.log.Logger;
import com.unipi.students.common.UserAgentGenerator;
import com.unipi.students.model.LoginForm;

import org.jsoup.Connection;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Map;

public class SEFScraper {
    private final String USER_AGENT;
    private boolean connected;
    private boolean authorized;
    private Document studentInfoPage;
    private Document gradesPage;
    private Map<String, String> cookies;
    private final Logger logger = new Logger.Builder()
            .setNetworkInfoEnabled(true)
            .setServiceName("com.unipi.students.scraper.SEFScraper")
            .setLogcatLogsEnabled(true)
            .setDatadogLogsEnabled(true)
            .setLoggerName("SEFScraper")
            .build();

    public SEFScraper(LoginForm loginForm) {
        this.connected = true;
        this.authorized = true;
        USER_AGENT = UserAgentGenerator.generate();
        getDocuments(loginForm.getUsername(), loginForm.getPassword(), loginForm.getCookies());
    }

    private void getDocuments(String username, String password, Map<String, String> cookies) {
        if (cookies == null) {
            getHtmlPages(username, password);
        } else {
            getHtmlPages(cookies);
            if (studentInfoPage == null || gradesPage == null) {
                getHtmlPages(username, password);
            }
        }
    }

    private void getHtmlPages(String username, String password) {
        username = username.trim();
        password = password.trim();

        Connection.Response response;
        Map<String, String> cookies;
        Document document;
        boolean authorized;

        // Get cookies
        try {
            response = Jsoup.connect("https://sef.samos.aegean.gr")
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
                    .header("Accept-Encoding", "gzip, deflate, br")
                    .header("Accept-Language", "el-GR,el;q=0.8,en-US;q=0.5,en;q=0.3")
                    .header("Connection", "keep-alive")
                    .header("Host", "sef.samos.aegean.gr")
                    .header("Upgrade-Insecure-Requests", "1")
                    .header("User-Agent", USER_AGENT)
                    .method(Connection.Method.GET)
                    .execute();
        } catch (SocketTimeoutException | UnknownHostException | HttpStatusException connException) {
            connected = false;
            logger.w("[AEGEAN.SEF] Warning: " +connException.getMessage(), connException);
            return;
        } catch (IOException e) {
            logger.e("[AEGEAN.SEF] Error: " + e.getMessage(), e);
            return;
        }

        // update cookies
        cookies = response.cookies();

        // Attempt to connect and get grades page
        try {
            response = Jsoup.connect("https://sef.samos.aegean.gr/authentication.php")
                    .data("username", username)
                    .data("password", password)
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
                    .header("Accept-Encoding", "gzip, deflate, br")
                    .header("Accept-Language", "el-GR,el;q=0.8,en-US;q=0.5,en;q=0.3")
                    .header("Connection", "keep-alive")
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .header("Host", "sef.samos.aegean.gr")
                    .header("Origin", "https://sef.samos.aegean.gr")
                    .header("Referer", "https://sef.samos.aegean.gr/")
                    .header("Upgrade-Insecure-Requests", "1")
                    .header("User-Agent", USER_AGENT)
                    .method(Connection.Method.POST)
                    .cookies(cookies)
                    .execute();

            document = response.parse();
            authorized = authorizationCheck(document);
        } catch (SocketTimeoutException | UnknownHostException | HttpStatusException connException) {
            connected = false;
            logger.w("[AEGEAN.SEF] Warning: " +connException.getMessage(), connException);
            return;
        } catch (IOException e) {
            logger.e("[AEGEAN.SEF] Error: " + e.getMessage(), e);
            return;
        }

        // Return if not authorized
        if (!authorized) {
            this.authorized = false;
            return;
        }

        // Set grades page
        setGradesPage(document);

        // Get info page
        try {
            response = Jsoup.connect("https://sef.samos.aegean.gr/request.php")
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
                    .header("Accept-Encoding", "gzip, deflate, br")
                    .header("Accept-Language", "el-GR,el;q=0.8,en-US;q=0.5,en;q=0.3")
                    .header("Connection", "keep-alive")
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .header("Host", "sef.samos.aegean.gr")
                    .header("Origin", "https://sef.samos.aegean.gr")
                    .header("Referer", "https://sef.samos.aegean.gr/")
                    .header("Upgrade-Insecure-Requests", "1")
                    .header("User-Agent", USER_AGENT)
                    .method(Connection.Method.GET)
                    .cookies(cookies)
                    .execute();
            document = response.parse();
        } catch (SocketTimeoutException | UnknownHostException | HttpStatusException connException) {
            connected = false;
            logger.w("[AEGEAN.SEF] Warning: " +connException.getMessage(), connException);
            return;
        } catch (IOException e) {
            logger.e("[AEGEAN.SEF] Error: " + e.getMessage(), e);
            return;
        }

        setStudentInfoPage(document);
        setCookies(cookies);
    }

    private void getHtmlPages(Map<String, String> cookies) {
        Connection.Response response;
        Document document;

        // Attempt to connect and get grades page
        try {
            response = Jsoup.connect("https://sef.samos.aegean.gr/main.php")
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
                    .header("Accept-Encoding", "gzip, deflate, br")
                    .header("Accept-Language", "el-GR,el;q=0.8,en-US;q=0.5,en;q=0.3")
                    .header("Connection", "keep-alive")
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .header("Host", "sef.samos.aegean.gr")
                    .header("Origin", "https://sef.samos.aegean.gr")
                    .header("Referer", "https://sef.samos.aegean.gr/")
                    .header("Upgrade-Insecure-Requests", "1")
                    .header("User-Agent", USER_AGENT)
                    .method(Connection.Method.GET)
                    .cookies(cookies)
                    .execute();

            document = response.parse();
        } catch (SocketTimeoutException | UnknownHostException | HttpStatusException connException) {
            connected = false;
            logger.w("[AEGEAN.SEF] Warning: " +connException.getMessage(), connException);
            return;
        } catch (IOException e) {
            logger.e("[AEGEAN.SEF] Error: " + e.getMessage(), e);
            return;
        }

        if (!response.url().toString().endsWith("main.php")) return;

        // Set grades page
        setGradesPage(document);

        // Get info page
        try {
            response = Jsoup.connect("https://sef.samos.aegean.gr/request.php")
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
                    .header("Accept-Encoding", "gzip, deflate, br")
                    .header("Accept-Language", "el-GR,el;q=0.8,en-US;q=0.5,en;q=0.3")
                    .header("Connection", "keep-alive")
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .header("Host", "sef.samos.aegean.gr")
                    .header("Origin", "https://sef.samos.aegean.gr")
                    .header("Referer", "https://sef.samos.aegean.gr/")
                    .header("Upgrade-Insecure-Requests", "1")
                    .header("User-Agent", USER_AGENT)
                    .method(Connection.Method.GET)
                    .cookies(cookies)
                    .execute();

            document = response.parse();
        } catch (SocketTimeoutException | UnknownHostException | HttpStatusException connException) {
            connected = false;
            logger.w("[AEGEAN.SEF] Warning: " +connException.getMessage(), connException);
            return;
        } catch (IOException e) {
            logger.e("[AEGEAN.SEF] Error: " + e.getMessage(), e);
            return;
        }

        setStudentInfoPage(document);
    }

    private boolean authorizationCheck(Document document) {
        String html = document.toString();
        return !html.contains("Δεν έχετε δικαίωμα πρόσβασης στο Σ.Ε.Φ. Ελέγξτε τα στοιχεία σας και προσπαθήστε ξανά..");
    }

    public Document getStudentInfoPage() {
        return studentInfoPage;
    }

    private void setStudentInfoPage(Document studentInfoPage) {
        this.studentInfoPage = studentInfoPage;
    }

    public Document getGradesPage() {
        return gradesPage;
    }

    private void setGradesPage(Document gradesPage) {
        this.gradesPage = gradesPage;
    }

    public boolean isAuthorized() {
        return authorized;
    }

    public boolean isConnected() {
        return connected;
    }

    public Map<String, String> getCookies() {
        return cookies;
    }

    public void setCookies(Map<String, String> cookies) {
        this.cookies = cookies;
    }
}
