import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';
import 'package:unistudents_app/widgets/web_view_stack.dart';

class LoginScreen extends StatefulWidget {
  static const String id = 'login_screen';
  var textStr = 'Empty';

  LoginScreen({Key? key}) : super(key: key);

  @override
  _LoginScreenState createState() => _LoginScreenState();
}

class _LoginScreenState extends State<LoginScreen> {
  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: SafeArea(
        child: Center(
          child: Column(
            children: [
              TextButton(
                child: const Text('Είσοδος'),
                onPressed: () => _navigateToWebViewLogin(context)
              ),
              Expanded(
                child: SingleChildScrollView(
                  scrollDirection: Axis.vertical,
                  child: Text(widget.textStr)
                ),
              )
            ]
          ),
        ),
      ),
    );
  }

  void _navigateToWebViewLogin(BuildContext buildContext) async {
    final result = await Navigator.of(context).push(
      MaterialPageRoute<String>(
        builder: (ctx) => WebViewStack(),
        fullscreenDialog: true
      )
    );

    setState(() {
      widget.textStr = result!;
    });
  }
}
