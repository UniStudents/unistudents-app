import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';
import 'package:flutter_screenutil/flutter_screenutil.dart';
import 'package:unistudents_app/core/colors.dart';
import 'package:unistudents_app/core/local/locals.dart';
import 'package:unistudents_app/models/course.dart';
import 'package:unistudents_app/models/exam_grade.dart';
import 'package:unistudents_app/widgets/web_view_stack.dart';

class GradeScreen extends StatefulWidget {
  static const String id = 'grade_screen';

  const GradeScreen({Key? key}) : super(key: key);

  @override
  _GradeScreenState createState() => _GradeScreenState();
}

class _GradeScreenState extends State<GradeScreen> {
  @override
  Widget build(BuildContext context) {
    var _isDarkMode = Theme.of(context).brightness == Brightness.dark;

    var _grade = ExamGrade(6.0, false, "6.0", "Χειμερινή", "2021-2022", "Χειμερινή");
    var course = Course("1", "N1-1010", "Μαθηματική Ανάλυση ΙΙ", "Υποχρεωτικό", "stream", "instructor",
        6, "6", 0, "0", 1.2, false, true, _grade, [_grade, _grade, _grade, _grade], []);

    return Scaffold(
      body: ListView(
        padding: EdgeInsets.only(right: 22.w, left: 22.w, top: 50.h, bottom: 37.h),
        children: [
          // Code & Title & Type
          Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text(
                course.displayCode,
                style: TextStyle(
                  fontSize: 16,
                  color: UniColors.getTextHalf(_isDarkMode),
                ),
              ),
              Text(
                course.name,
                style: TextStyle(
                  fontSize: 29,
                  color: UniColors.getText1(_isDarkMode),
                ),
              ),
              Text(
                course.type,
                style: TextStyle(
                  fontSize: 16,
                  color: UniColors.getTextHalf(_isDarkMode),
                ),
              ),
            ],
          ),

          Padding(padding: EdgeInsets.all(10.h)),

          // Circle
          Padding(
            padding: EdgeInsets.only(right: 127.w, left: 127.w),
            child: Container(
                width: 172.w,
                height: 172.h,
                color: const Color(0xFF000000)
            ),
          ),

          Padding(padding: EdgeInsets.all(25.h)),

          // ECTs & Weight - Difficulty & MO
          Row(
            children: [
              Card(
                  shape: RoundedRectangleBorder(
                    borderRadius: BorderRadius.circular(20.r),
                  ),
                  child: Padding(
                    padding: EdgeInsets.only(bottom: 18.h, left: 24.w, right: 8.w, top: 8.h),
                    child: Row(
                      children: [
                        // ECTs
                        Column(
                          children: [
                            Text(
                              course.displayEcts,
                              style: TextStyle(
                                  fontSize: 29.sp,
                                  color: UniColors.getUniBlue(_isDarkMode),
                                  fontWeight: FontWeight.w700
                              ),
                            ),
                            Padding(padding: EdgeInsets.only(top: 10.h)),
                            Text(
                              Locals.of(context)!.gradeECTs,
                              style: TextStyle(
                                  fontSize: 14.sp,
                                  color: UniColors.getTextHalf(_isDarkMode)
                              ),
                            )
                          ],
                        ),

                        Padding(padding: EdgeInsets.all(12.w)),

                        // Weight
                        Column(
                          children: [
                            Text(
                              '${course.weight}',
                              style: TextStyle(
                                  fontSize: 29.sp,
                                  color: UniColors.getUniBlue(_isDarkMode),
                                  fontWeight: FontWeight.w700
                              ),
                            ),
                            Padding(padding: EdgeInsets.only(top: 10.h)),
                            Text(
                              Locals.of(context)!.gradeWeight,
                              style: TextStyle(
                                  fontSize: 14.sp,
                                  color: UniColors.getTextHalf(_isDarkMode)
                              ),
                            )
                          ],
                        ),
                      ],
                    ),
                  )
              ),

              Padding(padding: EdgeInsets.all(5.w)),

              Card(
                  shape: RoundedRectangleBorder(
                    borderRadius: BorderRadius.circular(20.r),
                  ),
                  child: Padding(
                    padding: EdgeInsets.only(bottom: 9.h, left: 15.w, right: 11.w, top: 6.h),
                    child: Row(
                      children: [
                        // Difficulty
                        Column(
                          children: [
                            Text(
                              "2/5",
                              style: TextStyle(
                                  fontSize: 29.sp,
                                  color: UniColors.getUniBlue(_isDarkMode),
                                  fontWeight: FontWeight.w700
                              ),
                            ),
                            Padding(padding: EdgeInsets.only(top: 5.h)),
                            Text(
                              Locals.of(context)!.gradeDifficulty,
                              maxLines: 2,
                              textAlign: TextAlign.center,
                              style: TextStyle(
                                fontSize: 14.sp,
                                color: UniColors.getTextHalf(_isDarkMode),
                              ),
                            )
                          ],
                        ),

                        Padding(padding: EdgeInsets.all(12.w)),

                        // Average
                        Column(
                          children: [
                            Text(
                              '8.0',
                              style: TextStyle(
                                  fontSize: 29.sp,
                                  color: UniColors.getUniBlue(_isDarkMode),
                                  fontWeight: FontWeight.w700
                              ),
                            ),
                            Padding(padding: EdgeInsets.only(top: 5.h)),
                            Text(
                              Locals.of(context)!.gradeDepartmentAverage,
                              textAlign: TextAlign.center,
                              style: TextStyle(
                                  fontSize: 14.sp,
                                  color: UniColors.getTextHalf(_isDarkMode)
                              ),
                            )
                          ],
                        ),
                      ],
                    ),
                  )
              ),
            ],
          ),

          Padding(padding: EdgeInsets.only(top: 33.h)),

          // TODO - Hide history on empty
          // History
          Text(
            Locals.of(context)!.gradeGradeHistory,
            style: TextStyle(
              fontSize: 15,
              color: UniColors.getTextHalf(_isDarkMode),
              fontWeight: FontWeight.w700
            ),
          ),
          Padding(padding: EdgeInsets.only(top: 10.h)),
          Card(
              shape: RoundedRectangleBorder(
                borderRadius: BorderRadius.circular(20.r),
              ),
              child: Padding(
                padding: EdgeInsets.only(left: 24.w, right: 25.w, top: 18.h, bottom: 28.h),
                child: Column(
                  children: [
                    // Title
                    Row(
                      children: [
                        SizedBox(
                          width: 135.w,
                          child: Text(
                            Locals.of(context)!.gradePeriod,
                            style: TextStyle(
                                fontSize: 15.sp,
                                fontWeight: FontWeight.w700,
                                color: UniColors.getTextHalf(_isDarkMode)
                            ),
                          ),
                        ),
                        SizedBox(
                          width: 140.w,
                          child: Text(
                            Locals.of(context)!.gradeYear,
                            style: TextStyle(
                                fontSize: 15.sp,
                                fontWeight: FontWeight.w700,
                                color: UniColors.getTextHalf(_isDarkMode)
                            ),
                          ),
                        ),
                        Text(
                          Locals.of(context)!.gradeGrade,
                          style: TextStyle(
                              fontSize: 15.sp,
                              fontWeight: FontWeight.w700,
                              color: UniColors.getTextHalf(_isDarkMode)
                          ),
                        ),
                      ],
                    ),

                    Padding(padding: EdgeInsets.only(top: 12.h)),

                    // Data
                    ListView.builder(
                        itemCount: course.examGradeHistory.length,
                        shrinkWrap: true,
                        itemBuilder: (ctx, i) {
                          double paddBottom = i - 1 == course.examGradeHistory.length
                              ? 0 : 18.h;

                          return Padding(
                            padding: EdgeInsets.only(bottom: paddBottom),
                            child: Row(
                              children: [
                                SizedBox(
                                  width: 135.w,
                                  child: Text(
                                    course.examGradeHistory[i].displayPeriod,
                                    style: TextStyle(
                                        fontSize: 14.sp,
                                        fontWeight: FontWeight.w500,
                                        color: UniColors.getText1(_isDarkMode)
                                    ),
                                  ),
                                ),
                                SizedBox(
                                  width: 140.w,
                                  child: Text(
                                    course.examGradeHistory[i].academicYear,
                                    style: TextStyle(
                                        fontSize: 14.sp,
                                        fontWeight: FontWeight.w500,
                                        color: UniColors.getText1(_isDarkMode)
                                    ),
                                  ),
                                ),
                                SizedBox(
                                  width: 53.w,
                                  child: Text(
                                    course.examGradeHistory[i].displayGrade,
                                    textAlign: TextAlign.center,
                                    style: TextStyle(
                                        fontSize: 25.sp,
                                        fontWeight: FontWeight.w700,
                                        color: course.examGradeHistory[i].isPassed
                                            ? UniColors.getUniBlue(_isDarkMode)
                                            : UniColors.getRed()
                                    ),
                                  ),
                                ),
                              ],
                            ),
                          );
                        }
                    ),
                  ],
                ),
              )
          ),

          Padding(padding: EdgeInsets.only(top: 22.h)),

          // Options
          Card(
            shape: RoundedRectangleBorder(
              borderRadius: BorderRadius.circular(20.r),
            ),
            child: Padding(
              padding: EdgeInsets.only(left: 24.w, right: 36.w, top: 24.h, bottom: 22.h),
              child: Column(
                children: [
                  // Hide course
                  Row(
                    children: [
                      Flexible(
                        fit: FlexFit.tight,
                        child: Text(
                          Locals.of(context)!.gradeHideCourse,
                          style: TextStyle(
                            fontSize: 14.sp,
                            color: UniColors.getText1(_isDarkMode),
                            fontWeight: FontWeight.w700,
                          ),
                        ),
                      ),
                      Checkbox(
                        value: course.isExempted,
                        onChanged: (value) {},
                      )
                    ],
                  ),

                  Padding(padding: EdgeInsets.only(top: 22.h)),

                  // Do not calculate
                  Row(
                    children: [
                      Flexible(
                        fit: FlexFit.tight,
                        child: Text(
                          Locals.of(context)!.gradeDoNotCalculate,
                          style: TextStyle(
                            fontSize: 14.sp,
                            color: UniColors.getText1(_isDarkMode),
                            fontWeight: FontWeight.w700,
                          ),
                        ),
                      ),
                      Checkbox(
                        value: !course.isCalculated,
                        onChanged: (value) {},
                      )
                    ],
                  )
                ],
              ),
            ),
          ),

          Padding(padding: EdgeInsets.only(top: 22.h)),

          // Linked
          Card(
            shape: RoundedRectangleBorder(
              borderRadius: BorderRadius.circular(20.r),
            ),
            child: Padding(
              padding: EdgeInsets.only(top: 20.h, bottom: 20.h, left: 24.w, right: 40.w),
              child: Row(
                children: [
                  Flexible(
                    fit: FlexFit.tight,
                    child: Text(
                      Locals.of(context)!.gradeChains,
                      style: TextStyle(
                        fontSize: 14.sp,
                        color: UniColors.getText1(_isDarkMode),
                        fontWeight: FontWeight.w700,
                      ),
                    ),
                  ),

                  const Icon(Icons.keyboard_arrow_down_outlined)
                ],
              ),
            )
          ),

          Padding(padding: EdgeInsets.only(top: 22.h)),

          // Rating
          Card(
              shape: RoundedRectangleBorder(
                borderRadius: BorderRadius.circular(20.r),
              ),
              child: Padding(
                padding: EdgeInsets.only(top: 20.h, bottom: 20.h, left: 24.w, right: 40.w),
                child: Row(
                  children: [
                    Flexible(
                      fit: FlexFit.tight,
                      child: Text(
                        Locals.of(context)!.gradeReviews,
                        style: TextStyle(
                          fontSize: 14.sp,
                          color: UniColors.getText1(_isDarkMode),
                          fontWeight: FontWeight.w700,
                        ),
                      ),
                    ),

                    Icon(
                      Icons.star,
                      color: UniColors.getUniBlue(_isDarkMode),
                    )
                  ],
                ),
              )
          ),

          Padding(padding: EdgeInsets.only(top: 22.h)),

          // Report
          Card(
              shape: RoundedRectangleBorder(
                borderRadius: BorderRadius.circular(20.r),
              ),
              child: Padding(
                padding: EdgeInsets.only(top: 20.h, bottom: 20.h, left: 24.w, right: 40.w),
                child: Row(
                  children: [
                    Flexible(
                      fit: FlexFit.tight,
                      child: Text(
                        Locals.of(context)!.gradeReport,
                        style: TextStyle(
                          fontSize: 14.sp,
                          color: UniColors.getRed(),
                          fontWeight: FontWeight.w700,
                        ),
                      ),
                    ),

                    Icon(
                      Icons.arrow_forward_ios,
                      color: UniColors.getRed(),
                    ),
                  ],
                ),
              )
          ),
        ],
      ),
    );
  }

}
