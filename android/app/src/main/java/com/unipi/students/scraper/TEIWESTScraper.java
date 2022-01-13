package com.unipi.students.scraper;

import com.datadog.android.log.Logger;
import com.unipi.students.common.CommonsFactory;
import com.unipi.students.common.HttpStatus;
import com.unipi.students.common.UserAgentGenerator;
import com.unipi.students.model.LoginForm;
import com.unipi.students.model.ResponseEntity;
import com.unipi.students.model.StudentDTO;

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

public class TEIWESTScraper {
    private final String USER_AGENT;
    private boolean connected;
    private boolean captchaRequired;
    private Document infoPage;
    private Document gradesPage;
    private Map<String, String> cookies;
    private String university;
    private final String PRE_LOG;
    private static SSLSocketFactory sslSocketFactory;
    private Logger logger = new Logger.Builder()
            .setNetworkInfoEnabled(true)
            .setServiceName("com.unipi.students.scraper.TEIWESTScraper")
            .setLogcatLogsEnabled(true)
            .setDatadogLogsEnabled(true)
            .setLoggerName("TEIWESTScraper")
            .build();

    public TEIWESTScraper(String university) {
        this.connected = true;
        captchaRequired = true;
        USER_AGENT = UserAgentGenerator.generate();
        sslSocketFactory = CommonsFactory.sslSocketFactory;
        this.PRE_LOG = "[" + university + ".TEIWEST]";
    }

    public TEIWESTScraper(LoginForm loginForm, String university) {
        this.university = university;
        this.connected = true;
        captchaRequired = false;
        USER_AGENT = UserAgentGenerator.generate();
        this.PRE_LOG = "[" + university + ".TEIWEST]";
        getDocuments(loginForm.getUsername(), loginForm.getPassword(), loginForm.getCookies());
    }

    private void getDocuments(String username, String password, Map<String, String> cookies) {
        if (cookies == null) {
            getLoginPage();
        } else {
            String captchaText = cookies.get("captchaText");
            if (captchaText.equals("FILLED")) {
                getHtmlPages(cookies);
                if (infoPage == null || gradesPage == null) {
                    getLoginPage();
                }
            } else {
                getHtmlPages(username, password, cookies);
            }
        }
    }

