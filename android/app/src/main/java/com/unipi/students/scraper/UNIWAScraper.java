package com.unipi.students.scraper;

import com.datadog.android.log.Logger;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.unipi.students.common.UserAgentGenerator;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.*;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

public class UNIWAScraper {

    private String username;
    private String password;
    private boolean connected;
    private boolean authorized;
    private String infoJSON;
    private String gradesJSON;
    private String totalAverageGrade;
    private Logger logger = new Logger.Builder()
            .setNetworkInfoEnabled(true)
            .setServiceName("com.unipi.students.scraper.UNIWAScraper")
            .setLogcatLogsEnabled(true)
            .setDatadogLogsEnabled(true)
            .setLoggerName("UNIWAScraper")
            .build();

    public UNIWAScraper(String username, String password) {
        this.username = username.trim().replace(" ", "");
        this.password = password.trim().replace(" ", "");
        this.connected = true;
        this.authorized = true;
        this.getHtmlPages();
    }

    private void getHtmlPages() {

        //
        // Request Login Html Page
        //

        Connection.Response response;
        Document doc;
        String lt;
        String execution;

        String userAgent = UserAgentGenerator.generate();

        try {
            response = getResponse(userAgent);
            if (response == null) return;
            doc = response.parse();
            Elements el = doc.getElementsByAttributeValue("name", "lt");
            lt = el.first().attributes().get("value");
            Elements exec = doc.getElementsByAttributeValue("name", "execution");
            execution = exec.first().attributes().get("value");
        } catch (IOException e) {
            logger.e("Parsing Login Html Page threw error: " + e.getMessage(), e);
            return;
        }

        // store session cookies
        Map<String, String> sessionCookies = new HashMap<>();
        sessionCookies.putAll(response.cookies());

        //
        // Try to Login
        //

        try {
            response = Jsoup.connect("https://sso.uniwa.gr/login?service=https%3A%2F%2Fservices.uniwa.gr%2Flogin%2Fcas")
                    .data("username", this.username)
                    .data("password", this.password)
                    .data("lt", lt)
                    .data("execution", execution)
                    .data("_eventId", "submit")
                    .data("submitForm", "Login")
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
                    .header("Accept-Encoding", "gzip, deflate, br")
                    .header("Accept-Language", "en-US,en;q=0.9,el-GR;q=0.8,el;q=0.7")
                    .header("Cache-Control", "max-age=0")
                    .header("Connection", "keep-alive")
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .header("Host", "sso.uniwa.gr")
                    .header("Origin", "https://sso.uniwa.gr")
                    .header("Referer", "https://sso.uniwa.gr/login?service=https%3A%2F%2Fservices.uniwa.gr%2Flogin%2Fcas")
                    .header("Sec-Fetch-Dest", "document")
                    .header("Sec-Fetch-Mode", "navigate")
                    .header("Sec-Fetch-Site", "same-origin")
                    .header("Sec-Fetch-User", "?1")
                    .header("Upgrade-Insecure-Requests", "1")
                    .header("User-Agent", userAgent)
                    .cookies(sessionCookies)
                    .followRedirects(false)
                    .method(Connection.Method.POST)
                    .timeout(60 * 1000)
                    .execute();
        } catch (SocketTimeoutException | UnknownHostException connException) {
            connected = false;
            logger.e("Try to Login threw error: " + connException.getMessage(), connException);
            return;
        } catch (IOException e) {
            logger.e("Try to Login threw error: " + e.getMessage(), e);
            return;
        }

        // authentication check
        if (!isAuthorized(response)) {
            return;
        }

        //
        // Redirect to /login/cas?ticket="..."
        //

        String location = response.header("location");
        try {
            response = Jsoup.connect(location)
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
                    .header("Accept-Encoding", "gzip, deflate, br")
                    .header("Accept-Language", "en-US,en;q=0.9,el-GR;q=0.8,el;q=0.7")
                    .header("Cache-Control", "max-age=0")
                    .header("Connection", "keep-alive")
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .header("Host", "services.uniwa.gr")
                    .header("Referer", "https://sso.uniwa.gr/login?service=https%3A%2F%2Fservices.uniwa.gr%2Flogin%2Fcas")
                    .header("Sec-Fetch-Dest", "document")
                    .header("Sec-Fetch-Mode", "navigate")
                    .header("Sec-Fetch-Site", "same-origin")
                    .header("Sec-Fetch-User", "?1")
                    .header("Upgrade-Insecure-Requests", "1")
                    .header("User-Agent", userAgent)
                    .followRedirects(false)
                    .method(Connection.Method.GET)
                    .timeout(60 * 1000)
                    .execute();
        } catch (SocketTimeoutException | UnknownHostException connException) {
            connected = false;
            logger.e("Redirect to /login/cas?ticket threw error: " + connException.getMessage(), connException);
            return;
        } catch (IOException e) {
            logger.e("Redirect to /login/cas?ticket threw error: " + e.getMessage(), e);
            return;
        }

        // store session cookies
        StringBuilder cookie = new StringBuilder();
        for (Map.Entry<String, String> entry: response.cookies().entrySet()) {
            cookie.append(entry.getKey()).append("=").append(entry.getValue());
        }

        Map<String, String> cookiesSession = response.cookies();

        //
        // Redirect to services.uniwa.gr to get X-CSRF-TOKEN
        //

        Document pageIncludesToken;
        location = response.header("location");
        try {
            response = Jsoup.connect(location)
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
                    .header("Accept-Encoding", "gzip, deflate, br")
                    .header("Accept-Language", "en-US,en;q=0.9,el-GR;q=0.8,el;q=0.7")
                    .header("Cache-Control", "max-age=0")
                    .header("Connection", "keep-alive")
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .header("Host", "services.uniwa.gr")
                    .header("Referer", "https://sso.uniwa.gr/login?service=https%3A%2F%2Fservices.uniwa.gr%2Flogin%2Fcas")
                    .header("Sec-Fetch-Dest", "document")
                    .header("Sec-Fetch-Mode", "navigate")
                    .header("Sec-Fetch-Site", "same-origin")
                    .header("Sec-Fetch-User", "?1")
                    .header("Upgrade-Insecure-Requests", "1")
                    .header("User-Agent", userAgent)
                    .cookies(cookiesSession)
                    .followRedirects(false)
                    .method(Connection.Method.GET)
                    .timeout(60 * 1000)
                    .execute();
            pageIncludesToken = response.parse();
        } catch (SocketTimeoutException | UnknownHostException connException) {
            connected = false;
            logger.e("Redirect to services.uniwa.gr threw error: " + connException.getMessage(), connException);
            return;
        } catch (IOException e) {
            logger.e("Redirect to services.uniwa.gr threw error: " + e.getMessage(), e);
            return;
        }

        Elements el;
        String _csrf;
        if (pageIncludesToken != null) {
            el = pageIncludesToken.getElementsByAttributeValue("name", "_csrf");
            _csrf = el.first().attributes().get("content");
        } else {
            return;
        }

        //
        //  Http GET request to api endpoint
        //  using cookie form previous request
        //

        infoJSON = httpGET("https://services.uniwa.gr/api/person/profiles", cookie.toString(), userAgent);
        if (infoJSON == null) return;

        // get X-Profile variable from infoJSON
        String xProfile = getXProfile(infoJSON);
        gradesJSON = httpGET("https://services.uniwa.gr/feign/student/grades/diploma", cookie.toString(), _csrf, xProfile, userAgent);
        if (gradesJSON == null) return;

        totalAverageGrade = httpGET("https://services.uniwa.gr/feign/student/grades/average_student_course_grades", cookie.toString(), _csrf, xProfile, userAgent);
    }

