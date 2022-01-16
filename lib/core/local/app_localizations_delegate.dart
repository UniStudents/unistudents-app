import 'package:flutter/material.dart';
import 'package:unistudents_app/core/local/locals.dart';
import 'package:unistudents_app/core/local/packs/local_en.dart';
import 'package:unistudents_app/core/local/packs/local_gr.dart';

class AppLocalizationsDelegate extends LocalizationsDelegate<Locals> {
  @override
  bool isSupported(Locale locale) =>
      ['el', 'en'].contains(locale.languageCode);

  @override
  Future<Locals> load(Locale locale) => _load(locale);

  static Future<Locals> _load(Locale locale) async {
    switch (locale.languageCode) {
      case 'el': return LocalGr();
      case 'en': return LocalEn();
      default: return LocalGr();
    }
  }

  @override
  bool shouldReload(covariant LocalizationsDelegate<Locals> old) => false;

}