package com.unipi.students.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.AllArgsConstructor;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class Info {

    private String aem;
    private String firstName;
    private String lastName;
    private String department;
    private String semester;
    private String registrationYear;

    public Info() {
        this.aem = "";
        this.firstName = "";
        this.lastName = "";
        this.department = "";
        this.semester = "";
        this.registrationYear = "";
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (!(obj instanceof Info))
            return false;
        if (obj == this)
            return true;
        return this.getAem().equals(((Info) obj).getAem());
    }

    @Override
    public int hashCode() {
        return
                (
                    this.getAem() != null ?
                    this.getAem().hashCode() :
                    0
                        )
                +
                (
                    this.getFirstName() != null ?
                    this.getFirstName().hashCode() :
                    0
                        )
                +
                (
                    this.getLastName() != null ?
                    this.getLastName().hashCode() :
                    0
                        )
                +
                (
                    this.getDepartment() != null ?
                    this.getDepartment().hashCode() :
                    0
                        )
                +
                (
                    this.getSemester() != null ?
                    this.getSemester().hashCode() :
                    0
                        )
                +
                (
                    this.getRegistrationYear() != null ?
                    this.getRegistrationYear().hashCode() :
                    0
                        );
    }

    @Override
    public String toString() {
        return "Info{" +
                "aem='" + aem + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", department='" + department + '\'' +
                ", semester='" + semester + '\'' +
                ", registrationYear='" + registrationYear + '\'' +
                '}';
    }

}
