import 'package:flutter/material.dart';
import 'package:flutter_screenutil/flutter_screenutil.dart';
import 'package:provider/provider.dart';
import 'package:unistudents_app/core/local/locals.dart';
import 'package:unistudents_app/providers/news.dart';
import 'package:flutter/material.dart';
import 'package:scroll_to_index/scroll_to_index.dart';
import 'package:unistudents_app/providers/notifications.dart';
import 'package:unistudents_app/providers/theme.dart';

void showNotificationBSModal(BuildContext context) {
  showModalBottomSheet(
      context: context,
      isScrollControlled: true,
      shape: const RoundedRectangleBorder(
          borderRadius: BorderRadius.only(topLeft: Radius.circular(20), topRight: Radius.circular(20))
      ),
      builder: (context) => StatefulBuilder(
          builder: (BuildContext context, setState) => NotificationsBSModal(setState: setState)
      )
  );
}

class NotificationsBSModal extends StatefulWidget {
  @override
  State<StatefulWidget> createState() => _NotificationsBSModal();

  const NotificationsBSModal({Key? key, required this.setState}) : super(key: key);

  final StateSetter setState;
}

class _NotificationsBSModal extends State<NotificationsBSModal> {

  @override
  Widget build(BuildContext context) {
    var prov = Provider.of<NotificationProvider>(context, listen: false);
    var _isDarkMode = Theme.of(context).brightness == Brightness.dark;

    return Wrap(
      children: [
        Column(
          children: [
            // Simple design
            Padding(
              padding: EdgeInsets.only(top: 12.h),
              child: Container(
                height: 2.h,
                width: 56.w,
                decoration: BoxDecoration(
                    borderRadius: BorderRadius.circular(20.0),
                    color: Colors.grey[500]),
              ),
            ),

            Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                // Title
                Padding(
                  padding: EdgeInsets.only(left: 28.w, top: 18.h),
                  child: Text(
                    Locals.of(context)!.profileNotifications,
                    style: TextStyle(
                        fontSize: 17.sp,
                        color: _isDarkMode
                            ? const Color(0x99FFFFFF)
                            : const Color(0x99000000)),
                  ),
                ),

                // Options
                Padding(
                  padding: EdgeInsets.only(left: 40.w, right: 45.w, top: 37.h),
                  child: Column(
                    children: [
                      // All
                      Row(
                        children: [
                          Flexible(
                              fit: FlexFit.tight,
                              child: Padding(
                                padding: EdgeInsets.only(left: 20.w, top: 35.h),
                                child: Text(
                                  Locals.of(context)!.profileNotificationsAll,
                                  style: TextStyle(
                                      color: _isDarkMode
                                          ? const Color(0xD9FFFFFF)
                                          : const Color(0xD9000000),
                                    fontSize: 17.sp,
                                    fontWeight: FontWeight.w700
                                  ),
                                ),
                              ),
                          ),

                          Padding(
                            padding: EdgeInsets.only(right: 20.w),
                            child: Checkbox(
                              value: prov.isAllEnabled(),
                              onChanged: (value) {
                                widget.setState(() {
                                  if(value != null) {
                                    prov.changeAll(value);
                                  }
                                });
                              },
                            ),
                          ),
                        ],
                      ),

                      Padding(padding: EdgeInsets.only(top: 30.h)),

                      // Grades
                      Row(
                        children: [
                          Flexible(
                            fit: FlexFit.tight,
                            child: Padding(
                              padding: EdgeInsets.only(left: 20.w, top: 35.h),
                              child: Text(
                                Locals.of(context)!.profileNotificationsGrades,
                                style: TextStyle(
                                    color: _isDarkMode
                                        ? const Color(0xD9FFFFFF)
                                        : const Color(0xD9000000),
                                    fontSize: 17.sp,
                                    fontWeight: FontWeight.w700
                                ),
                              ),
                            ),
                          ),

                          Padding(
                            padding: EdgeInsets.only(right: 20.w),
                            child: Checkbox(
                              value: prov.mode[0] == "1",
                              onChanged: (value) {
                                widget.setState(() {
                                  if(value != null) {
                                    prov.change(value, 0);
                                  }
                                });
                              },
                            ),
                          ),
                        ],
                      ),


                      Padding(padding: EdgeInsets.only(top: 30.h)),

                      // News
                      Row(
                        children: [
                          Flexible(
                            fit: FlexFit.tight,
                            child: Padding(
                              padding: EdgeInsets.only(left: 20.w, top: 35.h),
                              child: Text(
                                Locals.of(context)!.profileNotificationsNews,
                                style: TextStyle(
                                    color: _isDarkMode
                                        ? const Color(0xD9FFFFFF)
                                        : const Color(0xD9000000),
                                    fontSize: 17.sp,
                                    fontWeight: FontWeight.w700
                                ),
                              ),
                            ),
                          ),

                          Padding(
                            padding: EdgeInsets.only(right: 20.w),
                            child: Checkbox(
                              value: prov.mode[1] == "1",
                              onChanged: (value) {
                                widget.setState(() {
                                  if(value != null) {
                                    prov.change(value, 1);
                                  }
                                });
                              },
                            ),
                          ),
                        ],
                      ),

                      Padding(padding: EdgeInsets.only(top: 30.h)),

                    ],
                  ),
                ),

                Padding(
                  padding: EdgeInsets.fromLTRB(24.w, 40.h, 24.w, 40.h),
                  // child: TextButton(
                  //   child: Text(Locals.of(context)!.cancel),
                  //   onPressed: () => Navigator.of(context).pop(),
                  // ),
                ),
              ],
            ),
          ],
        )
      ],
    );
  }
}