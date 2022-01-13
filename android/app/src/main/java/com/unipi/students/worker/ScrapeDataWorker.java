package com.unipi.students.worker;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.datadog.android.log.Logger;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;
import com.unipi.students.MainActivity;
import com.unipi.students.R;
import com.unipi.students.common.CommonsFactory;
import com.unipi.students.common.HttpStatus;
import com.unipi.students.model.Course;
import com.unipi.students.model.LoginForm;
import com.unipi.students.model.ResponseEntity;
import com.unipi.students.model.Student;
import com.unipi.students.model.StudentDTO;
import com.unipi.students.service.ApiService;
import com.unipi.students.service.CryptoService;
import com.unipi.students.service.ScrapeService;
import com.unipi.students.service.StorageService;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Calendar;
import java.util.Map;

public class ScrapeDataWorker extends Worker {
    private Logger logger = new Logger.Builder()
            .setNetworkInfoEnabled(true)
            .setServiceName("com.unipi.students.worker.ScrapeDataWorker")
            .setLogcatLogsEnabled(true)
            .setDatadogLogsEnabled(true)
            .setLoggerName("ScrapeDataWorker")
            .build();
    private static final String TAG = ScrapeDataWorker.class.getSimpleName();
    private StorageService storageService;
    private CryptoService cryptoService;
    private ApiService apiService;

