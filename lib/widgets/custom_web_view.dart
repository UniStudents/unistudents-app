import 'dart:async';
import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';
import 'package:webview_flutter/webview_flutter.dart';

class CustomWebView extends StatefulWidget {
  CustomWebView({Key? key, @required this.url, @required this.barTitle}) : super(key: key);

  final url;
  final barTitle;
  final controller = Completer<WebViewController>();

  @override
  State<CustomWebView> createState() => _CustomWebViewState();
}

class _CustomWebViewState extends State<CustomWebView> {
  var loadingPercentage = 0;

  @override
  Widget build(BuildContext context) {
    return CupertinoPageScaffold(
      navigationBar: CupertinoNavigationBar(
        leading: Container(),
        middle: Row(
          children: [
            if (widget.url.toString().contains('https'))
              const Icon(Icons.lock_outline, size: 20,),
            const SizedBox(width: 5,),
            Text(widget.barTitle),
          ],
        ),
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
            FutureBuilder(
              future: Future.delayed(const Duration(milliseconds: 500)),
              builder: (context, snapshot) => WebView(
                initialUrl: widget.url,
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
                javascriptMode: JavascriptMode.unrestricted,
              ),
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
}