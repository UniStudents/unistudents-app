import 'package:flutter/material.dart';

class ExpandableCardContainer extends StatefulWidget {
  late bool isExpanded;
  final Widget collapsedChild;
  final Widget expandedChild;

  ExpandableCardContainer({
    Key? key,
    required this.isExpanded,
    required this.collapsedChild,
    required this.expandedChild
  }) : super(key: key);

  @override
  _ExpandableCardContainerState createState() =>
      _ExpandableCardContainerState();
}

class _ExpandableCardContainerState extends State<ExpandableCardContainer> {


  @override
  Widget build(BuildContext context) {
    return GestureDetector(
      child: AnimatedContainer(
        duration: const Duration(milliseconds: 2000),
        curve: Curves.easeInOut,
        child: widget.isExpanded ? widget.expandedChild : widget.collapsedChild,
      ),
      onTap: () {
        setState(() {
          widget.isExpanded = !widget.isExpanded;
        });
      },
    );
  }
}