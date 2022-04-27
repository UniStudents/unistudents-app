import 'package:flutter/material.dart';
import 'package:flutter_screenutil/flutter_screenutil.dart';
import 'package:unistudents_app/core/colors.dart';
import 'package:unistudents_app/models/article.dart';

import '../core/local/locals.dart';
import 'bottomsheets/article_bs.dart';
import 'custom_web_view.dart';

class ArticleWidget extends StatelessWidget {
  final Article article;

  const ArticleWidget({Key? key, required this.article}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    var _isDarkMode = Theme.of(context).brightness == Brightness.dark;

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
            padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 0),
            child: Column(
              children: [
                // Source|time & More
                Row(
                  children: [
                    Flexible(
                        fit: FlexFit.tight,
                        child: Text('${article.source} | ${article.getElapsedTime(context)}',
                            style: TextStyle(
                              fontSize: 12.sp,
                              color: UniColors.getTextHalf(_isDarkMode)
                            ),
                        )),
                    SizedBox(
                      height: 40.h,
                      width: 40.w,
                      child: IconButton(
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
                      ),
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
                            style: TextStyle(
                              color: UniColors.getText1(_isDarkMode),
                              fontWeight: FontWeight.w700,
                              fontFamily: 'Roboto',
                              fontSize: 14.sp,
                            ),
                          ),

                          Padding(padding: EdgeInsets.all(2.h)),

                          // Attachments
                          article.attachments.isNotEmpty
                              ? _buildAttachmentChip(context, _navigateToWebView)
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
                          height: 80.h,
                          width: 80.w,
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
                        padding: EdgeInsets.symmetric(horizontal: 0, vertical: 5.h),
                        child: Text(
                          '#' + article.categories[j] + " ",
                          style: TextStyle(
                            fontSize: 12.sp,
                            color: UniColors.getTextHalf(_isDarkMode)
                          ),
                        )
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

  ActionChip _buildAttachmentChip(BuildContext context, void Function(BuildContext buildContext, String domain, String url) _navigateToWebView) {
    return ActionChip(
      label: Text('${Locals.of(context)!.articleWidgetAttachments} (${article.attachments.length})', style: TextStyle(fontSize: 14.sp),),
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
      }
    );
  }
}