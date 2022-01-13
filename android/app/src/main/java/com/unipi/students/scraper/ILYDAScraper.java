package com.unipi.students.scraper;

import com.datadog.android.log.Logger;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.unipi.students.common.CommonsFactory;
import com.unipi.students.common.UserAgentGenerator;
import com.unipi.students.model.LoginForm;

import org.jsoup.Connection;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;

public class ILYDAScraper {
    private final String UNIVERSITY;
    private final String DOMAIN;
    private final String PRE_LOG;
    private final String USER_AGENT;
    private boolean connected;
    private boolean authorized;
    private String infoJSON;
    private String gradesJSON;
    private String totalAverageGrade;
    private Map<String, String> cookies;
    private static SSLSocketFactory sslSocketFactory;
    private final Logger logger = new Logger.Builder()
            .setNetworkInfoEnabled(true)
            .setServiceName("com.unipi.students.scraper.ILYDAScraper")
            .setLogcatLogsEnabled(true)
            .setDatadogLogsEnabled(true)
            .setLoggerName("ILYDAScraper")
            .build();

    public ILYDAScraper(LoginForm loginForm, String university, String system, String domain) {
        this.UNIVERSITY = university;
        this.DOMAIN = domain;
        this.PRE_LOG = university + (system == null ? "" : "." + system);
        this.USER_AGENT = UserAgentGenerator.generate();
        this.connected = true;
        this.authorized = true;
        sslSocketFactory = CommonsFactory.sslSocketFactory;
        this.getDocuments(loginForm.getUsername(), loginForm.getPassword(), loginForm.getCookies());
    }

    private void getDocuments(String username, String password, Map<String, String> cookies) {
        if (cookies == null) {
            System.out.println("======> DEBUG: Normal request");
            getHtmlPages(username, password);
        } else {
            System.out.println("======> DEBUG: Cookies request");
            getHtmlPages(cookies);
            if (infoJSON == null || gradesJSON == null || totalAverageGrade == null) {
                System.out.println("======> DEBUG: Normal request after cookies request");
                getHtmlPages(username, password);
            }
        }
    }

