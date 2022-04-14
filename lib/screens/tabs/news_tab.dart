import 'dart:async';

import 'package:flutter/cupertino.dart';
import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:unistudents_app/core/storage.dart';
import 'package:unistudents_app/models/article.dart';
import 'package:unistudents_app/providers/news.dart';
import 'package:unistudents_app/screens/folllow_websites_screen.dart';
import 'package:unistudents_app/widgets/get_started_news.dart';
import 'package:unistudents_app/widgets/infinite_list_footer.dart';
import 'package:unistudents_app/widgets/website_filter_bar.dart';

import '../../widgets/article_widget.dart';

class NewsTab extends StatefulWidget {
  static const String id = 'news_tab';
  bool gotoTop;

  NewsTab({Key? key, this.gotoTop = false}) : super(key: key);

  @override
  State<NewsTab> createState() => _NewsTabState();
}

class _NewsTabState extends State<NewsTab> {
  // late AutoScrollController controller;
  final ScrollController _scrollController = ScrollController();
  int _pageNumber = 0;
  final int _pageLimit = 25;
  final List<Article> _articles = <Article>[];
  final List<String> _filters = <String>[];
  List<String> _latestIds = [];
  List<String> _oldestIds = [];
  var _isInit = true;
  var _isLoading = true;
  var _foundLastPage = false;
  var _errorFetchingOldArticles = false;

  @override
  void initState() {

    // controller = AutoScrollController(
    //     viewportBoundaryGetter: () =>
    //         Rect.fromLTRB(0, 0, 0, MediaQuery.of(context).padding.bottom),
    //     axis: Axis.vertical);

    super.initState();
    
    _scrollController.addListener(() {
      if (_scrollController.position.pixels >= _scrollController.position.maxScrollExtent - 40
          && !_isLoading
          && !_foundLastPage
          && !_errorFetchingOldArticles) {
        _fetchOldArticles();
      }
    });
  }

  @override
  void didChangeDependencies() async {
    final news = Provider.of<News>(context, listen: false);
    if (_isInit) {
      var followedWebsites = await Storage.readFollowedWebsites();
      news.followedWebsites = followedWebsites ?? [];
      _fetchArticles();
    }

    _isInit = false;
    super.didChangeDependencies();
  }

  @override
  void dispose() {
    super.dispose();
    _scrollController.dispose();
  }

  _fetchArticles() async {
    setState(() {
      _articles.clear();
      _isLoading = true;
    });

    try {
      final news = Provider.of<News>(context, listen: false);
      List<Article> articles = await news.fetchArticles(
          filteredWebsites: _filters,
          pageLimit: _pageLimit
      );

      setState(() {
        if (articles.isNotEmpty) {
          news.latestArticles = [...articles];
          _articles.addAll(articles);
          _setLatestArticlesIds();
          _setOldestArticleIds();
        }
        _isLoading = false;
      });
    } catch (err) {
      setState(() {
        _isLoading = false;
      });
    }
  }

  _fetchOldArticles() async {
    setState(() {
      _isLoading = true;
    });

    try {
      final news = Provider.of<News>(context, listen: false);
      List<Article> oldArticles = await news.fetchOldArticles(
          filteredWebsites: _filters,
          oldestArticlesIds: _oldestIds,
          pageLimit: _pageLimit
      );
      setState(() {
        if (oldArticles.isNotEmpty) {
          _articles.addAll(oldArticles);
          _setOldestArticleIds();
          _foundLastPage = false;
        } else {
          _foundLastPage = true;
        }
        _isLoading = false;
      });
    } catch (err) {
      setState(() {
        _isLoading = false;
        _errorFetchingOldArticles = true;
      });
      print('err: ' + err.toString());
    }
  }

  Future<void> _refreshArticles() async {
    setState(() {
      _isLoading = true;
    });

    try {
      final news = Provider.of<News>(context, listen: false);
      List<Article> articles = await news.fetchNewArticles(
          filteredWebsites: _filters,
          latestArticlesIds: _latestIds
      );

      setState(() {
        if (articles.isNotEmpty) {
          _articles.insertAll(0, articles);
          _setLatestArticlesIds();
        }
        _isLoading = false;
      });
    } catch (err) {
      setState(() {
        _isLoading = false;
      });
    }
  }

