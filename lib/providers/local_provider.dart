import 'package:flutter/cupertino.dart';

import '../core/local/locals.dart';

class LocalProvider with ChangeNotifier {
  Locale? _locale = const Locale('el', '');
  Locale? get locale => _locale;

  void setLocale(Locale locale) {
    if(!Locals.supportedLocals.contains(locale)) return;

    _locale = locale;
    notifyListeners();
  }
}