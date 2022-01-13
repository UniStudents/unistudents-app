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
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;


public class SEFParser {
    private Exception exception;
    private String document;
    HashMap<String, String> mathCourses = initMathCourses();
    HashMap<String, String> saxmCourses = initSAXMCourses();
    private final Logger logger = new Logger.Builder()
            .setNetworkInfoEnabled(true)
            .setServiceName("com.unipi.students.parser.SEFParser")
            .setLogcatLogsEnabled(true)
            .setDatadogLogsEnabled(true)
            .setLoggerName("SEFParser")
            .build();


    private Info parseInfoPage(Document infoPage) {
        Info info = new Info();

        try {
            String aem = infoPage.select("li.list-group-item:nth-child(1) > a:nth-child(2)").text();
            String firstName = infoPage.select("li.list-group-item:nth-child(3) > a:nth-child(2)").text();
            String lastName = infoPage.select("li.list-group-item:nth-child(2) > a:nth-child(2)").text();
            String semester = infoPage.select("li.list-group-item:nth-child(6) > a:nth-child(2)").text();

            String universityName = infoPage.select(".navbar-brand > b").text();
            String fullName = infoPage.select(".navbar-brand").text();
            String department = StringHelper.removeTones(fullName.replace(universityName, "").trim().toUpperCase());
            int registrationYear = getRegistrationYear(Integer.parseInt(semester));

            info.setAem(aem);
            info.setFirstName(firstName);
            info.setLastName(lastName);
            info.setDepartment(department);
            info.setSemester(semester);
            info.setRegistrationYear(String.valueOf(registrationYear));

            return info;
        } catch (Exception e) {
            logger.e("[AEGEAN.SEF] Error: " + e.getMessage(), e);
            setException(e);
            setDocument(infoPage.outerHtml());
            return null;
        }
    }

    private Grades parseGradesPage(Document gradesPage, int registrationYear) {
        DecimalFormat df2 = new DecimalFormat("#.##");
        Grades grades = new Grades();

        try {
            // Get first table
            Elements analyticalCoursesDOM = gradesPage.select("#example1 tr");
            ArrayList<Course> analyticalCourses = getAnalyticalReport(analyticalCoursesDOM);
            if (analyticalCourses == null) return null;

            // Fill first table with useful info from second table
            Elements declaredSubjectsDOM = gradesPage.select("#tab_2 > table > tbody > tr");
            ArrayList<Semester> filledAnalyticalCourses = fillAnalyticalFromDeclaredCourses(analyticalCourses, declaredSubjectsDOM, registrationYear);
            if (filledAnalyticalCourses == null) return null;

            // Get final table and fill useful info from latest table
            Elements passedCourses = gradesPage.select("#tab_3 table tr");
            ArrayList<Semester> finalCourses = getFinalCourses(filledAnalyticalCourses, analyticalCourses, passedCourses);
            if (finalCourses == null) return null;

            String totalECTS = passedCourses.last().select("td").last().text();
            int totalPassedCourses = Integer.parseInt(passedCourses.get(passedCourses.size() - 3).select("td").last().text().split(" ")[0].trim());

            grades.setTotalAverageGrade("-");
            grades.setTotalEcts(totalECTS);
            grades.setTotalPassedCourses(String.valueOf(totalPassedCourses));

            float totalAverageGrade = 0;
            float totalGradesSum = 0;
            float _totalPassedCourses = 0;
            for (Semester semester : finalCourses) {
                int semesterECTS = 0;
                int semesterPassedCourses = 0;
                float semesterAverageGrade = 0;
                float semesterGradesSum = 0;
                for (int i = 0; i < semester.getCourses().size(); i++) {
                    Course declaredCourse = semester.getCourses().get(i);
                    for (Element passedCourse : passedCourses.subList(1, passedCourses.size() - 3)) {
                        Elements passedCourseInfo = passedCourse.select("td");
                        String passedCourseId = passedCourseInfo.get(0).text();
                        boolean isSuccess = passedCourseInfo.get(11).text().equals("Επιτυχία");
                        if (declaredCourse.getId().equals(passedCourseId) && isSuccess) {
                            float passedCourseGrade = Float.parseFloat(passedCourseInfo.get(10).text());
                            boolean isCalculated = passedCourseInfo.get(7).text().equals("Ναι");
                            if (passedCourseGrade >= 5 && passedCourseGrade <= 10 && isCalculated) {
                                int passedCourseECTS = Integer.parseInt(passedCourseInfo.get(4).text());
                                semesterECTS += passedCourseECTS;
                                semesterPassedCourses++;
                                semesterGradesSum += passedCourseGrade;

                                totalGradesSum += passedCourseGrade;
                                _totalPassedCourses++;
                            }
                            declaredCourse.setGrade(String.valueOf(passedCourseGrade));
                        }
                    }
                }

                if (semesterPassedCourses > 0) {
                    semesterAverageGrade = semesterGradesSum / semesterPassedCourses;
                }

                semester.setGradeAverage(semesterPassedCourses > 0 ? df2.format(semesterAverageGrade) : "-");
                semester.setPassedCourses(semesterPassedCourses);
                semester.setEcts(String.valueOf(semesterECTS));
            }

            if (totalPassedCourses > 0) {
                totalAverageGrade = totalGradesSum / _totalPassedCourses;
            }

            grades.setTotalAverageGrade(totalPassedCourses > 0 ? df2.format(totalAverageGrade) : "-");
            grades.setSemesters(filledAnalyticalCourses);

            return grades;
        } catch (Exception e) {
            setException(e);
            setDocument(gradesPage.outerHtml() + "\n\n=====\n\n" + registrationYear);
            logger.e("[AEGEAN.SEF] Error: " + e.getMessage(), e);
            return null;
        }
    }

