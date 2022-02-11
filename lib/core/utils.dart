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
}