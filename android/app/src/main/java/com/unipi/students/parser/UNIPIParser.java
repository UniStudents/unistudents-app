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

public class UNIPIParser {
    private Logger logger = new Logger.Builder()
            .setNetworkInfoEnabled(true)
            .setServiceName("com.unipi.students.parser.UNIPIParser")
            .setLogcatLogsEnabled(true)
            .setDatadogLogsEnabled(true)
            .setLoggerName("UNIPIParser")
            .build();

    private Info parseInfoPage(Document infoPage) {
        Info info = new Info();

        try {
            int counter = 0;
            Elements table = infoPage.getElementsByAttributeValue("cellpadding", "4");
            for (Element element : table.select("tr")) {
                counter++;

                // get aem
                switch (counter) {
                    case 6:
                        info.setLastName(element.select("td").get(1).text());
                        break;
                    case 7:
                        info.setFirstName(element.select("td").get(1).text());
                        break;
                    case 8:
                        info.setAem(element.select("td").get(1).text());
                    case 9:
                        info.setDepartment(element.select("td").get(1).text());
                        break;
                    case 10:
                        info.setSemester(element.select("td").get(1).text());
                        break;
                    case 11:
                        info.setRegistrationYear(element.select("td").get(1).text());
                }
            }
            return info;
        } catch (Exception e) {
            logger.e("parseInfoPage threw error: " + e.getMessage(), e);
            return null;
        }
    }

    private Grades parseGradesPage(Document gradesPage) {
        Element elements = gradesPage.getElementById("mainTable");
        if (elements == null) return null;

        Elements table = elements.getElementsByAttributeValue("cellspacing", "0");
        if (table == null) return null;

        Grades results = new Grades();
        Semester semesterObj = null;
        Course courseObj = null;

        try {
            for (Element element : table.select("tr")) {

                // get new semester
                Elements semester = element.select("td.groupheader");
                if (semester != null) {
                    if (!semester.text().equals("")) {
                        semesterObj = new Semester();

                        // set semester id
                        int id = Integer.parseInt(semester.text().substring(semester.text().length() - 1));
                        semesterObj.setId(id);
                    }
                }

                // get courses
                Elements course = element.getElementsByAttributeValue("bgcolor", "#fafafa");
                if (!course.hasClass("grayfonts")) {
                    if (course != null) {
                        if (!course.text().equals("")) {
                            int counter = 0;
                            for (Element courseElement : course.select("td")) {
                                counter++;

                                // get course id & name
                                Elements courseName = courseElement.getElementsByAttributeValue("colspan", "2");
                                if (courseName != null) {
                                    if (!courseName.text().equals("")) {
                                        courseObj = new Course();
                                        String name = courseName.text();
                                        courseObj.setName(name.substring(name.indexOf(") ") + 2));
                                        courseObj.setId(name.substring(name.indexOf("(") + 1, name.indexOf(")")));
                                    }
                                }

                                if (counter == 3) {
                                    courseObj.setType(courseElement.text());
                                } else if (counter == 7) {
                                    courseObj.setGrade(courseElement.text().replace(",", "."));
                                } else if (counter == 8) {
                                    courseObj.setExamPeriod(courseElement.text());
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
                                    results.setTotalPassedCourses(elPassesCourses.text().substring(elPassesCourses.text().length() - 2));
                                } else {
                                    semesterObj.setPassedCourses(Integer.parseInt(elPassesCourses.text().substring(elPassesCourses.text().length() - 1)));
                                }
                            }

                            // get semester avg
                            Elements tableCell = finalInfoEl.getElementsByAttributeValue("colspan", "10");
                            if (tableCell != null) {
                                int counter = 0;
                                for (Element el : tableCell.select(".error")) {
                                    counter++;
                                    if (counter == 1) {
                                        if (results.getSemesters().contains(semesterObj)) {
                                            results.setTotalAverageGrade(el.text().replace("-", ""));
                                            if (results.getTotalAverageGrade().equals("")) {
                                                results.setTotalAverageGrade("0");
                                            }
                                        } else {
                                            semesterObj.setGradeAverage(el.text());
                                        }
                                    } else if (counter == 4) {
                                        if (results.getSemesters().contains(semesterObj)) {
                                            results.setTotalEcts(el.text());
                                        } else {
                                            semesterObj.setEcts(el.text());
                                        }
                                    }
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
            logger.e("parseGradesPages threw error: " + e.getMessage(), e);
            return null;
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
            logger.e("parseInfoAndGradesPages threw error: " + e.getMessage(), e);
            return null;
        }
    }
}
