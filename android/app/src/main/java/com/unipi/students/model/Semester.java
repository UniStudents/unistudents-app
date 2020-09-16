package com.unipi.students.model;

import java.util.ArrayList;

public class Semester {

    private int id;
    private int passedCourses;
    private String gradeAverage;
    private String ects;
    private ArrayList<Course> courses;

    public Semester() {
        this.courses = new ArrayList<>();
    }

    public Semester(int id, int passedCourses, String gradeAverage, String ects) {
        this.id = id;
        this.passedCourses = passedCourses;
        this.gradeAverage = gradeAverage;
        this.ects = ects;
        this.courses = new ArrayList<>();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getPassedCourses() {
        return passedCourses;
    }

    public void setPassedCourses(int passedCourses) {
        this.passedCourses = passedCourses;
    }

    public String getGradeAverage() {
        return gradeAverage;
    }

    public void setGradeAverage(String gradeAverage) {
        this.gradeAverage = gradeAverage;
    }

    public String getEcts() {
        return ects;
    }

    public void setEcts(String ects) {
        this.ects = ects;
    }

    public ArrayList<Course> getCourses() {
        return courses;
    }

    public void setCourses(ArrayList<Course> courses) {
        this.courses = courses;
    }

    @Override
    public String toString() {
        return "Semester{" +
                "id=" + id +
                ", passedCourses=" + passedCourses +
                ", gradeAverage=" + gradeAverage +
                ", ects=" + ects +
                ", courses=" + courses +
                '}';
    }
}
