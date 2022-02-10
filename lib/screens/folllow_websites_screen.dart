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
  var _isLoading = false;

  @override
  void didChangeDependencies() {
    final news = Provider.of<News>(context, listen: false);
    if (_isInit) {
      if (news.availableWebsites.isEmpty) {
        _isLoading = true;
        news.fetchWebsites("UNIPI").then((_) {
          setState(() {
            _isLoading = false;
          });
        });
      }
    }

    _isInit = false;
    super.didChangeDependencies();
  }

  @override
  Widget build(BuildContext context) {
    final news = Provider.of<News>(context, listen: false);
    final availableWebsites = news.availableWebsites;

    Widget expandedCards = ListView.separated(
        padding: EdgeInsets.only(top: 20, bottom: 20),
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
      appBar: AppBar(
        title: const Text('Websites'),
        actions: [
          IconButton(
            icon: const Icon(Icons.check),
            onPressed: () {
              Navigator.pop(context);
            },
          ),
        ],
      ),
      body: _isLoading
        ? const Center(child: CircularProgressIndicator())
        : Container(
          padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 0),
          child: expandedCards
          // child: minimizedCards
        ),
    );
  }
}
