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

  Course(
      this.id,
      this.displayCode,
      this.name,
      this.type,
      this.stream,
      this.instructor,
      this.ects,
      this.displayEcts,
      this.credits,
      this.displayCredits,
      this.weight,
      this.isExempted,
      this.isCalculated,
      this.latestExamGrade,
      this.examGradeHistory,
      this.subCourses);
}