import 'package:unistudents_app/core/local/locals.dart';

class LocalGr extends Locals {
  @override String get bnvHome => "Αρχική";
  @override String get bnvNews => "Νέα";
  @override String get bnvProfile => 'Προφίλ';
  @override String get bnvProgress => 'Πρόοδος';

  @override String get articleWidgetActionsTitle => "Ενέργειες";
  @override String get articleWidgetActionsReport => "Αναφορά";
  @override String get articleWidgetActionsSave => "Αποθήκευση";
  @override String get articleWidgetActionsShare => "Κοινοποίηση";

  @override String get articleWidgetAttachments => "Συννημένα";

  @override String get datetimeNow => 'τώρα';
  @override String get datetimeMinute => 'λεπτό';
  @override String get datetimeMinutes => 'λεπτά';
  @override String get datetimeHour => 'ώρα';
  @override String get datetimeHours => 'ώρες';
  @override String get datetimeDay => 'μέρα';
  @override String get datetimeDays => 'μέρες';
  @override String get datetimeWeek => 'εβδομάδα';
  @override String get datetimeWeeks => 'εβδομάδες';
  @override String get datetimeMonth => 'μήνα';
  @override String get datetimeMonths => 'μήνες';
  @override String get datetimeYear => 'χρόνο';
  @override String get datetimeYears => 'χρόνια';
}