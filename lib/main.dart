import 'package:flutter/material.dart';
import 'package:unistudents_app/screens/login_screen.dart';
import 'package:unistudents_app/screens/registration_screen.dart';
import 'package:unistudents_app/screens/tabs_screen.dart';
import 'package:unistudents_app/screens/welcome_screen.dart';

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
      debugShowCheckedModeBanner: false,
      initialRoute: WelcomeScreen.id,
      routes: {
        WelcomeScreen.id: (context) => WelcomeScreen(),
        LoginScreen.id: (context) => LoginScreen(),
        RegistrationScreen.id: (context) => RegistrationScreen(),
        TabsScreen.id: (context) => TabsScreen(),
      }
    );
  }
}
