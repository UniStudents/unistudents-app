import 'dart:async';

import 'package:flutter/cupertino.dart';
import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:scroll_to_index/scroll_to_index.dart';
import 'package:unistudents_app/core/storage.dart';
import 'package:unistudents_app/models/article.dart';
import 'package:unistudents_app/providers/news.dart';
import 'package:unistudents_app/screens/folllow_websites_screen.dart';

import '../../widgets/article_widget.dart';

class NewsTab extends StatefulWidget {
  static const String id = 'news_tab';
  bool gotoTop;

  NewsTab({Key? key, this.gotoTop = false}) : super(key: key);

  @override
  State<NewsTab> createState() => _NewsTabState();
}

class _NewsTabState extends State<NewsTab> {
  late AutoScrollController controller;
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

  @override
  Widget build(BuildContext context) {
    final news = Provider.of<News>(context, listen: false);

    // Scroll to the top & Refresh
    WidgetsBinding.instance!.addPostFrameCallback((_) {
      if (widget.gotoTop) {
        controller.scrollToIndex(0,
            preferPosition: AutoScrollPosition.begin,
            duration: const Duration(milliseconds: 500));
      }
    });

    Widget? body;
    if (news.followedWebsites.isEmpty) {
      body = buildEmptyBody(context, news);
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
              return buildWebsiteFilterSection(news);
            } else if (i == _articles.length + 1) {
              if (_isLoading && !_foundLastPage) {
                return const Padding(
                  padding: EdgeInsets.only(
                    top: 16,
                    bottom: 16,
                  ),
                  child: Center(
                    child: CircularProgressIndicator(),
                  ),
                );
              } else {
                return const SizedBox.shrink();
              }
            } else {
              return Container(
                padding: const EdgeInsets.fromLTRB(12, 0, 12, 12),
                child: ArticleWidget(article: _articles[i - 1])
              );
            }
          }
        ),
      );
    }

    return Scaffold(
      appBar: buildAppBar(context, news),
      body: body
    );
  }

  Center buildEmptyBody(BuildContext context, News news) {
    return Center(
      child: Padding(
        padding: const EdgeInsets.symmetric(vertical: 32, horizontal: 16),
        child: Column(
          children: [
            Container(
              padding: const EdgeInsets.all(50),
              child: Image.asset(
                'assets/follow-websites.png',
              ),
            ),
            const SizedBox(
              height: 16,
            ),
            Text(
              'Ακολούθησε websites',
              textAlign: TextAlign.center,
              style: Theme.of(context).textTheme.headline6,
            ),
            const SizedBox(
              height: 16,
            ),
            const Text(
              'Δημιούργησε το δικό σου personalized feed.',
              textAlign: TextAlign.center,
            ),
            const SizedBox(
              height: 16,
            ),
            ElevatedButton.icon(
              onPressed: () => navigateToWebsitesScreen(news, context),
              icon: const Icon(
                Icons.add,
                // color: Colors.white,
              ),
              label: const Text(
                'Ακολούθησε',
                style: TextStyle(
                  fontSize: 16,
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }

  AppBar buildAppBar(BuildContext context, News news) {
    return AppBar(
      title: const Text('Νέα'),
      actions: [
        IconButton(
          icon: const Icon(Icons.add),
          onPressed: () => navigateToWebsitesScreen(news, context),
        ),
      ],
    );
  }

  void navigateToWebsitesScreen(News news, BuildContext context) {
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

  SingleChildScrollView buildWebsiteFilterSection(News news) {
    return SingleChildScrollView(
      scrollDirection: Axis.horizontal,
      padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 0),
      child: Row(
        children: [
          // if (!_filters.isEmpty)
          //   RawMaterialButton(
          //     padding: EdgeInsets.zero,
          //     onPressed: () {},
          //     elevation: 0,
          //     fillColor: Colors.white,
          //     child: Icon(
          //       Icons.cancel_outlined,
          //     ),
          //     // padding: EdgeInsets.all(15.0),
          //     shape: CircleBorder(),
          //   ),
          ...news.followedWebsites.map((followedWebsite) => Padding(
            padding: const EdgeInsets.all(4.0),
            child: FilterChip(
              backgroundColor: Colors.transparent,
              selectedColor: Colors.white,
              label: Text(followedWebsite),
              selected: _filters.contains(followedWebsite),
              onSelected: (bool value) {
                if (value) {
                  _filters.add(followedWebsite);
                } else {
                  _filters.removeWhere((filter) => filter == followedWebsite);
                }
                _fetchArticles();
              },
            ),
          )).toList()
        ]
      ),
    );
  }
}
