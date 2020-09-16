package com.unipi.students.plugin;

import android.os.AsyncTask;

import com.datadog.android.log.Logger;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.getcapacitor.JSObject;
import com.getcapacitor.NativePlugin;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.unipi.students.model.ResponseEntity;
import com.unipi.students.model.Student;
import com.unipi.students.service.ScrapeService;

import java.util.HashMap;
import java.util.concurrent.ExecutionException;

@NativePlugin()
public class ScrapePlugin extends Plugin {

    private Logger logger = new Logger.Builder()
            .setNetworkInfoEnabled(true)
            .setServiceName("com.unipi.students.plugin.ScrapePlugin")
            .setLogcatLogsEnabled(true)
            .setDatadogLogsEnabled(true)
            .setLoggerName("ScrapePlugin")
            .build();

    @PluginMethod()
    public void getStudent(PluginCall call) {
        String university = call.getString("university");
        String username = call.getString("username");
        String password = call.getString("password");

        logger.i("[" + university + "]: Scraping task started");
        long startTime = System.currentTimeMillis();

        ResponseEntity response = null;
        Student student = null;
        String sStudent = null;
        try {
            response = new ScrapeTask().execute(university, username, password).get();

            if (response.getObject() != null) {
                ObjectMapper objectMapper = new ObjectMapper();
                student = (Student) response.getObject();
                sStudent = objectMapper.writeValueAsString(student);
            } else {
                long endTime = System.currentTimeMillis();
                logger.i("[" + university + "]: Scraping task finished with status code " + response.getHttpStatus().value() + ", after " + (endTime - startTime) + " ms");
                call.reject(response.getHttpStatus().getReasonPhrase(), String.valueOf(response.getHttpStatus().value()));
                return;
            }
        } catch (ExecutionException | InterruptedException | JsonProcessingException e) {
            long endTime = System.currentTimeMillis();
            logger.e("[" + university + "]: Scraping task finished with status code 500, after " + (endTime - startTime) + " ms");
            call.reject("Internal Server Error", "500");
            return;
        }

        long endTime = System.currentTimeMillis();
        Student finalStudent = student;
        logger.i("[" + university + "]: Scraping task finished with status code 200, after " + (endTime - startTime) + " ms",null,
                new HashMap<String, Object>() {{
                    put("department", finalStudent.getInfo().getDepartment());
                    put("semester", finalStudent.getInfo().getSemester());
                }});
        JSObject ret = new JSObject();
        ret.put("student", sStudent);
        call.success(ret);
    }

    private class ScrapeTask extends AsyncTask<String, Void, ResponseEntity> {
        @Override
        protected ResponseEntity doInBackground(String... strings) {
            return new ScrapeService().getStudent(strings[0], strings[1], strings[2], getContext());
        }
    }
}
