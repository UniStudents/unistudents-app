import 'exam_grade.dart';

class Course {
  late String id;
  late String displayCode;
  late String name;
  late String type;
  late String stream;
  late String instructor;
  late int ects;
  late String displayEcts;
  late int credits;
  late String displayCredits;
  late double weight;
  late bool isExempted;
  late bool isCalculated;
  late ExamGrade latestExamGrade;
  late List<ExamGrade> examGradeHistory;
  late List<Course> subCourses;
}