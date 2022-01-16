import 'package:unistudents_app/core/language_utils/packs/english.dart';
import 'package:unistudents_app/core/language_utils/packs/greek.dart';

import 'get_string.dart';

abstract class LanguagePack {
  Map<StringList, String> getStrings();
}

class LanguagePacks {
  static Map<String, LanguagePack> packs = {
    'greek': GreekPack(),
    'english': EnglishPack()
  };

  static const greek = 'greek';
  static const english = 'english';

  static LanguagePack get(String name) {
    return packs[name]!;
  }
}
