package com.unipi.students.parser;


import com.datadog.android.log.Logger;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.unipi.students.model.Course;
import com.unipi.students.model.Grades;
import com.unipi.students.model.Info;
import com.unipi.students.model.Semester;
import com.unipi.students.model.Student;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class UNIWAYParser {
    private Exception exception;
    private String document;
    private Logger logger = new Logger.Builder()
            .setNetworkInfoEnabled(true)
            .setServiceName("com.unipi.students.parser.UNIWAYParser")
            .setLogcatLogsEnabled(true)
            .setDatadogLogsEnabled(true)
            .setLoggerName("UNIWAYParser")
            .build();

    private Info parseInfoJSON(String infoJSON) {
        Info info = new Info();

        try {
            JsonNode node = new ObjectMapper().readTree(infoJSON);

            String username = node.get("data").get("displayName").asText();
            info.setFirstName(username);

            String aem = node.get("data").get("loginName").asText();
            info.setAem(aem);

            String department = node.get("data").get("enrolments").get(0).get("department").asText();
            info.setDepartment(department);

            String year = node.get("data").get("enrolments").get(0).get("inscriptionAcYear").asText();
            info.setRegistrationYear("ΕΤΟΣ ΕΓΓΡΑΦΗΣ " + year);

            return info;
        } catch (IOException e) {
            logger.e("[UOA.UNIWAY] Error: " + e.getMessage(), e);
            setException(e);
            setDocument(infoJSON);
            return null;
        }
    }

    private Grades parseGradesJSON(String gradesJSON, String declareHistoryJSON) {
        Grades grades = new Grades();
        DecimalFormat df2 = new DecimalFormat("#.##");

        double totalGradesSum = 0;
        int totalPassedCourses = 0;
        int totalRecognizedCourses = 0;
        double totalEcts = 0;
        double[] semesterGradesSum;

        List<String> courses = new ArrayList<>();
        ArrayList<Semester> semesters = getDeclaredCourses(declareHistoryJSON);
        if (semesters == null) {
            grades.setTotalAverageGrade("-");
            grades.setTotalEcts("-");
            grades.setTotalPassedCourses("0");
            grades.setSemesters(new ArrayList<>());
            return grades;
        }

        try {
            semesterGradesSum = new double[semesters.size()];

            JsonNode node = new ObjectMapper().readTree(gradesJSON);
            JsonNode data = node.get("data");

            for (JsonNode examPeriodNode : data) {
                String examPeriod = examPeriodNode.get("description").asText();

                for (JsonNode gradeNode : examPeriodNode.get("grades")) {
                    String courseId = gradeNode.get("displayCode").asText().trim();
                    String semesterString = gradeNode.get("semester").asText();
                    int semesterId = getSemester(semesterString) - 1;
                    String grade = gradeNode.get("grade").asText().trim().replace(",", ".");
                    String ectsString = gradeNode.get("ects").asText().trim();
                    double ects = ectsString.equals("null") ? -1 : Double.parseDouble(ectsString);

                    if (grade.contains("null") && gradeNode.get("passed").asInt() == 1) {
                        Course recognizedCourse = new Course();
                        recognizedCourse.setId(courseId);
                        recognizedCourse.setName(gradeNode.get("title").asText().trim());
                        recognizedCourse.setExamPeriod(examPeriod);
                        recognizedCourse.setGrade("");
                        courses.add(courseId);
                        semesters.get(semesterId).getCourses().add(recognizedCourse);
                        totalRecognizedCourses++;
                        continue;
                    } else if (grade.contains("null") && gradeNode.get("passed").asInt() == 0) {
                        grade = "-";
                    }

                    boolean exists = false;
                    boolean founded = false;
                    for (Course course : semesters.get(semesterId).getCourses()) {
                        if (course.getId().equals(courseId)) {
                            if (course.getExamPeriod().equals("-")) {
                                course.setGrade(grade);
                                course.setExamPeriod(examPeriod);
                                courses.add(courseId);
                            } else {
                                if (course.getExamPeriod().split("-")[1].equals(examPeriod.split("-")[1])) {
                                    course.setGrade(grade);
                                    course.setExamPeriod(examPeriod);
                                } else {
                                    exists = true;
                                }
                            }
                            founded = true;
                            break;
                        }
                    }

                    boolean doesCourseExistsInMultipleSemesters = false;
                    if (!founded) {
                        Course course = new Course();
                        course.setId(courseId);
                        course.setName(gradeNode.get("title").asText().trim());
                        course.setExamPeriod(examPeriod);
                        course.setGrade(grade);

                        boolean foundCourse = false;
                        for (Semester semester : semesters) {

                            if (semester.getId()-1 == semesterId) continue;
                            for (Course duplicateCourse : semester.getCourses()) {

                                if (duplicateCourse.getId().equals(courseId)) {
                                    doesCourseExistsInMultipleSemesters = true;
                                    foundCourse = true;

                                    if (!duplicateCourse.getExamPeriod().equals("-") && (semester.getId() > semesterId + 1)) {
                                        course.setGrade(duplicateCourse.getGrade());
                                        course.setExamPeriod(duplicateCourse.getExamPeriod());
                                    } else if ((semester.getId() < semesterId + 1) && isNumeric(course.getGrade()) && Double.parseDouble(course.getGrade()) >= 5) {
                                        duplicateCourse.setGrade(course.getGrade());
                                        duplicateCourse.setExamPeriod(course.getExamPeriod());
                                        doesCourseExistsInMultipleSemesters = false;
                                    }

                                    break;
                                }
                            }
                            if (foundCourse) break;
                        }

                        courses.add(courseId);
                        semesters.get(semesterId).getCourses().add(course);
                    }

                    if (!isNumeric(grade)) continue;
                    double courseGrade = Double.parseDouble(grade);
                    Semester semester = semesters.get(semesterId);
                    if (courseGrade >= 5 && !exists && !doesCourseExistsInMultipleSemesters) {
                        int semesterPassedCourses = semester.getPassedCourses();
                        semesterGradesSum[semesterId] += courseGrade;
                        totalGradesSum += courseGrade;
                        semester.setPassedCourses(semesterPassedCourses + 1);
                        if (ects != -1)
                            totalEcts += ects;
                    }
                }
            }

            for (int i = 0; i < semesters.size(); i++) {
                Semester semester = semesters.get(i);
                int semesterPassedCourses = semester.getPassedCourses();
                totalPassedCourses += semesterPassedCourses;
                semester.setGradeAverage((semesterPassedCourses != 0) ?
                        String.valueOf(df2.format(semesterGradesSum[i] / semesterPassedCourses)) : "-");
            }

            ArrayList<Semester> semestersToReturn = new ArrayList<>();
            for (Semester semester : semesters ) {
                if (semester.getCourses().size() != 0) {
                    semestersToReturn.add(semester);
                }
            }

            grades.setSemesters(semestersToReturn);
            grades.setTotalPassedCourses(String.valueOf(totalPassedCourses + totalRecognizedCourses));
            grades.setTotalAverageGrade((totalPassedCourses != 0) ? df2.format(totalGradesSum / totalPassedCourses) : "-");
            grades.setTotalEcts((totalEcts == 0) ? "-" : df2.format(totalEcts).replace("00", ""));
            return grades;
        } catch (Exception e) {
            logger.e("[UOA.UNIWAY] Error: " + e.getMessage(), e);
            setException(e);
            setDocument(gradesJSON + "\n\n=====\n\n" + declareHistoryJSON);
            return null;
        }
    }

    private ArrayList<Semester> getDeclaredCourses(String declareHistoryJSON) {
        ArrayList<Semester> semesters = initSemesters();
        ArrayList<Course> courses = new ArrayList<>();

        try {
            JsonNode node = new ObjectMapper().readTree(declareHistoryJSON);
            JsonNode data = node.get("data");

            for (JsonNode registrationPeriod : data) {

                for (JsonNode courseNode : registrationPeriod.get("courses")) {
                    Course course = new Course();
                    course.setId(courseNode.get("displayCode").asText().trim());
                    course.setName(courseNode.get("title").asText().trim());
                    course.setGrade("-");
                    course.setExamPeriod("-");

                    boolean exists = false;
                    for (Course c : courses) {
                        if (c.getId().equals(course.getId())) {
                            exists = true;
                            break;
                        }
                    }
                    if (exists) continue;

                    int semester = getSemester(courseNode.get("semester").asText());

                    courses.add(course);
                    semesters.get(semester - 1).getCourses().add(course);
                }

            }
        } catch (Exception e) {
            logger.e("[UOA.UNIWAY] Error: " + e.getMessage(), e);
            setException(e);
            setDocument(declareHistoryJSON);
            return null;
        }

        return semesters;
    }

    private ArrayList<Semester> initSemesters() {
        Semester[] semesters = new Semester[12];
        for (int i = 1; i <= 12; i++) {
            semesters[i-1] = new Semester();
            semesters[i-1].setId(i);
            semesters[i-1].setPassedCourses(0);
            semesters[i-1].setGradeAverage("-");
            semesters[i-1].setCourses(new ArrayList<>());
        }
        return new ArrayList<>(Arrays.asList(semesters));
    }

    private int getSemester(String semesterString) {
        try {
            String semester = semesterString.trim().split(" ")[0].replace("o", "");
            if (isNumeric(semester))
                return Integer.parseInt(semester);
        } catch (Exception e) {
            logger.e("[UOA.UNIWAY] Error: " + e.getMessage() + " with semester str " + semesterString, e);
        }
        return -1;
    }

    public Student parseInfoAndGradesPages(String infoJSON, String gradesJSON, String declareHistoryJSON) {
        Student student = new Student();

        try {
            Info info = parseInfoJSON(infoJSON);
            Grades grades = parseGradesJSON(gradesJSON, declareHistoryJSON);

            if (info == null || grades == null) {
                return null;
            }

            student.setInfo(info);
            student.setGrades(grades);

            return student;
        } catch (Exception e) {
            logger.e("[UOA.UNIWAY] Error: " + e.getMessage(), e);
            setException(e);
            setDocument(infoJSON + "\n\n=====\n\n" + gradesJSON + "\n\n=====\n\n" + declareHistoryJSON);
            return null;
        }
    }

    private void setDocument(String document) {
        this.document = document;
    }

    public String getDocument() {
        return this.document;
    }

    private void setException(Exception exception) {
        this.exception = exception;
    }

    public Exception getException() {
        return exception;
    }

    private static boolean isNumeric(String strNum) {
        if (strNum == null) {
            return false;
        }
        try {
            double d = Double.parseDouble(strNum);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }
}
