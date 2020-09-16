package com.unipi.students.parser;

import com.datadog.android.log.Logger;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.unipi.students.model.*;
import com.unipi.students.service.ApiService;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;

public class UNIWAParser {
    private Logger logger = new Logger.Builder()
            .setNetworkInfoEnabled(true)
            .setServiceName("com.unipi.students.parser.UNIWAParser")
            .setLogcatLogsEnabled(true)
            .setDatadogLogsEnabled(true)
            .setLoggerName("UNIWAParser")
            .build();

    private Info parseInfoJSON(String infoJSON) {
        Info info = new Info();

        try {
            JsonNode node = new ObjectMapper().readTree(infoJSON);

            JsonNode studentProfiles = node.get("studentProfiles");
            for (JsonNode student: studentProfiles)  {
                String aem = student.get("username").asText();
                info.setAem(aem);

                String firstName = student.get("firstname").asText();
                info.setFirstName(firstName);

                String lastName = student.get("lastname").asText();
                info.setLastName(lastName);

                String department = student.get("departmentTitle").asText();
                info.setDepartment(department);

                String registrationYear = student.get("programTitle").asText();
                info.setRegistrationYear(registrationYear);
            }
            return info;
        } catch (IOException e) {
            logger.e("parseInfoJSON threw error: " + e.getMessage(), e);
            return null;
        }
    }

    private Grades parseGradesJSON(String gradesJSON, String totalAverageGrade, Info info) {
        Grades grades = new Grades();
        ArrayList<Semester> semesters = initSemesters();
        DecimalFormat df2 = new DecimalFormat("#.##");

        int totalEcts = 0;
        int count = 0;
        int[] semesterCount = new int[10];
        try {
            JsonNode node = new ObjectMapper().readTree(gradesJSON);
            JsonNode studentCourses = node.get("studentCourses");

            if (studentCourses.size() == 0) {
                grades.setTotalAverageGrade("-");
                grades.setTotalPassedCourses("0");
                grades.setTotalEcts("0");
                grades.setSemesters(new ArrayList<>());
                return grades;
            }

            for (JsonNode courseJSON: studentCourses)  {
                JsonNode semesterId = courseJSON.get("semesterId");
                int studentSemester = semesterId.get("sortOrder").asInt();
                if (studentSemester == 253 || studentSemester == 254)
                    studentSemester = 7;
                Semester semester = semesters.get(studentSemester-1);
                semester.setId(studentSemester);

                Course course = new Course();
                String id = courseJSON.get("courseCode").asText();
                course.setId(id);

                String name = courseJSON.get("title").asText();
                course.setName(name);

                double grade = 0;
                if (courseJSON.get("grade").isNull()) {
                    course.setGrade("-");
                } else {
                    grade = courseJSON.get("grade").asDouble() * 10;
                    course.setGrade(df2.format(grade));
                }

                if (!courseJSON.get("examPeriodId").isNull()) {
                    JsonNode examPeriod = courseJSON.get("examPeriodId");
                    if (examPeriod.get("title") != null) {
                        String examPeriodTitle = examPeriod.get("title").asText();
                        course.setExamPeriod(examPeriodTitle);
                    }
                } else if (!courseJSON.get("homologationTypeId").isNull()) {
                    JsonNode homologationTypeId = courseJSON.get("homologationTypeId");
                    String examPeriodTitle = homologationTypeId.get("title").asText();
                    course.setExamPeriod(examPeriodTitle);
                } else {
                    course.setExamPeriod("-");
                }

                String s = courseJSON.get("idFather").asText();
                if (s.equals("null")) {
                    boolean diploma = courseJSON.get("isCountInDiploma").asBoolean();
                    if (diploma) {
                        String studentGradesId = courseJSON.get("studentGradesId").asText();
                        if (!studentGradesId.equals("null")) {
                            count++;
                            int ects = courseJSON.get("ects").asInt();
                            totalEcts += ects;

                            int semesterEcts = Integer.parseInt(semester.getEcts());
                            semesterEcts += ects;
                            semester.setEcts(String.valueOf(semesterEcts));

                            if (semester.getGradeAverage().equals("-")) {
                                semester.setGradeAverage(String.valueOf(grade));
                                semesterCount[studentSemester-1] = 1;
                            } else {
                                double semesterAverageGrade = Double.parseDouble(semester.getGradeAverage());
                                semesterAverageGrade += grade;
                                semester.setGradeAverage(String.valueOf(semesterAverageGrade));
                                semesterCount[studentSemester-1]++;
                            }
                        }
                    }
                }

                semester.getCourses().add(course);
            }

            ArrayList<Semester> found = new ArrayList<>();
            for (Semester semester : semesters) {
                if (semester.getId() == 0) {
                    found.add(semester);
                }
            }
            semesters.removeAll(found);

            for (int i = 0; i < semesters.size(); i++) {
                if (semesters.get(i).getGradeAverage().equals("-")) {
                    semesters.get(i).setPassedCourses(0);
                    continue;
                }
                double semesterSum = Double.parseDouble(semesters.get(i).getGradeAverage());
                semesters.get(i).setGradeAverage(df2.format(semesterSum/semesterCount[i]));
                semesters.get(i).setPassedCourses(semesterCount[i]);
            }

            grades.setTotalEcts(String.valueOf(totalEcts));
            grades.setTotalAverageGrade(totalAverageGrade);
            grades.setTotalPassedCourses(String.valueOf(count));
            grades.setSemesters(semesters);
        } catch (IOException e) {
            logger.e("parseGradesJSON threw error: " + e.getMessage(), e);
            return null;
        }

        return grades;
    }

    private ArrayList<Semester> initSemesters() {
        ArrayList<Semester> semesters = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            Semester semester = new Semester();
            ArrayList<Course> courses = new ArrayList<>();
            semester.setCourses(courses);
            semester.setEcts("0");
            semester.setGradeAverage("-");
            semesters.add(semester);
        }
        return semesters;
    }

    public Student parseInfoAndGradesJSON(String infoJSON, String gradesJSON, String totalAverageGrade) {
        Student student = new Student();

        try {
            Info info = parseInfoJSON(infoJSON);
            Grades grades = parseGradesJSON(gradesJSON, totalAverageGrade, info);

            if (info == null || grades == null) {
                return null;
            }

            int semester = grades.getSemesters().size();
            info.setSemester((semester == 0) ? "1" : String.valueOf(semester));

            student.setInfo(info);
            student.setGrades(grades);

            return student;
        } catch (Exception e) {
            logger.e("parseInfoAndGradesJSON threw error: " + e.getMessage(), e);
            return null;
        }
    }
}
