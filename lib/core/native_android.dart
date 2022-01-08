import 'package:flutter/services.dart';

class NativeAndroid {
  static const platform = MethodChannel('com.unipi.students/native');

  static Future<void> getStudent() async {
    String results;
    try {
      results = await platform.invokeMethod('getStudent');
    } on PlatformException catch (e) {
      results = "Failed to get battery level: '${e.message}'.";
    }
    print(results);
  }
}