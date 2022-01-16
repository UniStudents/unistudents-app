import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:unistudents_app/providers/news.dart';
import 'package:flutter/material.dart';
import 'package:scroll_to_index/scroll_to_index.dart';

class BottomSheetItem extends StatefulWidget {
  Widget image;
  String title;
  VoidCallback onTap;

  BottomSheetItem(
      {Key? key, required this.image, required this.title, required this.onTap})
      : super(key: key);

  @override
  State<BottomSheetItem> createState() => _BottomSheetItemState();
}

class _BottomSheetItemState extends State<BottomSheetItem> {

  @override
  Widget build(BuildContext context) {
    return TextButton(
      onPressed: widget.onTap,
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
              child: widget.image
          ),
          const Padding(padding: EdgeInsets.all(10)),
          Expanded(
              child: Text(
                widget.title,
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