    public ResponseEntity getLoginPage() {
        Map<String, String> cookies;
        String __VIEWSTATE;
        String __VIEWSTATEGENERATOR;
        String LBD_VCID_c_login_studentcaptcha;
        Connection.Response response;


        //
        // Get login page
        //

        try {
            response = Jsoup.connect("https://e-students.teiwest.gr/")
                    .method(Connection.Method.GET)
                    .header("User-Agent", USER_AGENT)
                    .sslSocketFactory(sslSocketFactory)
                    .execute();

            Document document = response.parse();
            __VIEWSTATE = document.getElementsByAttributeValue("name", "__VIEWSTATE").attr("value");
            __VIEWSTATEGENERATOR = document.getElementsByAttributeValue("name", "__VIEWSTATEGENERATOR").attr("value");
            LBD_VCID_c_login_studentcaptcha = document.getElementsByAttributeValue("name", "LBD_VCID_c_login_studentcaptcha").attr("value");

            cookies = response.cookies();
            cookies.put("__VIEWSTATE", __VIEWSTATE);
            cookies.put("__VIEWSTATEGENERATOR", __VIEWSTATEGENERATOR);
            cookies.put("LBD_VCID_c_login_studentcaptcha", LBD_VCID_c_login_studentcaptcha);
            cookies.put("captchaText", "");
            cookies.put("captchaImageUrl", "https://e-students.teiwest.gr/BotDetectCaptcha.ashx?get=image&c=c_login_studentcaptcha&t=" + LBD_VCID_c_login_studentcaptcha);

            setCookies(cookies);
            captchaRequired = true;
            return new ResponseEntity(new StudentDTO("TEIWEST", cookies, null), HttpStatus.OK);
        } catch (SocketTimeoutException | UnknownHostException | HttpStatusException | ConnectException connException) {
            connected = false;
            logger.w(this.PRE_LOG + " Warning: " + connException.getMessage(), connException);
            return new ResponseEntity(HttpStatus.REQUEST_TIMEOUT);
        } catch (IOException e) {
            logger.e(this.PRE_LOG + " Error: " + e.getMessage(), e);
            return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private void getHtmlPages(String username, String password, Map<String, String> cookies) {
        username = username.trim();
        password = password.trim();
        String captchaText = cookies.remove("captchaText").trim();
        String __VIEWSTATE = cookies.remove("__VIEWSTATE");
        String __VIEWSTATEGENERATOR = cookies.remove("__VIEWSTATEGENERATOR");
        String LBD_VCID_c_login_studentcaptcha = cookies.remove("LBD_VCID_c_login_studentcaptcha");
        Connection.Response response;


        //
        // Try to login
        //

        try {
            response = Jsoup.connect("https://e-students.teiwest.gr/?AspxAutoDetectCookieSupport=1")
                    .data("__EVENTTARGET", "")
                    .data("__EVENTARGUMENT", "")
                    .data("__VIEWSTATE", __VIEWSTATE)
                    .data("__VIEWSTATEGENERATOR", __VIEWSTATEGENERATOR)
                    .data("universityButtonList", (this.university.equals("UOP")) ? "0" : "1")      // 0 for UOP , 1 for UPATRAS
                    .data("universityButtonList$RB0", "C")
                    .data("universityButtonList$RB1", "U")
                    .data("tbUserName$State", "{&quot;validationState&quot;:&quot;&quot;}")
                    .data("tbUserName", username)
                    .data("tbPassword$State", "{&quot;validationState&quot;:&quot;&quot;}")
                    .data("tbPassword", password)
                    .data("LBD_VCID_c_login_studentcaptcha", LBD_VCID_c_login_studentcaptcha)
                    .data("LBD_BackWorkaround_c_login_studentcaptcha", "1")
                    .data("CaptchaCodeTextBox", captchaText)
                    .data("btnLogin", "Είσοδος")
                    .data("DXScript", "1_11,1_252,1_64,1_12,1_14,1_15,1_183,1_186,1_184,1_23,1_182")
                    .data("DXCss", "1_68,1_70,1_209,0_2203,0_20,0_2198,0_2305,0_2310,1_210,BotDetectCaptcha.ashx?get=layoutStyleSheet,favicon.ico,Content/Align.css")
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
                    .header("Accept-Encoding", "gzip, deflate, br")
                    .header("Accept-Language", "el-GR,el;q=0.8,en-US;q=0.5,en;q=0.3")
                    .header("Connection", "keep-alive")
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .header("Host", "e-students.teiwest.gr")
                    .header("Origin", "https://e-students.teiwest.gr")
                    .header("Referer", "https://e-students.teiwest.gr/?AspxAutoDetectCookieSupport=1")
                    .header("Upgrade-Insecure-Requests", "1")
                    .header("User-Agent", USER_AGENT)
                    .cookies(cookies)
                    .method(Connection.Method.POST)
                    .followRedirects(false)
                    .sslSocketFactory(sslSocketFactory)
                    .execute();

            if (response.statusCode() != 302) {
                return;
            }

            cookies = response.cookies();
        } catch (SocketTimeoutException | UnknownHostException | HttpStatusException | ConnectException connException) {
            connected = false;
            logger.w(this.PRE_LOG + " Warning: " + connException.getMessage(), connException);
            return;
        } catch (IOException e) {
            logger.e(this.PRE_LOG + " Error: " + e.getMessage(), e);
            return;
        }


        //
        //  Get info
        //

        try {
            response = Jsoup.connect("https://e-students.teiwest.gr/student/info/profile.aspx")
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
                    .header("Accept-Encoding", "gzip, deflate, br")
                    .header("Accept-Language", "el-GR,el;q=0.8,en-US;q=0.5,en;q=0.3")
                    .header("Connection", "keep-alive")
                    .header("Host", "e-students.teiwest.gr")
                    .header("Referer", "https://e-students.teiwest.gr/student/main.aspx")
                    .header("Upgrade-Insecure-Requests", "1")
                    .header("User-Agent", USER_AGENT)
                    .cookies(cookies)
                    .method(Connection.Method.GET)
                    .sslSocketFactory(sslSocketFactory)
                    .execute();

            setInfoPage(response.parse());
        } catch (SocketTimeoutException | UnknownHostException | HttpStatusException | ConnectException connException) {
            connected = false;
            logger.w(this.PRE_LOG + " Warning: " + connException.getMessage(), connException);
            return;
        } catch (IOException e) {
            logger.e(this.PRE_LOG + " Error: " + e.getMessage(), e);
            return;
        }


        //
        //  Get grades
        //

        try {
            response = Jsoup.connect("https://e-students.teiwest.gr/student/studies/gradesemester.aspx")
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
                    .header("Accept-Encoding", "gzip, deflate, br")
                    .header("Accept-Language", "el-GR,el;q=0.8,en-US;q=0.5,en;q=0.3")
                    .header("Connection", "keep-alive")
                    .header("Host", "e-students.teiwest.gr")
                    .header("Referer", "https://e-students.teiwest.gr/student/info/profile.aspx")
                    .header("Upgrade-Insecure-Requests", "1")
                    .header("User-Agent", USER_AGENT)
                    .cookies(cookies)
                    .method(Connection.Method.GET)
                    .sslSocketFactory(sslSocketFactory)
                    .execute();

            setGradesPage(response.parse());
        } catch (SocketTimeoutException | UnknownHostException | HttpStatusException | ConnectException connException) {
            connected = false;
            logger.w(this.PRE_LOG + " Warning: " + connException.getMessage(), connException);
            return;
        } catch (IOException e) {
            logger.e(this.PRE_LOG + " Error: " + e.getMessage(), e);
            return;
        }

        cookies.put("captchaText", "FILLED");
        setCookies(cookies);
    }

    private void getHtmlPages(Map<String, String> cookies) {
        Connection.Response response;


        //
        //  Get info
        //

        try {
            response = Jsoup.connect("https://e-students.teiwest.gr/student/info/profile.aspx")
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
                    .header("Accept-Encoding", "gzip, deflate, br")
                    .header("Accept-Language", "el-GR,el;q=0.8,en-US;q=0.5,en;q=0.3")
                    .header("Connection", "keep-alive")
                    .header("Host", "e-students.teiwest.gr")
                    .header("Referer", "https://e-students.teiwest.gr/student/main.aspx")
                    .header("Upgrade-Insecure-Requests", "1")
                    .header("User-Agent", USER_AGENT)
                    .cookies(cookies)
                    .method(Connection.Method.GET)
                    .followRedirects(false)
                    .sslSocketFactory(sslSocketFactory)
                    .execute();

            System.out.println("status: " + response.statusCode());
            System.out.println("location: " + response.header("location"));
            if (response.statusCode() != 200) return;

            setInfoPage(response.parse());
        } catch (SocketTimeoutException | UnknownHostException | HttpStatusException | ConnectException connException) {
            connected = false;
            logger.w(this.PRE_LOG + " Warning: " + connException.getMessage(), connException);
            return;
        } catch (IOException e) {
            logger.e(this.PRE_LOG + " Error: " + e.getMessage(), e);
            return;
        }


        //
        //  Get grades
        //

        try {
            response = Jsoup.connect("https://e-students.teiwest.gr/student/studies/gradesemester.aspx")
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
                    .header("Accept-Encoding", "gzip, deflate, br")
                    .header("Accept-Language", "el-GR,el;q=0.8,en-US;q=0.5,en;q=0.3")
                    .header("Connection", "keep-alive")
                    .header("Host", "e-students.teiwest.gr")
                    .header("Referer", "https://e-students.teiwest.gr/student/info/profile.aspx")
                    .header("Upgrade-Insecure-Requests", "1")
                    .header("User-Agent", USER_AGENT)
                    .cookies(cookies)
                    .method(Connection.Method.GET)
                    .followRedirects(false)
                    .sslSocketFactory(sslSocketFactory)
                    .execute();

            System.out.println("status: " + response.statusCode());
            System.out.println("location: " + response.header("location"));
            if (response.statusCode() != 200) return;

            setGradesPage(response.parse());
        } catch (SocketTimeoutException | UnknownHostException | HttpStatusException | ConnectException connException) {
            connected = false;
            logger.w(this.PRE_LOG + " Warning: " + connException.getMessage(), connException);
            return;
        } catch (IOException e) {
            logger.e(this.PRE_LOG + " Error: " + e.getMessage(), e);
            return;
        }

        cookies.put("captchaText", "FILLED");
        setCookies(cookies);
    }

    public boolean isConnected() {
        return connected;
    }

    public boolean isCaptchaRequired() {
        return captchaRequired;
    }

    public Document getInfoPage() {
        return infoPage;
    }

    private void setInfoPage(Document infoPage) {
        this.infoPage = infoPage;
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
}
