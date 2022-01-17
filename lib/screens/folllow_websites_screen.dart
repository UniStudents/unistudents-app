import 'dart:html';

import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:unistudents_app/providers/news.dart';
import 'package:unistudents_app/widgets/available_website_expanded.dart';
import 'package:unistudents_app/widgets/available_website_minimized.dart';

class FollowWebsitesScreen extends StatefulWidget {
  static const String id = 'follow_websites_screen';
  const FollowWebsitesScreen({Key? key}) : super(key: key);

  @override
  _FollowWebsitesScreenState createState() => _FollowWebsitesScreenState();
}

class _FollowWebsitesScreenState extends State<FollowWebsitesScreen> {
  var _isInit = true;
  var _isLoading = true;

  @override
  void didChangeDependencies() {
    if (_isInit) {
      Provider.of<News>(context).fetchWebsites("unipi.gr").then((_) {
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
    final news = Provider.of<News>(context, listen: false);
    final availableWebsites = news.availableWebsites;
    print(availableWebsites.length);

    Widget expandedCards = ListView.separated(
        itemCount: availableWebsites.length,
        separatorBuilder: (ctx, i) => const SizedBox(height: 20,),
        itemBuilder: (ctx, i) => AvailableWebsiteExpanded(
          newsWebsite: availableWebsites[i],
        )
    );

    Widget minimizedCards = ListView.separated(
        itemCount: availableWebsites.length,
        separatorBuilder: (ctx, i) => const SizedBox(height: 20,),
        itemBuilder: (ctx, i) => AvailableWebsiteMinimized(
          newsWebsite: availableWebsites[i],
        )
    );

    return Scaffold(
      body: _isLoading
        ? const Center(child: CircularProgressIndicator())
        : Container(
          padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 15),
          child: expandedCards
          // child: minimizedCards
        ),
    );
  }
}
