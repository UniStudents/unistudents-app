import 'package:expandable/expandable.dart';
import 'package:flutter/material.dart';
import 'package:flutter_screenutil/flutter_screenutil.dart';
import 'package:unistudents_app/models/news_website.dart';
import 'package:unistudents_app/widgets/available_website_expanded.dart';

class AvailableWebsiteMinimized extends StatelessWidget {
  final NewsWebsite newsWebsite;

  const AvailableWebsiteMinimized({Key? key, required this.newsWebsite,}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return Card(
        elevation: 0,
        shape: RoundedRectangleBorder(
          borderRadius: BorderRadius.circular(17.r),
        ),
        child: ExpandablePanel(
          header: Container(
            padding: EdgeInsets.symmetric(horizontal: 12.w, vertical: 12.h),
            child: Row(
              children: [
                Image.asset(newsWebsite.icon.replaceFirst('/', ''), height: 45.h, width: 45.w,),
                SizedBox(width: 20.w,),
                Flexible(
                  fit: FlexFit.tight,
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    mainAxisAlignment: MainAxisAlignment.spaceBetween,
                    children: [
                      Text(
                        newsWebsite.alias,
                        style: TextStyle(
                          color: Colors.black,
                          fontWeight: FontWeight.w600,
                          fontFamily: 'Roboto',
                          fontSize: 16.sp,
                        ),
                      ),
                      Text(
                        newsWebsite.departments.length.toString() + " websites",
                        style: TextStyle(
                          color: Colors.black45,
                          fontFamily: 'Roboto',
                          fontSize: 16.sp,
                        ),
                      ),
                    ],
                  ),
                ),
              ],
            ),
          ),
          collapsed: Container(),
          expanded: AvailableWebsiteExpanded(
            newsWebsite: newsWebsite,
          ),
          theme: const ExpandableThemeData(
            hasIcon: false,
            useInkWell: false
          ),
        )
    );
  }
}
