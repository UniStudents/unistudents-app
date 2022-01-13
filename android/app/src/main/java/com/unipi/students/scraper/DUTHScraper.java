package com.unipi.students.scraper;

import com.datadog.android.log.Logger;
import com.unipi.students.common.StringHelper;
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
import java.util.HashMap;
import java.util.Map;

public class DUTHScraper {
    private final String USER_AGENT;
    private boolean connected;
    private boolean authorized;
    private String infoJSON;
    private String gradesJSON;
    private Map<String, String> cookies;
    private Logger logger = new Logger.Builder()
            .setNetworkInfoEnabled(true)
            .setServiceName("com.unipi.students.scraper.DUTHScraper")
            .setLogcatLogsEnabled(true)
            .setDatadogLogsEnabled(true)
            .setLoggerName("DUTHScraper")
            .build();

    public DUTHScraper(LoginForm loginForm) {
        this.connected = true;
        this.authorized = true;
        this.USER_AGENT = UserAgentGenerator.generate();
        this.getDocuments(loginForm.getUsername(), loginForm.getPassword(), loginForm.getCookies());
    }

    private void getDocuments(String username, String password, Map<String, String> cookies) {
        if (cookies == null) {
            getJSONFiles(username, password);
        } else {
            getJSONFiles(cookies);
            if (infoJSON == null || gradesJSON == null) {
                getJSONFiles(username, password);
            }
        }
    }

