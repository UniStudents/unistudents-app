import 'dart:async';
import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';
import 'package:webview_flutter/webview_flutter.dart';

class WebViewStack extends StatefulWidget {
  WebViewStack({Key? key}) : super(key: key);

  final controller = Completer<WebViewController>();

  @override
  State<WebViewStack> createState() => _WebViewStackState();
}

class _WebViewStackState extends State<WebViewStack> {
  var loadingPercentage = 0;

  @override
  Widget build(BuildContext context) {
    return CupertinoPageScaffold(
      navigationBar: CupertinoNavigationBar(
        leading: Container(),
        middle: const Text('Είσοδος'),
        trailing: GestureDetector(
          child: const Text("Πίσω"),
          onTap: () {
            Navigator.pop(context);
          },
        ),
      ),
      child: SafeArea(
        child: Stack(
          children: [
            WebView(
              initialUrl: 'https://my-studies.uoa.gr/Secr3w/connect.aspx',
              onWebViewCreated: (webViewController) {
                widget.controller.complete(webViewController);
              },
              onPageStarted: (url) {
                setState(() {
                  loadingPercentage = 0;
                });
              },
              onProgress: (progress) {
                setState(() {
                  loadingPercentage = progress;
                });
              },
              onPageFinished: (url) {
                setState(() {
                  loadingPercentage = 100;
                });
              },
              navigationDelegate: _onNavigation,
              javascriptMode: JavascriptMode.unrestricted,
              javascriptChannels: _createJavascriptChannels(context),
            ),
            if (loadingPercentage < 100)
              LinearProgressIndicator(
                value: loadingPercentage / 100.0,
              ),
          ],
        ),
      ),
    );
  }

  FutureOr<NavigationDecision> _onNavigation(navigation) async {
    final url = Uri.parse(navigation.url).toString();
    final controller = await widget.controller.future;
    if (url.endsWith('https://my-studies.uoa.gr/Secr3w/app/')) {
      controller.loadUrl('https://my-studies.uoa.gr/Secr3w/app/accHistory/default.aspx');
      return NavigationDecision.navigate;
    }
    if (url.endsWith('https://my-studies.uoa.gr/Secr3w/app/accHistory/default.aspx')) {
      await controller.runJavascript('''
        var result = '';
        var xmlHttp = new XMLHttpRequest();
        xmlHttp.open( "GET", 'https://my-studies.uoa.gr/Secr3w/app/accHistory/accadFooter.aspx?', false );
        xmlHttp.send( null );
        result = 'FIRST_RESPONSE: ' + xmlHttp.responseText;
        
        xmlHttp.open( "GET", 'https://my-studies.uoa.gr/Secr3w/app/userprofile/generalInfo.aspx', false );
        xmlHttp.send( null );
        result = result + ' SECONST_RESPONSE: ' + xmlHttp.responseText;
        LoginResponse.postMessage('responseStatus: 200 | ' + result);
      ''');
    }
    return NavigationDecision.navigate;
  }

  Set<JavascriptChannel> _createJavascriptChannels(BuildContext context) {
    return {
      JavascriptChannel(
        name: 'LoginResponse',
        onMessageReceived: (message) {
          if (message.message.contains('responseStatus: 200')) {
            Navigator.of(context).pop(message.message);
          }
        },
      ),
    };
  }
}