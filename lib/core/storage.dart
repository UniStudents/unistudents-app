import 'dart:io';

import 'package:path_provider/path_provider.dart';

import './crypto.dart';
import '../components/news_website_last_id.dart';
import '../components/preferences.dart';
import '../models/progress_model.dart';

class Storage {

  static const String _FILE_PROGRESS = '/progress.json';
  static const String _FILE_FOLLWED_WEBSITES = '/followed_websites.json';
  static const String _FILE_LATEST_ARTICLES = '/latest_articles.json';
  static const String _FILE_PREFERENCES = '/preferences.json';

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
    final file = await _localFile(_FILE_PROGRESS);
    String contents = account.toJSON();



    await file.writeAsString(contents);
    return true;
  }

  static Future<bool> deleteProgress() async {
    final file = await _localFile(_FILE_PROGRESS);
    await file.delete();
    return true;
  }

  static Future<ProgressModel?> readProgress() async {
    try{
      final file = await _localFile(_FILE_PROGRESS);
      final contents = await file.readAsString();
      return ProgressModel.fromFile(contents);
    }
    catch(e) {
      return null;
    }
  }

  // Followed Websites
  static Future<bool> saveFollowedWebsites(List<String> followedWebsites) async {
    final file = await _localFile(_FILE_FOLLWED_WEBSITES);
    await file.writeAsString(followedWebsites.join('\n'));
    return true;
  }

  Future<bool> deleteFollowedWebsites() async {
    final file = await _localFile(_FILE_FOLLWED_WEBSITES);
    await file.delete();
    return true;
  }

  static Future<List<String>?> readFollowedWebsites() async {
    try{
      final file = await _localFile(_FILE_FOLLWED_WEBSITES);
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
  Future<bool> saveLatestArticleIds(List<NewsWebsiteLastId> websites) async {
    final file = await _localFile(_FILE_LATEST_ARTICLES);
    await file.writeAsString(NewsWebsiteLastId.toJSON(websites));
    return true;
  }

  Future<bool> deleteLatestArticleIds() async {
    final file = await _localFile(_FILE_LATEST_ARTICLES);
    await file.delete();
    return true;
  }

  Future<List<NewsWebsiteLastId>?> readLatestArticleIds() async {
    try{
      final file = await _localFile(_FILE_LATEST_ARTICLES);
      final contents = await file.readAsString();
      return NewsWebsiteLastId.fromJSON(contents);
    }
    catch(e) {
      return null;
    }
  }

  // Preferences
  Future<bool> savePreferences(Preferences preferences) async {
    final file = await _localFile(_FILE_PREFERENCES);
    await file.writeAsString(preferences.toJson());
    return true;
  }

  Future<bool> deletePreferences() async {
    final file = await _localFile(_FILE_PREFERENCES);
    await file.delete();
    return true;
  }

  Future<Preferences?> readPreferences() async {
    try{
      final file = await _localFile(_FILE_PREFERENCES);
      final contents = await file.readAsString();
      return Preferences.fromJson(contents);
    }
    catch(e) {
      return null;
    }
  }
}