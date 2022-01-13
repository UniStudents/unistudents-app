package com.unipi.students.scraper;


import com.datadog.android.log.Logger;
import com.unipi.students.common.Services;
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

public class UNIWAYScraper {
    private final String USER_AGENT;
    private boolean connected;
    private boolean authorized;
    private String studentInfoJSON;
    private String gradesJSON;
    private String declareHistoryJSON;
    private Map<String, String> cookies;
    private Logger logger = new Logger.Builder()
            .setNetworkInfoEnabled(true)
            .setServiceName("com.unipi.students.scraper.UNIWAYScraper")
            .setLogcatLogsEnabled(true)
            .setDatadogLogsEnabled(true)
            .setLoggerName("UNIWAYScraper")
            .build();

    public UNIWAYScraper(LoginForm loginForm) {
        this.authorized = true;
        this.connected = true;
        USER_AGENT = UserAgentGenerator.generate();
        this.getDocuments(loginForm.getUsername(), loginForm.getPassword(), loginForm.getCookies());
    }

    private void getDocuments(String username, String password, Map<String, String> cookies) {
        if (cookies == null) {
            getHtmlPages(username, password);
        } else {
            getHtmlPages(cookies);
            if (studentInfoJSON == null || gradesJSON == null || declareHistoryJSON == null) {
                getHtmlPages(username, password);
            }
        }
    }

