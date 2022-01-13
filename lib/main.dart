import 'package:flutter/material.dart';
import 'package:unistudents_app/screens/login_screen.dart';
import 'package:unistudents_app/screens/registration_screen.dart';
import 'package:unistudents_app/screens/tabs_screen.dart';
import 'package:unistudents_app/screens/welcome_screen.dart';

import 'core/storage.dart';
import 'models/progress_model.dart';

void main() {
  runApp(const MyApp());

  ProgressModel account = ProgressModel("Test", "test", "uniwa");
  Storage.saveProgress(account).then((value) {
    if(!value) {
      print("Failed to save");
      return;
    }

    Storage.readProgress().then((value) {
      if(value == null) {
        print("Failed to read");
        return;
      }

      print('test');
    });

  });



  // HttpAPI.requestProgress(account, false).then((value) {
  //   final text = account.toJSON();
  //   final tmp = ProgressModel.parseWhole(text);
  //   print(tmp);
  // });

  // HttpAPI.News.getArticles(["unipi.gr", "ds.unipi.gr"],
  //   pageSize: 5,
  //     beforeIds: ["61728d126f21a80af16f5191", "none"]
  // ).then((value) {
  //   print("here");
  // });



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
