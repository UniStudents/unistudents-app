import 'dart:convert';

class NewsArticleAttachment {
  String text, value, attribute;
  NewsArticleAttachment(this.text, this.value, this.attribute);
}

class NewsArticle {
  late String id;
  late String link;
  late List<NewsArticleAttachment> attachments;
  late List<String> categories;
  late String content;
  late Map<String, dynamic> extras;
  late String pubDate;
  late String title;
  late String source;

  NewsArticle(this.id, this.link, this.attachments, this.categories,
      this.content, this.extras, this.pubDate, this.title, this.source);


  static List<NewsArticle> parseMany(String res) {
    List<dynamic> result = json.decode(res);

    List<NewsArticle> parsed = [];
    for(int i = 0; i < result.length; i++) {
      NewsArticle? one = parseOne(result[i]);
      // Skip
      if(one == null) continue;
      parsed.add(one);
    }

    return parsed;
  }

  static NewsArticle? parseOne(Map<String, dynamic> res) {
    List<dynamic> attachments = res["attachments"] ?? [];
    List<dynamic> categories = res["categories"] ?? [];

    return NewsArticle(
      res["_id"],
      res["link"],
      attachments.map((e) => NewsArticleAttachment(e["text"], e['value'], e['attribute'])).toList(),
      categories.map((e) => e.toString()).toList(),
      res["content"] ?? "",
      res["extras"] ?? {},
      res["pubDate"],
      res["title"],
      res["source"],
    );
  }
}