    public Student parseInfoAndGradesPages(Document infoPage, Document gradesPage) {
        Student student = new Student();

        try {
            Info info = parseInfoPage(infoPage);

            if (info == null) return null;

            // We have to pass registration year in order to calculate invalid semester value.
            Grades grades = parseGradesPage(gradesPage, Integer.parseInt(info.getRegistrationYear()));

            student.setInfo(info);
            student.setGrades(grades);

            return student;
        } catch (Exception e) {
            setException(e);
            setDocument(gradesPage.outerHtml() + "\n\n=====\n\n" + gradesPage.outerHtml());
            logger.e("[AEGEAN.SEF] Error: " + e.getMessage(), e);
            return null;
        }
    }

    private ArrayList<Course> getAnalyticalReport(Elements declaredSubjectsDOM) {
        ArrayList<Course> analyticalCourses = new ArrayList<>();
        ArrayList<String> insertedCourses = new ArrayList<>();

        try {
            for (int i = 1; i < declaredSubjectsDOM.size(); i++) {
                Elements course = declaredSubjectsDOM.get(i).select("td");
                String courseId = course.get(0).text();

                if (!insertedCourses.contains(courseId)) {
                    insertedCourses.add(courseId);

                    String courseName = course.get(1).text();
                    String courseGrade = course.get(4).text().equals("") ? "-" : String.valueOf(Float.parseFloat(course.get(4).text()));

                    Course courseObj = new Course();
                    courseObj.setId(courseId);
                    courseObj.setName(courseName);
                    courseObj.setGrade(courseGrade);
                    courseObj.setExamPeriod("-");
                    courseObj.setType("-");

                    analyticalCourses.add(courseObj);
                }
            }
        } catch (Exception e) {
            setException(e);
            setDocument(declaredSubjectsDOM.outerHtml());
            logger.e("[AEGEAN.SEF] Error: " + e.getMessage(), e);
            return null;
        }

        return analyticalCourses;
    }

    private ArrayList<Semester> fillAnalyticalFromDeclaredCourses(ArrayList<Course> analyticalCourses, Elements declaredSubjectsDOM, int registrationYear) {
        ArrayList<Semester> declaredSemesters = initSemesters();
        ArrayList<String> insertedCourses = new ArrayList<>();

        try {
            for (int i = declaredSubjectsDOM.size() - 1; i >= 0; i--) {
                Elements course = declaredSubjectsDOM.get(i).select("td");
                String courseId = course.get(0).text();

                if (!insertedCourses.contains(courseId)) {
                    insertedCourses.add(courseId);
                    for (int j = 0; j < analyticalCourses.size(); j++) {
                        Course analyticalCourse = analyticalCourses.get(j);
                        if (analyticalCourse.getId().equals(courseId)) {
                            String courseType = course.get(5).text();
                            String courseExamPeriod = course.get(8).text();

                            analyticalCourse.setExamPeriod(courseExamPeriod);
                            analyticalCourse.setType(courseType);

                            String semesterId;
                            if ((isSemesterValid(course.get(2).text()))) {
                                if (mathCourses.get(courseId) != null) {
                                    semesterId = mathCourses.get(courseId);
                                } else if (saxmCourses.get(courseId) != null) {
                                    semesterId = saxmCourses.get(courseId);
                                } else {
                                    semesterId = String.valueOf(Integer.parseInt(course.get(2).text()));
                                }
                            } else {
                                if (!isSemesterContainsNumbers(course.get(2).text())) {
                                    if (mathCourses.get(courseId) != null) {
                                        semesterId = mathCourses.get(courseId);
                                    } else if (saxmCourses.get(courseId) != null) {
                                        semesterId = saxmCourses.get(courseId);
                                    } else {
                                        semesterId = String.valueOf(getSemesterFromExamPeriod(courseExamPeriod, registrationYear));
                                    }
                                } else {
                                    if (mathCourses.get(courseId) != null) {
                                        semesterId = mathCourses.get(courseId);
                                    } else if (saxmCourses.get(courseId) != null) {
                                        semesterId = saxmCourses.get(courseId);
                                    } else {
                                        semesterId = String.valueOf(Integer.parseInt(course.get(2).text().split(",")[0]));
                                    }
                                }
                            }

                            Semester semester = declaredSemesters.get(Integer.parseInt(semesterId) - 1);
                            semester.getCourses().add(analyticalCourse);
                        }
                    }
                }
            }
        } catch (Exception e) {
            setException(e);
            setDocument(declaredSubjectsDOM.outerHtml());
            logger.e("[AEGEAN.SEF] Error: " + e.getMessage(), e);
            return null;
        }

        return clearSemesters(declaredSemesters);
    }