    public ScrapeDataWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        storageService = new StorageService(context);
        cryptoService = new CryptoService();
        apiService = new ApiService();
    }

    @NonNull
    @Override
    public Result doWork() {
        String searchFor = getInputData().getString("SEARCH_FOR");
        Log.d(TAG, "searchFor " + searchFor);
        Log.d(TAG, "doWork: [START NEW WORK]");

//        // check if scraping is available based on some constrains
//        if (!scrapeIsAvailable() || searchFor != null) {
//            return Result.success();
//        }

        try {
            CommonsFactory.buildSSLSocketFactory(getApplicationContext());

            // get stored data: uni, username, pass
            String university = storageService.getUniversity();
            String system = storageService.getSystem();
            String username = storageService.getUsername();
            String password = storageService.getPassword();
            Map<String, String> cookies = convertToCookies(storageService.getCookies());
            // decrypt values

            // decrypt password...
            password = cryptoService.decrypt(password);
            Log.d(TAG, "doWork: " + university + " | " + username);

            // scrape data, get newStudent
            ResponseEntity responseEntity = new ScrapeService().getStudent(university, system, new LoginForm(username, password, cookies));

            if (responseEntity.getObject() == null) {
                if (responseEntity.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                    storageService.removePassword();
                    storageService.removeRememberMe();
                    storageService.removeSystem();
                    return Result.success();
                }
                return Result.failure();
            }

            StudentDTO studentDTO = (StudentDTO) responseEntity.getObject();
            Student newStudent = studentDTO.getStudent();
            Student storedStudent = getStoredStudent();

            // compare newStudent with storedStudent
            Map<Course, Integer> newCourses;
            if (storedStudent == null) { return Result.success(); }
            if (storedStudent.getInfo().getAem().equals(newStudent.getInfo().getAem())) {
                newCourses = storageService.compareGrades(storedStudent.getGrades(), newStudent.getGrades());

                // if newGrades found, notify server
                for (Map.Entry<Course, Integer> entry : newCourses.entrySet()) {

                    // show local notification
                    showNotification(entry.getKey(), "Πάτα να δεις το βαθμό σου");

                    // construct topic
                    String topicToUnsubscribe = university + "." + URLEncoder.encode(entry.getKey().getId(), StandardCharsets.UTF_8.name());

                    // unsub from this course
                    Task<Void> task = FirebaseMessaging
                            .getInstance().unsubscribeFromTopic(topicToUnsubscribe);

                    while (!task.isComplete()) {
                        System.out.println("Wait");
                        Thread.sleep(200);
                    }

                    if (searchFor == null) {
                        apiService.notifyForNewGrade(entry.getKey(), university, storedStudent.getInfo().getDepartment(), entry.getValue());
                    } else if (!entry.getKey().getId().equals(searchFor)) {
                        apiService.notifyForNewGrade(entry.getKey(), university, storedStudent.getInfo().getDepartment(), entry.getValue());
                    }
                }
            }
            else { return Result.success(); }

            // store cookies
            storageService.setCookies(convertCookiesMapToString(studentDTO.getCookies()));

            Log.d(TAG, "doWork: [COMPLETE WORK]");
            return Result.success();
        } catch (Exception e) {
            e.printStackTrace();
            return Result.failure();
        }
    }

    @Override
    public void onStopped() {
        super.onStopped();
        Log.i(TAG, "OnStopped called for this worker");
    }

    private boolean scrapeIsAvailable() {
        // get current day &time
        Calendar c = Calendar.getInstance();
        int dayOfDay = c.get(Calendar.DAY_OF_WEEK);
        int timeOfDay = c.get(Calendar.HOUR_OF_DAY);

        // scraping is available MON to FRI
        if (dayOfDay == Calendar.SATURDAY || dayOfDay == Calendar.SUNDAY) {
            Log.d(TAG, "scrapeIsAvailable: NOT AVAILABLE " + timeOfDay + ", " + dayOfDay);
            return false;
        }

        // scraping is available 8:00 to 17:00
        if ((timeOfDay <= 7) || (timeOfDay >= 18)) {
            Log.d(TAG, "scrapeIsAvailable: NOT AVAILABLE " + timeOfDay + ", " + dayOfDay);
            return false;
        }

        // scraping is available only on "remember_me" mode
        String rememberMe = storageService.getRememberMe();
        if (rememberMe == null) {
            Log.d(TAG, "scrapeIsAvailable: NOT AVAILABLE " + timeOfDay + ", " + dayOfDay + ", " + rememberMe);
            return false;
        }
        else if (!rememberMe.equals("true")) {
            Log.d(TAG, "scrapeIsAvailable: NOT AVAILABLE " + timeOfDay + ", " + dayOfDay + ", " + rememberMe);
            return false;
        }

        Log.d(TAG, "scrapeIsAvailable: AVAILABLE " + timeOfDay + ", " + dayOfDay + ", " + rememberMe);
        return true;
    }

    private String convertCookiesMapToString(Map<String, String> cookiesMap) {
        ObjectMapper mapper = new ObjectMapper();
        String cookies;
        try {
            cookies = mapper.writeValueAsString(cookiesMap);
            return cookies;
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }

    private Student convertToStudent(String jsonStudent) {
        ObjectMapper mapper = new ObjectMapper();
        Student student;
        try {
            student = mapper.readValue(jsonStudent, Student.class);
            return student;
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }

    private Map<String, String> convertToCookies(String jsonCookies) {
        if (jsonCookies == null) return null;
        ObjectMapper mapper = new ObjectMapper();
        Map<String, String> cookies;
        try {
            cookies = mapper.readValue(jsonCookies, Map.class);
            return cookies;
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }

    private Student getStoredStudent() {
        String studentJson = storageService.getStudent();
        if (studentJson != null) {
            return convertToStudent(studentJson);
        }
        return null;
    }

    private void showNotification(Course course, String context) {
        Intent intent;
        PendingIntent pendingIntent;
        NotificationCompat.Builder builder;

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {

            intent = new Intent(getApplicationContext(), MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, 0);

            builder = new NotificationCompat.Builder(getApplicationContext(), "grades_channel")
                    .setSmallIcon(R.drawable.ic_notification)
                    .setContentTitle(course.getName())
                    .setContentText(context)
                    .setContentIntent(pendingIntent)
                    .setColor(getApplicationContext().getResources().getColor(R.color.colorPrimary))
                    .setPriority(NotificationCompat.PRIORITY_HIGH);
        } else {

            builder = new NotificationCompat.Builder(getApplicationContext());

            intent = new Intent(getApplicationContext(), MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, 0);

            builder.setSmallIcon(R.drawable.ic_notification)
                    .setContentTitle(course.getName())
                    .setContentText(context)
                    .setContentIntent(pendingIntent)
                    .setColor(getApplicationContext().getResources().getColor(R.color.colorPrimary))
                    .setPriority(NotificationCompat.PRIORITY_HIGH);
        }

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getApplicationContext());
        notificationManager.notify(course.getId(), 2021 , builder.build());
        logger.d("showNotification for " + course.getId());
    }
}
