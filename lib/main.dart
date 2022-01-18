import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:unistudents_app/core/local/locals.dart';
import 'package:unistudents_app/providers/local_provider.dart';
import 'package:unistudents_app/providers/news.dart';
import 'package:unistudents_app/screens/folllow_websites_screen.dart';
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
    return MultiProvider(
      providers: [
        ChangeNotifierProvider.value(value: News()),
        ChangeNotifierProvider(create: (_) => LocalProvider())
      ],
      child: Consumer<LocalProvider>(
        builder: (context, provider, snapshot) {
          return MaterialApp(
            title: 'UniStudents',
            theme: ThemeData(
              primarySwatch: Colors.blue,
              scaffoldBackgroundColor: const Color(0xffF4F4F4),
            ),
            locale: provider.locale,
            debugShowCheckedModeBanner: false,
            supportedLocales: Locals.supportedLocals,
            localizationsDelegates: Locals.localizationsDelegates,
            initialRoute: TabsScreen.id,
            routes: {
              WelcomeScreen.id: (context) => WelcomeScreen(),
              LoginScreen.id: (context) => LoginScreen(),
              RegistrationScreen.id: (context) => RegistrationScreen(),
              TabsScreen.id: (context) => TabsScreen(),
              FollowWebsitesScreen.id: (context) => FollowWebsitesScreen(),
            }
          );
        }
      ),
    );
  }
}
