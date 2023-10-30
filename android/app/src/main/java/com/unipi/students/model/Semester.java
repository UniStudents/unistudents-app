package com.unipi.students.model;

import java.util.ArrayList;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@AllArgsConstructor
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Semester)) return false;

        Semester semester = (Semester) o;

        if (getId() != semester.getId()) return false;
        if (
                getPassedCourses() != semester.getPassedCourses()
        ) return false;
        if (
                getGradeAverage() != null ? !getGradeAverage()
                        .equals(semester.getGradeAverage()) :
                        semester.getGradeAverage() != null
        )
            return false;
        if (
                getEcts() != null ? !getEcts()
                        .equals(semester.getEcts()) :
                        semester.getEcts() != null
        )
            return false;
        return getCourses() != null ? getCourses()
                .equals(semester.getCourses()) :
                semester.getCourses() == null;
    }

    @Override
    public int hashCode() {
        int result = getId();
        result = 31 * result + getPassedCourses();
        result = 31 * result + (getGradeAverage() != null ? getGradeAverage().hashCode() : 0);
        result = 31 * result + (getEcts() != null ? getEcts().hashCode() : 0);
        result = 31 * result + (getCourses() != null ? getCourses().hashCode() : 0);
        return result;
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
