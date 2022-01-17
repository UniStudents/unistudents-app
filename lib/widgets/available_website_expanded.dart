
import 'package:flutter/material.dart';
import 'package:unistudents_app/models/news_website.dart';

class AvailableWebsiteExpanded extends StatelessWidget {
  final NewsWebsite newsWebsite;

  const AvailableWebsiteExpanded({Key? key, required this.newsWebsite,}) : super(key: key);

  @override
  Widget build(BuildContext context) {
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
                  newsWebsite.alias,
                  style: const TextStyle(
                    color: Colors.black,
                    fontWeight: FontWeight.w600,
                    fontFamily: 'Roboto',
                    fontSize: 18,
                  ),
                ),
                leading: Image.asset(newsWebsite.icon, height: 35,),
              ),
              const SizedBox(height: 15,),
              ListView.builder(
                shrinkWrap: true,
                itemCount: newsWebsite.departments.length,
                itemBuilder: (ctx, j) => CheckboxListTile(
                    title: Text(newsWebsite.departments[j].alias),
                    subtitle: Text(newsWebsite.departments[j].id),
                    value: true,
                    controlAffinity: ListTileControlAffinity.leading,
                    onChanged: (_) {}
                ),
              ),
            ]
        ),
      ),
    );
  }
}