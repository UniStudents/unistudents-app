import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:unistudents_app/models/article.dart';
import 'package:unistudents_app/providers/local_provider.dart';

import '../core/local/locals.dart';
import 'bottomsheet_item.dart';
import 'bottomsheet_modal.dart';

class ArticleWidget extends StatelessWidget {
  final Article article;

  // Timer.periodic(new Duration(seconds: 1), (timer) {
  //    debugPrint(timer.tick.toString());
  // });

  const ArticleWidget({Key? key, required this.article}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return Card(
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
                    child: Text('${article.source} | ${article.getElapsedTime()}',
                        style: const TextStyle(fontSize: 12)
                    )),
                IconButton(
                  icon: const Icon(Icons.more_horiz),
                  onPressed: () {
                    showModalBottomSheet(
                        context: context,
                        isScrollControlled: true,
                        shape: RoundedRectangleBorder(
                            borderRadius: BorderRadius.circular(20.0)
                        ),
                        builder: (builder) => BottomSheetModal(
                            title: Locals.of(context)!.articleWidgetActionsTitle,
                            children: [
                              BottomSheetItem(
                                  image: const Icon(Icons.favorite_border),
                                  title: Locals.of(context)!.articleWidgetActionsSave,
                                  onTap: () {}
                              ),
                              BottomSheetItem(
                                  image: const Icon(Icons.share),
                                  title: Locals.of(context)!.articleWidgetActionsShare,
                                  onTap: () {}
                              ),
                              BottomSheetItem(
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
                          showModalBottomSheet(
                              context: context,
                              isScrollControlled: true,
                              shape: RoundedRectangleBorder(
                                  borderRadius: BorderRadius.circular(20.0)
                              ),
                              builder: (builder) => BottomSheetModal(
                                  title: Locals.of(context)!.articleWidgetAttachments,
                                  children: article.attachments.map((e) =>
                                      BottomSheetItem(
                                          image: Icon(e.icon),
                                          title: e.text,
                                          onTap: () {
                                            // TODO - open attachment
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
                        child: Text(article.categories[j]));
                  }),
            )
          ],
        ),
      ),
    );
  }
}