    private void getJSONFiles(String username, String password) {
        username = username.trim();
        password = password.trim();
        Connection.Response response;
        final String formURL;
        final String _token;
        final String bearerToken;
        final String state = StringHelper.getRandomHashcode();


        //
        // Get Login Page
        //

        try {
            response = Jsoup.connect("https://oauth.duth.gr/login")
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
                    .header("Accept-Encoding", "gzip, deflate")
                    .header("Accept-Language", "en-US,en;q=0.9,el-GR;q=0.8,el;q=0.7")
                    .header("Connection", "keep-alive")
                    .header("Upgrade-Insecure-Request", "1")
                    .header("User-Agent", USER_AGENT)
                    .method(Connection.Method.GET)
                    .execute();

            Document document = response.parse();
            formURL = document.select("form").attr("action");
            _token = document.getElementsByAttributeValue("name", "_token").attr("value");

            if (formURL == null || formURL.isEmpty()) return;
            if (_token == null || _token.isEmpty()) return;
        } catch (SocketTimeoutException | UnknownHostException | HttpStatusException | ConnectException connException) {
            connected = false;
            logger.w("[DUTH] Warning: " + connException.getMessage(), connException);
            return;
        } catch (IOException e) {
            logger.e("[DUTH] Error: " + e.getMessage(), e);
            return;
        }


        //
        // Try login
        //

        try {
            response = Jsoup.connect(formURL)
                    .data("username", username)
                    .data("password", password)
                    .data("_token", _token)
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
                    .header("Accept-Encoding", "gzip, deflate, br")
                    .header("Accept-Language", "en-US,en;q=0.9")
                    .header("Connection", "keep-alive")
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .header("Origin", "https://oauth.duth.gr")
                    .header("Referer", "https://oauth.duth.gr/login")
                    .header("Sec-Fetch-Dest", "document")
                    .header("Sec-Fetch-Mode", "navigate")
                    .header("Sec-Fetch-Site", "same-origin")
                    .header("User-Agent", USER_AGENT)
                    .method(Connection.Method.POST)
                    .cookies(response.cookies())
                    .execute();

            Document document = response.parse();
            if (document.text().contains("Τα στοιχεία που εισάγατε δεν είναι σωστά")) {
                authorized = false;
                return;
            }
        } catch (SocketTimeoutException | UnknownHostException | HttpStatusException | ConnectException connException) {
            connected = false;
            logger.w("[DUTH] Warning: " + connException.getMessage(), connException);
            return;
        } catch (IOException e) {
            logger.e("[DUTH] Error: " + e.getMessage(), e);
            return;
        }


        //
        // Get Bearer Token
        //

        try {
            response = Jsoup.connect("https://oauth.duth.gr/oauth/authorize?client_id=6&redirect_uri=https%3A%2F%2Fstudents.duth.gr%2F%23%2Fauth%2Fcallback&response_type=token&scope=students&state=" + state)
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
                    .header("Accept-Encoding", "gzip, deflate, br")
                    .header("Accept-Language", "en-US,en;q=0.9")
                    .header("Connection", "keep-alive")
                    .header("Sec-Fetch-Dest", "document")
                    .header("Sec-Fetch-Mode", "navigate")
                    .header("Sec-Fetch-Site", "same-origin")
                    .header("User-Agent", USER_AGENT)
                    .method(Connection.Method.GET)
                    .followRedirects(false)
                    .cookies(response.cookies())
                    .execute();

            String url = response.header("location");
            if (url == null || !url.contains("access_token=") || !url.contains("&token_type")) {
                return;
            }

            bearerToken = url.substring(
                    url.indexOf("access_token=") + "access_token=".length(),
                    url.indexOf("&token_type"));

            if (bearerToken.isEmpty()) return;
        } catch (SocketTimeoutException | UnknownHostException | HttpStatusException | ConnectException connException) {
            connected = false;
            logger.w("[DUTH] Warning: " + connException.getMessage(), connException);
            return;
        } catch (IOException e) {
            logger.e("[DUTH] Error: " + e.getMessage(), e);
            return;
        }


        //
        // Get student's information
        //

        try {
            response = Jsoup.connect("https://api.duth.gr/api/students/me/?$expand=user,department,studyProgram,inscriptionMode,person($expand=gender)&$top=1&$skip=0&$count=false")
                    .header("Accept", "application/json, text/plain, */*")
                    .header("Accept-Encoding", "gzip, deflate, br")
                    .header("Accept-Language", "en-US,en;q=0.9")
                    .header("Authorization", "Bearer " + bearerToken)
                    .header("Connection", "keep-alive")
                    .header("Content-Type", "application/json")
                    .header("Host", "api.duth.gr")
                    .header("Origin", "https://students.duth.gr")
                    .header("Referer", "https://students.duth.gr/")
                    .header("Sec-Fetch-Dest", "empty")
                    .header("Sec-Fetch-Mode", "cors")
                    .header("Sec-Fetch-Site", "same-site")
                    .header("User-Agent", USER_AGENT)
                    .method(Connection.Method.GET)
                    .ignoreContentType(true)
                    .execute();

            Document document = response.parse();
            setInfoJSON(document.text());
        } catch (SocketTimeoutException | UnknownHostException | HttpStatusException | ConnectException connException) {
            connected = false;
            logger.w("[DUTH] Warning: " + connException.getMessage(), connException);
            return;
        } catch (IOException e) {
            logger.e("[DUTH] Error: " + e.getMessage(), e);
            return;
        }


        //
        // Get student's grades
        //

        try {
            response = Jsoup.connect("https://api.duth.gr/api/students/me/courses/?$expand=course($expand=locale),courseType($expand=locale),gradeExam($expand=instructors($expand=instructor($select=id,givenName,familyName,category,locale)))&$orderby=semester%20desc,gradeYear%20desc&$top=-1&$count=false")
                    .header("Accept", "application/json, text/plain, */*")
                    .header("Accept-Encoding", "gzip, deflate, br")
                    .header("Accept-Language", "en-US,en;q=0.9")
                    .header("Authorization", "Bearer " + bearerToken)
                    .header("Connection", "keep-alive")
                    .header("Content-Type", "application/json")
                    .header("Host", "api.duth.gr")
                    .header("Origin", "https://students.duth.gr")
                    .header("Referer", "https://students.duth.gr/")
                    .header("Sec-Fetch-Dest", "empty")
                    .header("Sec-Fetch-Mode", "cors")
                    .header("Sec-Fetch-Site", "same-site")
                    .header("User-Agent", USER_AGENT)
                    .method(Connection.Method.GET)
                    .ignoreContentType(true)
                    .execute();

            Document document = response.parse();
            setGradesJSON(document.text());
        } catch (SocketTimeoutException | UnknownHostException | HttpStatusException | ConnectException connException) {
            connected = false;
            logger.w("[DUTH] Warning: " + connException.getMessage(), connException);
            return;
        } catch (IOException e) {
            logger.e("[DUTH] Error: " + e.getMessage(), e);
            return;
        }

        Map<String, String> cookies = new HashMap<String, String>() {{
            put("bearerToken", bearerToken);
        }};
        setCookies(cookies);
    }

