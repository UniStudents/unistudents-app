package com.unipi.students.service;

import android.util.Log;

import com.unipi.students.model.Course;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class ApiService {

    private final String FUNCTIONS_URL = "https://europe-west2-unistudentsapp2020.cloudfunctions.net/courseNotification";

    public ApiService() {}

    public String notifyForNewGrade(Course course, String university, String department, Integer courseSemester) {
        String json = "{\n" +
                "    \"id\": \"" + course.getId() + "\",\n" +
                "    \"name\": \"" + course.getName() + "\",\n" +
                "    \"semester\":" + courseSemester + ",\n" +
                "    \"type\": \"" +  course.getType() + "\",\n" +
                "    \"departments\": [\n" +
                "        \"" + department + "\"\n" +
                "    ]\n" +
                "}";

        try {
            URL url = new URL(FUNCTIONS_URL + "?university=" + university);
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
                return response.toString();
            }
        } catch (Exception e) {
            Log.e("NOTIFY", "notifyForNewGrade: ", e);
        }
        return null;
    }
}
