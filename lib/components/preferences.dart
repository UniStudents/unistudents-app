import 'dart:convert';

class Preferences {
  bool isDarkMode = false;
  bool isNotificationsEnabled = true;

  Preferences(this.isDarkMode, this.isNotificationsEnabled);

  String toJson() {
    return json.encode({
      'isDarkMode': isDarkMode,
      'isNotificationsEnabled': isNotificationsEnabled
    });
  }

  static Preferences fromJson(String res) {
    var tmp = json.decode(res);
    return Preferences(tmp['isDarkMode'], tmp['isNotificationsEnabled']);
  }
}