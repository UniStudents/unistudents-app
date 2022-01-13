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

public class CardisoftParser {
    private Exception exception;
    private String document;
    private final String PRE_LOG;
    private Logger logger = new Logger.Builder()
            .setNetworkInfoEnabled(true)
            .setServiceName("com.unipi.students.parser.CardisoftParser")
            .setLogcatLogsEnabled(true)
            .setDatadogLogsEnabled(true)
            .setLoggerName("CardisoftParser")
            .build();

    public CardisoftParser(String university, String system) {
        this.PRE_LOG = university + (system == null ? "" : "." + system);
    }

    private Info parseInfoPage(Document infoPage) {
        Info info = new Info();

        try {
            Elements table = infoPage.getElementsByAttributeValue("cellpadding", "4");
            Elements els = table.select("tbody").get(0).children();

            info.setLastName(els.get(1).children().last().text());
            info.setFirstName(els.get(2).children().last().text());
            info.setAem(els.get(3).children().last().text());

            for (int i = 4; i < els.size(); i++) {
                if (els.get(i).children().first().text().contains("Τμήμα")) {
                    info.setDepartment(els.get(i).children().last().text());
                    info.setSemester(els.get(++i).children().last().text());
                    info.setRegistrationYear(els.get(++i).children().last().text());
                    break;
                }
            }
            return info;
        } catch (Exception e) {
            logger.e("[" + PRE_LOG + "] Error: " + e.getMessage(), e);
            setException(e);
            setDocument(infoPage.outerHtml());
            return null;
        }
    }

    private Grades parseGradesPage(Document gradesPage) {
        if (gradesPage.outerHtml().contains("Δε βρέθηκαν αποτελέσματα εξετάσεων")) {
            Grades results = new Grades();
            results.setSemesters(new ArrayList<>());
            results.setTotalEcts("0");
            results.setTotalPassedCourses("0");
            results.setTotalAverageGrade("-");
            return results;
        }

        DecimalFormat df2 = new DecimalFormat("#.##");
        double[] semesterSum = new double[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        int[] semesterPassedCourses = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

        Element elements = gradesPage.getElementById("mainTable");
        if (elements == null) return null;
        Elements table = elements.getElementsByAttributeValue("cellspacing", "0");
        if (table == null) return null;

        Grades results = new Grades();
        Semester semesterObj = null;
        Course courseObj;
        boolean foundValidSem = false;

        try {
            for (Element element : table.select("tr")) {

                // get new semester
                Elements semester = element.select("td.groupheader");
                if (semester != null) {
                    if (!semester.text().equals("")) {
                        semesterObj = new Semester();

                        // set semester id
                        String[] semString = semester.text().split(" ");
                        if (semString.length > 1) {
                            int id = parseSemesterId(semString[1]);
                            if (id == -1)
                                semesterObj = results.getSemesters().get(results.getSemesters().size() - 1);
                            else
                                semesterObj.setId(id);
                            foundValidSem = true;
                        } else {
                            continue;
                        }
                    }
                }

                if (!foundValidSem) continue;

                // get courses
                Elements course = element.getElementsByAttributeValue("bgcolor", "#fafafa");
                if (course != null) {
                    if (!course.hasClass("grayfonts")) {
                        if (!course.text().equals("")) {
                            courseObj = new Course();
                            Elements tds = course.select("td");

                            String name = tds.get(1).text();
                            courseObj.setName(name.substring(name.indexOf(") ") + 2));
                            courseObj.setId(name.substring(name.indexOf("(") + 1, name.indexOf(")")));

                            courseObj.setType(tds.get(2).text());
                            courseObj.setGrade(tds.get(tds.size() - 2).text().replace(",", "."));
                            courseObj.setExamPeriod(tds.get(tds.size() - 1).text());

                            if (!courseObj.getGrade().contains("-")) {
                                if (courseObj.getGrade().equals("ΕΠΙΤ")) {
                                    courseObj.setGrade("P");
                                } else if (courseObj.getGrade().equals("ΑΝΕΠ")) {
                                    courseObj.setGrade("F");
                                } else {
                                    double gradeD = Double.parseDouble(courseObj.getGrade());
                                    if (gradeD >= 5) {
                                        semesterSum[semesterObj.getId() - 1] += gradeD;
                                    }
                                }
                            }
                            semesterObj.getCourses().add(courseObj);
                        }
                    }
                }

                // get final info & add semester obj
                Elements finalInfo = element.select("tr.subHeaderBack");
                if (finalInfo != null) {
                    if (!finalInfo.text().equals("")) {

                        for (Element finalInfoEl : finalInfo) {

                            // get total passed courses
                            Elements elPassesCourses = finalInfoEl.getElementsByAttributeValue("colspan", "3");
                            if (elPassesCourses != null) {
                                if (results.getSemesters().contains(semesterObj)) {
                                    if (elPassesCourses.text().length() == 0) continue;
                                    results.setTotalPassedCourses(elPassesCourses.text().substring(elPassesCourses.text().length() - 2));
                                } else {
                                    int passedCourses = Integer.parseInt(elPassesCourses.text().substring(elPassesCourses.text().length() - 1));
                                    semesterObj.setPassedCourses(passedCourses);
                                    semesterPassedCourses[semesterObj.getId()-1] = passedCourses;
                                }
                            }

                            // get semester avg
                            Elements tableCell = finalInfoEl.getElementsByAttributeValue("colspan", "10");
                            if (tableCell != null) {
                                String ects;
                                String average = "";

                                Elements els = tableCell.select(".error");
                                if (tableCell.text().startsWith("ΜΟ")) {
                                    average = els.get(0).text().replace("-", "");
                                } else {
                                    int semesterIndex = semesterObj.getId()-1;
                                    average = (semesterPassedCourses[semesterIndex] == 0)
                                            ? "-"
                                            : df2.format(semesterSum[semesterIndex] / semesterPassedCourses[semesterIndex]);
                                }

                                ects = els.last().text();

                                if (results.getSemesters().contains(semesterObj)) {
                                    results.setTotalAverageGrade((average.equals("") ? "-" : average));
                                    results.setTotalEcts(ects);
                                } else {
                                    semesterObj.setGradeAverage((average.equals("") ? "-" : average));
                                    semesterObj.setEcts(ects);
                                }
                            }
                        }

                        // add semesterObj to resultsObj
                        if (!results.getSemesters().contains(semesterObj))
                            results.getSemesters().add(semesterObj);
                    }
                }
            }
            return results;
        } catch (Exception e) {
            logger.e("[" + PRE_LOG + "] Error: " + e.getMessage(), e);
            setException(e);
            setDocument(gradesPage.outerHtml());
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
            case "ΣΤ":
                return 6;
            case "Ζ":
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
            case "Χ/Ε":
            case "ΧΕΙΜ":
            case "ΕΑΡ":
                return 13;
            default:
                return Integer.parseInt(semesterString);
        }
    }

    public Student parseInfoAndGradesPages(Document infoPage, Document gradesPage) {
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
            logger.e("[" + PRE_LOG + "] Error: " + e.getMessage(), e);
            setException(e);
            setDocument(infoPage.outerHtml());
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