    private void getHtmlPages(String username, String password) {
        username = username.trim();
        password = password.trim();

        //
        // Request Login Html Page
        //

        Connection.Response response;
        Document doc;
        String lt;
        String execution;

        try {
            response = getResponse();
            if (response == null) return;
            doc = response.parse();
            Elements el = doc.getElementsByAttributeValue("name", "lt");
            lt = el.first().attributes().get("value");
            Elements exec = doc.getElementsByAttributeValue("name", "execution");
            execution = exec.first().attributes().get("value");
        } catch (IOException e) {
            logger.e("[" + PRE_LOG + "] Error: " + e.getMessage(), e);
            return;
        }

        // store session cookies
        Map<String, String> sessionCookies = new HashMap<>();
        sessionCookies.putAll(response.cookies());

        //
        // Try to Login
        //

        try {
            response = Jsoup.connect("https://sso." + UNIVERSITY.toLowerCase() + ".gr/login?service=https%3A%2F%2F" + DOMAIN + "%2Flogin%2Fcas")
                    .data("username", username)
                    .data("password", password)
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
                    .header("Host", "sso." + UNIVERSITY.toLowerCase() + ".gr")
                    .header("Origin", "https://sso." + UNIVERSITY.toLowerCase() + ".gr")
                    .header("Referer", "https://sso." + UNIVERSITY.toLowerCase() + ".gr/login?service=https%3A%2F%2F" + DOMAIN + "%2Flogin%2Fcas")
                    .header("Sec-Fetch-Dest", "document")
                    .header("Sec-Fetch-Mode", "navigate")
                    .header("Sec-Fetch-Site", "same-origin")
                    .header("Sec-Fetch-User", "?1")
                    .header("Upgrade-Insecure-Requests", "1")
                    .header("User-Agent", USER_AGENT)
                    .cookies(sessionCookies)
                    .followRedirects(false)
                    .method(Connection.Method.POST)
                    .execute();
        } catch (SocketTimeoutException | UnknownHostException | HttpStatusException | ConnectException connException) {
            connected = false;
            logger.w("[" + PRE_LOG + "] Warning: " + connException.getMessage(), connException);
            return;
        } catch (IOException e) {
            logger.e("[" + PRE_LOG + "] Error: " + e.getMessage(), e);
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
        if (location == null) return;
        try {
            response = Jsoup.connect(location)
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
                    .header("Accept-Encoding", "gzip, deflate, br")
                    .header("Accept-Language", "en-US,en;q=0.9,el-GR;q=0.8,el;q=0.7")
                    .header("Cache-Control", "max-age=0")
                    .header("Connection", "keep-alive")
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .header("Host", DOMAIN)
                    .header("Referer", "https://sso." + UNIVERSITY.toLowerCase() + ".gr/")
                    .header("Sec-Fetch-Dest", "document")
                    .header("Sec-Fetch-Mode", "navigate")
                    .header("Sec-Fetch-Site", "same-origin")
                    .header("Sec-Fetch-User", "?1")
                    .header("Upgrade-Insecure-Requests", "1")
                    .header("User-Agent", USER_AGENT)
                    .followRedirects(false)
                    .sslSocketFactory(sslSocketFactory)
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

        // store session cookies
        StringBuilder cookie = new StringBuilder();
        for (Map.Entry<String, String> entry: response.cookies().entrySet()) {
            cookie.append(entry.getKey()).append("=").append(entry.getValue());
        }

        Map<String, String> cookiesSession = response.cookies();

        //
        // Redirect to DOMAIN to get X-CSRF-TOKEN
        //

        String _csrf;
        Document pageIncludesToken;
        location = response.header("location");
        if (location == null) return;
        try {
            response = Jsoup.connect(location)
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
                    .header("Accept-Encoding", "gzip, deflate, br")
                    .header("Accept-Language", "en-US,en;q=0.9,el-GR;q=0.8,el;q=0.7")
                    .header("Cache-Control", "max-age=0")
                    .header("Connection", "keep-alive")
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .header("Host", DOMAIN)
                    .header("Referer", "https://sso." + UNIVERSITY.toLowerCase() + ".gr/")
                    .header("Sec-Fetch-Dest", "document")
                    .header("Sec-Fetch-Mode", "navigate")
                    .header("Sec-Fetch-Site", "same-origin")
                    .header("Sec-Fetch-User", "?1")
                    .header("Upgrade-Insecure-Requests", "1")
                    .header("User-Agent", USER_AGENT)
                    .cookies(cookiesSession)
                    .followRedirects(false)
                    .sslSocketFactory(sslSocketFactory)
                    .method(Connection.Method.GET)
                    .execute();
            pageIncludesToken = response.parse();

            Elements el;
            if (pageIncludesToken != null) {
                el = pageIncludesToken.getElementsByAttributeValue("name", "_csrf");
                _csrf = el.first().attributes().get("content");
            } else {
                return;
            }
        } catch (SocketTimeoutException | UnknownHostException | HttpStatusException | ConnectException connException) {
            connected = false;
            logger.w("[" + PRE_LOG + "] Warning: " + connException.getMessage(), connException);
            return;
        } catch (IOException e) {
            logger.e("[" + PRE_LOG + "] Error: " + e.getMessage(), e);
            return;
        }

        //
        //  Http GET request to api endpoint
        //  using cookie form previous request
        //

        infoJSON = httpGET("https://" + DOMAIN + "/api/person/profiles", cookie.toString());
        if (infoJSON == null) return;

        // get X-Profile variable from infoJSON
        String xProfile = getXProfile(infoJSON);
        gradesJSON = httpGET("https://" + DOMAIN + "/feign/student/grades/diploma", cookie.toString(), _csrf, xProfile);
        if (gradesJSON == null) return;

        totalAverageGrade = httpGET("https://" + DOMAIN + "/feign/student/grades/average_student_course_grades", cookie.toString(), _csrf, xProfile);
        setCookies(cookie.toString(), _csrf, xProfile);
    }

    private void getHtmlPages(Map<String, String> cookies) {
        String cookie = cookies.get("cookie");
        String _csrf = cookies.get("_csrf");
        String xProfile = cookies.get("xProfile");
        if (cookie == null ||
            _csrf == null ||
            xProfile == null) return;

        infoJSON = httpGET("https://" + DOMAIN + "/api/person/profiles", cookie);
        if (infoJSON == null) return;

        gradesJSON = httpGET("https://" + DOMAIN + "/feign/student/grades/diploma", cookie, _csrf, xProfile);
        if (gradesJSON == null) return;

        totalAverageGrade = httpGET("https://" + DOMAIN + "/feign/student/grades/average_student_course_grades", cookie, _csrf, xProfile);
        setCookies(cookie, _csrf, xProfile);
    }

    private Connection.Response getResponse() {
        try {
            return Jsoup.connect("https://sso." + UNIVERSITY.toLowerCase() + ".gr/login?service=https%3A%2F%2F" + DOMAIN + "%2Flogin%2Fcas")
                    .method(Connection.Method.GET)
                    .header("User-Agent", USER_AGENT)
                    .execute();
        } catch (SocketTimeoutException | UnknownHostException | HttpStatusException | ConnectException connException) {
            connected = false;
            logger.w("[" + PRE_LOG + "] Warning: " + connException.getMessage(), connException);
        } catch (IOException e) {
            logger.e("[" + PRE_LOG + "] Error: " + e.getMessage(), e);
        }
        return null;
    }

    private String httpGET(String url, String cookie) {
        try {
            URL obj = new URL(url);
            HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("Cookie", cookie);
            con.setRequestProperty("User-Agent", USER_AGENT);
            con.setSSLSocketFactory(sslSocketFactory);
            int responseCode = con.getResponseCode();
            if (responseCode == HttpsURLConnection.HTTP_OK) {
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
        }  catch (ConnectException | UnknownHostException | HttpStatusException connException) {
            connected = false;
            logger.w("[" + PRE_LOG + "] Warning: " + connException.getMessage(), connException);
        }  catch (Exception e) {
            logger.e("[" + PRE_LOG + "] Error: " + e.getMessage(), e);
        }
        return null;
    }

    private String httpGET(String url, String cookie, String _csrf, String xProfile) {
        try {
            URL obj = new URL(url);
            HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("Accept", "application/json, text/javascript, */*; q=0.01");
            con.setRequestProperty("Accept-Encoding", "gzip, deflate, br");
            con.setRequestProperty("Accept-Language", "en-US,en;q=0.9,el-GR;q=0.8,el;q=0.7");
            con.setRequestProperty("Connection", "keep-alive");
            con.setRequestProperty("Content-Type", "application/json; charset=utf-8");
            con.setRequestProperty("Host", DOMAIN);
            con.setRequestProperty("Referer", "https://" + DOMAIN  + "/student/grades/list_diploma?p=" + xProfile);
            con.setRequestProperty("Sec-Fetch-Dest", "empty");
            con.setRequestProperty("Sec-Fetch-Mode", "cors");
            con.setRequestProperty("Sec-Fetch-Site", "same-origin");
            con.setRequestProperty("User-Agent", USER_AGENT);
            con.setRequestProperty("X-CSRF-TOKEN", _csrf);
            con.setRequestProperty("X-Profile", xProfile);
            con.setRequestProperty("X-Requested-With", "XMLHttpRequest");
            con.setRequestProperty("Cookie", cookie);
            con.setSSLSocketFactory(sslSocketFactory);
            int responseCode = con.getResponseCode();
            if (responseCode == HttpsURLConnection.HTTP_OK) {
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
        } catch (ConnectException | UnknownHostException | HttpStatusException connException) {
            connected = false;
            logger.w("[" + PRE_LOG + "] Warning: " + connException.getMessage(), connException);
        } catch (Exception e) {
            logger.e("[" + PRE_LOG + "] Error: " + e.getMessage(), e);
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
                logger.e("[" + PRE_LOG + "] Error: " + e.getMessage(), e);
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
            logger.e("[" + PRE_LOG + "] Error: " + e.getMessage(), e);
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

    public Map<String, String> getCookies() {
        return cookies;
    }

    public void setCookies(String cookie, String _csrf, String xProfile) {
        this.cookies = new HashMap<>();
        cookies.put("cookie", cookie);
        cookies.put("_csrf", _csrf);
        cookies.put("xProfile", xProfile);
    }
}