  _setOldestArticleIds() {
    final news = Provider.of<News>(context, listen: false);
    var websites = _filters.isEmpty
      ? [...news.followedWebsites]
      : [..._filters];

    var oldArticlesMap = {};
    for (var article in _articles.reversed) {
      if (websites.contains(article.source)) {
        oldArticlesMap[article.source] = article.id;
        websites.removeWhere((item) => item == article.source);
        if (websites.isEmpty) break;
      }
    }

    for (var website in websites) {
      oldArticlesMap[website] = 'none';
    }

    websites = _filters.isEmpty
        ? [...news.followedWebsites]
        : [..._filters];

    List<String> oldestIdsArray = [];
    for (MapEntry e in oldArticlesMap.entries) {
      oldestIdsArray.add(e.value);
    }

    _oldestIds = [...oldestIdsArray];
  }

  _setLatestArticlesIds() {
    final news = Provider.of<News>(context, listen: false);
    var websites = _filters.isEmpty
        ? [...news.followedWebsites]
        : [..._filters];

    var latestArticlesMap = {};
    for (var article in _articles) {
      if (websites.contains(article.source)) {
        latestArticlesMap[article.source] = article.id;
        websites.removeWhere((item) => item == article.source);
        if (websites.isEmpty) break;
      }
    }

    for (var website in websites) {
      latestArticlesMap[website] = 'none';
    }

    websites = _filters.isEmpty
        ? [...news.followedWebsites]
        : [..._filters];

    List<String> latestIdsArray = [];
    for (MapEntry e in latestArticlesMap.entries) {
      latestIdsArray.add(e.value);
    }

    _latestIds = [...latestIdsArray];
  }

  _updateFilters(String website, bool added) {
    if (added) {
      _filters.add(website);
    } else {
      _filters.removeWhere((filter) => filter == website);
    }
    _fetchArticles();
  }

  @override
  Widget build(BuildContext context) {
    final news = Provider.of<News>(context, listen: false);

    // Scroll to the top & Refresh
    // WidgetsBinding.instance!.addPostFrameCallback((_) {
    //   if (widget.gotoTop) {
    //     controller.scrollToIndex(0,
    //         preferPosition: AutoScrollPosition.begin,
    //         duration: const Duration(milliseconds: 500));
    //   }
    // });

    Widget? body;
    if (news.followedWebsites.isEmpty) {
      body = GetStartedNews(navigateToWebsitesScreen: navigateToWebsitesScreen);
    } else if (_isLoading && _articles.isEmpty) {
      body = const Center(child: CircularProgressIndicator());
    } else {
      body = RefreshIndicator(
        onRefresh: _refreshArticles,
        child: ListView.builder(
          scrollDirection: Axis.vertical,
          physics: (_isLoading && _articles.isNotEmpty) ? const ClampingScrollPhysics() : null,
          controller: _scrollController,
          itemCount: _articles.length + 2,
          itemBuilder: (ctx, i) {
            if (i == 0) {
              return WebsiteFilterBar(
                followedWebsites: news.followedWebsites,
                filters: _filters,
                updateFilters: _updateFilters,
              );
            } else if (i == _articles.length + 1) {
              return InfiniteListFooter(
                isLoading: _isLoading,
                foundLastPage: _foundLastPage
              );
            } else {
              return ArticleWidget(article: _articles[i - 1]);
            }
          }
        ),
      );
    }

    return Scaffold(
      appBar: buildAppBar(context),
      body: body
    );
  }

  AppBar buildAppBar(BuildContext context) {
    return AppBar(
      title: const Text('Νέα'),
      actions: [
        IconButton(
          icon: const Icon(Icons.add),
          onPressed: () => navigateToWebsitesScreen(context),
        ),
      ],
    );
  }

  void navigateToWebsitesScreen(BuildContext context) {
    final news = Provider.of<News>(context, listen: false);
    var currentFollowedWebsites = [...news.followedWebsites];
    Navigator.push(
      context,
      MaterialPageRoute(
          builder: (ctx) => const FollowWebsitesScreen()
      )).then((value) => {
        setState(() {
          if (!listEquals(currentFollowedWebsites, news.followedWebsites)) {
            _filters.clear();
            _fetchArticles();
          }
        })
      });
  }
}
