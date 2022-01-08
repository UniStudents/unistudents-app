import 'dart:convert';

import 'package:http/http.dart' as http;
import '../components/bug.dart';
import 'Env.dart';

class _ArticlesAPI {
  Future<Map<String, dynamic>?> fetchWebsites(String university) async {
    String url = "${Env.GOHAN_URL}/websites?university=$university";
    final response = await http.get(Uri.parse(url));

    if(response.statusCode != 200) return null;
    return json.decode(response.body);
  }

  Future<Map<String, dynamic>?> fetchArticles(String pageSize, String pageNumber, List<String> subscribedWebsites) async {
    String url = "${Env.GOHAN_URL}/articles?websites=${subscribedWebsites.join(',')}&pageSize=$pageSize&pageNumber=$pageNumber";
    final response = await http.get(Uri.parse(url));

    if(response.statusCode != 200) return null;
    return json.decode(response.body);
  }

  Future<Map<String, dynamic>?> fetchNewArticles(List<String> latestIds, List<String> subscribedWebsites) async {
    String url = "${Env.GOHAN_URL}/articles?websites=${subscribedWebsites.join(',')}pageSize=0&pageNumber=0&after=${latestIds.join(",")}";
    final response = await http.get(Uri.parse(url));

    if(response.statusCode != 200) return null;
    return json.decode(response.body);
  }

  Future<Map<String, dynamic>?> fetchOldArticles(List<String> oldestIds, String pageLimit, List<String> subscribedWebsites) async {
    String url = "${Env.GOHAN_URL}/articles?websites=${subscribedWebsites.join(',')}pageSize=$pageLimit&pageNumber=0&before=${oldestIds.join(",")}";
    final response = await http.get(Uri.parse(url));

    if(response.statusCode != 200) return null;
    return json.decode(response.body);
  }
}

class _BugAPI {
  Future<bool> reportBug(Bug bug) async {
    String url = bug.getUrl(Env.NODEJS_URL);
    final response = await http.post(
        Uri.parse(url),
        body: bug.getJson(),
        headers: {
          'Content-type' : 'application/json',
          'Accept': 'application/json',
        }
    );

    return response.statusCode == 200;
  }
}

class API {
  
  static final _ArticlesAPI Articles = _ArticlesAPI();
  static final _BugAPI Bugs = _BugAPI();

  static String _API_URL = "";

  static Future<String?> getAPIUrl() async {
    if(_API_URL == "") {
      String url = "${Env.NODEJS_URL}/server";
      final response = await http.get(Uri.parse(url));

      if (response.statusCode != 200) return null;
      _API_URL = response.body;
    }

    return _API_URL;
  }
}