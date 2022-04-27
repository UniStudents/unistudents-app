import 'dart:async';
import 'package:flutter/material.dart';
import 'package:unistudents_app/core/crypto.dart';

class Splash extends StatefulWidget {
  const Splash({Key? key}) : super(key: key);

  @override
  State<Splash> createState() => _SplashState();
}

class _SplashState extends State<Splash> {

  @override
  void initState() {
    super.initState();

    Timer(const Duration(milliseconds: 100), () async {
      await Crypto.init();
      Navigator.of(context).pushReplacementNamed('login_screen');
    });
  }

  @override
  Widget build(BuildContext context) {
    return Image.asset('assets/assets/splash.png');
  }
}