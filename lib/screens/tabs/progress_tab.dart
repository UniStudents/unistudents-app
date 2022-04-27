import 'package:flutter/material.dart';
import 'package:flutter_screenutil/flutter_screenutil.dart';
import 'package:scroll_to_index/scroll_to_index.dart';
import 'package:unistudents_app/core/colors.dart';
import 'package:unistudents_app/screens/grade_screen.dart';
import 'package:unistudents_app/widgets/bottomsheets/progress_filter_bs.dart';

import 'package:unistudents_app/widgets/builders/semester_build.dart';

class ProgressTab extends StatefulWidget {
  static const String id = 'progress_tab';
  bool gotoTop;
  ProgressTab({Key? key, this.gotoTop = false}) : super(key: key);

  @override
  State<ProgressTab> createState() => _ProgressTabState();
}

class _ProgressTabState extends State<ProgressTab> {
  final AutoScrollController _scrollController = AutoScrollController();

  @override
  void dispose() {
    super.dispose();
    _scrollController.dispose();
  }

  @override
  Widget build(BuildContext context) {
    // Auto scroll to the top
    WidgetsBinding.instance!.addPostFrameCallback((_) {
        if (widget.gotoTop) {
          _scrollController.scrollToIndex(0,
              preferPosition: AutoScrollPosition.begin,
              duration: const Duration(milliseconds: 500));
        }
    });

    // Semester Selection



    // Filters


    // Semester View
    Widget semesterView = SemesterModal(
      semesterCount: 1,
      semesterAvg: "7.2",
      children: [
        SemesterItem(
          code: "N1-1010",
          name: "ΜΑΘΗΜΑΤΙΚΉ ΑΝΆΛΥΣΗ ΙΙ",
          sub: "Αντιστοίχιση με νέιο πρόγραμμα",
          grade: "6.0",
          onTap: () {
            Navigator.pushNamed(context, GradeScreen.id);
          },
          onPressed: () => ScaffoldMessenger.of(context).showSnackBar(const SnackBar(
            content: Text("ΜΑΘΗΜΑΤΙΚΉ ΑΝΆΛΥΣΗ ΙΙ"),
          )),
        ),
        SemesterItem(
          code: "N1-1010",
          name: "ΕΛΕΎΘΕΡΟ ΣΧΈΔΙΟ",
          sub: "Αντιστοίχιση με νέιο πρόγραμμα",
          grade: "7.0",
          onTap: () {
            showProgressFilterBSModal(context);
          },
          onPressed: () => ScaffoldMessenger.of(context).showSnackBar(const SnackBar(
            content: Text("ΕΛΕΎΘΕΡΟ ΣΧΈΔΙΟ"),
          )),
        ),
        SemesterItem(
          code: "ΗΥ-380",
          name: "ΑΛΓΌΡΙΘΜΟΙ ΚΑΙ ΠΟΛΥΠΛΟΚΌΤΗΤΑ",
          sub: "Αντιστοίχιση με νέιο πρόγραμμα",
          grade: "2.5",
          gradeColor: UniColors.getRed(),
          onTap: () {},
          onPressed: () => ScaffoldMessenger.of(context).showSnackBar(const SnackBar(
            content: Text("ΑΛΓΌΡΙΘΜΟΙ ΚΑΙ ΠΟΛΥΠΛΟΚΌΤΗΤΑ"),
          )),
        )
      ],
    );

    // Line padding for semesters
    var linePadding = Padding(padding: EdgeInsets.only(top: 10.h));

    return Scaffold(
        appBar: AppBar(
          title: const Text('Πρόοδος'),
        ),
        body: Column(
          children: [

            // Semesters
            SizedBox(
              height: 800.h,
              child: ListView(padding: EdgeInsets.all(20.h),
                controller: _scrollController,
                children: [
                  semesterView,
                  linePadding,
                  semesterView,
                  linePadding,
                  semesterView,
                  linePadding,
                ]
              )
            )

          ],
        )
    );
  }
}