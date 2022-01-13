package com.unipi.students.parser;

import com.datadog.android.log.Logger;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.unipi.students.model.Course;
import com.unipi.students.model.Grades;
import com.unipi.students.model.Info;
import com.unipi.students.model.Semester;
import com.unipi.students.model.Student;

import org.jsoup.nodes.Document;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;

public class NTUAParser {
    private Exception exception;
    private String document;
    private Logger logger = new Logger.Builder()
            .setNetworkInfoEnabled(true)
            .setServiceName("com.unipi.students.parser.NTUAParser")
            .setLogcatLogsEnabled(true)
            .setDatadogLogsEnabled(true)
            .setLoggerName("NTUAParser")
            .build();

    public Student parseJSONAndDocument(String json, Document document) {
        if (document == null)
            return parseJSON(json);

        Student student = new Student();
        Student studentCentral = parseJSON(json);
        if (studentCentral == null) return null;
        ECEParser eceParser = new ECEParser();
        Student studentECE = eceParser.parseGradeDocument(document);
        if (studentECE == null) {
            setException(eceParser.getException());
            setDocument(eceParser.getDocument());
            return null;
        }

        student.setInfo(studentCentral.getInfo());
        student.setGrades(mergeGrades(studentCentral.getGrades(), studentECE.getGrades()));
        return student;
    }

