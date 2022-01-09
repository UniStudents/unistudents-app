import 'dart:convert';

class NewsWebsites {
  final String id;
  final String alias;
  final String icon;
  final List<NewsWebsites> departments;
  
  NewsWebsites(this.id, this.alias, this.icon, this.departments);

  static List<NewsWebsites> parseMany(String res) {
    List<dynamic> result = json.decode(res);

    List<NewsWebsites> parsed = [];
    for(int i = 0; i < result.length; i++) {
      NewsWebsites? one = parseOne(result[i]);
      // Skip
      if(one == null) continue;
      parsed.add(one);
    }

    return parsed;
  }

  static NewsWebsites? parseOne(Map<String, dynamic> res) {
    if(res["parent"] == null) return null;
    if(res["parent"]["id"] == null) return null;
    if(res["parent"]["alias"] == null) return null;
    if(res["parent"]["icon"] == null) return null;

    List<NewsWebsites> departments = [];

    List<dynamic> children = res['children'];
    for(int i = 0; i < children.length; i++) {
      Map<String, dynamic> child = children[i];
      departments.add(NewsWebsites(child["id"], child["alias"], "", []));
    }

    return NewsWebsites(res["parent"]["id"], res["parent"]["alias"], res["parent"]["icon"], departments);
  }
}