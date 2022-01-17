import 'dart:convert';

class NewsWebsite {
  final String id;
  final String alias;
  final String icon;
  final List<NewsWebsiteChild> departments;
  
  NewsWebsite(this.id, this.alias, this.icon, this.departments);

  String toJSON() {
    return json.encode({
      "parent": {
        "id": id,
        "alias": alias,
        "icon": icon
      },
      "children": departments.map((e) => e.toJSON()).toList()
    });
  }
  
  static List<NewsWebsite> parseFromRequest(String res) {
    List<dynamic> result = json.decode(res);

    List<NewsWebsite> parsed = [];
    for(int i = 0; i < result.length; i++) {
      NewsWebsite? one = _parseOne(result[i]);
      // Skip
      if(one == null) continue;
      parsed.add(one);
    }

    return parsed;
  }

  static NewsWebsite? _parseOne(Map<String, dynamic> res) {
    if(res["parent"] == null) return null;
    if(res["parent"]["id"] == null) return null;
    if(res["parent"]["alias"] == null) return null;
    if(res["parent"]["icon"] == null) return null;

    List<NewsWebsiteChild> departments = [];

    List<dynamic> children = res['children'];
    for(int i = 0; i < children.length; i++) {
      Map<String, dynamic> child = children[i];
      departments.add(NewsWebsiteChild(child["id"], child["alias"]));
    }

    return NewsWebsite(res["parent"]["id"], res["parent"]["alias"], res["parent"]["icon"], departments);
  }
}

class NewsWebsiteChild {
  String id, alias;
  NewsWebsiteChild(this.id, this.alias);

  Map<String, dynamic> toJSON() {
    return {
      "id": id,
      "alias": alias
    };
  }
}