    private void getJSONFiles(Map<String, String> cookies) {
        Connection.Response response;
        String bearerToken = cookies.get("bearerToken");
        if (bearerToken == null || bearerToken.isEmpty()) return;

        //
        // Get student's information
        //

        try {
            response = Jsoup.connect("https://api.duth.gr/api/students/me/?$expand=user,department,studyProgram,inscriptionMode,person($expand=gender)&$top=1&$skip=0&$count=false")
                    .header("Accept", "application/json, text/plain, */*")
                    .header("Accept-Encoding", "gzip, deflate, br")
                    .header("Accept-Language", "en-US,en;q=0.9")
                    .header("Authorization", "Bearer " + bearerToken)
                    .header("Connection", "keep-alive")
                    .header("Content-Type", "application/json")
                    .header("Host", "api.duth.gr")
                    .header("Origin", "https://students.duth.gr")
                    .header("Referer", "https://students.duth.gr/")
                    .header("Sec-Fetch-Dest", "empty")
                    .header("Sec-Fetch-Mode", "cors")
                    .header("Sec-Fetch-Site", "same-site")
                    .header("User-Agent", USER_AGENT)
                    .method(Connection.Method.GET)
                    .ignoreContentType(true)
                    .execute();

            Document document = response.parse();
            setInfoJSON(document.text());
        } catch (SocketTimeoutException | UnknownHostException | HttpStatusException | ConnectException connException) {
            logger.w("[DUTH] Warning: " + connException.getMessage(), connException);
            return;
        } catch (IOException e) {
            logger.e("[DUTH] Error: " + e.getMessage(), e);
            return;
        }


        //
        // Get student's grades
        //

        try {
            response = Jsoup.connect("https://api.duth.gr/api/students/me/courses/?$expand=course($expand=locale),courseType($expand=locale),gradeExam($expand=instructors($expand=instructor($select=id,givenName,familyName,category,locale)))&$orderby=semester%20desc,gradeYear%20desc&$top=-1&$count=false")
                    .header("Accept", "application/json, text/plain, */*")
                    .header("Accept-Encoding", "gzip, deflate, br")
                    .header("Accept-Language", "en-US,en;q=0.9")
                    .header("Authorization", "Bearer " + bearerToken)
                    .header("Connection", "keep-alive")
                    .header("Content-Type", "application/json")
                    .header("Host", "api.duth.gr")
                    .header("Origin", "https://students.duth.gr")
                    .header("Referer", "https://students.duth.gr/")
                    .header("Sec-Fetch-Dest", "empty")
                    .header("Sec-Fetch-Mode", "cors")
                    .header("Sec-Fetch-Site", "same-site")
                    .header("User-Agent", USER_AGENT)
                    .method(Connection.Method.GET)
                    .ignoreContentType(true)
                    .execute();

            Document document = response.parse();
            setGradesJSON(document.text());
        } catch (SocketTimeoutException | UnknownHostException | HttpStatusException | ConnectException connException) {
            logger.w("[DUTH] Warning: " + connException.getMessage(), connException);
            return;
        } catch (IOException e) {
            logger.e("[DUTH] Error: " + e.getMessage(), e);
            return;
        }

        setCookies(cookies);
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

    public String getInfoJSON() {
        return infoJSON;
    }

    public void setInfoJSON(String infoJSON) {
        this.infoJSON = infoJSON;
    }

    public String getGradesJSON() {
        return gradesJSON;
    }

    public void setGradesJSON(String gradesJSON) {
        this.gradesJSON = gradesJSON;
    }

    public Map<String, String> getCookies() {
        return cookies;
    }

    public void setCookies(Map<String, String> cookies) {
        this.cookies = cookies;
    }
}