    private void getHtmlPages(String username, String password) {
        username = username.trim();
        password = password.trim();

        Document loginPage;
        Connection.Response response;

        String lt;
        String execution;
        String _eventId;

        //
        // Get Login Page
        //

        try {
            response = Jsoup.connect("https://sso.uoa.gr/login?service=https%3A%2F%2Funiway.gunet.gr%2Foauth%2Fauth_done%2FMAbmb0RivJ6S9B5NRMiG-Dso%3Fscope%3Duoa%26rt%3Dtoken&renew=true")
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
                    .header("Accept-Encoding", "gzip, deflate, br")
                    .header("Accept-Language", "en-US,en;q=0.9,el-GR;q=0.8,el;q=0.7")
                    .header("Host", "service.uniway.gr")
                    .header("Sec-Fetch-Dest", "document")
                    .header("Sec-Fetch-Mode", "navigate")
                    .header("Sec-Fetch-Site", "none")
                    .header("Sec-Fetch-User", "?1")
                    .header("Upgrade-Insecure-Requests", "1")
                    .header("User-Agent", USER_AGENT)
                    .method(Connection.Method.GET)
                    .execute();

            loginPage = response.parse();
            lt = loginPage.getElementsByAttributeValue("name", "lt").attr("value");
            execution = loginPage.getElementsByAttributeValue("name", "execution").attr("value");
            _eventId = loginPage.getElementsByAttributeValue("name", "_eventId").attr("value");
        } catch (SocketTimeoutException | UnknownHostException | HttpStatusException | ConnectException connException) {
            connected = false;
            logger.w("[UOA.UNIWAY] Warning: " + connException.getMessage(), connException);
            return;
        } catch (IOException e) {
            logger.e("[UOA.UNIWAY] Error: " + e.getMessage(), e);
            return;
        }


        //
        // Try to login
        //

        String url;
        try {
            response = Jsoup.connect("https://sso.uoa.gr/login?service=https%3A%2F%2Funiway.gunet.gr%2Foauth%2Fauth_done%2FMAbmb0RivJ6S9B5NRMiG-Dso%3Fscope%3Duoa%26rt%3Dtoken&renew=true")
                    .data("username", username)
                    .data("password", password)
                    .data("lt", lt)
                    .data("execution", execution)
                    .data("_eventId", _eventId)
                    .data("submitForm", "")
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
                    .header("Accept-Encoding", "gzip, deflate, br")
                    .header("Accept-Language", "en-US,en;q=0.9,el-GR;q=0.8,el;q=0.7")
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .header("Host", "sso.uoa.gr")
                    .header("Origin", "https://sso.uoa.gr")
                    .header("Referer", "https://sso.uoa.gr/login?service=https%3A%2F%2Funiway.gunet.gr%2Foauth%2Fauth_done%2FMAbmb0RivJ6S9B5NRMiG-Dso%3Fscope%3Duoa%26rt%3Dtoken&renew=true")
                    .header("Sec-Fetch-Dest", "document")
                    .header("Sec-Fetch-Mode", "navigate")
                    .header("Sec-Fetch-Site", "same-origin")
                    .header("Sec-Fetch-User", "?1")
                    .header("Upgrade-Insecure-Requests", "1")
                    .header("User-Agent", USER_AGENT)
                    .cookies(response.cookies())
                    .followRedirects(false)
                    .method(Connection.Method.POST)
                    .execute();

            loginPage = response.parse();
            url = response.header("location");
        } catch (SocketTimeoutException | UnknownHostException | HttpStatusException | ConnectException connException) {
            connected = false;
            logger.w("[UOA.UNIWAY] Warning: " + connException.getMessage(), connException);
            return;
        } catch (IOException e) {
            logger.e("[UOA.UNIWAY] Error: " + e.getMessage(), e);
            return;
        }

        // authentication check
        if (loginPage.text().contains("The credentials you provided cannot be determined to be authentic.")) {
            authorized = false;
            return;
        }

        // url check
        if (url == null) return;
        if (url.isEmpty()) return;


        //
        // Redirect to get auth token
        //

        String token;
        try {
            response = Jsoup.connect(url)
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
                    .header("Accept-Encoding", "gzip, deflate, br")
                    .header("Accept-Language", "en-US,en;q=0.9,el-GR;q=0.8,el;q=0.7")
                    .header("Host", "uniway.gunet.gr")
                    .header("Referer", "https://sso.uoa.gr/")
                    .header("Sec-Fetch-Dest", "document")
                    .header("Sec-Fetch-Mode", "navigate")
                    .header("Sec-Fetch-Site", "cross-site")
                    .header("Sec-Fetch-User", "?1")
                    .header("Upgrade-Insecure-Requests", "1")
                    .header("User-Agent", USER_AGENT)
                    .cookies(response.cookies())
                    .followRedirects(false)
                    .method(Connection.Method.GET)
                    .execute();

            token = getBetweenStrings(response.header("location"), "access_token=", "&token_type");
        } catch (SocketTimeoutException | UnknownHostException | HttpStatusException | ConnectException connException) {
            connected = false;
            logger.w("[UOA.UNIWAY] Warning: " + connException.getMessage(), connException);
            return;
        } catch (IOException e) {
            logger.e("[UOA.UNIWAY] Error: " + e.getMessage(), e);
            return;
        }

        // check token
        if (token == null) return;

        //
        //  Get username from API
        //

        String userName = null;
        String usernameJSON = null;
        try {
            response = Jsoup.connect("https://service.uniway.gr/funzy/rest/social-connections/gunet/usernames?accessToken=" + token)
                    .header("User-Agent", USER_AGENT)
                    .ignoreContentType(true)
                    .followRedirects(false)
                    .method(Connection.Method.GET)
                    .execute();

            usernameJSON = response.body();
        } catch (SocketTimeoutException | UnknownHostException | HttpStatusException | ConnectException connException) {
            logger.w("[UOA.UNIWAY] Warning: " + connException.getMessage(), connException);
        } catch (IOException e) {
            logger.e("[UOA.UNIWAY] Error: " + e.getMessage(), e);
            return;
        }

        // check JSON file
        if (usernameJSON != null) {
            if (!usernameJSON.contains("SUCCESS")) {
                userName = registerUser(token);
            } else {
                userName = getBetweenStrings(usernameJSON, "\"userName\":\"", "\"}}");
            }
        } else {
            userName = registerUser(token);
        }

        // username check
        if (userName == null) return;

        //
        //  Funzy Rest Login
        //

        try {
            response = Jsoup.connect("https://service.uniway.gr/funzy/rest/login")
                    .data("username", userName)
                    .data("password", token)
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .header("User-Agent", USER_AGENT)
                    .ignoreContentType(true)
                    .followRedirects(false)
                    .method(Connection.Method.POST)
                    .execute();

        } catch (SocketTimeoutException | UnknownHostException | HttpStatusException | ConnectException connException) {
            connected = false;
            logger.w("[UOA.UNIWAY] Warning: " + connException.getMessage(), connException);
            return;
        } catch (IOException e) {
            logger.e("[UOA.UNIWAY] Error: " + e.getMessage(), e);
            return;
        }

        // set cookies
        Map<String, String> cookies = response.cookies();


        //
        //  Get my-profile
        //

        String infoJSON;
        try {
            response = Jsoup.connect("https://service.uniway.gr/funzy/rest/gunet/uapi/u/my-profile")
                    .header("User-Agent", USER_AGENT)
                    .ignoreContentType(true)
                    .cookies(cookies)
                    .method(Connection.Method.GET)
                    .execute();

            infoJSON = response.body();
        } catch (SocketTimeoutException | UnknownHostException | HttpStatusException | ConnectException connException) {
            connected = false;
            logger.w("[UOA.UNIWAY] Warning: " + connException.getMessage(), connException);
            return;
        } catch (IOException e) {
            logger.e("[UOA.UNIWAY] Error: " + e.getMessage(), e);
            return;
        }

        // check JSON file
        if (!infoJSON.contains("SUCCESS")) return;


        //
        //  Get grades all
        //

        String gradesJSON;
        try {
            response = Jsoup.connect("https://service.uniway.gr/funzy/rest/gunet/uapi/u/e/d/grades/all")
                    .header("User-Agent", USER_AGENT)
                    .ignoreContentType(true)
                    .cookies(cookies)
                    .method(Connection.Method.GET)
                    .execute();

            gradesJSON = response.body();
        } catch (SocketTimeoutException | UnknownHostException | HttpStatusException | ConnectException connException) {
            connected = false;
            logger.w("[UOA.UNIWAY] Warning: " + connException.getMessage(), connException);
            return;
        } catch (IOException e) {
            logger.e("[UOA.UNIWAY] Error: " + e.getMessage(), e);
            return;
        }

        // check JSON file
        if (!gradesJSON.contains("SUCCESS")) return;


        //
        //  Get courses all
        //

        String declareHistoryJSON;
        try {
            response = Jsoup.connect("https://service.uniway.gr/funzy/rest/gunet/uapi/u/e/d/courses")
                    .header("User-Agent", USER_AGENT)
                    .ignoreContentType(true)
                    .cookies(cookies)
                    .method(Connection.Method.GET)
                    .execute();

            declareHistoryJSON = response.body();
        } catch (SocketTimeoutException | UnknownHostException | HttpStatusException | ConnectException connException) {
            connected = false;
            logger.w("[UOA.UNIWAY] Warning: " + connException.getMessage(), connException);
            return;
        } catch (IOException e) {
            logger.e("[UOA.UNIWAY] Error: " + e.getMessage(), e);
            return;
        }

        // check JSON file
        if (!declareHistoryJSON.contains("SUCCESS")) return;

        // set JSON files & cookies
        setStudentInfoJSON(infoJSON);
        setGradesJSON(gradesJSON);
        setDeclareHistoryJSON(declareHistoryJSON);
        setCookies(cookies);
    }

