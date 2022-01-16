import 'language_pack.dart';

enum StringList {
  str
}

class GetString {

  static LanguagePack? _pack;
  static Map<StringList, String>? _strings;
  
  static String get(StringList what) {
    return _strings![what]!;
  }

  static assignLanguagePack(String pack) {
    _pack = LanguagePacks.get(pack);
    _strings = _pack!.getStrings();
  }
}