
import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:unistudents_app/models/news_website.dart';
import 'package:unistudents_app/providers/news.dart';

class AvailableWebsiteExpanded extends StatefulWidget {
  final NewsWebsite newsWebsite;
  List<bool> isCheckedList = [];

  AvailableWebsiteExpanded({Key? key, required this.newsWebsite,}) : super(key: key);

  @override
  State<AvailableWebsiteExpanded> createState() => _AvailableWebsiteExpandedState();
}

class _AvailableWebsiteExpandedState extends State<AvailableWebsiteExpanded> {
  @override
  Widget build(BuildContext context) {
    final news = Provider.of<News>(context, listen: false);

    for (var i = 0; i < widget.newsWebsite.departments.length; i++) {
      widget.isCheckedList.add(news.followedWebsites.contains(widget.newsWebsite.departments[i].id));
    }

    void _onFollowChanged(bool newValue, int index) => setState(() {
      widget.isCheckedList[index] = newValue;

      if (newValue) {
        news.followWebsite(widget.newsWebsite.departments[index].id);
      } else {
        news.unfollowWebsite(widget.newsWebsite.departments[index].id);
      }
      print(news.followedWebsites);
    });

    return Card(
      elevation: 0,
      shape: RoundedRectangleBorder(
        borderRadius: BorderRadius.circular(17),
      ),
      child: Container(
        padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 12),
        child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              ListTile(
                title: Text(
                  widget.newsWebsite.alias,
                  style: const TextStyle(
                    color: Colors.black,
                    fontWeight: FontWeight.w600,
                    fontFamily: 'Roboto',
                    fontSize: 18,
                  ),
                ),
                leading: Image.asset(widget.newsWebsite.icon.replaceFirst('/', ''), height: 35,),
              ),
              const SizedBox(height: 15,),
              ListView.builder(
                shrinkWrap: true,
                physics: const ClampingScrollPhysics(),
                itemCount: widget.newsWebsite.departments.length,
                itemBuilder: (ctx, j) => CheckboxListTile(
                    title: Text(widget.newsWebsite.departments[j].alias),
                    subtitle: Text(widget.newsWebsite.departments[j].id),
                    value: widget.isCheckedList[j],
                    controlAffinity: ListTileControlAffinity.leading,
                    onChanged: (bool? status) {
                      _onFollowChanged(status!, j);
                    }
                ),
              ),
            ]
        ),
      ),
    );
  }
}