    // Clear unwanted semesters from initialization.
    private ArrayList<Semester> clearSemesters(ArrayList<Semester> semesters) {
        Iterator iterator = semesters.iterator();
        while (iterator.hasNext()) {
            Semester semester = (Semester) iterator.next();
            if (semester.getCourses().isEmpty()) {
                iterator.remove();
            }
        }

        return semesters;
    }

    private ArrayList<Semester> getFinalCourses(ArrayList<Semester> filledAnalyticalCourses, ArrayList<Course>analyticalCourses, Elements passedCourses) {
        try {
            for (int i = 0; i < analyticalCourses.size(); i++) {
                Course analyticalCourse = analyticalCourses.get(i);
                if (analyticalCourse.getType().equals("-")) {
                    for (Element passedCourse : passedCourses.subList(1, passedCourses.size() - 3)) {
                        String courseId = passedCourse.select("td").get(0).text();

                        if (analyticalCourse.getId().equals(courseId)) {
                            String courseType = passedCourse.select("td").get(2).text();
                            String examPeriod = passedCourse.select("td").get(8).text();

                            analyticalCourse.setType(courseType);
                            analyticalCourse.setExamPeriod(examPeriod);

                            String semesterId = "";
                            if (mathCourses.get(courseId) != null) {
                                semesterId = mathCourses.get(courseId);
                            } else if (saxmCourses.get(courseId) != null) {
                                semesterId = saxmCourses.get(courseId);
                            }

                            if (Integer.parseInt(semesterId) > filledAnalyticalCourses.size()) {
                                Semester newSemester = new Semester();
                                newSemester.getCourses().add(analyticalCourse);
                                newSemester.setId(Integer.parseInt(semesterId));
                                filledAnalyticalCourses.add(newSemester);
                            } else {
                                Semester semester = filledAnalyticalCourses.get(Integer.parseInt(semesterId) - 1);
                                semester.getCourses().add(analyticalCourse);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            setException(e);
            setDocument(passedCourses.outerHtml());
            logger.e("[AEGEAN.SEF] Error: " + e.getMessage(), e);
            return null;
        }

        return filledAnalyticalCourses;
    }

    // Check if semester is valid.
    private boolean isSemesterValid(String semester) {
        try {
            int _semester = Integer.parseInt(semester);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    // Check if semester value is in "01,02" format.
    private boolean isSemesterContainsNumbers(String semester) {
        return semester.matches("\\d\\d,\\d\\d");
    }

    // Initialize semesters.
    private ArrayList<Semester> initSemesters() {
        Semester[] semesters = new Semester[12];
        for (int i = 1; i <= 12; i++) {
            semesters[i - 1] = new Semester();
            semesters[i - 1].setId(i);
            semesters[i - 1].setPassedCourses(0);
            semesters[i - 1].setGradeAverage("-");
            semesters[i - 1].setCourses(new ArrayList<>());
        }
        return new ArrayList<>(Arrays.asList(semesters));
    }

    // Get registration year from the first course declaration.
    private int getRegistrationYear(int currentSemester) {
        return Calendar.getInstance().get(Calendar.YEAR) - (currentSemester / 2);
    }

    // Get semester from exam period. Necessary for some subjects with semester value: από μαθηματικό.
    private int getSemesterFromExamPeriod(String examPeriod, int registrationYear) {
        try {
            String coursePeriod = examPeriod.split(" ")[0];
            int coursePeriodNumber = ((coursePeriod.equals("Χειμερινό")) ? 1 : 0);
            int courseYear = Integer.parseInt(examPeriod.replaceAll("\\D+", "").substring(0, 4));
            int currentYear = (courseYear - registrationYear) + 1;
            return ((currentYear * 2) - coursePeriodNumber);
        } catch (Exception e) {
            logger.e("[AEGEAN.SEF] Error: " + e.getMessage(), e);
            return -1;
        }
    }

    private static HashMap<String, String> initMathCourses() {
        HashMap<String, String> mathCourses = new HashMap<>();

        // Semester: 1
        mathCourses.put("311-0012", "1");
        mathCourses.put("311-0543", "1");
        mathCourses.put("311-3900", "1");
        mathCourses.put("311-3950", "1");

        // Semester: 2
        mathCourses.put("311-0039", "2");
        mathCourses.put("311-0045", "2");
        mathCourses.put("311-0072", "2");
        mathCourses.put("311-1603", "2");

        // Semester: 3
        mathCourses.put("311-0086", "3");
        mathCourses.put("311-0194", "3");
        mathCourses.put("311-0551", "3");
        mathCourses.put("311-1653", "3");
        mathCourses.put("311-0106", "3");
        mathCourses.put("311-0186", "3");
        mathCourses.put("311-3351", "3");

        // Semester: 4
        mathCourses.put("311-0025", "4");
        mathCourses.put("311-0134", "4");
        mathCourses.put("311-0571", "4");
        mathCourses.put("311-1703", "4");
        mathCourses.put("311-0117", "4");
        mathCourses.put("311-0206", "4");
        mathCourses.put("311-0334", "4");
        mathCourses.put("311-3500", "4");

        // Semester: 5
        mathCourses.put("311-0297", "5");
        mathCourses.put("311-0562", "5");
        mathCourses.put("311-0824", "5");
        mathCourses.put("311-1051", "5");
        mathCourses.put("311-2304", "5");
        mathCourses.put("311-0926", "5");
        mathCourses.put("311-2453", "5");
        mathCourses.put("311-2653", "5");
        mathCourses.put("311-3800", "5");

        // Semester: 6
        mathCourses.put("311-0163", "6");
        mathCourses.put("311-0257", "6");
        mathCourses.put("311-3600", "6");
        mathCourses.put("311-0266", "6");
        mathCourses.put("311-0437", "6");
        mathCourses.put("311-0506", "6");
        mathCourses.put("311-1452", "6");
        mathCourses.put("311-2003", "6");
        mathCourses.put("311-2851", "6");
        mathCourses.put("311-0515", "6");
        mathCourses.put("311-0983", "6");

        // Semester: 7
        mathCourses.put("311-0224", "7");
        mathCourses.put("311-0239", "7");
        mathCourses.put("311-0832", "7");
        mathCourses.put("311-1953", "7");
        mathCourses.put("311-2353", "7");
        mathCourses.put("11-3001", "7");
        mathCourses.put("11-3002", "7");
        mathCourses.put("11-3004", "7");
        mathCourses.put("311-3251", "7");
        mathCourses.put("311-3400", "7");
        mathCourses.put("311-3551", "7");
        mathCourses.put("311-3850", "7");
        mathCourses.put("311-0359", "7");
        mathCourses.put("311-0453", "7");
        mathCourses.put("311-2554", "7");
        mathCourses.put("311-2564", "7");
        mathCourses.put("311-2752", "7");
        mathCourses.put("311-3650", "7");

        // Semester: 8
        mathCourses.put("311-0246", "8");
        mathCourses.put("311-0308", "8");
        mathCourses.put("311-0445", "8");
        mathCourses.put("311-1004", "8");
        mathCourses.put("311-2701", "8");
        mathCourses.put("311-3001", "8");
        mathCourses.put("311-3002", "8");
        mathCourses.put("311-3004", "8");
        mathCourses.put("311-3101", "8");
        mathCourses.put("311-1156", "8");
        mathCourses.put("311-1252", "8");
        mathCourses.put("311-1406", "8");
        mathCourses.put("311-2403", "8");
        mathCourses.put("311-2505", "8");
        mathCourses.put("311-2573", "8");
        mathCourses.put("311-2582", "8");
        mathCourses.put("311-2602", "8");

        return mathCourses;
    }

    private static HashMap<String, String> initSAXMCourses() {
        HashMap<String, String> saxmCourses = new HashMap<>();

        // Semester: 1
        saxmCourses.put("331-1006", "1");
        saxmCourses.put("331-1172", "1");
        saxmCourses.put("331-1108", "1");
        saxmCourses.put("331-2107", "1");
        saxmCourses.put("331-0462", "1");
        saxmCourses.put("331-04621", "1");

        // Semester: 2
        saxmCourses.put("331-2006", "2");
        saxmCourses.put("331-1164", "2");
        saxmCourses.put("331-2980", "2");
        saxmCourses.put("331-1207", "2");
        saxmCourses.put("331-1056", "2");
        saxmCourses.put("331-0510", "2");
        saxmCourses.put("331-05101", "2");

        // Semester: 3
        saxmCourses.put("331-2058", "3");
        saxmCourses.put("331-2808", "3");
        saxmCourses.put("331-3970", "3");
        saxmCourses.put("331-2256", "3");
        saxmCourses.put("331-5065", "3");
        saxmCourses.put("331-5026", "3");
        saxmCourses.put("331-4755", "3");
        saxmCourses.put("331-4257", "3");
        saxmCourses.put("331-0560", "3");
        saxmCourses.put("331-05601", "3");

        // Semester: 4
        saxmCourses.put("331-2160", "4");
        saxmCourses.put("331-2309", "4");
        saxmCourses.put("331-2408", "4");
        saxmCourses.put("331-2207", "4");
        saxmCourses.put("331-2658", "4");
        saxmCourses.put("331-4925", "4");
        saxmCourses.put("331-5056", "4");
        saxmCourses.put("331-4853", "4");

        // Semester: 5
        saxmCourses.put("331-2457", "5");
        saxmCourses.put("331-3009", "5");
        saxmCourses.put("331-3109", "5");
        saxmCourses.put("331-4057", "5");
        saxmCourses.put("331-5007", "5");
        saxmCourses.put("331-3257", "5");
        saxmCourses.put("331-5084", "5");
        saxmCourses.put("331-3957", "5");

        // Semester: 6
        saxmCourses.put("331-2711", "6");
        saxmCourses.put("331-6006", "6");
        saxmCourses.put("331-3709", "6");
        saxmCourses.put("331-3406", "6");
        saxmCourses.put("331-2757", "6");
        saxmCourses.put("331-3554", "6");
        saxmCourses.put("331-4207", "6");
        saxmCourses.put("331-3754", "6");
        saxmCourses.put("331-3508", "6");
        saxmCourses.put("331-4357", "6");
        saxmCourses.put("331-4306", "6");

        // Semester: 7
        saxmCourses.put("331-3309", "7");
        saxmCourses.put("331-4707", "7");
        saxmCourses.put("331-4157", "7");
        saxmCourses.put("331-3808", "7");
        saxmCourses.put("331-4107", "7");
        saxmCourses.put("331-7104", "7");
        saxmCourses.put("331-5102", "7");
        saxmCourses.put("331-4007", "7");
        saxmCourses.put("331-5092", "7");
        saxmCourses.put("331-3657", "7");
        saxmCourses.put("331-9302", "7");
        saxmCourses.put("331-9752", "7");
        saxmCourses.put("331-9355", "7");
        saxmCourses.put("331-9106", "7");
        saxmCourses.put("331-9703", "7");
        saxmCourses.put("331-4656", "7");
        saxmCourses.put("331-9028", "7");
        saxmCourses.put("331-7088", "7");

        // Semester: 8
        saxmCourses.put("331-4457", "8");
        saxmCourses.put("331-9205", "8");
        saxmCourses.put("331-9920", "8");
        saxmCourses.put("331-9601", "8");
        saxmCourses.put("331-3607", "8");
        saxmCourses.put("331-4965", "8");
        saxmCourses.put("331-9930", "8");
        saxmCourses.put("331-8143", "8");
        saxmCourses.put("331-3156", "8");
        saxmCourses.put("331-4408", "8");
        saxmCourses.put("331-4714", "8");
        saxmCourses.put("331-9402", "8");
        saxmCourses.put("331-4943", "8");
        saxmCourses.put("331-4557", "8");
        saxmCourses.put("331-6104", "8");
        saxmCourses.put("331-4992", "8");
        saxmCourses.put("331-9652", "8");
        saxmCourses.put("331-9154", "8");
        saxmCourses.put("331-9054", "8");
        saxmCourses.put("331-9900", "8");
        saxmCourses.put("331-9027", "8");
        saxmCourses.put("331-4611", "8");
        saxmCourses.put("331-9802", "8");

        return saxmCourses;
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
