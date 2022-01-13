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

public class ARCHIMEDIAParser {
    private Exception exception;
    private String document;
    private final String PRE_LOG;
    private Logger logger = new Logger.Builder()
            .setNetworkInfoEnabled(true)
            .setServiceName("com.unipi.students.parser.ARCHIMEDIAParser")
            .setLogcatLogsEnabled(true)
            .setDatadogLogsEnabled(true)
            .setLoggerName("ARCHIMEDIAParser")
            .build();

    public ARCHIMEDIAParser(String university) {
        this.PRE_LOG = university;
    }

    public Student parseInfoAndGradesPages(Document infoAndGradePage) {
        DecimalFormat df2 = new DecimalFormat("#.##");
        Student student = new Student();
        Info info = new Info();

        Elements studentData;
        Elements personalData;

        try {
            studentData = infoAndGradePage.select("StudentData");
            personalData = infoAndGradePage.select("PersonalData");

            info.setAem(studentData.select("Mhtrwo").text());
            info.setFirstName(personalData.select("FirstName").text());
            info.setLastName(personalData.select("SurName").text());
            info.setDepartment(studentData.select("DeptPrg").attr("title"));

            String semester = studentData.select("TrexonEksamFoit").attr("v");
            info.setSemester(semester);
            info.setRegistrationYear(StringHelper.removeTones(studentData.select("ProgramEisagwgis").attr("title").toUpperCase()));
            student.setInfo(info);
        } catch (Exception e) {
            logger.e("[" + PRE_LOG + "] Error: " + e.getMessage(), e);
            setException(e);
            setDocument(infoAndGradePage.outerHtml());
            return null;
        }

        try {
            Elements declared = infoAndGradePage.select("DilwseisAll > R");
            ArrayList<Semester> declaredCourses = getDeclaredCourses(declared);
            if (declaredCourses == null) return null;

            Elements gradeElements = infoAndGradePage.select("Bathmologies > R");
            Grades grades = parseGrades(declaredCourses, gradeElements);
            if (grades == null) return null;

            grades.setTotalAverageGrade(df2.format(Float.parseFloat(studentData.select("ProgressInd").attr("v"))));
            grades.setTotalEcts(studentData.select("ProgressInd2").attr("v").replace(".0", ""));
            student.setGrades(grades);
        } catch (Exception e) {
            logger.e("[" + PRE_LOG + "] Error: " + e.getMessage(), e);
            setException(e);
            setDocument(infoAndGradePage.outerHtml());
            return null;
        }

        return student;
    }

