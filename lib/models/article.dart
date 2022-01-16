import 'dart:convert';

class Article {
  late String id;
  late String link;
  late List<Attachment> attachments;
  late List<String> categories;
  late String content;
  late Map<String, dynamic> extras;
  late String pubDate;
  late String title;
  late String source;

  Article(this.id, this.link, this.attachments, this.categories,
      this.content, this.extras, this.pubDate, this.title, this.source);

  String? frontalImage;
  String? getFrontalImage() {
    return frontalImage ??= 'https://flutter.github.io/assets-for-api-docs/assets/widgets/owl.jpg';
  }

  static List<Article> parseFromRequest(String res) {
    List<dynamic> result = json.decode(res);

    List<Article> parsed = [];
    for(int i = 0; i < result.length; i++) {
      parsed.add(_parseOne(result[i]));
    }

    return parsed;
  }

  static Article _parseOne(Map<String, dynamic> res) {
    List<dynamic> attachments = res["attachments"] ?? [];
    List<dynamic> categories = res["categories"] ?? [];

    return Article(
      res["_id"],
      res["link"],
      attachments.map((e) => Attachment(e["text"], e['value'], e['attribute'])).toList(),
      categories.map((e) => e.toString()).toList(),
      res["content"] ?? "",
      res["extras"] ?? {},
      res["pubDate"],
      res["title"],
      res["source"],
    );
  }
}

class Attachment {
  String text, value, attribute;
  Attachment(this.text, this.value, this.attribute);
}