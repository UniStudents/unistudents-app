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

public class AUTHScraper {
    private final String USER_AGENT;
    private boolean connected;
    private boolean authorized;
    private String infoJSON;
    private String gradesJSON;
    private Map<String, String> cookies;
    private Logger logger = new Logger.Builder()
            .setNetworkInfoEnabled(true)
            .setServiceName("com.unipi.students.scraper.AUTHScraper")
            .setLogcatLogsEnabled(true)
            .setDatadogLogsEnabled(true)
            .setLoggerName("AUTHScraper")
            .build();

    public AUTHScraper(LoginForm loginForm) {
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
        String formURL;
        final String SAMLRequest;
        final String SAMLResponse;
        String RelayState;
        String AuthState;
        final String bearerToken;
        final String state = StringHelper.getRandomHashcode();
        HashMap<String, String> cookiesObj = new HashMap<>();


        //
        // Get Login Page
        //

        try {
            response = Jsoup.connect("https://oauth.it.auth.gr/auth/realms/master/protocol/openid-connect/auth?redirect_uri=https%3A%2F%2Fstudents.auth.gr%2Fauth%2Fcallback%2Findex.html&response_type=token&client_id=students.auth.gr&scope=students,openid&state=" + state)
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
                    .header("Accept-Encoding", "gzip, deflate")
                    .header("Accept-Language", "en-US,en;q=0.9,el-GR;q=0.8,el;q=0.7")
                    .header("Connection", "keep-alive")
                    .header("Host", "oauth.it.auth.gr")
                    .header("Referer", "https://students.auth.gr/")
                    .header("Upgrade-Insecure-Request", "1")
                    .header("User-Agent", USER_AGENT)
                    .method(Connection.Method.GET)
                    .execute();

            Document document = response.parse();
            formURL = document.select("form").attr("action");
            SAMLRequest = document.getElementsByAttributeValue("name", "SAMLRequest").attr("value");
            RelayState = document.getElementsByAttributeValue("name", "RelayState").attr("value");

            if (formURL == null || formURL.isEmpty()) return;
            if (SAMLRequest == null || SAMLRequest.isEmpty()) return;
            if (RelayState == null || RelayState.isEmpty()) return;
            cookiesObj.putAll(response.cookies());
        } catch (SocketTimeoutException | UnknownHostException | HttpStatusException | ConnectException connException) {
            connected = false;
            logger.w("[AUTH] Warning: " + connException.getMessage(), connException);
            return;
        } catch (IOException e) {
            logger.e("[AUTH] Error: " + e.getMessage(), e);
            return;
        }


        //
        // Get AuthState
        //

        try {
            response = Jsoup.connect(formURL)
                    .data("SAMLRequest", SAMLRequest)
                    .data("RelayState", RelayState)
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
                    .header("Accept-Encoding", "gzip, deflate, br")
                    .header("Accept-Language", "en-US,en;q=0.9")
                    .header("Connection", "keep-alive")
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .header("Host", "login.auth.gr")
                    .header("Origin", "https://oauth.it.auth.gr")
                    .header("Referer", "https://oauth.it.auth.gr/")
                    .header("Sec-Fetch-Dest", "document")
                    .header("Sec-Fetch-Mode", "navigate")
                    .header("Sec-Fetch-Site", "same-origin")
                    .header("User-Agent", USER_AGENT)
                    .method(Connection.Method.POST)
                    .execute();

            formURL = response.url().toString().split("\\?")[0];
            Document document = response.parse();
            AuthState = document.getElementsByAttributeValue("name", "AuthState").attr("value");

            if (AuthState == null || AuthState.isEmpty()) return;
            if (formURL == null || formURL.isEmpty()) return;
        } catch (SocketTimeoutException | UnknownHostException | HttpStatusException | ConnectException connException) {
            connected = false;
            logger.w("[AUTH] Warning: " + connException.getMessage(), connException);
            return;
        } catch (IOException e) {
            logger.e("[AUTH] Error: " + e.getMessage(), e);
            return;
        }


        //
        // Try login
        //

        try {
            response = Jsoup.connect(formURL)
                    .data("username", username)
                    .data("password", password)
                    .data("AuthState", AuthState)
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
                    .header("Accept-Encoding", "gzip, deflate, br")
                    .header("Accept-Language", "en-US,en;q=0.9")
                    .header("Connection", "keep-alive")
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .header("Host", "login.auth.gr")
                    .header("Origin", "https://login.auth.gr")
                    .header("Referer", response.url().toString())
                    .header("Sec-Fetch-Dest", "document")
                    .header("Sec-Fetch-Mode", "navigate")
                    .header("Sec-Fetch-Site", "same-origin")
                    .header("User-Agent", USER_AGENT)
                    .method(Connection.Method.POST)
                    .cookies(response.cookies())
                    .execute();

            Document document = response.parse();
            if (document.text().contains("το όνομα χρήστη ή ο κωδικός πρόσβασης ήταν λάθος")) {
                authorized = false;
                return;
            }

            formURL = document.select("form").attr("action");
            SAMLResponse = document.getElementsByAttributeValue("name", "SAMLResponse").attr("value");
            RelayState = document.getElementsByAttributeValue("name", "RelayState").attr("value");

            if (formURL == null || formURL.isEmpty()) return;
            if (SAMLResponse.isEmpty()) return;
            if (RelayState.isEmpty()) return;
        } catch (SocketTimeoutException | UnknownHostException | HttpStatusException | ConnectException connException) {
            connected = false;
            logger.w("[AUTH] Warning: " + connException.getMessage(), connException);
            return;
        } catch (IOException e) {
            logger.e("[AUTH] Error: " + e.getMessage(), e);
            return;
        }


        //
        // Proceed
        //

        try {
            response = Jsoup.connect(formURL)
                    .data("SAMLResponse", SAMLResponse)
                    .data("RelayState", RelayState)
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
                    .header("Accept-Encoding", "gzip, deflate, br")
                    .header("Accept-Language", "en-US,en;q=0.9")
                    .header("Cache-Control", "max-age=0")
                    .header("Connection", "keep-alive")
//                    .header("Content-Length", "12529")
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .header("Host", "oauth.it.auth.gr")
                    .header("Origin", "https://login.auth.gr")
                    .header("Referer", "https://login.auth.gr/")
                    .header("Sec-Fetch-Dest", "document")
                    .header("Sec-Fetch-Mode", "navigate")
                    .header("Sec-Fetch-Site", "same-site")
                    .header("Sec-Fetch-User", "?1")
                    .header("Upgrade-Insecure-Requests", "1")
                    .header("User-Agent", USER_AGENT)
                    .method(Connection.Method.POST)
                    .cookies(cookiesObj)
                    .ignoreHttpErrors(true)
                    .followRedirects(false)
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
            logger.w("[AUTH] Warning: " + connException.getMessage(), connException);
            return;
        } catch (IOException e) {
            logger.e("[AUTH] Error: " + e.getMessage(), e);
            return;
        }


        //
        // Get student's information
        //

        try {
            response = Jsoup.connect("https://universis-api.it.auth.gr/api/students/me/?$expand=user,department,studyProgram,inscriptionMode,person($expand=gender)&$top=1&$skip=0&$count=false")
                    .header("Accept", "application/json, text/plain, */*")
                    .header("Accept-Encoding", "gzip, deflate, br")
                    .header("Accept-Language", "en-US,en;q=0.9")
                    .header("Authorization", "Bearer " + bearerToken)
                    .header("Connection", "keep-alive")
                    .header("Content-Type", "application/json")
                    .header("Host", "universis-api.it.auth.gr")
                    .header("Origin", "https://students.auth.gr")
                    .header("Referer", "https://students.auth.gr/")
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
            logger.w("[AUTH] Warning: " + connException.getMessage(), connException);
            return;
        } catch (IOException e) {
            logger.e("[AUTH] Error: " + e.getMessage(), e);
            return;
        }


        //
        // Get student's grades
        //

        try {
            response = Jsoup.connect("https://universis-api.it.auth.gr/api/students/me/courses/?$expand=course($expand=locale),courseType($expand=locale),gradeExam($expand=instructors($expand=instructor($select=id,givenName,familyName,category,locale)))&$orderby=semester%20desc,gradeYear%20desc&$top=-1&$count=false")
                    .header("Accept", "application/json, text/plain, */*")
                    .header("Accept-Encoding", "gzip, deflate, br")
                    .header("Accept-Language", "en-US,en;q=0.9")
                    .header("Authorization", "Bearer " + bearerToken)
                    .header("Connection", "keep-alive")
                    .header("Content-Type", "application/json")
                    .header("Host", "universis-api.it.auth.gr")
                    .header("Origin", "https://students.auth.gr")
                    .header("Referer", "https://students.auth.gr/")
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
            logger.w("[AUTH] Warning: " + connException.getMessage(), connException);
            return;
        } catch (IOException e) {
            logger.e("[AUTH] Error: " + e.getMessage(), e);
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
            response = Jsoup.connect("https://universis-api.it.auth.gr/api/students/me/?$expand=user,department,studyProgram,inscriptionMode,person($expand=gender)&$top=1&$skip=0&$count=false")
                    .header("Accept", "application/json, text/plain, */*")
                    .header("Accept-Encoding", "gzip, deflate, br")
                    .header("Accept-Language", "en-US,en;q=0.9")
                    .header("Authorization", "Bearer " + bearerToken)
                    .header("Connection", "keep-alive")
                    .header("Content-Type", "application/json")
                    .header("Host", "universis-api.it.auth.gr")
                    .header("Origin", "https://students.auth.gr")
                    .header("Referer", "https://students.auth.gr/")
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
            logger.w("[AUTH] Warning: " + connException.getMessage(), connException);
            return;
        } catch (IOException e) {
            logger.e("[AUTH] Error: " + e.getMessage(), e);
            return;
        }


        //
        // Get student's grades
        //

        try {
            response = Jsoup.connect("https://universis-api.it.auth.gr/api/students/me/courses/?$expand=course($expand=locale),courseType($expand=locale),gradeExam($expand=instructors($expand=instructor($select=id,givenName,familyName,category,locale)))&$orderby=semester%20desc,gradeYear%20desc&$top=-1&$count=false")
                    .header("Accept", "application/json, text/plain, */*")
                    .header("Accept-Encoding", "gzip, deflate, br")
                    .header("Accept-Language", "en-US,en;q=0.9")
                    .header("Authorization", "Bearer " + bearerToken)
                    .header("Connection", "keep-alive")
                    .header("Content-Type", "application/json")
                    .header("Host", "universis-api.it.auth.gr")
                    .header("Origin", "https://students.auth.gr")
                    .header("Referer", "https://students.auth.gr/")
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
            logger.w("[AUTH] Warning: " + connException.getMessage(), connException);
            return;
        } catch (IOException e) {
            logger.e("[AUTH] Error: " + e.getMessage(), e);
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
