import 'dart:async';

import 'package:flutter/cupertino.dart';
import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:scroll_to_index/scroll_to_index.dart';
import 'package:unistudents_app/core/storage.dart';
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
  final List<String> _filters = <String>[];
  var _isInit = true;
  var _isLoading = true;

  @override
  void initState() {
    controller = AutoScrollController(
        viewportBoundaryGetter: () =>
            Rect.fromLTRB(0, 0, 0, MediaQuery.of(context).padding.bottom),
        axis: Axis.vertical);

    super.initState();
  }

  @override
  void didChangeDependencies() {
    final news = Provider.of<News>(context, listen: false);
    if (_isInit) {
      Storage.readFollowedWebsites().then((followedWebsites) => {
        setState(() {
          news.followedWebsites = followedWebsites ?? [];
        })
      }).then((value) => {
        news.fetchArticles().then((_) {
          setState(() {
            _isLoading = false;
          });
        })
      });
    }

    _isInit = false;
    super.didChangeDependencies();
  }

  Future<void> _refreshArticles() async {
    final news = Provider.of<News>(context, listen: false);
    await news.fetchArticles(filteredWebsites: _filters);
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
    // if(news.followedWebsites.isEmpty) {
    //   // TODO - Show subscribe button
    //   print('isempty');
    // }
    if(_isLoading) {
      body = const Center(child: CircularProgressIndicator());
    }
    else {
      // Show articles
      final articles = news.articles;
      body = RefreshIndicator(
        onRefresh: _refreshArticles,
        child: ListView.builder(
          scrollDirection: Axis.vertical,
          controller: controller,
          itemCount: articles.length + 1,
          itemBuilder: (ctx, i) => (i == 0)
            ? buildWebsiteFilterSection(news)
            : Container(
              padding: const EdgeInsets.fromLTRB(12, 0, 12, 12),
              child: ArticleWidget(article: articles[i - 1])
            )
        ),
      );
    }

    return Scaffold(
      appBar: buildAppBar(context, news),
      body: body
    );
  }

  AppBar buildAppBar(BuildContext context, News news) {
    return AppBar(
      title: const Text('Νέα'),
      actions: [
        IconButton(
          icon: const Icon(Icons.add),
          onPressed: () {
            var currentFollowedWebsites = [...news.followedWebsites];
            Navigator.push(
              context,
              MaterialPageRoute(
                  builder: (ctx) => const FollowWebsitesScreen())
              ).then((value) => {
                setState(() {
                  if (!listEquals(currentFollowedWebsites, news.followedWebsites)) {
                    _filters.clear();
                    _isLoading = true;
                    news.fetchArticles().then((value) => {
                      setState(() {
                        _isLoading = false;
                      })
                    });
                  }
                })
              });
          },
        ),
      ],
    );
  }

  SingleChildScrollView buildWebsiteFilterSection(News news) {
    return SingleChildScrollView(
      scrollDirection: Axis.horizontal,
      padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 0),
      child: Row(
        children: [
          ...news.followedWebsites.map((followedWebsite) => Padding(
            padding: const EdgeInsets.all(4.0),
            child: FilterChip(
              label: Text(followedWebsite),
              selected: _filters.contains(followedWebsite),
              onSelected: (bool value) {
                setState(() {
                  if (value) {
                    _filters.add(followedWebsite);
                    _isLoading = true;
                    news.fetchArticles(filteredWebsites: _filters).then((value) => {
                      setState(() {
                        _isLoading = false;
                      })
                    });
                  } else {
                    _filters.removeWhere((filter) => filter == followedWebsite);
                    _isLoading = true;
                    news.fetchArticles(filteredWebsites: _filters).then((value) => {
                      setState(() {
                        _isLoading = false;
                      })
                    });
                  }
                });
              },
            ),
          )).toList()
        ]
      ),
    );
  }
}
