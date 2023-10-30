package com.unipi.students.model;

import java.util.ArrayList;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.AllArgsConstructor;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class Grades {

    private String totalPassedCourses;
    private String totalAverageGrade;
    private String totalEcts;
    private ArrayList<Semester> semesters;

    public Grades() {
        this.semesters = new ArrayList<>();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (!(obj instanceof Grades))
            return false;
        if (obj == this)
            return true;
        return this.getTotalPassedCourses().equals(((Grades) obj).getTotalPassedCourses());
    }

    @Override
    public int hashCode() {
        return
                (
                    this.getTotalPassedCourses() != null ?
                    this.getTotalPassedCourses().hashCode() :
                    0
                        )
                +
                (
                    this.getTotalAverageGrade() != null ?
                    this.getTotalAverageGrade().hashCode() :
                    0
                        )
                +
                (
                    this.getTotalEcts() != null ?
                    this.getTotalEcts().hashCode() :
                    0
                        )
                +
                (
                    this.getSemesters() != null ?
                    this.getSemesters().hashCode() :
                    0
                        );
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
