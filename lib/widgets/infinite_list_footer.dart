import 'package:flutter/material.dart';

class InfiniteListFooter extends StatelessWidget {
  final bool isLoading;
  final bool foundLastPage;

  const InfiniteListFooter({
    Key? key,
    required this.isLoading,
    required this.foundLastPage
  }) : super(key: key);

  @override
  Widget build(BuildContext context) {
    if (isLoading && !foundLastPage) {
      return const Padding(
        padding: EdgeInsets.only(
          top: 16,
          bottom: 16,
        ),
        child: Center(
          child: CircularProgressIndicator(),
        ),
      );
    } else {
      return const SizedBox.shrink();
    }
  }
}
