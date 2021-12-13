import 'package:flutter/material.dart';
import 'package:unistudents_app/splash.dart';
import 'home/home.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({Key? key}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'UniStudents',
      theme: ThemeData(
        primarySwatch: Colors.blue,
      ),
      home: const Splash(),
      debugShowCheckedModeBanner: false,

      // Change activity - Navigator.of(context).pushReplacementNamed(route) (e.x. route = '/splash')
      routes: <String, WidgetBuilder> {
        '/splash': (BuildContext context) => const Splash(),
        '/home': (BuildContext context) => const Home(),
      }
    );
  }
}
