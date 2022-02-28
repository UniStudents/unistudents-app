import 'package:flutter/material.dart';
import 'package:shared_preferences/shared_preferences.dart';

class ThemeProvider with ChangeNotifier {
  final lightTheme = ThemeData(
    brightness: Brightness.light,
    primarySwatch: Colors.blue,
    scaffoldBackgroundColor: const Color(0xFFF7F7F7),
    cardColor: const Color(0xFFFFFFFF),
  );

  final darkTheme = ThemeData(
    brightness: Brightness.dark,
    primarySwatch: Colors.blue,
    scaffoldBackgroundColor: const Color(0xFF151C26),
    cardColor: const Color(0xFF2F3640),
  );

  ThemeMode _themeMode = ThemeMode.system;
  int _themeNum = 0;

  get theme => _themeMode;

  get themeNum => _themeNum; // 0 -> system, 1 -> light, 2 -> dark

  ThemeProvider() {
    SharedPreferences.getInstance().then((pref) {
      setTheme(pref.getInt('theme'));
    });
  }

  setTheme(int? theme) {
    theme ??= 0;
    _themeNum = theme;
    if (theme == 1) {
      _themeMode = ThemeMode.light;
    } else if (theme == 2) {
      _themeMode = ThemeMode.dark;
    } else {
      _themeMode = ThemeMode.system;
    }
    notifyListeners();

    SharedPreferences.getInstance().then((pref) {
      pref.setInt('theme', theme!);
    });
  }
}
