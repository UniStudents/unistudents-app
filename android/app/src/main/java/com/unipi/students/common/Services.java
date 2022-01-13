package com.unipi.students.common;


import com.datadog.android.log.Logger;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.unipi.students.service.CryptoService;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Services {
    private Logger logger = new Logger.Builder()
            .setNetworkInfoEnabled(true)
            .setServiceName("com.unipi.students.common.Services")
            .setLogcatLogsEnabled(true)
            .setDatadogLogsEnabled(true)
            .setLoggerName("Services")
            .build();

    private final static String NODEJS = "https://unistudents-nodejs.herokuapp.com";

    public String[] jsUnFuck(String decodedString) {
        String json = "{\n" +
                "    \"data\": \"" + decodedString + "\"\n" +
                "}";

        try {
            URL url = new URL(NODEJS + "/eval");
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
            con.setRequestProperty("Accept", "application/json");
            con.setDoOutput(true);
            con.setDoInput(true);
            con.connect();

            OutputStream os = con.getOutputStream();
            os.write(json.getBytes("UTF-8"));
            os.close();

            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(con.getInputStream(), "utf-8"))) {
                StringBuilder response = new StringBuilder();
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
                con.disconnect();

                String responseString = response.toString();
                String[] keyValue = responseString.split(",");

                keyValue[0] = keyValue[0].split(":")[1].replace("\"", "");
                keyValue[1] = keyValue[1].split(":")[1].replace("\"", "").replace("}", "");

                return keyValue;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public String uploadLogFile(Exception exception, String document, String university) {
        String text;
        try {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            exception.printStackTrace(pw);
            text = sw.toString() + "\n\n======================\n\n" + document;
            CryptoService crypto = new CryptoService();
            text = crypto.encrypt(text);
        } catch (Exception e) {
            logger.e("uploadLogFile error" + e.getMessage(), e);
            return "Something went wrong.";
        }

        try {
            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            File tmpFile = File.createTempFile("_unistudents_bug_" + university.toUpperCase() + "_" + timestamp, ".txt");
            FileWriter writer = new FileWriter(tmpFile);
            writer.write(text);
            writer.close();

            OkHttpClient client = new OkHttpClient();
            RequestBody formBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("file", tmpFile.getName(), RequestBody.create(MediaType.parse("text/plain"), tmpFile))
                    .build();

            Request request = new Request.Builder().url("https://file.io?expires=1d").post(formBody).build();
            Response response = client.newCall(request).execute();

            if (response.code() == 200 && response.body() != null) {
                String responseString = response.body().string();
                ObjectMapper mapper = new ObjectMapper();
                JsonNode node = mapper.readTree(responseString);
                if (node.has("key")) {
                    logger.i("Log file uploaded successfully");
                    return node.get("key").asText();
                }
            }
            return null;
        } catch (Exception e) {
            logger.e("uploadLogFile error" + e.getMessage(), e);
            return null;
        }
    }

    public String postRequestWithJSONBody(final String stringURL, final String jsonBody) throws IOException {
        URL url = new URL (stringURL);

        HttpURLConnection con = (HttpURLConnection)url.openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/json; utf-8");
        con.setRequestProperty("Accept", "application/json");
        con.setDoOutput(true);

        try (OutputStream os = con.getOutputStream()) {
            byte[] input = jsonBody.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        InputStream inputStream;
        int code = con.getResponseCode();
        if (code < HttpURLConnection.HTTP_BAD_REQUEST) {
            inputStream = con.getInputStream();
        } else {
            inputStream = con.getErrorStream();
        }

        try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            StringBuilder response = new StringBuilder();
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
            return response.toString();
        }
    }
}
