import 'package:unistudents_app/models/pa_course_model.dart';
import 'package:unistudents_app/models/pa_grades_model.dart';
import 'package:unistudents_app/models/pa_info_model.dart';
import 'dart:convert';

import 'package:unistudents_app/models/pa_semeseter_model.dart';

class ProgressAccountModel {
  late String username;
  late String password;
  late String university;
  String? cookies, system;

  late PAInfoModel info;
  late PAGradesModel grades;

  String geHerokutUrl() {
    String url = "https://unistudents-prod-1.herokuapp.com/api/student/$university";
    if(system != null) url += "/$system";
    return url;
  }

  bool assignFromHeroku(String response) {
    Map<String, dynamic> resJson = json.decode(response);
    system = resJson["system"].toString();
    cookies = resJson["cookies"].toString();

    if(resJson['student'] == null) return false;
    if(resJson['student']['info'] == null) return false;
    if(resJson['student']['grades'] == null) return false;

    var infoJson = resJson["student"]["info"];

    info = PAInfoModel();
    info.aem = infoJson["aem"].toString();
    info.firstName = infoJson["firstName"].toString();
    info.lastName = infoJson["lastName"].toString();
    info.department = infoJson["department"].toString();
    info.semester = infoJson['semester'].toString();
    info.programYear = infoJson['registrationYear'].toString();

    var gradesJson = resJson["student"]["grades"];

    grades = PAGradesModel();
    grades.passedCourses = gradesJson["totalPassedCourses"].toString();
    grades.averageGrade = gradesJson["totalAverageGrade"].toString();
    grades.ects = gradesJson["totalEcts"].toString();

    if(gradesJson["semesters"] == null) return false;

    var semesters = (gradesJson["semesters"] as List);
    grades.semesters = semesters.map((sem) {
      var semester = PASemesterModel();
      semester.id = sem['id'].toString();
      semester.passedCourses = sem['passedCourses'].toString();
      semester.averageGrade = sem['gradeAverage'].toString();
      semester.ects = sem['ects'].toString();

      if(sem['courses'] !is List) return semester;

      semester.courses = (sem['courses'] as List).map((co) {
        PACourseModel course = PACourseModel();
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