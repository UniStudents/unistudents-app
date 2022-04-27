import 'package:flutter/material.dart';
import 'package:flutter_screenutil/flutter_screenutil.dart';
import 'package:provider/provider.dart';
import 'package:unistudents_app/core/colors.dart';
import 'package:unistudents_app/providers/news.dart';
import 'package:unistudents_app/widgets/available_website_minimized.dart';

class FollowWebsitesScreen extends StatefulWidget {
  static const String id = 'follow_websites_screen';
  const FollowWebsitesScreen({Key? key}) : super(key: key);

  @override
  _FollowWebsitesScreenState createState() => _FollowWebsitesScreenState();
}

class _FollowWebsitesScreenState extends State<FollowWebsitesScreen> {
  var _isLoading = false;

  @override
  void initState() {
    _isLoading = true;
    final news = Provider.of<News>(context, listen: false);
    if (news.availableWebsites.isEmpty) {
      news.fetchWebsites("UNIPI").then((_) {
        setState(() {
          _isLoading = false;
        });
      });
    } else {
      Future.delayed(const Duration(milliseconds: 500), () {
        setState(() {
          _isLoading = false;
        });
      });
    }

    super.didChangeDependencies();
  }

  Widget _buildMinimizedCards(List availableWebsites) {
    return ListView.separated(
      primary: false,
      shrinkWrap: true,
      itemCount: availableWebsites.length,
      separatorBuilder: (ctx, i) => SizedBox(height: 12.h),
      itemBuilder: (ctx, i) => AvailableWebsiteMinimized(
        newsWebsite: availableWebsites[i],
      )
    );
  }

  @override
  Widget build(BuildContext context) {
    var _isDarkMode = Theme.of(context).brightness == Brightness.dark;
    final availableWebsites = Provider.of<News>(context, listen: false).availableWebsites;

    return Scaffold(
      appBar: AppBar(
        title: const Text('Websites'),
        actions: [
          IconButton(
            icon: const Icon(Icons.check),
            onPressed: () {
              Navigator.pop(context);
            },
          ),
        ],
      ),
      body: _isLoading
        ? const Center(child: CircularProgressIndicator())
        : ListView(
            padding: EdgeInsets.fromLTRB(12.w, 0, 12.w, 0),
            children: [
              SizedBox(height: 30.h,),
              Text(
                "Διαθέσιμα Websites",
                style: TextStyle(
                  color: UniColors.getTextHalf(_isDarkMode),
                  fontFamily: 'Roboto',
                  fontSize: 16.sp,
                ),
              ),
              SizedBox(height: 12.h,),
              _buildMinimizedCards(availableWebsites)
            ],
          ),
    );
  }
}
