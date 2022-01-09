import 'dart:convert';

class ProgressCourseModel {
  late String id;
  late String name;
  late String type;
  late String grade;
  late String period;
}

class ProgressSemesterModel {
  late String id;
  late String passedCourses;
  late String averageGrade;
  late String ects;
  late List<ProgressCourseModel> courses;
}

class ProgressGradesModel {
  late String passedCourses;
  late String averageGrade;
  late String ects;
  late List<ProgressSemesterModel> semesters;
}

class ProgressInfoModel {
  late String aem;
  late String firstName;
  late String lastName;
  late String department;
  late String semester;
  late String programYear;
}

class ProgressModel {
  String username;
  String password;
  String university;
  String? cookies, system;

  late ProgressInfoModel info;
  late ProgressGradesModel grades;

  ProgressModel(this.username, this.password, this.university);


  String geHerokuUrl(String base) {
    String url = "$base/api/student/$university";
    if(system != null) url += "/$system";
    return url;
  }

  bool assignFromHeroku(String response) {
    Map<String, dynamic> resJson = json.decode(response);
    system = resJson["system"];
    cookies = resJson["cookies"];

    if(resJson['student'] == null) return false;
    if(resJson['student']['info'] == null) return false;
    if(resJson['student']['grades'] == null) return false;
    
    var infoJson = resJson["student"]["info"];

    info = ProgressInfoModel();
    info.aem = infoJson["aem"].toString();
    info.firstName = infoJson["firstName"].toString();
    info.lastName = infoJson["lastName"].toString();
    info.department = infoJson["department"].toString();
    info.semester = infoJson['semester'].toString();
    info.programYear = infoJson['registrationYear'].toString();

    var gradesJson = resJson["student"]["grades"];

    grades = ProgressGradesModel();
    grades.passedCourses = gradesJson["totalPassedCourses"].toString();
    grades.averageGrade = gradesJson["totalAverageGrade"].toString();
    grades.ects = gradesJson["totalEcts"].toString();

    if(gradesJson["semesters"] == null) return false;

    var semesters = (gradesJson["semesters"] as List);
    grades.semesters = semesters.map((sem) {
      var semester = ProgressSemesterModel();
      semester.id = sem['id'].toString();
      semester.passedCourses = sem['passedCourses'].toString();
      semester.averageGrade = sem['gradeAverage'].toString();
      semester.ects = sem['ects'].toString();

      if(sem['courses'] !is List) return semester;

      semester.courses = (sem['courses'] as List).map((co) {
        ProgressCourseModel course = ProgressCourseModel();
        course.id = co['id'].toString();
        course.name = co['name'].toString();
        course.type = co['type'].toString();
        course.grade = co['grade'].toString();
        course.period = co['examPeriod'].toString();
        return course;
      }).toList();

      return semester;
    }).toList();

    return true;
  }

  Map<String, String?> getAuth() {
    return {
      'username': username,
      'password': password,
      'cookies': cookies
    };
  }
}