import 'dart:convert';

class NewsWebsiteLastId {
  String website, lastArticleId;

  NewsWebsiteLastId(this.website, this.lastArticleId);

  Map<String, dynamic> _toJson() {
    return {
      "website": website,
      "lastArticleId": lastArticleId
    };
  }

  static NewsWebsiteLastId fromJson(String res) {
    var tmp = json.decode(res);
    return NewsWebsiteLastId(tmp['website'], tmp['lastArticleId']);
  }

  static String toJSON(List<NewsWebsiteLastId> websites) {
    return json.encode({
      'websites': websites.map((e) => e._toJson())
    });
  }
  
  static List<NewsWebsiteLastId> fromJSON(String res) {
    return (json.decode(res)['websites'] as List<dynamic>).map((e) => NewsWebsiteLastId.fromJson(e)).toList();
  }
}