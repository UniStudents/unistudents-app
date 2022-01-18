import 'package:flutter/material.dart';
import 'package:unistudents_app/models/news_website.dart';

class AvailableWebsiteMinimized extends StatelessWidget {
  final NewsWebsite newsWebsite;

  const AvailableWebsiteMinimized({Key? key, required this.newsWebsite,}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return Card(
        elevation: 0,
        shape: RoundedRectangleBorder(
          borderRadius: BorderRadius.circular(17),
        ),
        child: Container(
          padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 12),
          child: Row(
            children: [
              Image.asset(newsWebsite.icon.replaceFirst('/', ''), height: 50, width: 50,),
              const SizedBox(width: 20,),
              Flexible(
                fit: FlexFit.tight,
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  mainAxisAlignment: MainAxisAlignment.spaceBetween,
                  children: [
                    Text(
                      newsWebsite.alias,
                      style: const TextStyle(
                        color: Colors.black,
                        fontWeight: FontWeight.w600,
                        fontFamily: 'Roboto',
                        fontSize: 18,
                      ),
                    ),
                    Text(
                      newsWebsite.departments.length.toString() + " websites",
                      style: const TextStyle(
                        color: Colors.black45,
                        fontFamily: 'Roboto',
                        fontSize: 18,
                      ),
                    ),
                  ],
                ),
              ),
            ],
          ),
        )
    );
  }
}
