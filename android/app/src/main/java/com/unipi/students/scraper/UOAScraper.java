package com.unipi.students.scraper;

import com.datadog.android.log.Logger;
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

public class UOAScraper {
    private final String USER_AGENT;
    private boolean connected;
    private boolean authorized;
    private Document studentInfoPage;
    private Document gradesPage;
    private Document declareHistoryPage;
    private Map<String, String> cookies;
    private Logger logger = new Logger.Builder()
            .setNetworkInfoEnabled(true)
            .setServiceName("com.unipi.students.scraper.UOAScraper")
            .setLogcatLogsEnabled(true)
            .setDatadogLogsEnabled(true)
            .setLoggerName("UOAScraper")
            .build();

    public UOAScraper(LoginForm loginForm) {
        this.authorized = true;
        this.connected = true;
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

        //
        // Get Login Page
        //

        try {
            response = Jsoup.connect("https://my-studies.uoa.gr/Secr3w/connect.aspx")
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
                    .header("Accept-Encoding", "gzip, deflate, br")
                    .header("Accept-Language", "en-US,en;q=0.9,el-GR;q=0.8,el;q=0.7")
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
            logger.w("Warning: " + connException.getMessage(), connException);
            return;
        } catch (IOException e) {
            logger.e("Error: " + e.getMessage(), e);
            return;
        }

        Document loginPage;
        String ecv = "";
        try {
            loginPage = response.parse();
            ecv = loginPage.getElementsByAttributeValue("name", "ecv").attr("value");
            if (ecv.isEmpty()) return;
        } catch (IOException e) {
            logger.e("Error: " + e.getMessage(), e);
            return;
        }

        // set session cookies
        Map<String, String> cookies = response.cookies();

        //
        // Try to Login
        //

        try {
            response = Jsoup.connect("https://my-studies.uoa.gr/Secr3w/connect.aspx")
                    .data("username", username)
                    .data("password", password)
                    .data("connect", "Σύνδεση")
                    .data("casheusage", "nocashe")
                    .data("ecv", ecv)
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
                    .header("Accept-Encoding", "gzip, deflate, br")
                    .header("Accept-Language", "en-US,en;q=0.9,el-GR;q=0.8,el;q=0.7")
                    .header("Cache-Control", "max-age=0")
                    .header("Connection", "keep-alive")
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .header("Origin", "https://my-studies.uoa.gr")
                    .header("Referer", "https://my-studies.uoa.gr/Secr3w/connect.aspx")
                    .header("Sec-Fetch-Dest", "document")
                    .header("Sec-Fetch-Mode", "navigate")
                    .header("Sec-Fetch-Site", "same-origin")
                    .header("Sec-Fetch-User", "?1")
                    .header("Upgrade-Insecure-Requests", "1")
                    .header("User-Agent", USER_AGENT)
                    .followRedirects(false)
                    .cookies(cookies)
                    .method(Connection.Method.POST)
                    .timeout(60 * 1000)
                    .execute();
        } catch (SocketTimeoutException | UnknownHostException | HttpStatusException | ConnectException connException) {
            connected = false;
            logger.w("Warning: " + connException.getMessage(), connException);
            return;
        } catch (IOException e) {
            logger.e("Error: " + e.getMessage(), e);
            return;
        }

        // authorization check
        if (!isAuthorized(response)) {
            return;
        }

        //
        // Fetch general info
        //

        try {
            response = Jsoup.connect("https://my-studies.uoa.gr/Secr3w/app/userprofile/generalInfo.aspx")
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
                    .header("Accept-Encoding", "gzip, deflate, br")
                    .header("Accept-Language", "en-US,en;q=0.9,el-GR;q=0.8,el;q=0.7")
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .header("Referer", "https://my-studies.uoa.gr/Secr3w/app/headings.aspx")
                    .header("Sec-Fetch-Dest", "frame")
                    .header("Sec-Fetch-Mode", "navigate")
                    .header("Sec-Fetch-Site", "same-origin")
                    .header("Sec-Fetch-User", "?1")
                    .header("Upgrade-Insecure-Requests", "1")
                    .header("User-Agent", USER_AGENT)
                    .cookies(cookies)
                    .method(Connection.Method.GET)
                    .timeout(60 * 1000)
                    .execute();
        } catch (SocketTimeoutException | UnknownHostException | HttpStatusException | ConnectException connException) {
            connected = false;
            logger.w("Warning: " + connException.getMessage(), connException);
            return;
        } catch (IOException e) {
            logger.e("Error: " + e.getMessage(), e);
            return;
        }

        try {
            Document infoPage = response.parse();
            setStudentInfoPage(infoPage);
        } catch (IOException e) {
            logger.e("Error: " + e.getMessage(), e);
            return;
        }

        //
        // Redirect to /Secr3w/app/
        //

        try {
            response = Jsoup.connect("https://my-studies.uoa.gr/Secr3w/app/accHistory/accadFooter.aspx?")
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
                    .header("Accept-Encoding", "gzip, deflate, br")
                    .header("Accept-Language", "en-US,en;q=0.9,el-GR;q=0.8,el;q=0.7")
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .header("Referer", "https://my-studies.uoa.gr/Secr3w/app/accHistory/default.aspx")
                    .header("Sec-Fetch-Dest", "frame")
                    .header("Sec-Fetch-Mode", "navigate")
                    .header("Sec-Fetch-Site", "same-origin")
                    .header("Sec-Fetch-User", "?1")
                    .header("Upgrade-Insecure-Requests", "1")
                    .header("User-Agent", USER_AGENT)
                    .cookies(cookies)
                    .method(Connection.Method.GET)
                    .timeout(60 * 1000)
                    .execute();
        } catch (SocketTimeoutException | UnknownHostException | HttpStatusException | ConnectException connException) {
            connected = false;
            logger.w("Warning: " + connException.getMessage(), connException);
            return;
        } catch (IOException e) {
            logger.e("Error: " + e.getMessage(), e);
            return;
        }

        try {
            Document gradesPage = response.parse();
            setGradesPage(gradesPage);
        } catch (IOException e) {
            logger.e("Error: " + e.getMessage(), e);
            return;
        }

        //
        // Redirect to /Secr3w/app/declareHistory/declareFooter.aspx?
        //

        try {
            response = Jsoup.connect("https://my-studies.uoa.gr/Secr3w/app/declareHistory/declareFooter.aspx?")
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
                    .header("Accept-Encoding", "gzip, deflate, br")
                    .header("Accept-Language", "en-US,en;q=0.9,el-GR;q=0.8,el;q=0.7")
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .header("Referer", "https://my-studies.uoa.gr/Secr3w/app/accHistory/default.aspx")
                    .header("Sec-Fetch-Dest", "frame")
                    .header("Sec-Fetch-Mode", "navigate")
                    .header("Sec-Fetch-Site", "same-origin")
                    .header("Upgrade-Insecure-Requests", "1")
                    .header("User-Agent", USER_AGENT)
                    .cookies(cookies)
                    .method(Connection.Method.GET)
                    .timeout(60 * 1000)
                    .execute();
        } catch (SocketTimeoutException | UnknownHostException | HttpStatusException | ConnectException connException) {
            connected = false;
            logger.w("Warning: " + connException.getMessage(), connException);
            return;
        } catch (IOException e) {
            logger.e("Error: " + e.getMessage(), e);
            return;
        }

        try {
            Document declareHistoryPage = response.parse();
            setDeclareHistoryPage(declareHistoryPage);
            setCookies(cookies);
        } catch (IOException e) {
            logger.e("Error: " + e.getMessage(), e);
        }
    }

