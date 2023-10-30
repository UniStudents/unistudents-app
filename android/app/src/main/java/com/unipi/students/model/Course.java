package com.unipi.students.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class Course {

    private String id;
    private String name;
    private String type;
    private String grade;
    private String examPeriod;

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (!(obj instanceof Course))
            return false;
        if (obj == this)
            return true;
        return this.getId().equals(((Course) obj).getId());
    }

    @Override
    public int hashCode() {
        return this.getId().hashCode();
    }

    @Override
    public String toString() {
        return
            "Course{" +
            "id='" + id + '\'' +
            ", name='" + name + '\'' +
            ", type='" + type + '\'' +
            ", grade='" + grade + '\'' +
            ", examPeriod='" + examPeriod + '\'' +
            '}';
    }

}
