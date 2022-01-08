import 'dart:async';

import 'package:flutter/material.dart';
import 'package:unistudents_app/widgets/menu.dart';
import 'package:unistudents_app/widgets/navigation_controls.dart';
import 'package:unistudents_app/widgets/web_view_stack.dart';
import 'package:webview_flutter/webview_flutter.dart';

class WelcomeScreen extends StatefulWidget {
  static const String id = 'welcome_screen';

  const WelcomeScreen({Key? key}) : super(key: key);

  @override
  _WelcomeScreenState createState() => _WelcomeScreenState();
}

class _WelcomeScreenState extends State<WelcomeScreen> {
  final controller = Completer<WebViewController>();

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Flutter WebView'),
        actions: [
          NavigationControls(controller: controller),
          Menu(controller: controller),
        ],
      ),
      body: WebViewStack(controller: controller),
    );
  }
}
