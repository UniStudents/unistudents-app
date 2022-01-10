import 'dart:convert';

class _NewsWebsiteChild {
  String id, alias;
  _NewsWebsiteChild(this.id, this.alias);

  Map<String, dynamic> toJSON() {
    Map<String, dynamic> result = {
      "id": id,
      "alias": alias
    };

    return result;
  }
}

class NewsWebsites {
  final String id;
  final String alias;
  final String icon;
  final List<_NewsWebsiteChild> departments;
  
  NewsWebsites(this.id, this.alias, this.icon, this.departments);

  String toJSON() {
    Map<String, dynamic> result = {
      "parent": {
        "id": id,
        "alias": alias,
        "icon": icon
      },
      "children": departments.map((e) => e.toJSON()).toList()
    };

    return json.encode(result);
  }


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

    List<_NewsWebsiteChild> departments = [];

    List<dynamic> children = res['children'];
    for(int i = 0; i < children.length; i++) {
      Map<String, dynamic> child = children[i];
      departments.add(_NewsWebsiteChild(child["id"], child["alias"]));
    }

    return NewsWebsites(res["parent"]["id"], res["parent"]["alias"], res["parent"]["icon"], departments);
  }
}