package com.unipi.students.parser;


import com.datadog.android.log.Logger;
import com.unipi.students.model.Course;
import com.unipi.students.model.Grades;
import com.unipi.students.model.Info;
import com.unipi.students.model.Semester;
import com.unipi.students.model.Student;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.text.DecimalFormat;

public class PANTEIONParser {
    private Logger logger = new Logger.Builder()
            .setNetworkInfoEnabled(true)
            .setServiceName("com.unipi.students.parser.PANTEIONParser")
            .setLogcatLogsEnabled(true)
            .setDatadogLogsEnabled(true)
            .setLoggerName("PANTEIONParser")
            .build();

    public Student parseInfoAndGradesPages(Document[] pages) {
        // Student initialization
        Student student = new Student();
        Info info = new Info();
        DecimalFormat df2 = new DecimalFormat("#.##");

        try {
            // Getting info
            Elements table = pages[0].select(".infoTable");

            String aem = table.select("tr").get(0).select("#ctl00_ContentData_lblCode").text();
            String[] fullName = table.select("tr").get(0).select("#ctl00_ContentData_lblName").text().split(" ");
            String firstName = fullName[1];
            String lastName = fullName[0];
            String department = table.select("tr").get(1).select("#ctl00_ContentData_lblSection").text();
            String[] termInfo = table.select("tr").get(4).select("#ctl00_ContentData_lblTermInfo").text().split("\\*");
            String semester = termInfo[1].trim().replace("Τρέχον Εξάμηνο:", "");
            String registrationYear = termInfo[0].trim().replace("Έτος εισαγωγής:", "ΕΤΟΣ ΕΙΣΑΓΩΓΗΣ ");

            info.setAem(aem);
            info.setFirstName(firstName);
            info.setLastName(lastName);
            info.setDepartment(department);
            info.setSemester(semester);
            info.setRegistrationYear(registrationYear);

            // Setting info
            student.setInfo(info);

            Grades results = new Grades();
            Semester semesterObj;
            Course courseObj;

            int currentSemester = Integer.parseInt(semester);
            int totalPassedCourses = Integer.parseInt(pages[0].select("#ctl00_ContentData_labelLessons_DLpassed").text());
            int totalPassedCoursesSum = 0;
            int totalEcts = Integer.parseInt(pages[0].select("#ctl00_ContentData_labelUnits").text());
            float totalAverageGrade = 0;
            for (int s = 1; s < (currentSemester + 1); s++) {

                semesterObj = new Semester();
                semesterObj.setId(s);
                int passedCourses = 0;
                int passedCoursesSum = 0;
                int ects = 0;

                Elements gradesTable;

                for (int i = 0; i < pages.length; i++) {
                    if (pages[i] != null) {
                        // Getting grades table
                        gradesTable = pages[i].select("#ctl00_ContentData_grdStudLess");
                        for (Element element : gradesTable.select("tr")) {
                            if (element.hasClass("gvRowStyle") || element.hasClass("gvAlternatingRowStyle")) {
                                int courseSemester = Integer.parseInt(element.select("td").get(4).text());
                                if (courseSemester == s) {

                                    // collect course info
                                    String examPeriod = element.select("td").get(0).text();
                                    String id = element.select("td").get(2).text();
                                    String name = element.select("td").get(3).text();
                                    String courseType = element.select("td").get(5).text();
                                    int courseEcts = Integer.parseInt(element.select("td").get(6).text());
                                    String[] grade = element.select("td").get(7).text().split(" ");
                                    int gradeToCompute;
                                    if (grade.length == 2) {
                                        grade[0] = grade[1].split(":")[1];
                                        gradeToCompute = Integer.parseInt(grade[0]);
                                        examPeriod += " ΕΠΑΝ";
                                    } else {
                                        grade[0] = (!grade[0].trim().equals("")) ? grade[0].split(":")[1] : "-";
                                        gradeToCompute = (!grade[0].equals("-")) ? Integer.parseInt(grade[0]) : -1;
                                        examPeriod = (grade[0].equals("-")) ? "-" : examPeriod;
                                    }

                                    if (gradeToCompute > 10) {
                                        grade[0] = "";
                                    }

                                    courseObj = new Course();
                                    courseObj.setId(id);
                                    courseObj.setName(name);
                                    courseObj.setType(courseType);
                                    courseObj.setGrade(grade[0]);
                                    courseObj.setExamPeriod(examPeriod);
                                    semesterObj.getCourses().add(courseObj);

                                    if (gradeToCompute >= 5 && gradeToCompute <= 10) {
                                        // Calculate passed courses & ects for each semester
                                        passedCourses++;
                                        passedCoursesSum += gradeToCompute;
                                        ects += courseEcts;
                                    }
                                }
                            }
                        }
                    }
                }

                totalPassedCoursesSum += passedCoursesSum;

                float gradeAverage = 0;
                if (passedCourses != 0) {
                    gradeAverage = (float) passedCoursesSum / passedCourses;
                }

                semesterObj.setPassedCourses(passedCourses);
                semesterObj.setGradeAverage((passedCourses != 0) ? df2.format(gradeAverage) : "-");
                semesterObj.setEcts(String.valueOf(ects));
                results.getSemesters().add(semesterObj);
            }

            if (totalPassedCourses != 0)
                totalAverageGrade = (float) totalPassedCoursesSum / totalPassedCourses;

            results.setTotalPassedCourses(String.valueOf(totalPassedCourses));
            results.setTotalAverageGrade((totalPassedCourses != 0) ? df2.format(totalAverageGrade) : "-");
            results.setTotalEcts(String.valueOf(totalEcts));

            // Setting grades
            student.setGrades(results);
            return student;
        } catch (Exception e) {
            logger.e("Error: " + e.getMessage(), e);
            return null;
        }
    }
}