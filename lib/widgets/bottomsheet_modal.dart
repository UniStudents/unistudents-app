import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:unistudents_app/providers/news.dart';
import 'package:flutter/material.dart';
import 'package:scroll_to_index/scroll_to_index.dart';

class BottomSheetModal extends StatefulWidget {
  String title;
  List<Widget> children;

  BottomSheetModal({Key? key, required this.children, required this.title}) : super(key: key);

  @override
  State<BottomSheetModal> createState() => _BottomSheetModalState();
}

class _BottomSheetModalState extends State<BottomSheetModal> {

  @override
  Widget build(BuildContext context) {
    List<Widget> items = [];

    for (var element in widget.children) {
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
                    widget.title,
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
