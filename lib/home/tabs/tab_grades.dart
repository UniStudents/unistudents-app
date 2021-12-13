import 'dart:async';
import 'package:flutter/material.dart';

class TabGrades extends StatefulWidget {
  const TabGrades({Key? key}) : super(key: key);

  @override
  State<TabGrades> createState() => _TabGradesState();
}

class _TabGradesState extends State<TabGrades> {


  @override
  Widget build(BuildContext context) {
    return const Scaffold(
      body: Center(
        child: Text('Tab Grade'),
      ),
    );
  }
}