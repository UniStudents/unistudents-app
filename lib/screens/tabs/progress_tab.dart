import 'dart:async';
import 'package:flutter/material.dart';

class ProgressTab extends StatefulWidget {
  static const String id = 'progress_tab';

  const ProgressTab({Key? key}) : super(key: key);

  @override
  State<ProgressTab> createState() => _ProgressTabState();
}

class _ProgressTabState extends State<ProgressTab> {


  @override
  Widget build(BuildContext context) {
    return const Scaffold(
      body: Center(
        child: Text('Progress tab'),
      ),
    );
  }
}