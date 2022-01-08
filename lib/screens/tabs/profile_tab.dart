import 'dart:async';
import 'package:flutter/material.dart';

class ProfileTab extends StatefulWidget {
  static const String id = 'profile_tab';

  const ProfileTab({Key? key}) : super(key: key);

  @override
  State<ProfileTab> createState() => _ProfileTabState();
}

class _ProfileTabState extends State<ProfileTab> {


  @override
  Widget build(BuildContext context) {
    return const Scaffold(
      body: Center(
        child: Text('Profile tab'),
      ),
    );
  }
}