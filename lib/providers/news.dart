import 'package:http/http.dart';
import 'package:unistudents_app/core/api.dart';
import 'package:unistudents_app/core/storage.dart';
import 'package:unistudents_app/models/article.dart';
import 'package:unistudents_app/models/news_website.dart';
import 'package:flutter/foundation.dart';

enum ArticlesState { fetch, refresh, old }

class News with ChangeNotifier {
  int _pageNumber = 0;
  int _pageLimit = 10;

  List<Article> _articles = [
    Article('', 'link', [Attachment("Image", 'https://flutter.github.io/assets-for-api-docs/assets/widgets/owl.jpg', 'img')],
        ["Ανακοινώσεις", "Νέα"], "content", {}, "2022-01-16 22:12:00", "Πρόγραμμα σπουδών", "unipi.gr")
  ];

  List<Article> _backupArticles = [];
  List<String> _latestIds = [];
  List<String> _oldestIds = [];

  List<NewsWebsite> _availableWebsites = [
    NewsWebsite("unipi.gr", "Πανεπιστήμιο Πειραιώς", "/assets/unipi-logo.png", [
      NewsWebsiteChild("unipi.gr", "Πανεπιστήμιο Πειραιά"),
      NewsWebsiteChild("ds.unipi.gr", "Ψηφιακά Συστήματα"),
      NewsWebsiteChild("oik.unipi.gr", "Οικονομικό Τμήμα")
    ]),
    NewsWebsite("uoc.gr", "Πανεπιστήμιο Κρήτης", "/assets/uoc-logo.png", [
      NewsWebsiteChild("uoc.gr", "Πανεπιστήμιο Πειραιά"),
      NewsWebsiteChild("ds.uoc.gr", "Ψηφιακά Συστήματα"),
      NewsWebsiteChild("oik.uoc.gr", "Οικονομικό Τμήμα")
    ]),
    NewsWebsite("tuc.gr", "Πολυτεχνίο Κρήτης", "/assets/tuc-logo.png", [
      NewsWebsiteChild("tuc.gr", "Πανεπιστήμιο Πειραιά"),
      NewsWebsiteChild("ds.tuc.gr", "Ψηφιακά Συστήματα"),
      NewsWebsiteChild("oik.tuc.gr", "Οικονομικό Τμήμα")
    ])
  ];
  List<String> _followedWebsites = [];
  List<String> _filteredWebsites = [];

  List<Article> get articles => [..._articles];

  List<Article> get backupArticles => [..._backupArticles];

  List<String> get latestIds => [..._latestIds];

  List<String> get oldestIds => [..._oldestIds];

  List<NewsWebsite> get availableWebsites => [..._availableWebsites];

  List<String> get followedWebsites => [..._followedWebsites];

  set followedWebsites(List<String> value) {
    _followedWebsites = value;
    Storage.saveFollowedWebsites(_followedWebsites);
  }

  void followWebsite(String website) {
    if (!_followedWebsites.contains(website)) {
      _followedWebsites.add(website);
      Storage.saveFollowedWebsites(_followedWebsites);
    }
  }

  void unfollowWebsite(String website) {
    if (_followedWebsites.contains(website)) {
      _followedWebsites.remove(website);
      Storage.saveFollowedWebsites(_followedWebsites);
    }
  }

  List<String> get filteredWebsites => [..._filteredWebsites];

  Future<void> fetchArticles({ArticlesState state = ArticlesState.fetch}) async {
    Response response;
    switch (state) {
      case ArticlesState.fetch:
        response = await API.getArticles(_followedWebsites,
            pageSize: _pageLimit, pageNumber: _pageNumber);
        break;
      case ArticlesState.refresh:
        response =
            await API.getArticles(_followedWebsites, afterIds: _latestIds);
        break;
      case ArticlesState.old:
        response = await API.getArticles(_followedWebsites,
            pageSize: _pageLimit, beforeIds: _oldestIds);
        break;
    }

    if (response.statusCode != 200) return;

    _articles = Article.parseFromRequest(response.body).toList();
    notifyListeners();
  }

  Future<void> fetchWebsites(String university) async {
    final response = await API.getAvailableWebsites(university);

    if (response.statusCode != 200) return;
    _availableWebsites = NewsWebsite.parseFromRequest(response.body);

    notifyListeners();
  }
}
