package com.unipi.students.model;

public class Course {

    private String id;
    private String name;
    private String type;
    private String grade;
    private String examPeriod;

    public Course() {
    }

    public Course(String id, String name, String type, String grade, String examPeriod) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.grade = grade;
        this.examPeriod = examPeriod;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }

    public String getExamPeriod() {
        return examPeriod;
    }

    public void setExamPeriod(String examPeriod) {
        this.examPeriod = examPeriod;
    }

    @Override
    public boolean equals(Object o) {

        if (o == this) {
            return true;
        }

        if (!(o instanceof Course)) {
            return false;
        }

        Course c = (Course) o;

        return this.id.equals(c.id);
    }

    @Override
    public String toString() {
        return "Course{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", grade=" + grade +
                ", examPeriod='" + examPeriod + '\'' +
                '}';
    }
}
