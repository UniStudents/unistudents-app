package com.unipi.students.model;

public class Info {

    private String aem;
    private String firstName;
    private String lastName;
    private String department;
    private String semester;
    private String registrationYear;

    public Info() {
    }

    public Info(String aem, String firstName, String lastName, String department, String semester, String registrationYear) {
        this.aem = aem;
        this.firstName = firstName;
        this.lastName = lastName;
        this.department = department;
        this.semester = semester;
        this.registrationYear = registrationYear;
    }

    public String getAem() {
        return aem;
    }

    public void setAem(String aem) {
        this.aem = aem;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getSemester() {
        return semester;
    }

    public void setSemester(String semester) {
        this.semester = semester;
    }

    public String getRegistrationYear() {
        return registrationYear;
    }

    public void setRegistrationYear(String registrationYear) {
        this.registrationYear = registrationYear;
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
