import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:unistudents_app/providers/news.dart';
import 'package:scroll_to_index/scroll_to_index.dart';
import 'package:unistudents_app/widgets/bottomsheet_item.dart';
import 'package:unistudents_app/widgets/bottomsheet_modal.dart';

class ArticlesList extends StatefulWidget {
  static const String id = 'news_tab';
  bool gotoTop;

  ArticlesList({Key? key, this.gotoTop = false}) : super(key: key);

  @override
  State<ArticlesList> createState() => _ArticlesListState();
}

class _ArticlesListState extends State<ArticlesList> {
  late AutoScrollController controller;

  @override
  void initState() {
    controller = AutoScrollController(
        viewportBoundaryGetter: () =>
            Rect.fromLTRB(0, 0, 0, MediaQuery.of(context).padding.bottom),
        axis: Axis.vertical);

    super.initState();
  }

  @override
  Widget build(BuildContext context) {
    final news = Provider.of<News>(context, listen: false);
    final articles = news.articles;

    WidgetsBinding.instance!.addPostFrameCallback((_) {
      if (widget.gotoTop) {
        controller.scrollToIndex(0,
            preferPosition: AutoScrollPosition.begin,
            duration: const Duration(milliseconds: 500));
      }
    });

    return Container(
        padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 15),
        child: ListView.builder(
            scrollDirection: Axis.vertical,
            controller: controller,
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
                        // Source|time & More
                        Row(
                          children: [
                            Flexible(
                              fit: FlexFit.tight,
                                child: Text('${articles[i].source} | 1min',
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
                                        title: 'Ενέργειες',
                                        children: [
                                          BottomSheetItem(
                                              image: const Icon(Icons.favorite_border),
                                              title: "Αποθήκευση",
                                              onTap: () {}
                                          ),
                                          BottomSheetItem(
                                              image: const Icon(Icons.share),
                                              title: "Κοινοποίηση",
                                              onTap: () {}
                                          ),
                                          BottomSheetItem(
                                              image: const Icon(Icons.info),
                                              title: "Αναφορά",
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
                                    articles[i].title,
                                    style: const TextStyle(
                                      color: Colors.black,
                                      fontWeight: FontWeight.w700,
                                      fontFamily: 'Roboto',
                                      fontSize: 18,
                                    ),
                                  ),

                                  const Padding(padding: EdgeInsets.all(5.0)),

                                  // Attachments
                                  articles[i].attachments.isNotEmpty
                                      ? TextButton.icon(
                                    icon: const Icon(Icons.attachment),
                                    label: const Text('Συνημμένα'),
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
                                              title: 'Συννημένα',
                                              children: articles[i].attachments.map((e) =>
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
                            articles[i].getFrontalImage() != null
                                ? Column(children: [
                                    ClipRRect(
                                      borderRadius: BorderRadius.circular(20),
                                      child: Image(
                                        image: NetworkImage(
                                            articles[i].getFrontalImage() ??
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
                              itemCount: articles[i].categories.length,
                              itemBuilder: (context, j) {
                                return Padding(
                                    padding: const EdgeInsets.all(5.0),
                                    child: Text(articles[i].categories[j]));
                              }),
                        )
                      ],
                    ),
                  ),
                )));
  }
}