    private Student parseJSON(String json) {
        Student student = new Student();
        Info info = new Info();
        Grades grades = initGrades();
        ArrayList<Semester> semesters = initSemesters();
        DecimalFormat df2 = new DecimalFormat("#.##");

        double totalSum = 0;
        int totalPassedCourses = 0;
        int totalPassedCoursesWithoutGrades = 0;
        double[] semesterSum = new double[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        int[] semesterPassedCourses = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

        try {
            JsonNode node = new ObjectMapper().readTree(json);

            info.setAem(node.get("username").asText());
            info.setFirstName(node.get("firstname").asText());
            info.setLastName(node.get("lastname").asText());
            info.setDepartment(node.get("department").get("name").asText());
            info.setRegistrationYear("ΕΓΓΡΑΦΗ " + node.get("studdata").get("userdata").get("dateOfEntry").asText());


            JsonNode jsonNode = node.get("studdata").get("lessons");
            for (int c = jsonNode.size() - 1; c >= 0; c--) {
                Course course = new Course();
                JsonNode courseNode = jsonNode.get(c);
                course.setId(courseNode.get("code").asText());
                course.setName(courseNode.get("nameGR").asText());
                course.setGrade(courseNode.get("fgrade").asText());
                course.setExamPeriod(courseNode.get("year").asText());
                course.setType(courseNode.get("type").asText());
                String fGrade = courseNode.get("fgrade").asText().trim();
                String normalGrade = courseNode.get("gradeNormal").asText().trim();
                String iterativeGrade = courseNode.get("gradeIterative").asText().trim();
                String extraGrade = courseNode.get("gradeExtra").asText().trim();
                String examPeriod = "";
                if (!fGrade.equals("")) {
                    if (fGrade.equals(normalGrade)) {
                        examPeriod = "Κανονική ";
                    }
                    if (fGrade.equals(iterativeGrade)) {
                        examPeriod = "Επαναληπτική ";
                    }
                    if (fGrade.equals(extraGrade)) {
                        examPeriod = "Επιπλέον ";
                    }
                } else {
                    if (normalGrade.equals("-9")) {
                        fGrade = "P";
                        examPeriod = "Κανονική ";
                    } else if (normalGrade.equals("-1")) {
                        fGrade = "F";
                        examPeriod = "Κανονική ";
                    }
                    if (iterativeGrade.equals("-9")) {
                        fGrade = "P";
                        examPeriod = "Επαναληπτική ";
                    } else if (iterativeGrade.equals("-1")) {
                        fGrade = "F";
                        examPeriod = "Επαναληπτική ";
                    }
                    if (extraGrade.equals("-9")) {
                        fGrade = "P";
                        examPeriod = "Επιπλέον ";
                    } else if (extraGrade.equals("-1")) {
                        fGrade = "F";
                        examPeriod = "Επιπλέον ";
                    }
                }
                course.setGrade(fGrade);
                course.setExamPeriod(examPeriod + "" + courseNode.get("year").asText());
                int semesterIndex = Integer.parseInt(courseNode.get("semesterThatBelong").asText()) - 1;

                boolean found = false;
                for (Course semCourse: semesters.get(semesterIndex).getCourses()) {
                    if (semCourse.getId().equals(course.getId())) {
                        found = true;
                        break;
                    }
                }

                if (!found) {
                    semesters.get(semesterIndex).getCourses().add(course);
                    String sGrade = course.getGrade();
                    if (!sGrade.equals("")) {
                        if (sGrade.equals("P")) {
                            totalPassedCoursesWithoutGrades++;
                        } else if (!sGrade.equals("F")) {
                            double grade = Double.parseDouble(course.getGrade());
                            if (grade >= 5 && grade <= 10) {
                                totalSum += grade;
                                semesterSum[semesterIndex] += grade;
                                semesterPassedCourses[semesterIndex]++;
                            }
                        }
                    } else {
                        boolean isException = false;
                        for (JsonNode exceptionNode: node.get("studdata").get("exemptions")) {
                            if (exceptionNode.get("lessonCode").asText().equals(course.getId())) {
                                course.setGrade("");
                                course.setExamPeriod("Απαλλαγή");
                                isException = true;
                                totalPassedCoursesWithoutGrades++;
                                break;
                            }
                        }
                        if (!isException) course.setGrade("-");
                    }
                }
            }

            ArrayList<Semester> semestersToAdd = new ArrayList<>();
            for (int s = 0; s < semesters.size(); s++) {
                Semester semester = semesters.get(s);
                int passedCourses = semesterPassedCourses[s];
                totalPassedCourses += passedCourses;
                semester.setPassedCourses(passedCourses);
                semester.setGradeAverage((passedCourses == 0) ? "-" : df2.format(semesterSum[s] / passedCourses));
                if (semester.getCourses().size() > 0)
                    semestersToAdd.add(semester);
            }

            info.setSemester(String.valueOf(semestersToAdd.size()));
            grades.setSemesters(semestersToAdd);
            grades.setTotalAverageGrade((totalPassedCourses == 0) ? "-" : df2.format(totalSum / totalPassedCourses));
            totalPassedCourses += totalPassedCoursesWithoutGrades;
            grades.setTotalPassedCourses(String.valueOf(totalPassedCourses));

            student.setInfo(info);
            student.setGrades(grades);
            return student;
        } catch (Exception e) {
            logger.e("[NTUA] Error: " + e.getMessage(), e);
            setException(e);
            setDocument(json);
            return null;
        }
    }

    private Grades mergeGrades(Grades centralGrades, Grades eceGrades) {
        Grades grades = initGrades();
        ArrayList<Semester> semesters = initSemesters();
        DecimalFormat df2 = new DecimalFormat("#.##");
        double totalSum = 0;

        int totalPassedCourses = 0;
        int totalPassedCoursesWithoutGrades = 0;

        int cIndex = 0;
        int eIndex = 0;

        Semester centralSemester;
        Semester eceSemester;

        while (cIndex < centralGrades.getSemesters().size() &&
                eIndex < eceGrades.getSemesters().size()) {
            centralSemester = centralGrades.getSemesters().get(cIndex);
            eceSemester = eceGrades.getSemesters().get(eIndex);

            if (centralSemester.getId() == eceSemester.getId()) {
                double sum = 0;
                int passedCourses = 0;
                Semester semester = semesters.get(centralSemester.getId() - 1);
                for (Course cCourse: centralSemester.getCourses()) {
                    boolean found = false;
                    for (Course eCourse: eceSemester.getCourses()) {
                        if (cCourse.getId().equals(eCourse.getId())) {
                            String grade = eCourse.getGrade();
                            if (grade.trim().equals("-") || eCourse.getExamPeriod() == null)
                                break;
                            if (grade.trim().equals("P")) {
                                totalPassedCoursesWithoutGrades++;
                            } else if (!grade.trim().equals("F") && !grade.trim().contains("-")) {
                                double courseGradeDouble = Double.parseDouble(grade);
                                if (courseGradeDouble >= 5 && courseGradeDouble <= 10) {
                                    sum += courseGradeDouble;
                                    totalSum += courseGradeDouble;
                                    passedCourses++;
                                    totalPassedCourses++;
                                }
                            }
                            cCourse.setGrade(grade);
                            cCourse.setExamPeriod(eCourse.getExamPeriod());
                            semester.getCourses().add(cCourse);
                            found = true;
                            break;
                        }
                    }

                    if (!found) {
                        semester.getCourses().add(cCourse);
                        String grade = cCourse.getGrade();
                        if (grade.equals("P") || grade.trim().equals("")) {
                            totalPassedCoursesWithoutGrades++;
                        } else if (!grade.equals("F") && !grade.equals("-")) {
                            double courseGradeDouble = Double.parseDouble(grade);
                            if (courseGradeDouble >= 5 && courseGradeDouble <= 10) {
                                sum += courseGradeDouble;
                                totalSum += courseGradeDouble;
                                passedCourses++;
                                totalPassedCourses++;
                            }
                        }
                    }
                }
                semester.setPassedCourses(passedCourses);
                semester.setGradeAverage((passedCourses == 0) ? "-" : df2.format(sum / passedCourses));
                semesters.set(centralSemester.getId() - 1, semester);
                cIndex++;
                eIndex++;
            } else if (centralSemester.getId() < eceSemester.getId()) {
                semesters.set(centralSemester.getId() - 1, centralSemester);
                for (Course course: centralSemester.getCourses()) {
                    String courseGrade = course.getGrade();
                    if (courseGrade.equals("P") || courseGrade.trim().equals("")) {
                        totalPassedCoursesWithoutGrades++;
                    } else if (!courseGrade.equals("F") && !courseGrade.equals("-")) {
                        double courseGradeDouble = Double.parseDouble(courseGrade);
                        if (courseGradeDouble >= 5 && courseGradeDouble <= 10) {
                            totalSum += courseGradeDouble;
                            totalPassedCourses++;
                        }
                    }
                }
                cIndex++;
            } else if (centralSemester.getId() > eceSemester.getId()) {
                System.out.println("Meh??" + eceSemester.getId());
                eIndex++;
            }
        }

        for (int s = cIndex; s < centralGrades.getSemesters().size(); s++) {
            centralSemester = centralGrades.getSemesters().get(s);
            semesters.set(centralSemester.getId() - 1, centralSemester);
            for (Course course: centralSemester.getCourses()) {
                String courseGrade = course.getGrade();
                if (courseGrade.equals("P") || courseGrade.trim().equals("")) {
                    totalPassedCoursesWithoutGrades++;
                } else if (!courseGrade.equals("F") && !courseGrade.equals("-")) {
                    double courseGradeDouble = Double.parseDouble(courseGrade);
                    if (courseGradeDouble >= 5 && courseGradeDouble <= 10) {
                        totalSum += courseGradeDouble;
                        totalPassedCourses++;
                    }
                }
            }
        }

        ArrayList<Semester> semestersToAdd = new ArrayList<>();
        for (Semester semester: semesters) {
            if (semester.getCourses().size() > 0)
                semestersToAdd.add(semester);
        }

        grades.setSemesters(semestersToAdd);
        grades.setTotalPassedCourses(String.valueOf(totalPassedCourses + totalPassedCoursesWithoutGrades));
        grades.setTotalAverageGrade((totalPassedCourses == 0) ? "-" : df2.format(totalSum / totalPassedCourses));
        return grades;
    }

    private Grades initGrades() {
        Grades grades = new Grades();
        grades.setTotalAverageGrade("-");
        grades.setTotalEcts("-");
        grades.setTotalPassedCourses("0");
        grades.setSemesters(new ArrayList<>());
        return grades;
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
}