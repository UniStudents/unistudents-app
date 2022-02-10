import 'package:flutter/material.dart';

class WebsiteFilterBar extends StatelessWidget {
  final List<String> followedWebsites;
  final List<String> filters;
  final Function updateFilters;

  const WebsiteFilterBar({
    Key? key,
    required this.followedWebsites,
    required this.filters,
    required this.updateFilters
  }) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return SingleChildScrollView(
      scrollDirection: Axis.horizontal,
      padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 0),
      child: Row(
          children: [
            // if (!_filters.isEmpty)
            //   RawMaterialButton(
            //     padding: EdgeInsets.zero,
            //     onPressed: () {},
            //     elevation: 0,
            //     fillColor: Colors.white,
            //     child: Icon(
            //       Icons.cancel_outlined,
            //     ),
            //     // padding: EdgeInsets.all(15.0),
            //     shape: CircleBorder(),
            //   ),
            ...followedWebsites.map((followedWebsite) => Padding(
              padding: const EdgeInsets.all(4.0),
              child: FilterChip(
                backgroundColor: Colors.transparent,
                selectedColor: Colors.white,
                label: Text(followedWebsite),
                selected: filters.contains(followedWebsite),
                onSelected: (value) => updateFilters(followedWebsite, value),
              ),
            )).toList()
          ]
      ),
    );
  }
}
