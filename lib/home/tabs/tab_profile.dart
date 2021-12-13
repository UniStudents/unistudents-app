import 'dart:async';
import 'package:flutter/material.dart';

class TabProfile extends StatefulWidget {
  const TabProfile({Key? key}) : super(key: key);

  @override
  State<TabProfile> createState() => _TabProfileState();
}

class _TabProfileState extends State<TabProfile> {


  @override
  Widget build(BuildContext context) {
    return const Scaffold(
      body: Center(
        child: Text('Tab Profile'),
      ),
    );
  }
}