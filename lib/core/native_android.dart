import 'package:flutter/services.dart';
import 'package:unistudents_app/models/progress_model.dart';

class NativeAndroid {
  static const platform = MethodChannel('com.unipi.students/native');

  static Future<String?> getProgress(ProgressModel account) async {
    String results;
    results = await platform.invokeMethod('getProgress', {
      "university": account.university,
      "system": account.system,
      "username": account.username,
      "password": account.password,
      "cookies": account.cookies
    });
    return results;
  }
}