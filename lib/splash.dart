import 'dart:async';
import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:unistudents_app/providers/local_provider.dart';

class Splash extends StatefulWidget {
  const Splash({Key? key}) : super(key: key);

  @override
  State<Splash> createState() => _SplashState();
}

class _SplashState extends State<Splash> {

  @override
  void initState() {
    super.initState();

    // TODO - Check user files validity and decide if login page or how page

    Timer(const Duration(milliseconds: 2000),
            () => Navigator.of(context).pushReplacementNamed('login_screen'));
  }

  @override
  Widget build(BuildContext context) {
    return Image.asset('assets/images/splash.png');
  }
}