import 'package:flutter/material.dart';
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
      shape: const RoundedRectangleBorder(
          borderRadius: BorderRadius.only(topLeft: Radius.circular(20), topRight: Radius.circular(20))
      ),
      builder: (context) => StatefulBuilder(
          builder: (BuildContext context, setState) => ThemeBSModal(setState: setState)
      )
  );
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
                  Row(
                    children: [
                      Checkbox(
                          value: prov.themeNum == 0,
                          onChanged: (bool? value) {
                            widget.setState(() {
                              prov.setTheme(0);
                            });
                          }),
                      Text(Locals.of(context)!.profileThemeSystem)
                    ],
                  ),
                  const Padding(padding: EdgeInsets.all(10.0)),
                  Row(
                    children: [
                      Checkbox(
                          value: prov.themeNum == 1,
                          onChanged: (bool? value) {
                            widget.setState(() {
                              prov.setTheme(1);
                            });
                          }),
                      Text(Locals.of(context)!.profileThemeLight)
                    ],
                  ),
                  const Padding(padding: EdgeInsets.all(10.0)),
                  Row(
                    children: [
                      Checkbox(
                          value: prov.themeNum == 2,
                          onChanged: (bool? value) {
                            widget.setState(() {
                              prov.setTheme(2);
                            });
                          }),
                      Text(Locals.of(context)!.profileThemeDark)
                    ],
                  ),
                  const Padding(padding: EdgeInsets.all(10.0))
                ],
              ),
            )
          ],
        )
      ],
    );
  }
}
