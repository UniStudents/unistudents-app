import 'package:unistudents_app/core/api.dart';
import 'package:unistudents_app/models/article.dart';
import 'package:unistudents_app/models/website.dart';
import 'package:flutter/foundation.dart';

class News with ChangeNotifier {
  int _pageNumber = 0;
  int _pageLimit = 10;
  // bool _isInitializing = false;
  // int _newArticlesCounter = 0;

  List<Article> _articles = [];
  List<Article> _backupArticles = [];
  List<String> _latestIds = [];
  List<String> _oldestIds = [];

  List<AvailableWebsite> _availableWebsites = [];
  List<String> _subscribedWebsites = ["ds.unipi.gr"];
  List<String> _filteredWebsites = [];

  List<Article> get articles {
    return [..._articles];
  }

  Future<void> fetchArticles() async {
    print('fetch articles');
    final response = await API.getArticles(
        _subscribedWebsites,
        pageSize: _pageLimit,
        pageNumber: _pageNumber
    );
    if (response.statusCode != 200) return null;
    _articles = Article.parseMany(response.body).toList();
    print('fetched: ' + _articles.length.toString());
    notifyListeners();
  }

  Future<void> refreshArticles() async {
    final response = await API.getArticles(
        _subscribedWebsites,
        afterIds: _latestIds);
    if (response.statusCode != 200) return null;
    _articles = Article.parseMany(response.body).toList();
    notifyListeners();
  }

  Future<void> fetchOldArticles() async {
    final response = await API.getArticles(
        _subscribedWebsites,
        pageSize: _pageLimit,
        beforeIds: _oldestIds);
    if (response.statusCode != 200) return null;
    _articles = Article.parseMany(response.body).toList();
    notifyListeners();
  }

  List<Article> get backupArticles {
    return [..._backupArticles];
  }

  List<String> get latestIds {
    return [..._latestIds];
  }

  List<String> get oldestIds {
    return [..._oldestIds];
  }

  List<AvailableWebsite> get availableWebsites {
    return [..._availableWebsites];
  }

  Future<void> fetchAvailableWebsites(String university) async {
    final response = await API.getAvailableWebsites(university);
    if (response.statusCode != 200) return null;
    _availableWebsites = AvailableWebsite.parseMany(response.body);
    notifyListeners();
  }

  List<String> get subscribedWebsites {
    return [..._subscribedWebsites];
  }

  void addSubscribedWebsite(String website) {
    if (!_subscribedWebsites.contains(website)) {
      _subscribedWebsites.add(website);
    }
  }

  List<String> get filteredWebsites {
    return [..._filteredWebsites];
  }
}