import 'package:flutter/material.dart';
import 'package:flutter_screenutil/flutter_screenutil.dart';

void showArticleBSModal(BuildContext context, ArticleBSModal modal) {
  showModalBottomSheet(
      context: context,
      isScrollControlled: true,
      shape: const RoundedRectangleBorder(
          borderRadius: BorderRadius.only(topLeft: Radius.circular(20), topRight: Radius.circular(20))
      ),
      builder: (builder) => modal
  );
}

class ArticleBSModal extends StatelessWidget {

  const ArticleBSModal({Key? key, required this.children, required this.title}) : super(key: key);

  final String title;
  final List<Widget> children;

  @override
  Widget build(BuildContext context) {
    List<Widget> items = [];

    for (var element in children) {
      items.add(element);
      items.add(const Padding(padding: EdgeInsets.all(10.0)));
    }

    return Wrap(
      children: [
        Column(
          children: [
            // Simple design
            const Padding(padding: EdgeInsets.fromLTRB(0, 10.0, 0, 0)),
            Container(
              height: 5,
              width: 60,
              decoration: BoxDecoration(
                  borderRadius: BorderRadius.circular(20.0),
                  color: Colors.grey[500]
              ),
            ),

            // Title & Items
            Padding(
              padding: EdgeInsets.all(30.h),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    title,
                    style: TextStyle(
                      color: Colors.black,
                      fontWeight: FontWeight.w800,
                      fontFamily: 'Roboto',
                      fontSize: 19.sp,
                    ),
                  ),
                  Padding(padding: EdgeInsets.all(15.w)),
                  ...items
                ],
              ),
            )
          ],
        )
      ],
    );
  }
}

class ArticleBSItem extends StatelessWidget {

  const ArticleBSItem({
    Key? key,
    required this.image,
    required this.title,
    required this.onTap
  }) : super(key: key);

  final Widget image;
  final String title;
  final VoidCallback onTap;

  @override
  Widget build(BuildContext context) {
    return TextButton(
      onPressed: onTap,
      style: TextButton.styleFrom(
          backgroundColor: Colors.blue.shade50,
          shape: RoundedRectangleBorder(
              borderRadius: BorderRadius.all(Radius.circular(15.r))
          )
      ),
      child: Container(
        padding: EdgeInsets.all(12.h),
        child: Row(children: [
          ClipRRect(
              borderRadius: BorderRadius.circular(20.r),
              child: image
          ),
          Padding(padding: EdgeInsets.all(10.w)),
          Expanded(
            child: Text(
              title,
              style: TextStyle(
                color: Colors.black,
                fontWeight: FontWeight.w700,
                fontFamily: 'Roboto',
                fontSize: 16.sp,
              ),
            ),
          )
        ]),
      ),
    );
  }
}