    private void getHtmlPages(Map<String, String> cookies) {
        Connection.Response response;

        //
        //  Get my-profile
        //

        String infoJSON;
        try {
            response = Jsoup.connect("https://service.uniway.gr/funzy/rest/gunet/uapi/u/my-profile")
                    .header("User-Agent", USER_AGENT)
                    .ignoreContentType(true)
                    .cookies(cookies)
                    .method(Connection.Method.GET)
                    .execute();

            infoJSON = response.body();
        } catch (SocketTimeoutException | UnknownHostException | HttpStatusException | ConnectException connException) {
            return;
        } catch (IOException e) {
            logger.e("[UOA.UNIWAY] Error: " + e.getMessage(), e);
            return;
        }

        // check JSON file
        if (!infoJSON.contains("SUCCESS")) return;


        //
        //  Get grades all
        //

        String gradesJSON;
        try {
            response = Jsoup.connect("https://service.uniway.gr/funzy/rest/gunet/uapi/u/e/d/grades/all")
                    .header("User-Agent", USER_AGENT)
                    .ignoreContentType(true)
                    .cookies(cookies)
                    .method(Connection.Method.GET)
                    .execute();

            gradesJSON = response.body();
        } catch (SocketTimeoutException | UnknownHostException | HttpStatusException | ConnectException connException) {
            return;
        } catch (IOException e) {
            logger.e("[UOA.UNIWAY] Error: " + e.getMessage(), e);
            return;
        }

        // check JSON file
        if (!gradesJSON.contains("SUCCESS")) return;


        //
        //  Get courses all
        //

        String declareHistoryJSON;
        try {
            response = Jsoup.connect("https://service.uniway.gr/funzy/rest/gunet/uapi/u/e/d/courses")
                    .header("User-Agent", USER_AGENT)
                    .ignoreContentType(true)
                    .cookies(cookies)
                    .method(Connection.Method.GET)
                    .execute();

            declareHistoryJSON = response.body();
        } catch (SocketTimeoutException | UnknownHostException | HttpStatusException | ConnectException connException) {
            return;
        } catch (IOException e) {
            logger.e("[UOA.UNIWAY] Error: " + e.getMessage(), e);
            return;
        }

        // check JSON file
        if (!declareHistoryJSON.contains("SUCCESS")) return;

        // set JSON files & cookies
        setStudentInfoJSON(infoJSON);
        setGradesJSON(gradesJSON);
        setDeclareHistoryJSON(declareHistoryJSON);
        setCookies(cookies);
    }

