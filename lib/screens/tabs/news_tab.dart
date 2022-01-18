import 'dart:async';

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
    final news = Provider.of<News>(context);
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

    Widget? body;
    if(news.followedWebsites.isEmpty) {
      // TODO - Show subscribe button
      print('isempty');
    }
    else if(_isLoading) {
      body = const Center(child: CircularProgressIndicator());
    }
    else {
      // Show articles
      final articles = news.articles;
      body = Column(
        mainAxisSize: MainAxisSize.min,
        children: [
          SizedBox(
            height: 50,
            child: ListView.builder(
              // shrinkWrap: true,
              padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 0),
              scrollDirection: Axis.horizontal,
              itemCount: news.followedWebsites.length,
              itemBuilder: (ctx, i) => Card(
                elevation: 0,
                shape: RoundedRectangleBorder(
                  borderRadius: BorderRadius.circular(10),
                ),
                child: Container(
                  padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 0),
                  child: Text(news.followedWebsites[i])
                )
              ),
            ),
          ),
          Expanded(
            child: Container(
              padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 15),
              child: ListView.builder(
                scrollDirection: Axis.vertical,
                controller: controller,
                itemCount: articles.length,
                itemBuilder: (ctx, i) => ArticleWidget(article: articles[i])
              )
            ),
          ),
        ],
      );
    }

    return Scaffold(
      appBar: AppBar(
        title: const Text('Νέα'),
        actions: [
          IconButton(
            icon: const Icon(Icons.add),
            onPressed: () {
              Navigator.push(
                context,
                MaterialPageRoute(builder: (ctx) => const FollowWebsitesScreen())
              ).then((value) => {
                setState(() {
                  _isLoading = true;
                  news.fetchArticles().then((value) => {
                    setState(() {
                      _isLoading = false;
                    })
                  });
                })
              });
            },
          ),
        ],
      ),
      body: body
    );
  }
}
