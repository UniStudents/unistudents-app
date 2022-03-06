import 'package:flutter/material.dart';
import 'package:unistudents_app/core/local/locals.dart';

class Utils {
  static String format(String text, List<String> replaces) {
    String r = text;

    int i = 1;
    for(String rep in replaces) {
      r = r.replaceFirst('%$i', rep);
      i++;
    }

    return r;
  }

  static String ordinalSuffix(BuildContext context, int i) {
    var j = i % 10,
        k = i % 100;
    if (j == 1 && k != 11) {
      return i.toString() + Locals.of(context)!.ordinalSuffixSt;
    }
    if (j == 2 && k != 12) {
      return i.toString() + Locals.of(context)!.ordinalSuffixNd;
    }
    if (j == 3 && k != 13) {
      return i.toString() + Locals.of(context)!.ordinalSuffixRd;
    }
    return i.toString() + Locals.of(context)!.ordinalSuffixTh;
  }
}