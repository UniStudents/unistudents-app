package com.unipi.students.service;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import com.unipi.students.model.Course;
import com.unipi.students.model.Grades;
import com.unipi.students.model.Semester;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StorageService {

    private static final String PREFS_NAME = "CapacitorStorage";
    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;

    public StorageService(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Activity.MODE_PRIVATE);
        editor = prefs.edit();
    }

    public String getUniversity() {
        return prefs.getString("university", null);
    }

    public String getSystem() { return prefs.getString("system", null); }

    public void removeSystem() {
        editor.remove("system");
        editor.apply();
    }

    public void setCookies(String cookies) {
        editor.putString("cookies", cookies);
        editor.apply();
    }

    public String getCookies() {
        return prefs.getString("cookies", null);
    }

    public String getUsername() {
        return prefs.getString("username", null);
    }

    public String getPassword() {
        return prefs.getString("password", null);
    }

    public void removePassword() {
        editor.remove("password");
        editor.apply();
    }

    public String getStudent() {
        return prefs.getString("student", null);
    }

    public String getRememberMe() {
        return prefs.getString("remember_me", null);
    }

    public void removeRememberMe() {
        editor.remove("remember_me");
        editor.apply();
    }

    public String getSurvey() {
        return prefs.getString("survey", null);
    }

    public Map<Course, Integer> compareGrades(Grades oldGrades, Grades newGrades) {
        Map<Course, Integer> newCourses = new HashMap<>();
        List<Course> oldCourses = new ArrayList<>();

        for (Semester semester : oldGrades.getSemesters())
            oldCourses.addAll(semester.getCourses());

        for (Semester semester : newGrades.getSemesters()) {
            for (Course course : semester.getCourses()) {
                int i = 0;
                while (oldCourses.size() > 0 && i < oldCourses.size()) {
                    if (course.getId().equals(oldCourses.get(i).getId())) {
                        if (!course.getExamPeriod().equals(oldCourses.get(i).getExamPeriod())) {
                            if (!course.getGrade().equals("-")) {
                                newCourses.put(course, semester.getId());
                            }
                        }
                        oldCourses.remove(i);
                        break;
                    }
                    else {
                        i++;
                    }
                }
            }
        }
        return newCourses;
    }
}
