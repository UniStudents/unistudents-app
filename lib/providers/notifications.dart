import 'package:flutter/material.dart';
import 'package:shared_preferences/shared_preferences.dart';

get allOn => "11";
get allOff => "00";

class NotificationProvider with ChangeNotifier {

  String _mode = allOn;
  get mode => _mode;

  NotificationProvider() {
    SharedPreferences.getInstance().then((pref) {
      set(pref.getString("notifications") ?? allOn);
    });
  }

  change(bool value, int position) {
    var mode = "";

    for(int i = 0; i < position; i++) {
      mode += _mode[i];
    }
    mode += value ? "1" : "0";
    for(int i = position + 1; i < _mode.length; i++) {
      mode += _mode[i];
    }

    set(mode);
  }

  changeAll(bool value) {
    set(value ? allOn : allOff);
  }

  set(String mode) {
    print(mode);
    _mode = mode;
    notifyListeners();

    SharedPreferences.getInstance().then((pref) {
      pref.setString('notifications', mode);
    });
  }

  bool isAllEnabled() {
    for (int i = 0; i < _mode.length; i++) {
      if (_mode[i] != '1') {
        return false;
      }
    }

    return true;
  }
}
