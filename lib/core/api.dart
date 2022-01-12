import 'dart:convert';
import 'dart:io';
import 'package:path_provider/path_provider.dart';
import 'package:http/http.dart' as http;
import 'package:unistudents_app/components/news_website_last_id.dart';
import 'package:unistudents_app/components/preferences.dart';

import '../components/bug.dart';
import '../models/news_article.dart';
import '../models/progress_model.dart';
import '../models/news_websites.dart';
import 'env.dart';

class HttpAPI {
  
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
    String? url = await HttpAPI.getLoadBalancedUrl();
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

class StorageAPI {

  static const String _FILE_PROGRESS = '/progress.json';
  static const String _FILE_FOLLWED_WEBSITES = '/followed_websites.json';
  static const String _FILE_LATEST_ARTICLES = '/latest_articles.json';
  static const String _FILE_PREFERENCES = '/preferences.json';

  static Future<String> _localPath() async {
    final directory = await getApplicationDocumentsDirectory();
    return directory.path;
  }

  static Future<File> _localFile(String file) async {
    final path = await _localPath();
    return File('$path$file');
  }

  // Progress
  static Future<bool> saveProgress(ProgressModel account) async {
    final file = await _localFile(_FILE_PROGRESS);
    await file.writeAsString(account.toJSON());
    return true;
  }

  static Future<bool> deleteProgress() async {
    final file = await _localFile(_FILE_PROGRESS);
    await file.delete();
    return true;
  }

  static Future<ProgressModel?> readProgress() async {
    try{
      final file = await _localFile(_FILE_PROGRESS);
      final contents = await file.readAsString();
      return ProgressModel.parseWhole(contents);
    }
    catch(e) {
      return null;
    }
  }

  // Followed Websites
  Future<bool> saveFollowedWebsites(List<String> followedWebsites) async {
    final file = await _localFile(_FILE_FOLLWED_WEBSITES);
    await file.writeAsString(followedWebsites.join('\n'));
    return true;
  }

  Future<bool> deleteFollowedWebsites() async {
    final file = await _localFile(_FILE_FOLLWED_WEBSITES);
    await file.delete();
    return true;
  }

  Future<List<String>?> readFollowedWebsites() async {
    try{
      final file = await _localFile(_FILE_FOLLWED_WEBSITES);
      final contents = await file.readAsString();

      List<String> l = [];
      for(String s in contents.split('\n')) {
        l.add(s);
      }

      return l;
    }
    catch(e) {
      return null;
    }
  }

  // Latest Articles
  Future<bool> saveLatestArticleIds(List<NewsWebsiteLastId> websites) async {
    final file = await _localFile(_FILE_LATEST_ARTICLES);
    await file.writeAsString(NewsWebsiteLastId.toJSON(websites));
    return true;
  }

  Future<bool> deleteLatestArticleIds() async {
    final file = await _localFile(_FILE_LATEST_ARTICLES);
    await file.delete();
    return true;
  }

  Future<List<NewsWebsiteLastId>?> readLatestArticleIds() async {
    try{
      final file = await _localFile(_FILE_LATEST_ARTICLES);
      final contents = await file.readAsString();
      return NewsWebsiteLastId.fromJSON(contents);
    }
    catch(e) {
      return null;
    }
  }

  // Preferences
  Future<bool> savePreferences(Preferences preferences) async {
    final file = await _localFile(_FILE_PREFERENCES);
    await file.writeAsString(preferences.toJson());
    return true;
  }

  Future<bool> deletePreferences() async {
    final file = await _localFile(_FILE_PREFERENCES);
    await file.delete();
    return true;
  }

  Future<Preferences?> readPreferences() async {
    try{
      final file = await _localFile(_FILE_PREFERENCES);
      final contents = await file.readAsString();
      return Preferences.fromJson(contents);
    }
    catch(e) {
      return null;
    }
  }
}