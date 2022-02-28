import 'package:flutter/material.dart';
import 'package:flutter_screenutil/flutter_screenutil.dart';
import 'package:provider/provider.dart';
import 'package:unistudents_app/core/local/locals.dart';
import 'package:unistudents_app/providers/news.dart';
import 'package:flutter/material.dart';
import 'package:scroll_to_index/scroll_to_index.dart';
import 'package:unistudents_app/providers/theme.dart';

void showThemeBSModal(BuildContext context) {
  showModalBottomSheet(
      context: context,
      isScrollControlled: true,
      shape: RoundedRectangleBorder(
          borderRadius: BorderRadius.only(
              topLeft: Radius.circular(20.r), topRight: Radius.circular(20.r))),
      builder: (context) => StatefulBuilder(
          builder: (BuildContext context, setState) =>
              ThemeBSModal(setState: setState)));
}

class ThemeBSModal extends StatefulWidget {
  @override
  State<StatefulWidget> createState() => _ThemeBSModal();

  const ThemeBSModal({Key? key, required this.setState}) : super(key: key);

  final StateSetter setState;
}

class _ThemeBSModal extends State<ThemeBSModal> {
  @override
  Widget build(BuildContext context) {
    var prov = Provider.of<ThemeProvider>(context, listen: false);
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
                    Locals.of(context)!.profileTheme,
                    style: TextStyle(
                        fontSize: 17.sp,
                        color: _isDarkMode
                            ? const Color(0x99FFFFFF)
                            : const Color(0x99000000)),
                  ),
                ),

                // Themes
                Padding(
                  padding: EdgeInsets.only(left: 45.w, right: 45.w, top: 37.h),
                  child: Row(
                    children: [
                      // Light
                      Column(
                        children: [
                          Image(
                            image: const AssetImage('assets/theme_light.png'),
                            width: 88.w,
                            height: 79.w,
                          ),
                          Padding(
                            padding: EdgeInsets.only(top: 20.h, bottom: 17.h),
                            child: Text(
                              Locals.of(context)!.profileThemeLight,
                              style: TextStyle(
                                  color: _isDarkMode
                                      ? const Color(0xD9FFFFFF)
                                      : const Color(0xD9000000)),
                            ),
                          ),
                          Radio<int>(
                            value: 1,
                            activeColor: const Color(0xFF4388FF),
                            groupValue: prov.themeNum,
                            onChanged: (value) {
                              widget.setState(() {
                                prov.setTheme(1);
                              });
                            },
                          )
                        ],
                      ),

                      Padding(padding: EdgeInsets.only(right: 37.w)),

                      // Dark
                      Column(
                        children: [
                          Image(
                            image: const AssetImage('assets/theme_dark.png'),
                            width: 88.w,
                            height: 79.w,
                          ),
                          Padding(
                            padding: EdgeInsets.only(top: 20.h, bottom: 17.h),
                            child: Text(
                              Locals.of(context)!.profileThemeDark,
                              style: TextStyle(
                                  color: _isDarkMode
                                      ? const Color(0xD9FFFFFF)
                                      : const Color(0xD9000000)),
                            ),
                          ),
                          Radio<int>(
                            value: 2,
                            activeColor: const Color(0xFF4388FF),
                            groupValue: prov.themeNum,
                            onChanged: (value) {
                              widget.setState(() {
                                prov.setTheme(2);
                              });
                            },
                          )
                        ],
                      ),

                      Padding(padding: EdgeInsets.only(right: 37.w)),

                      // Dark
                      Column(
                        children: [
                          Image(
                            image: const AssetImage('assets/theme_system.png'),
                            width: 88.w,
                            height: 79.w,
                          ),
                          Padding(
                            padding: EdgeInsets.only(top: 20.h, bottom: 17.h),
                            child: Text(
                              Locals.of(context)!.profileThemeSystem,
                              style: TextStyle(
                                  color: _isDarkMode
                                      ? const Color(0xD9FFFFFF)
                                      : const Color(0xD9000000)),
                            ),
                          ),
                          Radio<int>(
                            value: 0,
                            activeColor: const Color(0xFF4388FF),
                            groupValue: prov.themeNum,
                            onChanged: (value) {
                              widget.setState(() {
                                prov.setTheme(0);
                              });
                            },
                          )
                        ],
                      ),
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
