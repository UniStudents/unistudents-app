import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:unistudents_app/core/local/locals.dart';
import 'package:unistudents_app/providers/news.dart';
import 'package:flutter/material.dart';
import 'package:scroll_to_index/scroll_to_index.dart';
import 'package:unistudents_app/providers/notifications.dart';
import 'package:unistudents_app/providers/theme.dart';

void showNotificationBSModal(BuildContext context) {
  showModalBottomSheet(
      context: context,
      isScrollControlled: true,
      shape: const RoundedRectangleBorder(
          borderRadius: BorderRadius.only(topLeft: Radius.circular(20), topRight: Radius.circular(20))
      ),
      builder: (context) => StatefulBuilder(
          builder: (BuildContext context, setState) => NotificationsBSModal(setState: setState)
      )
  );
}

class NotificationsBSModal extends StatefulWidget {
  @override
  State<StatefulWidget> createState() => _NotificationsBSModal();

  const NotificationsBSModal({Key? key, required this.setState}) : super(key: key);

  final StateSetter setState;
}

class _NotificationsBSModal extends State<NotificationsBSModal> {

  @override
  Widget build(BuildContext context) {
    var prov = Provider.of<NotificationProvider>(context, listen: false);

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
                          value: prov.enabled,
                          onChanged: (bool? value) {
                            widget.setState(() {
                              prov.setEnabled(true);
                            });
                          }),
                      Text(Locals.of(context)!.profileNotificationsEnabled)
                    ],
                  ),
                  const Padding(padding: EdgeInsets.all(10.0)),
                  Row(
                    children: [
                      Checkbox(
                          value: !prov.enabled,
                          onChanged: (bool? value) {
                            widget.setState(() {
                              prov.setEnabled(false);
                            });
                          }),
                      Text(Locals.of(context)!.profileNotificationsDisabled)
                    ],
                  ),
                  const Padding(padding: EdgeInsets.all(10.0)),
                ],
              ),
            )
          ],
        )
      ],
    );
  }
}
