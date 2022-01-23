import 'package:flutter/material.dart';
import 'package:flutter_localizations/flutter_localizations.dart';

import 'app_localizations_delegate.dart';

abstract class Locals {
  static Locals? of(BuildContext context) {
    return Localizations.of<Locals>(context, Locals);
  }

  static List<Locale> get supportedLocals => [
    const Locale('el', ''),
    const Locale('en', ''),
  ];

  static List<LocalizationsDelegate> get localizationsDelegates => [
    AppLocalizationsDelegate(),
    GlobalMaterialLocalizations.delegate,
    GlobalWidgetsLocalizations.delegate,
    GlobalCupertinoLocalizations.delegate
  ];


  String get bnvHome;
  String get bnvProgress;
  String get bnvNews;
  String get bnvProfile;

  String get articleWidgetActionsTitle;
  String get articleWidgetActionsSave;
  String get articleWidgetActionsShare;
  String get articleWidgetActionsReport;

  String get articleWidgetAttachments;

  String get datetimeNow;
  String get datetimeMinute;
  String get datetimeMinutes;
  String get datetimeHour;
  String get datetimeHours;
  String get datetimeDay;
  String get datetimeDays;
  String get datetimeWeek;
  String get datetimeWeeks;
  String get datetimeMonth;
  String get datetimeMonths;
  String get datetimeYear;
  String get datetimeYears;
}