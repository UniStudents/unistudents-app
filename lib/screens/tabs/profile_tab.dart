import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';
import 'package:launch_review/launch_review.dart';
import 'package:provider/provider.dart';
import 'package:unistudents_app/core/local/locals.dart';
import 'package:unistudents_app/providers/theme.dart';
import 'package:unistudents_app/widgets/custom_web_view.dart';
import 'package:unistudents_app/widgets/settings_build.dart';

class ProfileTab extends StatefulWidget {
  static const String id = 'profile_tab';

  const ProfileTab({Key? key}) : super(key: key);

  @override
  State<ProfileTab> createState() => _ProfileTabState();
}

class _ProfileTabState extends State<ProfileTab> {

  void navigateToWebView(BuildContext buildContext, String title, String url) async {
    await Navigator.of(context).push(
        MaterialPageRoute<String>(
            builder: (ctx) => CustomWebView(
              barTitle: title,
              url: url,
            ),
            fullscreenDialog: true
        )
    );
  }

  void showThemeSelection(BuildContext context) {
    var prov = Provider.of<ThemeProvider>(context, listen: false);
    prov.setTheme(prov.themeNum == 1 ? 2 : 1);
  }

  @override
  Widget build(BuildContext context) {
    String image = 'https://i.imgur.com/x6TwpSQ.jpeg';
    String name = 'Γεώργιος Ανδρεδάκης';
    String department = 'Τμήμα Γραφιστικής και Οπτικής Επικοινωνίας';
    int theme = Provider.of<ThemeProvider>(context).themeNum;

    Widget profile = Card(
      elevation: 0,
      shape: RoundedRectangleBorder(
        borderRadius: BorderRadius.circular(10),
      ),
      child: Container(
        padding: const EdgeInsets.all(20),
        child: Row(
          children: [
            // Image
            Container(
                width: 60.0,
                height: 60.0,
                decoration: BoxDecoration(
                    shape: BoxShape.circle,
                    image: DecorationImage(
                        fit: BoxFit.fill, image: NetworkImage(image)))),

            // Text
            const Padding(padding: EdgeInsets.only(left: 20)),
            Flexible(
              fit: FlexFit.tight,
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    name,
                    overflow: TextOverflow.ellipsis,
                    style: const TextStyle(
                        fontWeight: FontWeight.bold, fontSize: 22),
                  ),
                  const Padding(padding: EdgeInsets.only(top: 5)),
                  Text(
                    department,
                    overflow: TextOverflow.ellipsis,
                    softWrap: false,
                  ),
                ],
              ),
            ),

            // Arrow
            const Icon(
              Icons.keyboard_arrow_right,
              size: 50,
              color: Colors.grey,
            )
          ],
        ),
      ),
    );

    Widget settings = SettingsModal(
        title: Locals.of(context)!.profileSettings,
        children: [
          SettingsItem(
            icon: Icons.dark_mode,
            title: Locals.of(context)!.profileTheme,
            value: theme == 0
                ? Locals.of(context)!.profileThemeSystem
                : theme == 1
                ? Locals.of(context)!.profileThemeLight
                : Locals.of(context)!.profileThemeDark,
            onTap: () => showThemeSelection(context),
          ),
          SettingsItem(
            icon: Icons.campaign,
            title: Locals.of(context)!.profileAds,
            onTap: (){},
          ),
          SettingsItem(
            icon: Icons.notifications,
            title: Locals.of(context)!.profileNotifications,
            onTap: (){},
          ),
        ]
    );

    Widget security = SettingsModal(
        title: Locals.of(context)!.profileSecurity,
        children: [
          SettingsItem(
            icon: Icons.lock,
            title: Locals.of(context)!.profilePrivacy,
            onTap: (){},
          ),
        ]
    );

    Widget aboutUs = SettingsModal(
        title: Locals.of(context)!.profileAboutUs,
        children: [
          SettingsItem(
            icon: Icons.star,
            title: Locals.of(context)!.profileRateUs,
            onTap: () => LaunchReview.launch(),
          ),
          SettingsItem(
            icon: Icons.security,
            title: Locals.of(context)!.profilePrivacyPolicy,
            onTap: () => navigateToWebView(context, 'unistudents.gr', 'https://unistudents.gr/privacy-policy/'),
          ),
        ]
    );

    Widget help = SettingsModal(
        title: Locals.of(context)!.profileHelp,
        children: [
          SettingsItem(
            icon: Icons.warning,
            title: Locals.of(context)!.profileReportIssues,
            onTap: (){},
          ),
          SettingsItem(
            icon: Icons.question_answer,
            title: Locals.of(context)!.profileContactUs,
            onTap: (){},
          ),
          SettingsItem(
            icon: Icons.help,
            title: Locals.of(context)!.profileFAQ,
            onTap: (){},
          ),

        ]
    );

    Widget logOut = SettingsModal(
        children: [
          SettingsItem(
            icon: Icons.logout,
            title: Locals.of(context)!.profileLogOut,
            iconColor: Colors.red,
            onTap: (){},
          )
        ]
    );

    return Scaffold(
        appBar: AppBar(
          title: Text(Locals.of(context)!.profileTitle),
        ),
        body: ListView(
          padding: const EdgeInsets.all(20.0),
          children: buildSettingsList([profile, settings, security, aboutUs, help, logOut]),
        ));
  }
}

