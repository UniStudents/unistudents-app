package com.unipi.students.parser;

import com.datadog.android.log.Logger;
import com.unipi.students.model.Course;
import com.unipi.students.model.Grades;
import com.unipi.students.model.Semester;
import com.unipi.students.model.Student;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Arrays;

public class ECEParser {
    private Exception exception;
    private String document;
    private Logger logger = new Logger.Builder()
            .setNetworkInfoEnabled(true)
            .setServiceName("com.unipi.students.parser.ECEParser")
            .setLogcatLogsEnabled(true)
            .setDatadogLogsEnabled(true)
            .setLoggerName("ECEParser")
            .build();

    public Student parseGradeDocument(Document gradeDocument) {
        Student student = new Student();
        Grades grades = initGrades();
        ArrayList<Semester> semesters = initSemesters();

        try {
            Elements elSemesters = gradeDocument.select(".card-body .courses > h5");
            Elements elList = gradeDocument.select(".card-body .courses > .courses-list");

            for (int s = 0; s < elSemesters.size(); s++) {
                String semesterId = elSemesters.get(s).text().split(" ")[1];
                int semesterIndex = Integer.parseInt(semesterId);

                Elements elCourses = elList.get(s).select("table > tbody > tr");
                for (Element elCourse: elCourses) {
                    Elements elCourseFields = elCourse.select("td");
                    Course course = new Course();
                    course.setId(elCourseFields.get(0).text());
                    course.setName(elCourseFields.get(1).text());

                    String normalGrade = elCourseFields.get(2).text().trim();
                    if (!normalGrade.contains("–")) {
                        normalGrade = normalGrade.split(" ")[0];
                        if (normalGrade.contains("Προβιβάστηκε")) normalGrade = "P";
                        if (normalGrade.contains("Απέτυχε")) normalGrade = "F";
                        course.setGrade(normalGrade);
                        course.setExamPeriod("Κανονική " + elCourseFields.get(2).select("span").first().attributes().get("title").split(" ")[2].split("-")[0]);
                    }

                    String septGrade = elCourseFields.get(3).text().trim();
                    if (!septGrade.contains("–")) {
                        septGrade = septGrade.split(" ")[0];
                        if (septGrade.contains("Προβιβάστηκε")) septGrade = "P";
                        if (septGrade.contains("Απέτυχε")) septGrade = "F";
                        course.setGrade(septGrade);
                        course.setExamPeriod("Επαναληπτική " + elCourseFields.get(3).select("span").first().attributes().get("title").split(" ")[2].split("-")[0]);
                    }

                    String extraGrade = elCourseFields.get(4).text().trim();
                    if (!extraGrade.contains("–")) {
                        extraGrade = extraGrade.split(" ")[0];
                        if (extraGrade.contains("Προβιβάστηκε")) extraGrade = "P";
                        if (extraGrade.contains("Απέτυχε")) extraGrade = "F";
                        course.setGrade(extraGrade);
                        course.setExamPeriod("Επιπλέον " + elCourseFields.get(4).select("span").first().attributes().get("title").split(" ")[2].split("-")[0]);
                    }

                    if (course.getGrade() == null)
                        course.setGrade("-");

                    semesters.get(semesterIndex - 1).getCourses().add(course);
                }
            }

            ArrayList<Semester> semestersToAdd = new ArrayList<>();
            for (Semester semester: semesters) {
                if (semester.getCourses().size() > 0)
                    semestersToAdd.add(semester);
            }

            grades.setSemesters(semestersToAdd);
            student.setGrades(grades);
            return student;
        } catch (Exception e) {
            logger.e("[NTUA.ECE] Error: " + e.getMessage(), e);
            setException(e);
            setDocument(gradeDocument.outerHtml());
            return null;
        }
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
