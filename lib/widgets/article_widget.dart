import 'package:flutter/material.dart';
import 'package:unistudents_app/models/article.dart';

import '../core/local/locals.dart';
import 'bottomsheets/article_bs.dart';
import 'custom_web_view.dart';

class ArticleWidget extends StatelessWidget {
  final Article article;

  const ArticleWidget({Key? key, required this.article}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    void _navigateToWebView(BuildContext buildContext, String domain, String url) async {
      final result = await Navigator.of(context).push(
          MaterialPageRoute<String>(
              builder: (ctx) => CustomWebView(
                barTitle: domain,
                url: url,
              ),
              fullscreenDialog: true
          )
      );
    }

    return Container(
      padding: const EdgeInsets.fromLTRB(12, 0, 12, 12),
      child: GestureDetector(
        onTap: () => _navigateToWebView(context, article.source, article.link),
        child: Card(
          elevation: 0,
          shape: RoundedRectangleBorder(
            borderRadius: BorderRadius.circular(10),
          ),
          child: Container(
            padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 4),
            child: Column(
              children: [
                // Source|time & More
                Row(
                  children: [
                    Flexible(
                        fit: FlexFit.tight,
                        child: Text('${article.source} | ${article.getElapsedTime(context)}',
                            style: const TextStyle(fontSize: 12)
                        )),
                    IconButton(
                      icon: const Icon(Icons.more_horiz),
                      onPressed: () {
                        showArticleBSModal(
                          context,
                            ArticleBSModal(
                                title: Locals.of(context)!.articleWidgetActionsTitle,
                                children: [
                                  ArticleBSItem(
                                      image: const Icon(Icons.favorite_border),
                                      title: Locals.of(context)!.articleWidgetActionsSave,
                                      onTap: () {}
                                  ),
                                  ArticleBSItem(
                                      image: const Icon(Icons.share),
                                      title: Locals.of(context)!.articleWidgetActionsShare,
                                      onTap: () {}
                                  ),
                                  ArticleBSItem(
                                      image: const Icon(Icons.info),
                                      title: Locals.of(context)!.articleWidgetActionsReport,
                                      onTap: () {}
                                  )
                                ]
                            )
                        );
                      },
                    )
                  ],
                ),

                // Title & Attachments & Image
                Row(
                  mainAxisAlignment: MainAxisAlignment.start,
                  children: [
                    Flexible(
                      fit: FlexFit.tight,
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          // Title
                          Text(
                            article.title,
                            style: const TextStyle(
                              color: Colors.black,
                              fontWeight: FontWeight.w700,
                              fontFamily: 'Roboto',
                              fontSize: 18,
                            ),
                          ),

                          const Padding(padding: EdgeInsets.all(5.0)),

                          // Attachments
                          article.attachments.isNotEmpty
                              ? TextButton.icon(
                            icon: const Icon(Icons.attachment),
                            label: Text('${Locals.of(context)!.articleWidgetAttachments} (${article.attachments.length})'),
                            style: ButtonStyle(
                              backgroundColor: MaterialStateProperty.all(Colors.cyan),
                              foregroundColor: MaterialStateProperty.all(Colors.white),
                            ),
                            onPressed: () {
                              showArticleBSModal(
                                  context,
                                  ArticleBSModal(
                                      title: Locals.of(context)!.articleWidgetAttachments,
                                      children: article.attachments.map((e) =>
                                          ArticleBSItem(
                                              image: Icon(e.icon),
                                              title: e.text,
                                              onTap: () {
                                                Navigator.pop(context);
                                                Future.delayed(const Duration(milliseconds: 500), () {
                                                  _navigateToWebView(context, article.source, e.value);
                                                });
                                              }
                                          )
                                      ).toList()
                                  )
                              );
                            },
                          )
                              : Container()
                        ],
                      ),
                    ),

                    // Image
                    article.getFrontalImage() != null
                        ? Column(children: [
                      ClipRRect(
                        borderRadius: BorderRadius.circular(20),
                        child: Image(
                          image: NetworkImage(
                              article.getFrontalImage() ??
                                  ""),
                          height: 80,
                          width: 80,
                        ),
                      )
                    ])
                        : Column()
                  ],
                ),

                // Categories
                SizedBox(
                  height: 30,
                  child: ListView.builder(
                    scrollDirection: Axis.horizontal,
                    itemCount: article.categories.length,
                    itemBuilder: (context, j) {
                      return Padding(
                        padding: const EdgeInsets.all(5.0),
                        child: Text('#' + article.categories[j])
                      );
                    }
                  ),
                )
              ],
            ),
          ),
        ),
      ),
    );
  }
}