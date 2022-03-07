import 'package:unistudents_app/models/semester.dart';

class Progress {
  late int passedCourses;
  late String displayPassedCourses;
  late int failedCourses;
  late String displayFailedCourses;
  late double averageGrade;
  late String displayAverageGrade;
  late double weightedAverageGrade;
  late double displayWeightedAverageGrade;
  late int ects;
  late String displayEcts;
  late int credits;
  late String displayCredits;
  late List<Semester> semesters;
}