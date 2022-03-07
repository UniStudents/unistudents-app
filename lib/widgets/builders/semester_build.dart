import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';
import 'package:flutter_screenutil/flutter_screenutil.dart';
import 'package:unistudents_app/core/local/locals.dart';
import 'package:unistudents_app/core/utils.dart';

class SemesterModal extends StatelessWidget {
  const SemesterModal({Key? key, required this.semesterCount, required this.semesterAvg, required this.children})
      : super(key: key);

  final int semesterCount;
  final double semesterAvg;
  final List<SemesterItem> children;

  @override
  Widget build(BuildContext context) {
    var _isDarkMode = Theme.of(context).brightness == Brightness.dark;

    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Row(
          children: [
            Flexible(
                fit: FlexFit.tight,
                child: Text('${Utils.ordinalSuffix(context, semesterCount)} ${Locals.of(context)!.progressSemester}',
                    style: TextStyle(
                      fontSize: 12.sp,
                      fontWeight: FontWeight.w700,
                    )
                )
            ),

            Text('${Locals.of(context)!.progressSemesterAvg}: $semesterAvg',
                style: TextStyle(
                  fontSize: 12.sp,
                  fontWeight: FontWeight.w700,
                )
            )
          ],
        ),
        Padding(padding: EdgeInsets.only(top: 10.h)),
        Card(
          elevation: 0,
          shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(10.r)),
          child: Column(
            children: children.map((e) => Padding(
              padding: EdgeInsets.only(top: 20.h, bottom: 20.h),
              child: e,
            )).toList(),
          ),
        ),

        Padding(padding: EdgeInsets.only(top: 18.h)),
      ],
    );
  }
}

class SemesterItem extends StatelessWidget {
  SemesterItem( {Key? key,
    required this.code,
    required this.name,
    required this.sub,
    required this.grade,
    required this.onTap,
    this.gradeColor
  }) : super(key: key);

  final String code, name, sub;
  final String grade;
  Color? gradeColor;
  final VoidCallback onTap;

  @override
  Widget build(BuildContext context) {
    var _isDarkMode = Theme.of(context).brightness == Brightness.dark;
    gradeColor ??= const Color(0xFF4388FF);

    return GestureDetector(
        onTap: onTap,
        behavior: HitTestBehavior.translucent,
        child: Container(
          padding: EdgeInsets.only(left: 15.5.w, right: 39.w),
          child: Row(
            children: [
              // Subject
              Flexible(
                  fit: FlexFit.tight,
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(
                        code,
                        style: TextStyle(
                            fontSize: 12.sp,
                            color: _isDarkMode
                                ? Colors.white60
                                : Colors.black54
                        ),
                      ),

                      Padding(padding: EdgeInsets.only(top: 7.5.h)),

                      Text(
                        name,
                        style: TextStyle(
                          fontSize: 16.sp,
                          fontWeight: FontWeight.w700,
                          overflow: TextOverflow.ellipsis,
                        ),
                      ),

                      Padding(padding: EdgeInsets.only(top: 8.5.h)),

                      Text(
                        sub,
                        style: TextStyle(
                            fontSize: 12.sp,
                            color: _isDarkMode
                                ? Colors.white60
                                : Colors.black54
                        ),
                      )
                    ],
                  ),
              ),

              Padding(padding: EdgeInsets.only(left: 25.w)),

              // Grade
              Text(
                grade,
                style: TextStyle(
                    fontSize: 30.sp,
                    fontWeight: FontWeight.w700,
                    color: gradeColor
                ),
              ),

            ],
          ),
        ));
  }
}
