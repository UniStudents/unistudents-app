import 'dart:async';
import 'package:flutter/material.dart';
import 'package:unistudents_app/home/tabs/tab_grades.dart';
import 'package:unistudents_app/home/tabs/tab_home.dart';
import 'package:unistudents_app/home/tabs/tab_news.dart';
import 'package:unistudents_app/home/tabs/tab_profile.dart';

class Home extends StatefulWidget {
  const Home({Key? key}) : super(key: key);

  @override
  State<Home> createState() => _HomeState();
}

class _HomeState extends State<Home> {
  int navbarIndex = 0;
  static const List<Widget> _widgetOptions = <Widget>[TabHome(), TabGrades(), TabNews(), TabProfile()];

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      // body: _widgetOptions.elementAt(navbarIndex),
      body: IndexedStack(
        children: _widgetOptions,
        index: navbarIndex,
      ),
      bottomNavigationBar: BottomNavigationBar(
        items: const [
          BottomNavigationBarItem(icon: Icon(Icons.home), label: 'Αρχική'),
          BottomNavigationBarItem(icon: Icon(Icons.bar_chart), label: 'Πρόοδος'),
          BottomNavigationBarItem(icon: Icon(Icons.web), label: 'Νέα'),
          BottomNavigationBarItem(icon: Icon(Icons.person), label: 'Προφίλ')
        ],
        currentIndex: navbarIndex,
        selectedItemColor: Colors.blue[400],
        unselectedItemColor: Colors.grey[400],
        onTap: (int index) {
          setState(() {navbarIndex = index;});
        },
      ),
    );
  }
}