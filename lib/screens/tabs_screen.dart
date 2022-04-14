import 'package:flutter/material.dart';
import 'package:unistudents_app/core/local/locals.dart';
import 'package:unistudents_app/screens/tabs/progress_tab.dart';
import 'package:unistudents_app/screens/tabs/home_tab.dart';
import 'package:unistudents_app/screens/tabs/news_tab.dart';
import 'package:unistudents_app/screens/tabs/profile_tab.dart';

class TabsScreen extends StatefulWidget {
  static const String id = 'tabs_screen';

  const TabsScreen({Key? key}) : super(key: key);

  @override
  State<TabsScreen> createState() => _TabsScreenState();
}

class _TabsScreenState extends State<TabsScreen> {
  int navbarIndex = 1;
  bool isConsecutiveTap = false;

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      // body: _widgetOptions.elementAt(navbarIndex),
      body: IndexedStack(
        children: [
          const HomeTab(),
          ProgressTab(gotoTop: isConsecutiveTap),
          NewsTab(gotoTop: isConsecutiveTap),
          const ProfileTab()
        ],
        index: navbarIndex,
      ),
      bottomNavigationBar: BottomNavigationBar(
        items: [
          BottomNavigationBarItem(icon: const Icon(Icons.home), label: Locals.of(context)!.bnvHome),
          BottomNavigationBarItem(icon: const Icon(Icons.bar_chart), label: Locals.of(context)!.bnvProgress),
          BottomNavigationBarItem(icon: const Icon(Icons.web), label: Locals.of(context)!.bnvNews),
          BottomNavigationBarItem(icon: const Icon(Icons.person), label: Locals.of(context)!.bnvProfile)
        ],
        currentIndex: navbarIndex,
        selectedItemColor: Colors.blue[400],
        unselectedItemColor: Colors.grey[400],
        onTap: (int index) {
          setState(() {
            isConsecutiveTap = index == navbarIndex;
            navbarIndex = index;
          });
        },
      ),
    );
  }
}
