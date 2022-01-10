import 'dart:convert';

class ProgressCourseModel {
  late String id;
  late String name;
  late String type;
  late String grade;
  late String period;

  Map<String, dynamic> toJSON() {
    return {
      'id': id,
      'name': name,
      'type': type,
      'grade': grade,
      'examPeriod': period,
    };
  }
}

class ProgressSemesterModel {
  late String id;
  late String passedCourses;
  late String averageGrade;
  late String ects;
  late List<ProgressCourseModel> courses;

  Map<String, dynamic> toJSON() {
    return {
      'id': id,
      'passedCourses': passedCourses,
      'gradeAverage': averageGrade,
      'ects': ects,
      'courses': courses.map((e) => e.toJSON()).toList()
    };
  }
}

class ProgressGradesModel {
  late String passedCourses;
  late String averageGrade;
  late String ects;
  late List<ProgressSemesterModel> semesters;

  Map<String, dynamic> toJSON() {
    return {
      'totalPassedCourses': passedCourses,
      'totalAverageGrade': averageGrade,
      'totalEcts': ects,
      'semesters': semesters.map((e) => e.toJSON()).toList()
    };
  }
}

class ProgressInfoModel {
  late String aem;
  late String firstName;
  late String lastName;
  late String department;
  late String semester;
  late String programYear;

  Map<String, dynamic> toJSON() {
    return {
      'aem': aem,
      'firstName': firstName,
      'lastName': lastName,
      'department': department,
      'semester': semester,
      'registrationYear': programYear
    };
  }
}

class ProgressModel {
  String username;
  String password;
  String university;
  String? system;
  Map<String, dynamic>? cookies;

  late ProgressInfoModel info;
  late ProgressGradesModel grades;

  ProgressModel(this.username, this.password, this.university);

  String geUrl(String base) {
    String url = "$base/api/student/$university";
    if(system != null) url += "/$system";
    return url;
  }

  static ProgressModel? parseWhole(String response) {
    ProgressModel model = ProgressModel("", "", "");
    bool result = model.parse(response);
    return result ? model : null;
  }

  bool parse(String response) {
    Map<String, dynamic> resJson = json.decode(response);

    username = resJson["username"] ?? username;
    password = resJson["username"] ?? password;
    university = resJson["username"] ?? university;

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
    grades.semesters = [];

    if(gradesJson["semesters"] == null) return false;

    var semesters = (gradesJson["semesters"] as List);
    grades.semesters = semesters.map((sem) {
      var semester = ProgressSemesterModel();
      semester.id = sem['id'].toString();
      semester.passedCourses = sem['passedCourses'].toString();
      semester.averageGrade = sem['gradeAverage'].toString();
      semester.ects = sem['ects'].toString();
      semester.courses = [];

      if(sem['courses'] == null) return semester;

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

  Map<String, dynamic> getAuth() {
    return {
      'username': username,
      'password': password,
      'cookies': cookies
    };
  }

  String toJSON() {
    return json.encode({
      'username': username,
      'password': password,
      'university': university,
      'system': system,
      'cookies': cookies,
      'student': {
        'info': info.toJSON(),
        'grades': grades.toJSON()
      }
    });
  }
}