import 'dart:async';
import 'package:flutter/material.dart';

class Splash extends StatefulWidget {
  const Splash({Key? key}) : super(key: key);

  @override
  State<Splash> createState() => _SplashState();
}

class _SplashState extends State<Splash> {

  @override
  void initState() {
    super.initState();

    // TODO - Check user files validity

    Timer(const Duration(milliseconds: 2000),
            () => Navigator.of(context).pushReplacementNamed('/home'));
  }

  @override
  Widget build(BuildContext context) {
    return Image.asset('assets/images/splash.png');
  }
}