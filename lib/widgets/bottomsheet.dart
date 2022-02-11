import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:unistudents_app/providers/news.dart';
import 'package:flutter/material.dart';
import 'package:scroll_to_index/scroll_to_index.dart';

class BottomSheetModal extends StatelessWidget {

  const BottomSheetModal({Key? key, required this.children, required this.title}) : super(key: key);

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
              padding: const EdgeInsets.all(30.0),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    title,
                    style: const TextStyle(
                      color: Colors.black,
                      fontWeight: FontWeight.w800,
                      fontFamily: 'Roboto',
                      fontSize: 20,
                    ),
                  ),

                  const Padding(padding: EdgeInsets.all(15.0)),

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

class BottomSheetItem extends StatelessWidget {

  const BottomSheetItem({
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
          shape: const RoundedRectangleBorder(
              borderRadius: BorderRadius.all(Radius.circular(15))
          )
      ),
      child: Container(
        padding: const EdgeInsets.all(20.0),
        child: Row(children: [
          ClipRRect(
              borderRadius: BorderRadius.circular(20),
              child: image
          ),
          const Padding(padding: EdgeInsets.all(10)),
          Expanded(
            child: Text(
              title,
              style: const TextStyle(
                color: Colors.black,
                fontWeight: FontWeight.w700,
                fontFamily: 'Roboto',
                fontSize: 18,
              ),
            ),
          )
        ]),
      ),
    );
  }
}
