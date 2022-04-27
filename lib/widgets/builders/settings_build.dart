import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';
import 'package:flutter_screenutil/flutter_screenutil.dart';
import 'package:unistudents_app/core/colors.dart';

class SettingsModal extends StatelessWidget {
  const SettingsModal({Key? key, this.title, required this.children})
      : super(key: key);

  final String? title;
  final List<SettingsItem> children;

  @override
  Widget build(BuildContext context) {
    var _isDarkMode = Theme.of(context).brightness == Brightness.dark;

    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        title != null
            ? Padding(
                padding: EdgeInsets.only(left: 5.h),
                child: Text(
                  title!,
                  style: TextStyle(
                    fontSize: 15.sp,
                    color: UniColors.getTextHalf(_isDarkMode),
                  ),
                ),
              )
            : Padding(padding: EdgeInsets.only(top: 10.h)),

        Padding(padding: EdgeInsets.only(top: 15.h)),

        Card(
          elevation: 0,
          shape:
              RoundedRectangleBorder(borderRadius: BorderRadius.circular(10.r)),
          child: Column(
            children: children,
          ),
        )
      ],
    );
  }
}

class SettingsItem extends StatelessWidget {
  SettingsItem(
      {Key? key,
      required this.icon,
      required this.title,
      this.value,
      this.iconColor,
      required this.onTap})
      : super(key: key);

  final IconData icon;
  final String title;
  final String? value;
  Color? iconColor;
  final VoidCallback onTap;

  @override
  Widget build(BuildContext context) {
    var _isDarkMode = Theme.of(context).brightness == Brightness.dark;
    iconColor ??= UniColors.getUniBlue(_isDarkMode);

    return GestureDetector(
        onTap: onTap,
        behavior: HitTestBehavior.translucent,
        child: Container(
          padding: EdgeInsets.only(left: 29.w, right: 29.w, top: 33.h, bottom: 33.h),
          child: Row(
            children: [
              Icon(
                icon,
                size: 23.w,
                color: iconColor,
              ),

              Padding(padding: EdgeInsets.only(left: 25.w)),

              value != null
                  ? Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text(
                          title,
                          style: TextStyle(
                            fontSize: 17.sp,
                          ),
                        ),

                        Padding(padding: EdgeInsets.only(top: 5.w)),

                        Text(
                          value!,
                          style: TextStyle(
                              fontSize: 12.sp,
                              color: UniColors.getTextHalf(_isDarkMode)
                          ),
                        ),
                      ],
                    )
                  : Text(
                      title,
                      style: TextStyle(
                        fontSize: 17.sp,
                      ),
                    ),
            ],
          ),
        ));
  }
}