    private Connection.Response getResponse(String userAgent) {
        try {
            return Jsoup.connect("https://sso.uniwa.gr/login?service=https%3A%2F%2Fservices.uniwa.gr%2Flogin%2Fcas")
                    .method(Connection.Method.GET)
                    .header("User-Agent", userAgent)
                    .timeout(60 * 1000)
                    .execute();
        } catch (SocketTimeoutException | UnknownHostException connException) {
            connected = false;
            logger.e("Request Login Html Page threw error: " + connException.getMessage(), connException);
        } catch (IOException e) {
            logger.e("Request Login Html Page threw error: " + e.getMessage(), e);
        }
        return null;
    }

    private String httpGET(String url, String cookie, String userAgent) {
        try {
            URL obj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("Cookie", cookie);
            con.setRequestProperty("User-Agent", userAgent);
            int responseCode = con.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                // success
                BufferedReader in = new BufferedReader(new InputStreamReader(
                        con.getInputStream()));
                String inputLine;
                StringBuffer responseString = new StringBuffer();

                while ((inputLine = in.readLine()) != null) {
                    responseString.append(inputLine);
                }
                in.close();
                con.disconnect();

                // print result
                return responseString.toString();
            } else {
                return null;
            }
        }  catch (ConnectException connException) {
            connected = false;
            logger.e("HttpGet " + url + " threw error: " + connException.getMessage(), connException);
        }  catch (Exception e) {
            logger.e("HttpGet " + url + " threw error: " + e.getMessage(), e);
        }
        return null;
    }

    private String httpGET(String url, String cookie, String _csrf, String xProfile, String userAgent) {
        try {
            URL obj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("Accept", "application/json, text/javascript, */*; q=0.01");
            con.setRequestProperty("Accept-Encoding", "gzip, deflate, br");
            con.setRequestProperty("Accept-Language", "en-US,en;q=0.9,el-GR;q=0.8,el;q=0.7");
            con.setRequestProperty("Connection", "keep-alive");
            con.setRequestProperty("Host", "services.uniwa.gr");
            con.setRequestProperty("Referer", "https://services.uniwa.gr/student/grades/list_diploma?p=" + xProfile);
            con.setRequestProperty("Sec-Fetch-Dest", "empty");
            con.setRequestProperty("Sec-Fetch-Mode", "cors");
            con.setRequestProperty("Sec-Fetch-Site", "same-origin");
            con.setRequestProperty("User-Agent", userAgent);
            con.setRequestProperty("X-CSRF-TOKEN", _csrf);
            con.setRequestProperty("X-Profile", xProfile);
            con.setRequestProperty("X-Requested-With", "XMLHttpRequest");
            con.setRequestProperty("Cookie", cookie);
            int responseCode = con.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                // success
                BufferedReader in = new BufferedReader(new InputStreamReader(
                        con.getInputStream()));
                String inputLine;
                StringBuffer responseString = new StringBuffer();

                while ((inputLine = in.readLine()) != null) {
                    responseString.append(inputLine);
                }
                in.close();
                con.disconnect();

                // print result
                return responseString.toString();
            } else {
                return null;
            }
        } catch (ConnectException connException) {
            connected = false;
            logger.e("HttpGet " + url + " threw error: " + connException.getMessage(), connException);
        } catch (Exception e) {
            logger.e("HttpGet " + url + " threw error: " + e.getMessage(), e);
        }
        return null;
    }

    private boolean isAuthorized(Connection.Response response) {
        if (response.statusCode() == 200) {
            String html;
            try {
                html = response.parse().toString();
                if (html.contains("The credentials you provided cannot be determined to be authentic.")) {
                    this.authorized = false;
                    return false;
                } else {
                    this.authorized = true;
                    return true;
                }
            } catch (IOException e) {
                logger.e("isAuthorized threw error: " + e.getMessage());
                return false;
            }
        } else {
            this.authorized = true;
            return true;
        }
    }

    private String getXProfile(String infoJSON) {
        try {
            JsonNode node = new ObjectMapper().readTree(infoJSON);
            JsonNode studentProfiles = node.get("studentProfiles");
            for (JsonNode student: studentProfiles)  {
                return student.get("id").asText();
            }
        } catch (IOException e) {
            logger.e("getXProfile threw error: " + e.getMessage());
        }
        return null;
    }

    public boolean isConnected() {
        return connected;
    }

    public boolean isAuthorized() {
        return authorized;
    }

    public String getInfoJSON() {
        return infoJSON;
    }

    public String getGradesJSON() {
        return gradesJSON;
    }

    public String getTotalAverageGrade() {
        return totalAverageGrade;
    }
}