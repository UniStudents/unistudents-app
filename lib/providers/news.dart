import 'package:http/http.dart';
import 'package:unistudents_app/core/api.dart';
import 'package:unistudents_app/core/storage.dart';
import 'package:unistudents_app/models/article.dart';
import 'package:unistudents_app/models/news_website.dart';
import 'package:flutter/foundation.dart';

enum ArticlesState { fetch, refresh, old }

class News with ChangeNotifier {
  List<Article> _latestArticles = [];
  List<NewsWebsite> _availableWebsites = [];
  List<String> _followedWebsites = [];

  List<Article> get latestArticles => [..._latestArticles];

  set latestArticles(List<Article> value) {
    _latestArticles = value;
  }

  List<NewsWebsite> get availableWebsites => [..._availableWebsites];
  List<String> get followedWebsites => [..._followedWebsites];

  set followedWebsites(List<String> value) {
    _followedWebsites = value;
    Storage.saveFollowedWebsites(_followedWebsites);
  }

  void followWebsite(String website) {
    if (!_followedWebsites.contains(website)) {
      _followedWebsites.add(website);
      _followedWebsites.sort((a, b) => a.toString().compareTo(b.toString()));
      Storage.saveFollowedWebsites(_followedWebsites);
    }
  }

  void unfollowWebsite(String website) {
    if (_followedWebsites.contains(website)) {
      _followedWebsites.remove(website);
      Storage.saveFollowedWebsites(_followedWebsites);
    }
  }

  Future<List<Article>> fetchArticles({
    List<String> filteredWebsites = const <String>[],
    required int pageLimit
  }) async {
    Response response;
    var websites = (filteredWebsites.isEmpty)
        ? [..._followedWebsites]
        : [...filteredWebsites];

    response = await API.getArticles(websites, pageSize: pageLimit, pageNumber: 0);

    if (response.statusCode != 200) throw Exception(response.statusCode);

    return Article.parseFromRequest(response.body).toList();
  }

  Future<List<Article>> fetchOldArticles({
    List<String> filteredWebsites = const <String>[],
    required List<String> oldestArticlesIds,
    required int pageLimit
  }) async {
    Response response;
    var websites = (filteredWebsites.isEmpty)
        ? [..._followedWebsites]
        : [...filteredWebsites];

    response = await API.getArticles(websites,
        pageSize: pageLimit,
        pageNumber: 0,
        beforeIds: oldestArticlesIds);

    if (response.statusCode != 200) throw Exception(response.statusCode);

    return Article.parseFromRequest(response.body).toList();
  }

  Future<List<Article>> fetchNewArticles({
    List<String> filteredWebsites = const <String>[],
    required List<String> latestArticlesIds
  }) async {
    Response response;
    var websites = (filteredWebsites.isEmpty)
        ? [..._followedWebsites]
        : [...filteredWebsites];

    response = await API.getArticles(websites,
        pageSize: 0,
        pageNumber: 0,
        afterIds: latestArticlesIds);

    if (response.statusCode != 200) throw Exception(response.statusCode);

    return Article.parseFromRequest(response.body).toList();
  }

  Future<void> fetchWebsites(String university) async {
    final response = await API.getAvailableWebsites(university);

    if (response.statusCode != 200) return;
    _availableWebsites = NewsWebsite.parseFromRequest(response.body);

    notifyListeners();
  }
}
