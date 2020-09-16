package com.unipi.students.parser;


import com.datadog.android.log.Logger;
import com.unipi.students.common.StringHelper;
import com.unipi.students.model.*;
import com.unipi.students.service.ApiService;

import org.jsoup.nodes.DataNode;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class UOAParser {
    private Logger logger = new Logger.Builder()
            .setNetworkInfoEnabled(true)
            .setServiceName("com.unipi.students.parser.UOAParser")
            .setLogcatLogsEnabled(true)
            .setDatadogLogsEnabled(true)
            .setLoggerName("UOAParser")
            .build();

    private Info parseInfoPage(Document infoPage) {
        Info info = new Info();

        try {
            Element titleElement = infoPage.select("p.TitleText").first();
            String string = titleElement.text();
            String[] nameAndDepartment = string.split("\\(");
            String[] department = nameAndDepartment[1].split("-");

            info.setFirstName(nameAndDepartment[0]);
            info.setDepartment("ΤΜΗΜΑ " + StringHelper.removeTones(department[1].substring(0, department[1].length() - 1).toUpperCase()));

            String html = infoPage.toString();
            int semesterIndex = html.indexOf("Εξάμηνο Φοίτησης");
            info.setSemester(html.substring(semesterIndex + 19, semesterIndex + 20));

            int registrationYearIndex = html.indexOf("Ακαδημαϊκό Έτος");
            info.setRegistrationYear("ΕΤΟΣ ΕΓΓΡΑΦΗΣ " + html.substring(registrationYearIndex + 31, registrationYearIndex + 35));

            return info;
        } catch (Exception e) {
            logger.e("parseInfoPage threw error: " + e.getMessage(), e);
            return null;
        }
    }

    private Grades parseGradesPage(Document gradesPage, Document declareHistoryPage, Info info) {
        Grades grades = new Grades();
        DecimalFormat df2 = new DecimalFormat("#.##");

        double totalGradesSum = 0;
        int totalPassedCourses = 0;
        int totalRecognizedCourses = 0;
        double[] semesterGradesSum;

        List<String> courses = new ArrayList<>();
        ArrayList<Semester> semesters = getDeclaredCourses(declareHistoryPage, info);
        if (semesters == null) {
            grades.setTotalAverageGrade("-");
            grades.setTotalEcts("-");
            grades.setTotalPassedCourses("0");
            grades.setSemesters(new ArrayList<>());
            return grades;
        }

        try {
            String[] lines = null;
            Element scriptElement = gradesPage.getElementsByTag("script").first();
            for (DataNode node : scriptElement.dataNodes()) {
                lines = node.getWholeData().split(System.getProperty("line.separator"));
            }

            if (lines == null) return null;
            semesterGradesSum = new double[semesters.size()];

            for (String line : lines) {
                if (line.trim().startsWith("cAccadArray[")) {
                    String[] data = line.trim().replace("\\\'", "$QT").split("\'");
                    String courseId = data[3];

                    boolean exists = false;
                    for (String c : courses) {
                        if (c.equals(courseId)) {
                            exists = true;
                            break;
                        }
                    }
                    if (exists) continue;

                    String examPeriod = data[1].trim();
                    String[] index = data[4].trim().split(",");
                    String semesterNo = index[2].trim();
                    String grade = data[7].trim().replace(",", ".").replace("null", "");

                    int semesterId = Integer.parseInt(semesterNo) - 1;
                    for (Course course : semesters.get(semesterId).getCourses()) {
                        if (course.getId().equals(courseId)) {
                            course.setGrade(grade);
                            course.setExamPeriod(examPeriod);
                            courses.add(courseId);
                            break;
                        }
                    }

                    if (grade.equals("")) {
                        totalRecognizedCourses++;
                        continue;
                    }
                    double courseGrade = Double.parseDouble(grade);
                    Semester semester = semesters.get(semesterId);
                    if (courseGrade >= 5) {
                        int semesterPassedCourses = semester.getPassedCourses();
                        semesterGradesSum[semesterId] += courseGrade;
                        totalGradesSum += courseGrade;
                        semester.setPassedCourses(semesterPassedCourses + 1);
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
            return grades;
        } catch (Exception e) {
            logger.e("parseGradesPage threw error: " + e.getMessage(), e);
            return null;
        }
    }

    private ArrayList<Semester> getDeclaredCourses(Document declareHistoryPage, Info info) {
        ArrayList<Semester> semesters = initSemesters();
        ArrayList<Course> courses = new ArrayList<>();

        List<String> lines = new ArrayList<>();
        Scanner scanner = new Scanner(declareHistoryPage.outerHtml());
        while (scanner.hasNextLine()) {
            lines.add(scanner.nextLine());
        }

        try {
            for (String line : lines) {
                if (line.trim().startsWith("cDeclareArray[")) {
                    Course course = new Course();

                    String[] data = line.trim().replace("\\\'", "$QT").split("\'");
                    String courseId = data[3].trim();
                    String[] index = data[4].trim().split(",");
                    String semester = index[2].trim();
                    String name = data[5].trim().replaceAll("\\s{2,}", " ");

                    boolean exists = false;
                    for (Course c : courses) {
                        if (c.getId().equals(courseId)) {
                            exists = true;
                            break;
                        }
                    }
                    if (exists) continue;

                    course.setId(courseId);
                    course.setName(name.replace("$QT", "'"));
                    course.setGrade("-");
                    course.setExamPeriod("-");
                    courses.add(course);
                    semesters.get(Integer.parseInt(semester) - 1).getCourses().add(course);
                }
            }
        } catch (Exception e) {
            logger.e("Error: " + e.getMessage(), e);
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

    public Student parseInfoAndGradesPages(Document infoPage, Document gradesPage, Document declareHistoryPage) {
        Student student = new Student();

        try {
            Info info = parseInfoPage(infoPage);
            Grades grades = parseGradesPage(gradesPage, declareHistoryPage, info);

            if (info == null || grades == null) {
                return null;
            }

            student.setInfo(info);
            student.setGrades(grades);

            return student;
        } catch (Exception e) {
            logger.e("parseInfoAndGradesPages threw error: " + e.getMessage(), e);
            return null;
        }
    }
}
