import 'package:flutter/material.dart';
import 'package:flutter_screenutil/flutter_screenutil.dart';

import 'package:unistudents_app/widgets/builders/semester_build.dart';

class ProgressTab extends StatefulWidget {
  static const String id = 'progress_tab';
  bool gotoTop;
  ProgressTab({Key? key, this.gotoTop = false}) : super(key: key);

  @override
  State<ProgressTab> createState() => _ProgressTabState();
}

class _ProgressTabState extends State<ProgressTab> {

  int _selectedSemester = 0;

  @override
  Widget build(BuildContext context) {
    int semesters = 7;

    Widget semesterView = SemesterModal(
      semesterCount: 1,
      semesterAvg: 7.2,
      children: [
        SemesterItem(
          code: "N1-1010",
          name: "ΜΑΘΗΜΑΤΙΚΉ ΑΝΆΛΥΣΗ ΙΙ",
          sub: "Αντιστοίχιση με νέιο πρόγραμμα",
          grade: 6.0,
          onTap: () {},
        ),
        SemesterItem(
          code: "N1-1010",
          name: "ΕΛΕΎΘΕΡΟ ΣΧΈΔΙΟ",
          sub: "Αντιστοίχιση με νέιο πρόγραμμα",
          grade: 7.0,
          onTap: () {},
        ),
        SemesterItem(
          code: "ΗΥ-380",
          name: "ΑΛΓΌΡΙΘΜΟΙ ΚΑΙ ΠΟΛΥΠΛΟΚΌΤΗΤΑ",
          sub: "Αντιστοίχιση με νέιο πρόγραμμα",
          grade: 2.5,
          gradeColor: const Color(0xFFFE6D7A),
          onTap: () {},
        )
      ],
    );

    var linePadding = Padding(padding: EdgeInsets.only(top: 10.h));

    return Scaffold(
       appBar: AppBar(
        title: const Text('Πρόοδος'),
      ),
      body: ListView(padding: EdgeInsets.all(20.h),
          controller: ScrollController(),
          children: [
          semesterView,
          linePadding,
          semesterView,
          linePadding,
          semesterView,
          linePadding,
      ]));
  }
}