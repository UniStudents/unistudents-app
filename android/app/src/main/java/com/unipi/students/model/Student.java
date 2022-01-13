package com.unipi.students.model;

public class Student {

    private Info info;
    private Grades grades;

    public Student() {
    }

    public Student(Info info, Grades grades) {
        this.info = info;
        this.grades = grades;
    }

    public Info getInfo() {
        return info;
    }

    public void setInfo(Info info) {
        this.info = info;
    }

    public Grades getGrades() {
        return grades;
    }

    public void setGrades(Grades grades) {
        this.grades = grades;
    }

    @Override
    public String toString() {
        return "Student{" +
                "info=" + info +
                ", grades=" + grades +
                '}';
    }
}
