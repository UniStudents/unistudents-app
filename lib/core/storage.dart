import 'dart:io';

import 'package:path_provider/path_provider.dart';

import '../components/news_website_last_id.dart';
import '../models/progress_model.dart';

class Storage {

  static const String _fileProgress = '/progress.json';
  static const String _fileFollowedWebsites = '/followed_websites.json';
  static const String _fileLatestArticles = '/latest_articles.json';

  static Future<String> _localPath() async {
    final directory = await getApplicationDocumentsDirectory();
    return directory.path;
  }

  static Future<File> _localFile(String file) async {
    final path = await _localPath();
    return File('$path$file');
  }

  // Progress
  static Future<bool> saveProgress(ProgressModel account) async {
    final file = await _localFile(_fileProgress);
    String contents = account.toJSON();



    await file.writeAsString(contents);
    return true;
  }

  static Future<bool> deleteProgress() async {
    final file = await _localFile(_fileProgress);
    await file.delete();
    return true;
  }

  static Future<ProgressModel?> readProgress() async {
    try{
      final file = await _localFile(_fileProgress);
      final contents = await file.readAsString();
      return ProgressModel.fromFile(contents);
    }
    catch(e) {
      return null;
    }
  }

  // Followed Websites
  static Future<bool> saveFollowedWebsites(List<String> followedWebsites) async {
    final file = await _localFile(_fileFollowedWebsites);
    await file.writeAsString(followedWebsites.join('\n'));
    return true;
  }

  static Future<bool> deleteFollowedWebsites() async {
    final file = await _localFile(_fileFollowedWebsites);
    await file.delete();
    return true;
  }

  static Future<List<String>?> readFollowedWebsites() async {
    try{
      final file = await _localFile(_fileFollowedWebsites);
      final contents = await file.readAsString();

      List<String> l = [];
      for(String s in contents.split('\n')) {
        l.add(s);
      }

      return l;
    }
    catch(e) {
      return null;
    }
  }

  // Latest Articles
  static Future<bool> saveLatestArticleIds(List<NewsWebsiteLastId> websites) async {
    final file = await _localFile(_fileLatestArticles);
    await file.writeAsString(NewsWebsiteLastId.toJSON(websites));
    return true;
  }

  static Future<bool> deleteLatestArticleIds() async {
    final file = await _localFile(_fileLatestArticles);
    await file.delete();
    return true;
  }

  static Future<List<NewsWebsiteLastId>?> readLatestArticleIds() async {
    try{
      final file = await _localFile(_fileLatestArticles);
      final contents = await file.readAsString();
      return NewsWebsiteLastId.fromJSON(contents);
    }
    catch(e) {
      return null;
    }
  }
}