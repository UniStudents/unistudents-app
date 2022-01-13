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
import java.util.ArrayList;
import java.util.Arrays;

public class ICARUSParser {
    private Exception exception;
    private String document;
    private Logger logger = new Logger.Builder()
            .setNetworkInfoEnabled(true)
            .setServiceName("com.unipi.students.parser.ICARUSParser")
            .setLogcatLogsEnabled(true)
            .setDatadogLogsEnabled(true)
            .setLoggerName("ICARUSParser")
            .build();

    public Student parseInfoAndGradesPages(Document infoAndGradePage) {
        DecimalFormat df2 = new DecimalFormat("#.##");
        Student student = new Student();
        Grades grades = initGrades();
        Info info = new Info();

        // get some information
        try {
            String[] fullName = infoAndGradePage.select("#header_login u").text().split(" ");
            info.setFirstName(fullName[0]);
            info.setLastName(fullName[fullName.length - 1]);
            String[] moreInfo = infoAndGradePage.select("#wrapper #content #tabs-1 #stylized > h2").text().split(" ");
            info.setAem(moreInfo[2]);
            info.setRegistrationYear(moreInfo[moreInfo.length - 1]);
            info.setDepartment("Μηχανικών Πληροφοριακών και Επικοινωνιακών Συστημάτων");

            double totalSum = 0;
            int totalPassedCourses = 0;
            double[] semesterSum = new double[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
            int[] semesterPassedCourses = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

            ArrayList<Semester> semesters = initSemesters();
            Elements els = infoAndGradePage.select("#analytic_grades tbody tr");
            for (Element el : els) {
                Elements courseInfo = el.select("td");
                Course course = new Course();
                course.setId(courseInfo.get(1).text().trim());
                course.setName(courseInfo.get(2).text().trim());
                course.setGrade(courseInfo.get(3).text().trim());
                course.setExamPeriod(courseInfo.get(6).text().trim());
                String semesterId = courseInfo.get(4).text().trim();
                String status = courseInfo.get(7).text().trim();
                if (status.equals("Δε δόθηκε")) {
                    course.setGrade("-");
                    course.setExamPeriod("-");
                }

                boolean found = false;
                int semesterIndex = Integer.parseInt(semesterId) - 1;
                Semester semester = semesters.get(semesterIndex);
                for (int c = 0; c < semester.getCourses().size(); c++) {
                    Course semCourse = semester.getCourses().get(c);
                    if (semCourse.getId().equals(course.getId())) {
                        if (status.contains("Επιτυχία")) {
                            if (semCourse.getGrade().equals("-")) {
                                semester.getCourses().remove(semCourse);
                                break;
                            }
                            if (Double.parseDouble(semCourse.getGrade()) < 5) {
                                semester.getCourses().remove(semCourse);
                                break;
                            }
                        }
                        found = true;
                        break;
                    }
                }

                if (!found) {
                    semester.getCourses().add(course);
                    if (status.equals("Επιτυχία")) {
                        double grade = Double.parseDouble(course.getGrade());
                        semesterSum[semesterIndex] += grade;
                        semesterPassedCourses[semesterIndex]++;
                        totalSum += grade;
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
            grades.setTotalPassedCourses(String.valueOf(totalPassedCourses));

            student.setInfo(info);
            student.setGrades(grades);
            return student;
        } catch (Exception e) {
            logger.e("[AEGEAN.ICARUS] Error: " + e.getMessage(), e);
            setException(e);
            setDocument(infoAndGradePage.outerHtml());
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
