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
import java.util.HashMap;
import java.util.Map;

public class TUCScraper {
    private final String USER_AGENT;
    private boolean connected;
    private boolean authorized;
    private String infoJSON;
    private String gradesJSON;
    private Map<String, String> cookies;
    private Logger logger = new Logger.Builder()
            .setNetworkInfoEnabled(true)
            .setServiceName("com.unipi.students.scraper.TUCScraper")
            .setLogcatLogsEnabled(true)
            .setDatadogLogsEnabled(true)
            .setLoggerName("TUCScraper")
            .build();

    public TUCScraper(LoginForm loginForm) {
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
        final String bearerToken;


        //
        // Get Login Page
        //

        try {
            response = Jsoup.connect("https://tuc-oauthsrv.admserv.tuc.gr/auth/realms/Universis/protocol/openid-connect/auth?redirect_uri=https%3A%2F%2Fstudents.tuc.gr%2Fauth%2Fcallback%2Findex.html&response_type=token&client_id=universis-students&scope=students")
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
                    .header("Accept-Encoding", "gzip, deflate")
                    .header("Accept-Language", "en-US,en;q=0.9,el-GR;q=0.8,el;q=0.7")
                    .header("Connection", "keep-alive")
                    .header("Host", "tuc-oauthsrv.admserv.tuc.gr")
                    .header("Referer", "https://students.tuc.gr/")
                    .header("Upgrade-Insecure-Request", "1")
                    .header("User-Agent", USER_AGENT)
                    .method(Connection.Method.GET)
                    .execute();

            Document document = response.parse();
            formURL = document.getElementById("kc-form-login").attr("action");

            if (formURL == null || formURL.isEmpty()) return;
        } catch (SocketTimeoutException | UnknownHostException | HttpStatusException | ConnectException connException) {
            connected = false;
            logger.w("[TUC] Warning: " + connException.getMessage(), connException);
            return;
        } catch (IOException e) {
            logger.e("[TUC] Error: " + e.getMessage(), e);
            return;
        }


        //
        // Try login
        //

        try {
            response = Jsoup.connect(formURL)
                    .data("username", username)
                    .data("password", password)
                    .data("credentialId", "")
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
                    .header("Accept-Encoding", "gzip, deflate, br")
                    .header("Accept-Language", "en-US,en;q=0.9")
                    .header("Connection", "keep-alive")
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .header("Host", "tuc-oauthsrv.admserv.tuc.gr")
                    .header("Origin", "https://tuc-oauthsrv.admserv.tuc.gr")
                    .header("Referer", "https://tuc-oauthsrv.admserv.tuc.gr/auth/realms/Universis/protocol/openid-connect/auth?redirect_uri=https%3A%2F%2Fstudents.tuc.gr%2Fauth%2Fcallback%2Findex.html&response_type=token&client_id=universis-students&scope=students")
                    .header("Sec-Fetch-Dest", "document")
                    .header("Sec-Fetch-Mode", "navigate")
                    .header("Sec-Fetch-Site", "same-origin")
                    .header("User-Agent", USER_AGENT)
                    .method(Connection.Method.POST)
                    .cookies(response.cookies())
                    .followRedirects(false)
                    .execute();

            Document document = response.parse();
            String location = response.header("location");
            if (location == null || location.isEmpty()) {
                if (document.text().contains("Invalid username or password.")) {
                    authorized = false;
                } else {
                    connected = false;
                }
                return;
            }

            bearerToken = location.substring(
                    location.indexOf("access_token=") + "access_token=".length(),
                    location.indexOf("&token_type"));

            if (bearerToken.isEmpty()) return;
        } catch (SocketTimeoutException | UnknownHostException | HttpStatusException | ConnectException connException) {
            connected = false;
            logger.w("[TUC] Warning: " + connException.getMessage(), connException);
            return;
        } catch (IOException e) {
            logger.e("[TUC] Error: " + e.getMessage(), e);
            return;
        }


        //
        // Get student's information
        //

        try {
            response = Jsoup.connect("https://universis-api.tuc.gr/api/students/me/?$expand=user,department,studyProgram,inscriptionMode,person($expand=gender)&$top=1&$skip=0&$count=false")
                    .header("Accept", "application/json, text/plain, */*")
                    .header("Accept-Encoding", "gzip, deflate, br")
                    .header("Accept-Language", "en-US,en;q=0.9")
                    .header("Authorization", "Bearer " + bearerToken)
                    .header("Connection", "keep-alive")
                    .header("Content-Type", "application/json")
                    .header("Host", "universis-api.tuc.gr")
                    .header("Origin", "https://students.tuc.gr")
                    .header("Referer", "https://students.tuc.gr/")
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
            logger.w("[TUC] Warning: " + connException.getMessage(), connException);
            return;
        } catch (IOException e) {
            logger.e("[TUC] Error: " + e.getMessage(), e);
            return;
        }


        //
        // Get student's grades
        //

        try {
            response = Jsoup.connect("https://universis-api.tuc.gr/api/students/me/courses/?$expand=course($expand=locale),courseType($expand=locale),gradeExam($expand=instructors($expand=instructor($select=id,givenName,familyName,category,locale)))&$orderby=semester%20desc,gradeYear%20desc&$top=-1&$count=false")
                    .header("Accept", "application/json, text/plain, */*")
                    .header("Accept-Encoding", "gzip, deflate, br")
                    .header("Accept-Language", "en-US,en;q=0.9")
                    .header("Authorization", "Bearer " + bearerToken)
                    .header("Connection", "keep-alive")
                    .header("Content-Type", "application/json")
                    .header("Host", "universis-api.tuc.gr")
                    .header("Origin", "https://students.tuc.gr")
                    .header("Referer", "https://students.tuc.gr/")
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
            logger.w("[TUC] Warning: " + connException.getMessage(), connException);
            return;
        } catch (IOException e) {
            logger.e("[TUC] Error: " + e.getMessage(), e);
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
            response = Jsoup.connect("https://universis-api.tuc.gr/api/students/me/?$expand=user,department,studyProgram,inscriptionMode,person($expand=gender)&$top=1&$skip=0&$count=false")
                    .header("Accept", "application/json, text/plain, */*")
                    .header("Accept-Encoding", "gzip, deflate, br")
                    .header("Accept-Language", "en-US,en;q=0.9")
                    .header("Authorization", "Bearer " + bearerToken)
                    .header("Connection", "keep-alive")
                    .header("Content-Type", "application/json")
                    .header("Host", "universis-api.tuc.gr")
                    .header("Origin", "https://students.tuc.gr")
                    .header("Referer", "https://students.tuc.gr/")
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
            logger.w("[TUC] Warning: " + connException.getMessage(), connException);
            return;
        } catch (IOException e) {
            logger.e("[TUC] Error: " + e.getMessage(), e);
            return;
        }


        //
        // Get student's grades
        //

        try {
            response = Jsoup.connect("https://universis-api.tuc.gr/api/students/me/courses/?$expand=course($expand=locale),courseType($expand=locale),gradeExam($expand=instructors($expand=instructor($select=id,givenName,familyName,category,locale)))&$orderby=semester%20desc,gradeYear%20desc&$top=-1&$count=false")
                    .header("Accept", "application/json, text/plain, */*")
                    .header("Accept-Encoding", "gzip, deflate, br")
                    .header("Accept-Language", "en-US,en;q=0.9")
                    .header("Authorization", "Bearer " + bearerToken)
                    .header("Connection", "keep-alive")
                    .header("Content-Type", "application/json")
                    .header("Host", "universis-api.tuc.gr")
                    .header("Origin", "https://students.tuc.gr")
                    .header("Referer", "https://students.tuc.gr/")
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
            logger.w("[TUC] Warning: " + connException.getMessage(), connException);
            return;
        } catch (IOException e) {
            logger.e("[TUC] Error: " + e.getMessage(), e);
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
