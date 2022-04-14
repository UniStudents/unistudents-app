import 'package:flutter/material.dart';

class HomeTab extends StatefulWidget {
  static const String id = 'home_tab';

  const HomeTab({Key? key}) : super(key: key);

  @override
  State<HomeTab> createState() => _HomeTabState();
}

class _HomeTabState extends State<HomeTab> {

  @override
  Widget build(BuildContext context) {
    return const Scaffold(
      body: Center(
        child: Text('Home tab'),
      ),
    );
  }
}