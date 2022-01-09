import 'package:flutter/material.dart';
import 'package:unistudents_app/screens/login_screen.dart';
import 'package:unistudents_app/screens/registration_screen.dart';
import 'package:unistudents_app/screens/tabs_screen.dart';
import 'package:unistudents_app/screens/welcome_screen.dart';

import 'core/api.dart';
import 'models/progress_model.dart';

void main() {
  ProgressModel account = ProgressModel("", "", "uniwa");
  HttpAPI.requestProgress(account, false).then((value) {
    final text = account.toJSON();
    final tmp = ProgressModel.parseWhole(text);
    print(tmp);
  });

  // API.News.getArticles(["unipi.gr", "ds.unipi.gr"],
  //   pageSize: 5,
  //     beforeIds: ["61728d126f21a80af16f5191", "none"]
  // ).then((value) {
  //   print("here");
  // });



  // runApp(const MyApp());
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
        WelcomeScreen.id: (context) => const WelcomeScreen(),
        LoginScreen.id: (context) => LoginScreen(),
        RegistrationScreen.id: (context) => RegistrationScreen(),
        TabsScreen.id: (context) => TabsScreen(),
      }
    );
  }
}
