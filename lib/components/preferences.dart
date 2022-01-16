import 'dart:convert';

class Preferences {
  bool isDarkMode = false;
  bool isNotificationsEnabled = true;
  String languageCode;

  Preferences(this.isDarkMode, this.isNotificationsEnabled, this.languageCode);

  String toJson() {
    return json.encode({
      'isDarkMode': isDarkMode,
      'isNotificationsEnabled': isNotificationsEnabled,
      'languageCode': languageCode
    });
  }

  static Preferences fromJson(String res) {
    var tmp = json.decode(res);
    return Preferences(tmp['isDarkMode'], tmp['isNotificationsEnabled'], tmp['languageCode']);
  }
}