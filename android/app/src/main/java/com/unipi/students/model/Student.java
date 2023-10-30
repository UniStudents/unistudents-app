package com.unipi.students.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class Student {

    private Info info;
    private Grades grades;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Student )) return false;
        return info != null && info.equals(((Student) o).info);
    }

    @Override
    public int hashCode() {
        return (
                (info != null ? info.hashCode() : 0) +
                (grades != null ? grades.hashCode() : 0)
        );
    }

    @Override
    public String toString() {
        return "Student{" +
                "info=" + info +
                ", grades=" + grades +
                '}';
    }

}