    private Grades parseGrades(ArrayList<Semester> declaresSemesters, Elements gradeElements) {
        DecimalFormat df2 = new DecimalFormat("#.##");
        Grades grades = initGrades();
        ArrayList<Semester> semestersToAdd = new ArrayList<>();

        int totalPassedCourses = 0;
        double[] semesterSum = new double[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        int[] semesterPassedCourses = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

        try {
            for (int i = gradeElements.size() - 1; i >= 0; i--) {
                Element courseEl = gradeElements.get(i);
                String courseName = courseEl.select("Mathima > Document > T").text();

                String[] courseNameSplit = courseName.trim().split(" ");
                String courseId = courseNameSplit[courseNameSplit.length - 1].replace("[", "").replace("]", "");

                StringBuilder courseNameString = new StringBuilder();
                for (int k = 0; k < courseNameSplit.length - 1; k++) {
                    courseNameString.append(courseNameSplit[k]).append(" ");
                }
                courseName = courseNameString.toString().trim();

                String courseGrade = courseEl.select("Bathmos").attr("v");
                String apofasis = courseEl.select("Apofasis").text();
                String evalType = courseEl.select("EvalType").attr("v");
                courseGrade = (apofasis.contains("απαλλαγή")) ? "" : courseGrade;
                courseGrade = (evalType.contains("ap")) ? "" : courseGrade;

                String courseType = courseEl.select("KatOmType2").attr("title");

                String courseExamPeriod = courseEl.select("ExamPeriod").attr("title");
                String courseExamPeriodYear = courseEl.select("BathmologioDate").attr("v").split("-")[0];
                switch (courseExamPeriod) {
                    case "Εξετ. Ιαν":
                        courseExamPeriod = "ΙΑΝ " + courseExamPeriodYear;
                        break;
                    case "Εξετ. Ιουν":
                        courseExamPeriod = "ΙΟΥΝ " + courseExamPeriodYear;
                        break;
                    case "Εξετ. Σεπτ":
                        courseExamPeriod = "ΣΕΠΤ " + courseExamPeriodYear;
                    default:
                        break;
                }

                String semestedId = courseEl.select("StuSemester").attr("v");
                semestedId = (semestedId.equals("0") ? "1" : semestedId);

                Course course = new Course();
                course.setId(courseId);
                course.setName(courseName);
                course.setGrade(courseGrade);
                course.setType(courseType);
                course.setExamPeriod(courseExamPeriod);

                boolean found = false;
                int semesterIndex = Integer.parseInt(semestedId) - 1;
                for (int s = 0; s < declaresSemesters.size(); s++) {
                    Semester semester = declaresSemesters.get(s);
                    for (int c = 0; c < semester.getCourses().size(); c++) {
                        Course semCourse = semester.getCourses().get(c);
                        if (course.getId().equals(semCourse.getId())) {
                            if (semCourse.getGrade().equals("-")) {
                                semCourse.setGrade(course.getGrade());
                                semCourse.setExamPeriod(course.getExamPeriod());
                                semCourse.setType(course.getType());

                                if (!courseGrade.equals("")) {
                                    double grade = Double.parseDouble(courseGrade);
                                    if (grade >= 5 && grade <= 10) {
                                        semesterSum[s] += grade;
                                        semesterPassedCourses[s]++;
                                    }
                                } else {
                                    totalPassedCourses++;
                                }
                            }
                            found = true;
                            break;
                        }
                    }
                }

                if (!found) {
                    declaresSemesters.get(semesterIndex).getCourses().add(course);
                    if (!courseGrade.equals("")) {
                        double grade = Double.parseDouble(courseGrade);
                        if (grade >= 5 && grade <= 10) {
                            semesterSum[semesterIndex] += grade;
                            semesterPassedCourses[semesterIndex]++;
                        }
                    } else {
                        totalPassedCourses++;
                    }
                }
            }

            for (int index = 0; index < 12; index++) {
                Semester semester = declaresSemesters.get(index);
                int passedCourses = semesterPassedCourses[index];
                totalPassedCourses += passedCourses;
                semester.setPassedCourses(passedCourses);
                semester.setGradeAverage((passedCourses == 0) ? "-" : df2.format(semesterSum[index] / passedCourses));
                if (semester.getCourses().size() > 0)
                    semestersToAdd.add(semester);
            }
        } catch (Exception e) {
            logger.e("[" + PRE_LOG + "] Error: " + e.getMessage(), e);
            setException(e);
            setDocument(gradeElements.outerHtml());
            return null;
        }

        grades.setTotalPassedCourses(String.valueOf(totalPassedCourses));
        grades.setSemesters(semestersToAdd);
        return grades;
    }

    private ArrayList<Semester> getDeclaredCourses(Elements declared) {
        ArrayList<Semester> semesters = initSemesters();
        ArrayList<String> insertedCourses = new ArrayList<>();

        try {
            for (Element course : declared) {
                String courseName = course.select("Mathima > Document > T").text();

                String[] courseNameSplit = courseName.trim().split(" ");
                String courseId = courseNameSplit[courseNameSplit.length - 1].replace("[", "").replace("]", "");

                StringBuilder courseNameString = new StringBuilder();
                for (int i = 0; i < courseNameSplit.length - 1; i++) {
                    courseNameString.append(courseNameSplit[i]).append(" ");
                }
                courseName = courseNameString.toString().trim();

                String semesterId = course.select("StuSemester").attr("v");
                semesterId = (semesterId.equals("0")) ? "1" : semesterId;

                // check if exists
                if (!insertedCourses.contains(courseId)) {
                    insertedCourses.add(courseId);

                    Course courseObj = new Course();
                    courseObj.setId(courseId);
                    courseObj.setName(courseName);
                    courseObj.setGrade("-");
                    courseObj.setExamPeriod("-");
                    courseObj.setType("");

                    Semester semester = semesters.get(Integer.parseInt(semesterId) - 1);
                    semester.getCourses().add(courseObj);
                }
            }
        } catch (Exception e) {
            logger.e("[" + PRE_LOG + "] Error: " + e.getMessage(), e);
            setException(e);
            setDocument(declared.outerHtml());
            return null;
        }

        return semesters;
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
