import 'package:flutter/cupertino.dart';

class UniColors {

  static int uniBlue(bool isDarkMode) => isDarkMode ? 0xFF766FF1 : 0xFF6C63FF;

  static Color getBackground(bool isDarkMode) => Color(isDarkMode ? 0xFF151C26 : 0xFFF7F7F7);
  static Color getForeground(bool isDarkMode) => Color(isDarkMode ? 0xFF2F3640 : 0xFFFFFFFF);

  static Color getText(bool isDarkMode) => Color(isDarkMode ? 0xFFFFFFFF : 0xFF000000);
  static Color getText1(bool isDarkMode) => Color(isDarkMode ? 0xDEFFFFFF : 0xDE000000);
  static Color getTextHalf(bool isDarkMode) => Color(isDarkMode ? 0x99FFFFFF : 0x99000000);
  static Color getUniBlue(bool isDarkMode) => Color(isDarkMode ? 0xFF766FF1 : 0xFF6C63FF);

  static Color getGreen() => const Color(0xFF82BF40);
  static Color getRed() => const Color(0xFFFE6D7A);
  static Color getYellow() => const Color(0xFFFBCA3D);
}