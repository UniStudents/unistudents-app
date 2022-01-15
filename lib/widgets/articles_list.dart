import 'dart:js';

import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:unistudents_app/providers/news.dart';

class ArticlesList extends StatelessWidget {

  @override
  Widget build(BuildContext context) {
    final news = Provider.of<News>(context, listen: false);
    final articles = news.articles;
    print(articles.length);
    return Container(
        padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 15),
        child: ListView.builder(
            itemCount: articles.length,
            itemBuilder: (ctx, i) => Card(
              elevation: 0,
              shape: RoundedRectangleBorder(
                borderRadius: BorderRadius.circular(10),
              ),
              child: Container(
                padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 4),
                child: Column(
                  children: [
                    Row(
                      children: [
                        Text(
                          articles[i].source + ' | 1min',
                          style: const TextStyle(
                            fontSize: 12,
                          ),
                        ),
                        const Spacer(),
                        IconButton(
                          icon: const Icon(Icons.more_vert),
                          onPressed: () {},
                        )
                      ],
                    ),
                    Row(
                      children: [
                        Flexible(
                          child: Column(
                            crossAxisAlignment: CrossAxisAlignment.start,
                            children: [
                              Text(
                                articles[i].title,
                                style: const TextStyle(
                                  color: Colors.black,
                                  fontWeight: FontWeight.w700,
                                  fontFamily: 'Roboto',
                                  fontSize: 18,
                                ),
                              ),
                              TextButton(onPressed: () {}, child: Text('Συνημμένα'))
                            ],
                          ),
                        ),
                        Column(
                          children: [
                            ClipRRect(
                              borderRadius: BorderRadius.circular(20),
                              child: const Image(
                                image: NetworkImage('https://flutter.github.io/assets-for-api-docs/assets/widgets/owl.jpg'),
                                height: 80,
                                width: 80,
                              ),
                            )
                          ],
                        )
                      ],
                    ),
                    Container(
                      height: 30,
                      child: ListView(
                        scrollDirection: Axis.horizontal,
                        children: const [
                          Text('#Ανακοινώσεις'),
                          Padding(padding: EdgeInsets.all(5.00)),
                          Text('#Εκδηλώσεις'),
                          Padding(padding: EdgeInsets.all(5.00)),
                          Text('#Συνέδρια – Ημερίδες'),
                        ],
                      ),
                    )
                  ],
                ),
              ),
            )
        )
    );
  }
}