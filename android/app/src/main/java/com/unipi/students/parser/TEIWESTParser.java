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
import java.util.Iterator;

public class TEIWESTParser {
    private Exception exception;
    private String document;
    private final String PRE_LOG;
    private Logger logger = new Logger.Builder()
            .setNetworkInfoEnabled(true)
            .setServiceName("com.unipi.students.parser.TEIWESTParser")
            .setLogcatLogsEnabled(true)
            .setDatadogLogsEnabled(true)
            .setLoggerName("TEIWESTParser")
            .build();


    public TEIWESTParser(String university) {
        this.PRE_LOG = "[" + university + ".TEIWEST]";
    }

    private Info parseInfoPage(Document infoPage) {
        Info info = new Info();

        try {

            String aem = infoPage.select("#ctl00_FooterPane_DepartmentUser").text();
            aem = aem.substring(aem.indexOf("AM:") + "AM:".length(), aem.indexOf("Τμήμα:")).trim();
            String firstName = infoPage.select("#ctl00_MainPane_Content_StudentInfoFormLayout_FirstNameLabel").text();
            String lastName = infoPage.select("#ctl00_MainPane_Content_StudentInfoFormLayout_LastNameLabel").text();
            String department = infoPage.select("#ctl00_MainPane_Content_StudentInfoFormLayout_DepartmentLabel").text();
            String registrationYear = infoPage.select("#ctl00_MainPane_Content_StudentInfoFormLayout_ProgramStudyLabel").text();
            String semester = infoPage.select("#ctl00_MainPane_Content_StudentInfoFormLayout_Semester").text();

            info.setAem(aem);
            info.setFirstName(firstName);
            info.setLastName(lastName);
            info.setDepartment(department);
            info.setRegistrationYear(registrationYear);
            info.setSemester(semester);

            return info;
        } catch (Exception e) {
            logger.e(this.PRE_LOG  + " Error: " + e.getMessage(), e);
            setException(e);
            setDocument(infoPage.outerHtml());
            return null;
        }
    }

    private Grades parseGradesPage(Document gradePage) {
        Grades grades = initGrades();
        ArrayList<Semester> semesters = initSemesters();
        DecimalFormat df2 = new DecimalFormat("#.##");

        try {
            Element table = gradePage.select("#ctl00_MainPane_Content_DegreeGridView_DXMainTable").first();
            Elements rows = table.select("tr");

            double totalGradeSum = 0;
            int totalPassedCourses = 0;

            double semesterGradeSum = 0;
            int semesterPassedCourses = 0;
            Semester semester = null;
            for (Element row : rows) {
                if (row.hasClass("dxgvGroupRow_Metropolis")) {
                    String s = row.text();
                    String semesterId = s.substring(s.indexOf("ΕΞΑΜ: ") + "ΕΞΑΜ: ".length(), s.indexOf("(")).trim();
                    semester = semesters.get(parseSemesterId(semesterId) - 1);
                    semesterGradeSum = 0;
                    semesterPassedCourses = 0;
                } else if (row.hasClass("dxgvDataRow_Metropolis")) {
                    Elements els = row.select("td");
                    Course course = new Course();
                    course.setName(els.get(3).text());
                    course.setType(els.get(5).text());
                    course.setExamPeriod(els.get(1).text());
                    course.setId(course.getName().replace(" ", "").trim() +
                            course.getType().replace(" ", "").trim());

                    String grade = els.get(6).text().replace(",", ".").replace(".00", "");
                    course.setGrade(grade);

                    double gradeToCompute = Double.parseDouble(grade);
                    if (gradeToCompute >= 5) {
                        semesterGradeSum += gradeToCompute;
                        semesterPassedCourses++;

                        totalGradeSum += gradeToCompute;
                        totalPassedCourses++;
                    }

                    if (semester == null) return null;
                    semester.setPassedCourses(semesterPassedCourses);
                    semester.getCourses().add(course);

                    if (semesterPassedCourses > 0) {
                        double averageGrade = (double) Math.round((semesterGradeSum / semesterPassedCourses) * 100) / 100;
                        semester.setGradeAverage(df2.format(averageGrade));
                    } else {
                        semester.setGradeAverage("-");
                    }
                }
            }


            clearSemesters(semesters);
            grades.setSemesters(semesters);
            grades.setTotalPassedCourses(String.valueOf(totalPassedCourses));
            if (totalPassedCourses > 0) {
                double averageGrade = (double) Math.round((totalGradeSum / totalPassedCourses) * 100) / 100;
                grades.setTotalAverageGrade(df2.format(averageGrade));
            } else {
                grades.setTotalAverageGrade("-");
            }
        } catch (Exception e) {
            logger.e(this.PRE_LOG  + " Error: " + e.getMessage(), e);
            setException(e);
            setDocument(gradePage.outerHtml());
            return null;
        }

        return grades;
    }

    private ArrayList<Semester> clearSemesters(ArrayList<Semester> semesters) {
        Iterator<Semester> iterator = semesters.iterator();
        while (iterator.hasNext()) {
            Semester semester = (Semester) iterator.next();
            if (semester.getCourses().isEmpty()) {
                iterator.remove();
            }
        }

        return semesters;
    }

    private ArrayList<Semester> initSemesters() {
        Semester[] semesters = new Semester[12];
        for (int i = 1; i <= 12; i++) {
            semesters[i - 1] = new Semester();
            semesters[i - 1].setId(i);
            semesters[i - 1].setPassedCourses(0);
            semesters[i - 1].setGradeAverage("-");
            semesters[i - 1].setEcts("-");
            semesters[i - 1].setCourses(new ArrayList<>());
        }
        return new ArrayList<>(Arrays.asList(semesters));
    }

    private Grades initGrades() {
        Grades grades = new Grades();
        grades.setTotalAverageGrade("-");
        grades.setTotalEcts("-");
        grades.setTotalPassedCourses("0");
        grades.setSemesters(new ArrayList<>());
        return grades;
    }

    public Student parseInfoAndGradesDocuments(Document infoPage, Document gradesPage) {
        Student student = new Student();

        try {
            Info info = parseInfoPage(infoPage);
            Grades grades = parseGradesPage(gradesPage);

            if (info == null || grades == null) {
                return null;
            }

            student.setInfo(info);
            student.setGrades(grades);

            return student;
        } catch (Exception e) {
            logger.e(this.PRE_LOG  + " Error: " + e.getMessage(), e);
            setException(e);
            setDocument(infoPage.outerHtml() + "\n\n\n======\n\n\n" + gradesPage.outerHtml());
            return null;
        }
    }

    private int parseSemesterId(String semesterString) {
        switch (semesterString) {
            case "Α":
                return 1;
            case "Β":
                return 2;
            case "Γ":
                return 3;
            case "Δ":
                return 4;
            case "Ε":
                return 5;
            case "Ζ":
                return 6;
            case "ΣΤ":
                return 7;
            case "Η":
                return 8;
            case "Θ":
                return 9;
            case "Ι":
                return 10;
            case "Κ":
                return 11;
            case "Λ":
                return 12;
            default:
                return Integer.parseInt(semesterString);
        }
    }

    public Exception getException() {
        return exception;
    }

    public void setException(Exception exception) {
        this.exception = exception;
    }

    public String getDocument() {
        return document;
    }

    public void setDocument(String document) {
        this.document = document;
    }
}