    private void getHtmlPages(Map<String, String> cookies) {
        Connection.Response response;

        //
        // Fetch general info
        //

        try {
            response = Jsoup.connect("https://my-studies.uoa.gr/Secr3w/app/userprofile/generalInfo.aspx")
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
                    .header("Accept-Encoding", "gzip, deflate, br")
                    .header("Accept-Language", "en-US,en;q=0.9,el-GR;q=0.8,el;q=0.7")
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .header("Referer", "https://my-studies.uoa.gr/Secr3w/app/headings.aspx")
                    .header("Sec-Fetch-Dest", "frame")
                    .header("Sec-Fetch-Mode", "navigate")
                    .header("Sec-Fetch-Site", "same-origin")
                    .header("Sec-Fetch-User", "?1")
                    .header("Upgrade-Insecure-Requests", "1")
                    .header("User-Agent", USER_AGENT)
                    .cookies(cookies)
                    .followRedirects(false)
                    .method(Connection.Method.GET)
                    .timeout(60 * 1000)
                    .execute();
        } catch (SocketTimeoutException | UnknownHostException | HttpStatusException | ConnectException connException) {
            connected = false;
            logger.w("Warning: " + connException.getMessage(), connException);
            return;
        } catch (IOException e) {
            logger.e("Error: " + e.getMessage(), e);
            return;
        }

        try {
            Document infoPage = response.parse();

            // check if user is authorized
            if (!isCookieValid(infoPage)) return;

            setStudentInfoPage(infoPage);
        } catch (IOException e) {
            logger.e("Error: " + e.getMessage(), e);
            return;
        }

        //
        // Redirect to /Secr3w/app/
        //

        try {
            response = Jsoup.connect("https://my-studies.uoa.gr/Secr3w/app/accHistory/accadFooter.aspx?")
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
                    .header("Accept-Encoding", "gzip, deflate, br")
                    .header("Accept-Language", "en-US,en;q=0.9,el-GR;q=0.8,el;q=0.7")
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .header("Referer", "https://my-studies.uoa.gr/Secr3w/app/accHistory/default.aspx")
                    .header("Sec-Fetch-Dest", "frame")
                    .header("Sec-Fetch-Mode", "navigate")
                    .header("Sec-Fetch-Site", "same-origin")
                    .header("Sec-Fetch-User", "?1")
                    .header("Upgrade-Insecure-Requests", "1")
                    .header("User-Agent", USER_AGENT)
                    .cookies(cookies)
                    .method(Connection.Method.GET)
                    .timeout(60 * 1000)
                    .execute();
        } catch (SocketTimeoutException | UnknownHostException | HttpStatusException | ConnectException connException) {
            connected = false;
            logger.w("Warning: " + connException.getMessage(), connException);
            return;
        } catch (IOException e) {
            logger.e("Error: " + e.getMessage(), e);
            return;
        }

        try {
            Document gradesPage = response.parse();
            setGradesPage(gradesPage);
        } catch (IOException e) {
            logger.e("Error: " + e.getMessage(), e);
            return;
        }

        //
        // Redirect to /Secr3w/app/declareHistory/declareFooter.aspx?
        //

        try {
            response = Jsoup.connect("https://my-studies.uoa.gr/Secr3w/app/declareHistory/declareFooter.aspx?")
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
                    .header("Accept-Encoding", "gzip, deflate, br")
                    .header("Accept-Language", "en-US,en;q=0.9,el-GR;q=0.8,el;q=0.7")
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .header("Referer", "https://my-studies.uoa.gr/Secr3w/app/accHistory/default.aspx")
                    .header("Sec-Fetch-Dest", "frame")
                    .header("Sec-Fetch-Mode", "navigate")
                    .header("Sec-Fetch-Site", "same-origin")
                    .header("Upgrade-Insecure-Requests", "1")
                    .header("User-Agent", USER_AGENT)
                    .cookies(cookies)
                    .method(Connection.Method.GET)
                    .timeout(60 * 1000)
                    .execute();
        } catch (SocketTimeoutException | UnknownHostException | HttpStatusException | ConnectException connException) {
            connected = false;
            logger.w("Warning: " + connException.getMessage(), connException);
            return;
        } catch (IOException e) {
            logger.e("Error: " + e.getMessage(), e);
            return;
        }

        try {
            Document declareHistoryPage = response.parse();
            setDeclareHistoryPage(declareHistoryPage);
            setCookies(cookies);
        } catch (IOException e) {
            logger.e("Error: " + e.getMessage(), e);
        }
    }

    private boolean isCookieValid(Document document) {
        String html = document.toString();
        return !html.contains("<body></body>");
    }

    private boolean isAuthorized(Connection.Response response) {
        try {
            String html = response.parse().toString();
            if (html.contains(" Ο λογαριασμός πρόσβασης δεν υπάρχει ή λάθος κωδικός.") ||
                    html.contains("Αποτυχία Σύνδεσης:")) {
                this.authorized = false;
                return false;
            }
            this.authorized = true;
            return true;
        } catch (IOException e) {
            logger.e("isAuthorized threw error: " + e.getMessage());
            return false;
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

    public Document getDeclareHistoryPage() {
        return declareHistoryPage;
    }

    private void setDeclareHistoryPage(Document declareHistoryPage) {
        this.declareHistoryPage = declareHistoryPage;
    }

    public Map<String, String> getCookies() {
        return cookies;
    }

    public void setCookies(Map<String, String> cookies) {
        this.cookies = cookies;
    }
}