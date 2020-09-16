package com.unipi.students.model;

import java.util.ArrayList;

public class Grades {

    private String totalPassedCourses;
    private String totalAverageGrade;
    private String totalEcts;
    private ArrayList<Semester> semesters;

    public Grades() {
        this.semesters = new ArrayList<>();
    }

    public Grades(String totalPassedCourses, String totalAverageGrade, String totalEcts) {
        this.totalPassedCourses = totalPassedCourses;
        this.totalAverageGrade = totalAverageGrade;
        this.totalEcts = totalEcts;
        this.semesters = new ArrayList<>();
    }

    public String getTotalPassedCourses() {
        return totalPassedCourses;
    }

    public void setTotalPassedCourses(String totalPassedCourses) {
        this.totalPassedCourses = totalPassedCourses;
    }

    public String getTotalAverageGrade() {
        return totalAverageGrade;
    }

    public void setTotalAverageGrade(String totalAverageGrade) {
        this.totalAverageGrade = totalAverageGrade;
    }

    public String getTotalEcts() {
        return totalEcts;
    }

    public void setTotalEcts(String totalEcts) {
        this.totalEcts = totalEcts;
    }

    public ArrayList<Semester> getSemesters() {
        return semesters;
    }

    public void setSemesters(ArrayList<Semester> semesters) {
        this.semesters = semesters;
    }

    @Override
    public String toString() {
        return "Grades{" +
                "totalPassedCourses=" + totalPassedCourses +
                ", totalAverageGrade='" + totalAverageGrade + '\'' +
                ", totalEcts=" + totalEcts +
                ", semesters=" + semesters +
                '}';
    }
}
