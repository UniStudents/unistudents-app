package com.unipi.students.worker;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.unipi.students.common.HttpStatus;
import com.unipi.students.model.Course;
import com.unipi.students.model.ResponseEntity;
import com.unipi.students.model.Student;
import com.unipi.students.service.ApiService;
import com.unipi.students.service.CryptoService;
import com.unipi.students.service.ScrapeService;
import com.unipi.students.service.StorageService;

import java.util.Calendar;
import java.util.List;
import java.util.Map;

public class ScrapeDataWorker extends Worker {
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
        Log.d(TAG, "doWork: [START NEW WORK]");

        // check if scraping is available based on some constrains
        if (!scrapeIsAvailable()) {
            return Result.success();
        }

        try {
            // get stored data: uni, username, pass
            String university = storageService.getUniversity();
            String username = storageService.getUsername();
            String password = storageService.getPassword();

            // decrypt password...
            password = cryptoService.decrypt(password);
            Log.d(TAG, "doWork: " + university + " | " + username);

            // scrape data, get newStudent
            ResponseEntity responseEntity = new ScrapeService().getStudent(university, username, password, getApplicationContext());

            if (responseEntity.getObject() == null) {
                if (responseEntity.getHttpStatus() == HttpStatus.UNAUTHORIZED) {
                    storageService.removePassword();
                    storageService.removeRememberMe();
                    return Result.failure();
                }
                return Result.failure();
            }

            Student newStudent = (Student) responseEntity.getObject();
            Student storedStudent = getStoredStudent();

            // compare newStudent with storedStudent
            Map<Course, Integer> newCourses;
            if (storedStudent == null) { return Result.failure(); }
            if (storedStudent.getInfo().getAem().equals(newStudent.getInfo().getAem())) {
                newCourses = storageService.compareGrades(storedStudent.getGrades(), newStudent.getGrades());

                // if newGrades found, notify server
                for (Map.Entry<Course, Integer> entry : newCourses.entrySet()) {
                    apiService.notifyForNewGrade(entry.getKey(), university, storedStudent.getInfo().getDepartment(), entry.getValue());
                }
            }
            else { return Result.failure(); }

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

    private Student getStoredStudent() {
        String studentJson = storageService.getStudent();
        if (studentJson != null) {
            return convertToStudent(studentJson);
        }
        return null;
    }
}