    private String registerUser(String token) {
        String userName;
        try {
            String registerResponse = new Services().postRequestWithJSONBody("https://service.uniway.gr/funzy/rest/users", "{\n" +
                    "    \"password\": \"" + token + "\",\n" +
                    "    \"userType\":\"GUNET\"\n" +
                    "}");
            userName = getBetweenStrings(registerResponse, "\"userName\":\"", "\"}}");
        } catch (IOException e) {
            logger.e("[UOA.UNIWAY] Error: " + e.getMessage(), e);
            return null;
        }
        return userName;
    }

    private String getBetweenStrings(String text, String textFrom, String textTo) {
        try {
            String result = "";
            result = text.substring(text.indexOf(textFrom) + textFrom.length(), text.length());
            result = result.substring(0, result.indexOf(textTo));
            return result;
        } catch (Exception e) {
            logger.e("[UOA.UNIWAY] Error: " + e.getMessage() + " | With text: " + text, e);
        }
        return null;
    }

    public boolean isConnected() {
        return connected;
    }

    public boolean isAuthorized() {
        return authorized;
    }

    public String getStudentInfoJSON() {
        return studentInfoJSON;
    }

    private void setStudentInfoJSON(String studentInfoJSON) {
        this.studentInfoJSON = studentInfoJSON;
    }

    public String getGradesJSON() {
        return gradesJSON;
    }

    private void setGradesJSON(String gradesJSON) {
        this.gradesJSON = gradesJSON;
    }

    public String getDeclareHistoryJSON() {
        return declareHistoryJSON;
    }

    private void setDeclareHistoryJSON(String declareHistoryJSON) {
        this.declareHistoryJSON = declareHistoryJSON;
    }

    public Map<String, String> getCookies() {
        return cookies;
    }

    public void setCookies(Map<String, String> cookies) {
        this.cookies = cookies;
    }
}
