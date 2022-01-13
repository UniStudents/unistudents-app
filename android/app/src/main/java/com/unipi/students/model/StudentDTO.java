package com.unipi.students.model;

import java.util.Map;

public class StudentDTO {
    private String system;
    private Map<String, String> cookies;
    private Student student;

    public StudentDTO(Map<String, String> cookies, Student student) {
        this.cookies = cookies;
        this.student = student;
    }

    public StudentDTO(String system, Map<String, String> cookies, Student student) {
        this.system = system;
        this.cookies = cookies;
        this.student = student;
    }

    public String getSystem() {
        return system;
    }

    public void setSystem(String system) {
        this.system = system;
    }

    public Map<String, String> getCookies() {
        return cookies;
    }

    public void setCookies(Map<String, String> cookies) {
        this.cookies = cookies;
    }

    public Student getStudent() {
        return student;
    }

    public void setStudent(Student student) {
        this.student = student;
    }
}
