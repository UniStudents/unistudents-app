package com.unipi.students.parser;

import com.datadog.android.log.Logger;
import com.unipi.students.common.StringHelper;
import com.unipi.students.model.Course;
import com.unipi.students.model.Grades;
import com.unipi.students.model.Info;
import com.unipi.students.model.Semester;
import com.unipi.students.model.Student;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class AUAParser {
    private Exception exception;
    private String document;
    private Logger logger = new Logger.Builder()
            .setNetworkInfoEnabled(true)
            .setServiceName("com.unipi.students.parser.AUAParser")
            .setLogcatLogsEnabled(true)
            .setDatadogLogsEnabled(true)
            .setLoggerName("AUAParser")
            .build();

    private List parseInfoPage(Document infoPage) {
        List<Object> variousInfoList = new ArrayList<>();
        Info info = new Info();

        try {
            info.setAem(infoPage.select(".nicetable > tbody:nth-child(1) > tr:nth-child(1) > td:nth-child(2) > table:nth-child(1) > tbody:nth-child(1) > tr:nth-child(1) > td:nth-child(1) > strong:nth-child(1)").text());

            String[] fullName = infoPage.select(".nicetable > tbody:nth-child(1) > tr:nth-child(1) > td:nth-child(2) > table:nth-child(1) > tbody:nth-child(1) > tr:nth-child(1) > td:nth-child(2) > strong:nth-child(1)").text().split(" ");
            info.setFirstName(fullName[1]);
            info.setLastName(fullName[0]);

            info.setDepartment(infoPage.select(".nicetable > tbody:nth-child(1) > tr:nth-child(1) > td:nth-child(2) > table:nth-child(1) > tbody:nth-child(1) > tr:nth-child(2) > td:nth-child(1) > strong:nth-child(1)").text());
            info.setSemester(infoPage.select(".nicetable > tbody:nth-child(1) > tr:nth-child(4) > td:nth-child(1) > table:nth-child(1) > tbody:nth-child(1)").select("td[style]").last().text());
            String[] registrationYear = infoPage.select(".nicetable > tbody:nth-child(1) > tr:nth-child(1) > td:nth-child(2) > table:nth-child(1) > tbody:nth-child(1) > tr:nth-child(3) > td:nth-child(3) > strong:nth-child(1)").text().split("/");
            info.setRegistrationYear("ΠΡΟΓΡΑΜΜΑ ΣΠΟΥΔΩΝ " + registrationYear[registrationYear.length - 1]);

            variousInfoList.add(info);
            // Add total passed courses
            variousInfoList.add(infoPage.select(".nicetable > tbody:nth-child(1) > tr:nth-child(1) > td:nth-child(2) > table:nth-child(1) > tbody:nth-child(1) > tr:nth-child(4) > td:nth-child(2) > strong:nth-child(3)").text());
            // Add total average grade
            variousInfoList.add(infoPage.select(".nicetable > tbody:nth-child(1) > tr:nth-child(1) > td:nth-child(2) > table:nth-child(1) > tbody:nth-child(1) > tr:nth-child(4) > td:nth-child(2) > strong:nth-child(4)").text().replace(",", "."));
            // Add semester
            variousInfoList.add(Integer.parseInt(info.getSemester()));

            return variousInfoList;
        } catch (Exception e) {
            logger.e("[AUA.CUSTOM] Error: " +  e.getMessage(), e);
            setException(e);
            setDocument(infoPage.outerHtml());
            return null;
        }
    }

    private Grades parseGradesPage(Document gradesPage, String totalPassedCourses, String totalAverageGrade, Integer currentSemester) {
        DecimalFormat df2 = new DecimalFormat("#.##");
        Grades grades = new Grades();

        // Set known values
        grades.setTotalAverageGrade(totalAverageGrade);
        grades.setTotalPassedCourses(totalPassedCourses);
        grades.setTotalEcts("-");

        Semester semesterObj;

        currentSemester = (currentSemester > 10) ? 10 : currentSemester;

        try {
            // This will prevent child elements to be selected with the same tag
            Elements gradesTable = gradesPage.select(".borderAll > tbody > tr");
            for (int s = 1; s < (currentSemester + 1); s++) {

                semesterObj = new Semester();
                semesterObj.setId(s);
                int passedCourses = 0;
                int passedCoursesSum = 0;

                int courseSemester = 0;

                // Theory
                String theoryCourseId = null;
                String theoryExamPeriod;
                String theoryGrade;

                // Lab
                String labCourseId;
                String labExamPeriod;
                String labGrade;

                String courseName = null;
                Boolean isException = null;
                Boolean noWritten = false;
                for (int i = 0; i < gradesTable.size(); i++) {
                    Course theoryCourseObj = null;
                    Course labCourseObj = null;
                    Element element = gradesTable.get(i);
                    if (!element.attr("style").equals("background-color: #FE8522")) {
                        if (element.hasClass("odd") || element.hasClass("even")) {
                            Elements course = element.select("td");
                            courseSemester = Integer.parseInt(course.get(1).text());
                            if (s == courseSemester) {
                                theoryCourseId = course.get(2).text();
                                courseName = course.get(3).text();
                                isException = course.get(4).text().equals("Απαλλαγή");
                                noWritten = course.get(5).text().equals("") && course.get(4).text().equals("----");
                                if (noWritten) {
                                    theoryCourseObj = new Course();
                                    theoryCourseObj.setGrade("-");
                                    theoryCourseObj.setId(theoryCourseId);
                                    theoryCourseObj.setExamPeriod("-");
                                    theoryCourseObj.setName(courseName);
                                    semesterObj.getCourses().add(theoryCourseObj);
                                }
                            } else {
                                if (courseSemester - s > 0) {
                                    break;
                                }
                            }
                        } else {
                            if (s == courseSemester) {
                                Elements examPeriodsInfoTables = element.select("tbody").select("tr");
                                for (Element table : examPeriodsInfoTables) {
                                    Elements infoTable = table.select("td");
                                    if (!infoTable.text().equals("Δεν υπάρχει συμμετοχή σε εξετάσεις")) {
                                        if (infoTable.get(3).text().equals("Θεωρία")) {
                                            if (!isException) {
                                                theoryExamPeriod = StringHelper.removeTones(infoTable.get(2).text().toUpperCase()) + " " + infoTable.get(1).text();
                                                theoryGrade = infoTable.get(4).text().replace(",", ".");
                                            } else {
                                                theoryExamPeriod = "ΑΠΑΛΛΑΓΗ - " + infoTable.get(1).text();
                                                theoryGrade = "";
                                            }

                                            theoryCourseObj = new Course();
                                            theoryCourseObj.setGrade(theoryGrade);
                                            theoryCourseObj.setId(theoryCourseId);
                                            theoryCourseObj.setExamPeriod(theoryExamPeriod);
                                            theoryCourseObj.setName(courseName);
                                        } else {
                                            labExamPeriod = StringHelper.removeTones(infoTable.get(2).text().toUpperCase()) + " " + infoTable.get(1).text();
                                            labGrade = infoTable.get(4).text().replace(",", ".");
                                            labCourseId = theoryCourseId + " - ΕΡΓΑΣΤΗΡΙΟ";

                                            labCourseObj = new Course();
                                            labCourseObj.setGrade(labGrade);
                                            labCourseObj.setId(labCourseId);
                                            labCourseObj.setExamPeriod(labExamPeriod);
                                            labCourseObj.setName(courseName);
                                        }
                                    } else {
                                        if (!noWritten) {
                                            theoryCourseObj = new Course();
                                            theoryCourseObj.setGrade("-");
                                            theoryCourseObj.setId(theoryCourseId);
                                            theoryCourseObj.setExamPeriod("-");
                                            theoryCourseObj.setName(courseName);
                                        }
                                    }
                                }
                                if (theoryCourseObj != null) {
                                    semesterObj.getCourses().add(theoryCourseObj);
                                }
                                if (labCourseObj != null) {
                                    semesterObj.getCourses().add(labCourseObj);
                                }

                                if (!isException) {
                                    float gradeToCompute;
                                    if (labCourseObj != null) {
                                        gradeToCompute = Float.parseFloat(labCourseObj.getGrade());
                                        if (!labCourseObj.getGrade().equals("-") &&  gradeToCompute>= 5) {
                                            passedCourses++;
                                            passedCoursesSum += gradeToCompute;
                                        }
                                    }
                                    if (theoryCourseObj != null) {
                                        gradeToCompute = Float.parseFloat(theoryCourseObj.getGrade());
                                        if (!theoryCourseObj.getGrade().equals("-") && gradeToCompute >= 5) {
                                            passedCourses++;
                                            passedCoursesSum += gradeToCompute;
                                        }
                                    }
                                }
                            }
                        }
                    }
                    float gradeAverage = 0;
                    if (passedCourses != 0) {
                        gradeAverage = (float) passedCoursesSum / passedCourses;
                    }

                    semesterObj.setPassedCourses(passedCourses);
                    semesterObj.setGradeAverage((passedCourses != 0) ? df2.format(gradeAverage) : "-");
                    semesterObj.setEcts("-");
                }

                grades.getSemesters().add(semesterObj);
            }

            return grades;
        } catch (Exception e) {
            logger.e("[AUA.CUSTOM] Error: " +  e.getMessage(), e);
            setException(e);
            setDocument(gradesPage.outerHtml());
            return null;
        }
    }

    public Student parseInfoAndGradesPages(Document infoPage, Document gradesPage) {
        Student student = new Student();

        try {
            List infoPageList = parseInfoPage(infoPage);
            Info info = (Info) infoPageList.get(0);
            Grades grades = parseGradesPage(gradesPage, (String) infoPageList.get(1), (String) infoPageList.get(2), (Integer) infoPageList.get(3));

            student.setInfo(info);
            student.setGrades(grades);

            return student;
        } catch (Exception e) {
            logger.e("[AUA.CUSTOM] Error: " +  e.getMessage(), e);
            setException(e);
            setDocument(infoPage.outerHtml() + "\n\n=====\n\n" + gradesPage.outerHtml());
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
}
