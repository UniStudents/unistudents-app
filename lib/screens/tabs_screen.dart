import 'dart:async';
import 'package:flutter/material.dart';
import 'package:unistudents_app/screens/tabs/progress_tab.dart';
import 'package:unistudents_app/screens/tabs/home_tab.dart';
import 'package:unistudents_app/screens/tabs/news_tab.dart';
import 'package:unistudents_app/screens/tabs/profile_tab.dart';

class TabsScreen extends StatefulWidget {
  static const String id = 'tabs_screen';

  @override
  State<TabsScreen> createState() => _TabsScreenState();
}

class _TabsScreenState extends State<TabsScreen> {
  int navbarIndex = 2;
  static const List<Widget> _widgetOptions = <Widget>[HomeTab(), ProgressTab(), NewsTab(), ProfileTab()];

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