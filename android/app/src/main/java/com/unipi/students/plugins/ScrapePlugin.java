package com.unipi.students.plugins;

import android.os.AsyncTask;

import com.datadog.android.log.Logger;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.unipi.students.model.LoginForm;
import com.unipi.students.model.ResponseEntity;
import com.unipi.students.model.StudentDTO;
import com.unipi.students.service.ScrapeService;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;

public class ScrapePlugin {

    private Logger logger = new Logger.Builder()
            .setNetworkInfoEnabled(true)
            .setServiceName("com.unipi.students.plugin.ScrapePlugin")
            .setLogcatLogsEnabled(true)
            .setDatadogLogsEnabled(true)
            .setLoggerName("ScrapePlugin")
            .build();

    public void getProgess(MethodCall call, MethodChannel.Result result) {
        String university = call.argument("university");
        String system = call.argument("system");
        String username = call.argument("username");
        String password = call.argument("password");
        JSONObject cookies = call.argument("cookies");
        String cookiesString = cookies == null ? null : (cookies.keys().hasNext()) ? cookies.toString() : null;
        final String PRE_LOG = university + (system != null ? "." + system : "");

        logger.i("[" + PRE_LOG + "]: Scraping task started");
        long startTime = System.currentTimeMillis();

        ResponseEntity response;
        StudentDTO studentDTO;
        String sStudent;
        try {
            response = new ScrapeTask().execute(university, system, username, password, cookiesString).get();

            if (response.getStatusCode().value() == 200) {
                ObjectMapper objectMapper = new ObjectMapper();
                studentDTO = (StudentDTO) response.getObject();
                sStudent = objectMapper.writeValueAsString(studentDTO);
            } else {
                long endTime = System.currentTimeMillis();
                if (response.getStatusCode().value() == 401 || response.getStatusCode().value() == 408) {
                    logger.w("[" + PRE_LOG + "]: Scraping task finished with status code " + response.getStatusCode().value() + ", after " + (endTime - startTime) + " ms");
                    result.error(String.valueOf(response.getStatusCode().value()), response.getStatusCode().getReasonPhrase(), null);
                } else {
                    logger.e("[" + PRE_LOG + "]: Scraping task finished with status code " + response.getStatusCode().value() + ", after " + (endTime - startTime) + " ms");
                    if (response.getObject() == null) {
                        result.error(String.valueOf(response.getStatusCode().value()), "Internal Server Error", null);
                    } else {
                        result.error(String.valueOf(response.getStatusCode().value()), response.getObject().toString(), null);
                    }
                }
                return;
            }
        } catch (ExecutionException | InterruptedException | JsonProcessingException e) {
            long endTime = System.currentTimeMillis();
            logger.e("[" + PRE_LOG + "]: Scraping task finished with status code 500, after " + (endTime - startTime) + " ms");
            result.error("500", "Internal Server Error", null);
            return;
        }

        long endTime = System.currentTimeMillis();
        if (studentDTO.getStudent() != null) {
            logger.i("[" + PRE_LOG + "]: Scraping task finished with status code 200, after " + (endTime - startTime) + " ms",null, new HashMap<String, Object>() {{
                put("department", studentDTO.getStudent().getInfo().getDepartment());
                put("semester", studentDTO.getStudent().getInfo().getSemester());
            }});
        } else {
            logger.i("[" + PRE_LOG + "]: Scraping task finished with status code 200, after " + (endTime - startTime) + " ms",null);
        }

        result.success(sStudent);
    }

    private class ScrapeTask extends AsyncTask<String, Void, ResponseEntity> {
        @Override
        protected ResponseEntity doInBackground(String... strings) {
            Map<String, String> cookies = null;
            try {
                if (strings[4] != null)
                    cookies = new ObjectMapper().readValue(strings[4], Map.class);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
            return new ScrapeService().getStudent(strings[0], strings[1], new LoginForm(strings[2], strings[3], cookies));
        }
    }
}
