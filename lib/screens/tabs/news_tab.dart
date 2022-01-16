import 'dart:async';

import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:scroll_to_index/scroll_to_index.dart';
import 'package:unistudents_app/providers/news.dart';

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
    if (_isInit) {
      Provider.of<News>(context).fetchArticles().then((_) {
        setState(() {
          _isLoading = false;
        });
      });
    }

    _isInit = false;
    super.didChangeDependencies();
  }

  @override
  Widget build(BuildContext context) {
    // Scroll to the top & Refresh
    WidgetsBinding.instance!.addPostFrameCallback((_) {
      if (widget.gotoTop) {
        controller.scrollToIndex(0,
            preferPosition: AutoScrollPosition.begin,
            duration: const Duration(milliseconds: 500));
      }
    });

    final news = Provider.of<News>(context, listen: false);

    Widget? w;
    if(news.subscribedWebsites.isEmpty) {
      // TODO - Show subscribe button
    }
    else if(_isLoading) {
      w = const Center(child: CircularProgressIndicator());
    }
    else {
      // Show articles
      final articles = news.articles;
      w = Container(
          padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 15),
          child: ListView.builder(
              scrollDirection: Axis.vertical,
              controller: controller,
              itemCount: articles.length,
              itemBuilder: (ctx, i) => ArticleWidget(article: articles[i])));
    }

    return Scaffold(body: w);
  }
}
