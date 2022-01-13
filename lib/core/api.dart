import 'dart:convert';
import 'package:http/http.dart' as http;

import '../components/bug.dart';
import '../models/news_article.dart';
import '../models/progress_model.dart';
import '../models/news_websites.dart';
import 'env.dart';

class API {
  
  static String _LOAD_BALANCED_URL = "";

  static Future<String?> getLoadBalancedUrl() async {
    if(_LOAD_BALANCED_URL == "") {
      String url = "${Env.API_URL}/server";
      final response = await http.get(Uri.parse(url));

      if (response.statusCode != 200) return null;
      _LOAD_BALANCED_URL = response.body;
    }

    return _LOAD_BALANCED_URL;
  }

  static Future<bool> requestProgress(ProgressModel account, bool isAndroid) async {
    if(isAndroid) {
      // TODO - Android native
      return false;
    }

    // Request from API
    String? url = await API.getLoadBalancedUrl();
    if(url == null) return false;

    final response = await http.post(
        Uri.parse(account.geUrl(url)),
        body: json.encode(account.getAuth()),
        headers: {
          'Content-type' : 'application/json',
          'Accept': 'application/json',
        }
    );

    if(response.statusCode != 200) return false;
    return account.parse(response.body);
  }

  Future<bool> reportBug(Bug bug) async {
    String url = bug.getUrl(Env.API_URL);
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

  Future<List<NewsWebsites>?> getWebsites(String first) async {
    String url = "${Env.GOHAN_URL}/websites?university=$first";
    final response = await http.get(Uri.parse(url));

    if(response.statusCode != 200) return null;
    return NewsWebsites.parseMany(response.body);
  }

  Future<List<NewsArticle>?> getArticles(List<String> subscribedWebsites, {int? pageSize, int? pageNumber,
    List<String>? afterIds, List<String>? beforeIds}) async {

    String url = "${Env.GOHAN_URL}/articles?websites=${subscribedWebsites.join(',')}"
        "&pageSize=${pageSize ?? "0"}"
        "&pageNumber=${pageNumber ?? "0"}"
        "${afterIds != null ? "&after=${afterIds.join(",")}" : ""}"
        "${beforeIds != null ? "&before=${beforeIds.join(",")}" : ""}";

    final response = await http.get(Uri.parse(url));

    if(response.statusCode != 200) return null;
    return NewsArticle.parseMany(response.body);
  }
}

