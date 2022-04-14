import 'package:flutter/material.dart';
import 'package:flutter_screenutil/flutter_screenutil.dart';
import 'package:unistudents_app/core/local/locals.dart';

void showProgressFilterBSModal(BuildContext context) {
  showModalBottomSheet(
      context: context,
      isScrollControlled: true,
      shape: RoundedRectangleBorder(
          borderRadius: BorderRadius.only(
              topLeft: Radius.circular(20.r), topRight: Radius.circular(20.r))),
      builder: (context) => StatefulBuilder(
          builder: (BuildContext context, setState) =>
              ProgressFilterBSModal(setState: setState)));
}

class ProgressFilterBSModal extends StatefulWidget {
  @override
  State<StatefulWidget> createState() => _ProgressFilter();

  const ProgressFilterBSModal({Key? key, required this.setState}) : super(key: key);

  final StateSetter setState;
}

class _ProgressFilter extends State<ProgressFilterBSModal> {

  Widget buildTitle(bool _isDarkMode, String text) {
    return Padding(
      padding: EdgeInsets.only(bottom: 10.h),
      child: Text(
        text,
        style: TextStyle(
            fontSize: 17.sp,
            fontWeight: FontWeight.w700,
            color: _isDarkMode
                ? const Color(0x99FFFFFF)
                : const Color(0x99000000)),
      ),
    );
  }

  Widget buildItem(bool _isDarkMode, bool isSelected, bool isCheckbox, String text) {
    return Padding(
      padding: EdgeInsets.only(top: 5.h, bottom: 5.h),
      child: Row(
        children: [
          Checkbox(
              value: isSelected,
              onChanged: (value) {}
          ),

          Text("Test")
        ],
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    var _isDarkMode = Theme.of(context).brightness == Brightness.dark;

    var sort = [
      buildTitle(_isDarkMode, Locals.of(context)!.progressFilterSort),

      buildItem(_isDarkMode, true, false, Locals.of(context)!.progressFilterSortHighestGrade),
      buildItem(_isDarkMode, false, false, Locals.of(context)!.progressFilterSortLowerGrade),
      buildItem(_isDarkMode, false, false,Locals.of(context)!.progressFilterSortRecent),
      buildItem(_isDarkMode, false, false, Locals.of(context)!.progressFilterSortOlder),
    ];

    var filters = [
      buildTitle(_isDarkMode, Locals.of(context)!.progressFilterFilter),
      buildItem(_isDarkMode, false, true, Locals.of(context)!.progressFilterFilterRecent),
      buildItem(_isDarkMode, true, true, Locals.of(context)!.progressFilterFilterOlder),
    ];

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

            Padding(
              padding: EdgeInsets.only(left: 28.w, top: 18.h),
              child: SizedBox(
                height: 400.h,
                child: ListView(
                  children: [
                    ...sort,
                    Padding(padding: EdgeInsets.only(top: 33.h)),
                    ...filters,
                  ],
                ),
              ),
            ),
          ],
        )
      ],
    );
  }
}
