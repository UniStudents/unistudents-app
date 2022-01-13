package com.unipi.students.scraper;

import com.datadog.android.log.Logger;
import com.unipi.students.common.CommonsFactory;
import com.unipi.students.common.UserAgentGenerator;
import com.unipi.students.model.LoginForm;

import org.jsoup.Connection;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Map;

import javax.net.ssl.SSLSocketFactory;

public class AUAScraper {
    private final String USER_AGENT;
    private boolean connected;
    private boolean authorized;
    private Document studentInfoPage;
    private Document gradesPage;
    private Map<String, String> cookies;
    private static SSLSocketFactory sslSocketFactory;
    private Logger logger = new Logger.Builder()
            .setNetworkInfoEnabled(true)
            .setServiceName("com.unipi.students.scraper.AUAScraper")
            .setLogcatLogsEnabled(true)
            .setDatadogLogsEnabled(true)
            .setLoggerName("AUAScraper")
            .build();

    public AUAScraper(LoginForm loginForm) {
        this.connected = true;
        this.authorized = true;
        sslSocketFactory = CommonsFactory.sslSocketFactory;
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
            if (studentInfoPage == null || gradesPage == null) {
                System.out.println("======> DEBUG: Normal request after cookies request");
                getHtmlPages(username, password);
            }
        }
    }

    private void getHtmlPages(String username, String password) {
        username = username.trim();
        password = password.trim();

        Connection.Response response;

        // First response to get the wanted cookie
        Map<String, String> cookies;
        try {
            response = Jsoup.connect("https://estudent.aua.gr:8443/estudent/")
                    .method(Connection.Method.GET)
                    .header("User-Agent", USER_AGENT)
                    .timeout(60 * 1000)
                    .sslSocketFactory(sslSocketFactory)
                    .execute();

            cookies = response.cookies();
        } catch (SocketTimeoutException | UnknownHostException | HttpStatusException | ConnectException connException) {
            connected = false;
            logger.w("[AUA.CUSTOM] Warning: " +  connException.getMessage(), connException);
            return;
        } catch (IOException e) {
            logger.e("[AUA.CUSTOM] Error: " +  e.getMessage(), e);
            return;
        }

        // Attempt to login
        try {
            response = Jsoup.connect("https://estudent.aua.gr:8443/estudent/login")
                    .data("username", username)
                    .data("password", password)
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
                    .header("Accept-Encoding", "gzip, deflate, br")
                    .header("Accept-Language", "el-GR,el;q=0.8,en-US;q=0.5,en;q=0.3")
                    .header("Connection", "keep-alive")
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .header("Host", "estudent.aua.gr:8443")
                    .header("Origin", "https://estudent.aua.gr:8443")
                    .header("Referer", "https://estudent.aua.gr:8443/estudent/logoff.action")
                    .header("Upgrade-Insecure-Requests", "1")
                    .header("User-Agent", USER_AGENT)
                    .cookies(cookies)
                    .method(Connection.Method.POST)
                    .timeout(60 * 1000)
                    .sslSocketFactory(sslSocketFactory)
                    .execute();

            // Update cookie
            cookies = response.cookies();

        } catch (SocketTimeoutException | UnknownHostException | HttpStatusException | ConnectException connException) {
            connected = false;
            logger.w("[AUA.CUSTOM] Warning: " +  connException.getMessage(), connException);
            return;
        } catch (IOException e) {
            logger.e("[AUA.CUSTOM] Error: " +  e.getMessage(), e);
            return;
        }

        // Check if user is authorized
        try {
            authorized = authorizationCheck(response.parse());
        } catch (IOException e) {
            logger.e("[AUA.CUSTOM] Error: " +  e.getMessage(), e);
        }

        if (!authorized) {
            return;
        }

        // Student info
        try {
            response = Jsoup.connect("https://estudent.aua.gr:8443/estudent/stud_studentOverview.action")
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
                    .header("Accept-Encoding", "gzip, deflate, br")
                    .header("Accept-Language", "el-GR,el;q=0.8,en-US;q=0.5,en;q=0.3")
                    .header("Connection", "keep-alive")
                    .header("Host", "estudent.aua.gr:8443")
                    .header("Origin", "https://estudent.aua.gr:8443")
                    .header("Referer", "https://estudent.aua.gr:8443/estudent/welcome.action")
                    .header("Upgrade-Insecure-Requests", "1")
                    .header("User-Agent", USER_AGENT)
                    .cookies(cookies)
                    .method(Connection.Method.GET)
                    .timeout(60 * 1000)
                    .sslSocketFactory(sslSocketFactory)
                    .execute();
        } catch (SocketTimeoutException | UnknownHostException | HttpStatusException | ConnectException connException) {
            connected = false;
            logger.w("[AUA.CUSTOM] Warning: " +  connException.getMessage(), connException);
            return;
        } catch (IOException e) {
            logger.e("[AUA.CUSTOM] Error: " +  e.getMessage(), e);
            return;
        }

        try {
            setStudentInfoPage(response.parse());
        } catch (IOException e) {
            logger.e("[AUA.CUSTOM] Error: " +  e.getMessage(), e);
        }

        // Student grades
        try {
            response = Jsoup.connect("https://estudent.aua.gr:8443/estudent/stud_showStudentGrades.action")
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
                    .header("Accept-Encoding", "gzip, deflate, br")
                    .header("Accept-Language", "el-GR,el;q=0.8,en-US;q=0.5,en;q=0.3")
                    .header("Connection", "keep-alive")
                    .header("Host", "estudent.aua.gr:8443")
                    .header("Origin", "https://estudent.aua.gr:8443")
                    .header("Referer", "https://estudent.aua.gr:8443/estudent/stud_studentOverview.action")
                    .header("Upgrade-Insecure-Requests", "1")
                    .header("User-Agent", USER_AGENT)
                    .cookies(cookies)
                    .method(Connection.Method.GET)
                    .timeout(60 * 1000)
                    .sslSocketFactory(sslSocketFactory)
                    .execute();
        } catch (SocketTimeoutException | UnknownHostException | HttpStatusException | ConnectException connException) {
            connected = false;
            logger.w("[AUA.CUSTOM] Warning: " +  connException.getMessage(), connException);
            return;
        } catch (IOException e) {
            logger.e("[AUA.CUSTOM] Error: " +  e.getMessage(), e);
            return;
        }

        try {
            setGradesPage(response.parse());
            setCookies(cookies);
        } catch (IOException e) {
            logger.e("[AUA.CUSTOM] Error: " +  e.getMessage(), e);
        }
    }

    private void getHtmlPages(Map<String, String> cookies) {
        Connection.Response response;

        // Student info
        try {
            response = Jsoup.connect("https://estudent.aua.gr:8443/estudent/stud_studentOverview.action")
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
                    .header("Accept-Encoding", "gzip, deflate, br")
                    .header("Accept-Language", "el-GR,el;q=0.8,en-US;q=0.5,en;q=0.3")
                    .header("Connection", "keep-alive")
                    .header("Host", "estudent.aua.gr:8443")
                    .header("Origin", "https://estudent.aua.gr:8443")
                    .header("Referer", "https://estudent.aua.gr:8443/estudent/welcome.action")
                    .header("Upgrade-Insecure-Requests", "1")
                    .header("User-Agent", USER_AGENT)
                    .cookies(cookies)
                    .followRedirects(false)
                    .method(Connection.Method.GET)
                    .timeout(60 * 1000)
                    .sslSocketFactory(sslSocketFactory)
                    .execute();
        } catch (SocketTimeoutException | UnknownHostException | HttpStatusException | ConnectException connException) {
            connected = false;
            logger.w("[AUA.CUSTOM] Warning: " +  connException.getMessage(), connException);
            return;
        } catch (IOException e) {
            logger.e("[AUA.CUSTOM] Error: " +  e.getMessage(), e);
            return;
        }

        // check if user is authorized
        if (response.statusCode() != 200) return;

        try {
            setStudentInfoPage(response.parse());
        } catch (IOException e) {
            logger.e("[AUA.CUSTOM] Error: " +  e.getMessage(), e);
        }

        // Student grades
        try {
            response = Jsoup.connect("https://estudent.aua.gr:8443/estudent/stud_showStudentGrades.action")
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
                    .header("Accept-Encoding", "gzip, deflate, br")
                    .header("Accept-Language", "el-GR,el;q=0.8,en-US;q=0.5,en;q=0.3")
                    .header("Connection", "keep-alive")
                    .header("Host", "estudent.aua.gr:8443")
                    .header("Origin", "https://estudent.aua.gr:8443")
                    .header("Referer", "https://estudent.aua.gr:8443/estudent/stud_studentOverview.action")
                    .header("Upgrade-Insecure-Requests", "1")
                    .header("User-Agent", USER_AGENT)
                    .cookies(cookies)
                    .method(Connection.Method.GET)
                    .timeout(60 * 1000)
                    .sslSocketFactory(sslSocketFactory)
                    .execute();
        } catch (SocketTimeoutException | UnknownHostException | HttpStatusException | ConnectException connException) {
            connected = false;
            logger.w("[AUA.CUSTOM] Warning: " +  connException.getMessage(), connException);
            return;
        } catch (IOException e) {
            logger.e("[AUA.CUSTOM] Error: " +  e.getMessage(), e);
            return;
        }

        try {
            setGradesPage(response.parse());
            setCookies(cookies);
        } catch (IOException e) {
            logger.e("[AUA.CUSTOM] Error: " +  e.getMessage(), e);
        }
    }

    public boolean isConnected() {
        return connected;
    }

    public boolean isAuthorized() {
        return authorized;
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

    public Map<String, String> getCookies() {
        return cookies;
    }

    public void setCookies(Map<String, String> cookies) {
        this.cookies = cookies;
    }

    private boolean authorizationCheck(Document document) {
        String html = document.toString();

        return !(html.contains("Η προσπάθεια σύνδεσης σας απέτυχε. Προσπαθήστε ξανά.") || html.contains("Λόγος: Bad credentials."));
    }
}
