import 'package:flutter/material.dart';
import 'package:shared_preferences/shared_preferences.dart';

class NotificationProvider with ChangeNotifier {

  bool _enabled = false;
  get enabled => _enabled;

  NotificationProvider() {
    SharedPreferences.getInstance().then((pref) {
      setEnabled(pref.getBool("notifications") ?? true);
    });
  }

  setEnabled(bool enabled) {
    _enabled = enabled;
    notifyListeners();

    SharedPreferences.getInstance().then((pref) {
      pref.setBool('notifications', _enabled);
    });
  }
}
