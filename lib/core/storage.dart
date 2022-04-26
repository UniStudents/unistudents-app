import 'dart:io';

import 'package:path_provider/path_provider.dart';

import '../components/news_website_last_id.dart';
import '../models/progress_model.dart';
import 'crypto.dart';

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

  static Future<void> _write(File file, String data) async {
    await file.writeAsString(Crypto.encrypt(data));
  }

  static Future<String> _read(File file) async {
    return Crypto.decrypt(await file.readAsString());
  }

  static Future<void> saveProgress(ProgressModel account) async {
    final file = await _localFile(_fileProgress);
    String contents = account.toJSON();
    await _write(file, contents);
  }

  static Future<void> deleteProgress() async {
    final file = await _localFile(_fileProgress);
    await file.delete();
  }

  static Future<ProgressModel> readProgress() async {
    final file = await _localFile(_fileProgress);
    final contents = await _read(file);
    return ProgressModel.fromFile(contents)!;
  }

  static Future<void> saveFollowedWebsites(List<String> followedWebsites) async {
    final file = await _localFile(_fileFollowedWebsites);
    _write(file, followedWebsites.join('\n'));
  }

  static Future<void> deleteFollowedWebsites() async {
    final file = await _localFile(_fileFollowedWebsites);
    await file.delete();
  }

  static Future<List<String>?> readFollowedWebsites() async {
    final file = await _localFile(_fileFollowedWebsites);
    final contents = await _read(file);

    List<String> l = [];
    for(String s in contents.split('\n')) {
      l.add(s);
    }

    return l;
  }

  static Future<void> saveLatestArticleIds(List<NewsWebsiteLastId> websites) async {
    final file = await _localFile(_fileLatestArticles);
    await _write(file, NewsWebsiteLastId.toJSON(websites));
  }

  static Future<void> deleteLatestArticleIds() async {
    final file = await _localFile(_fileLatestArticles);
    await file.delete();
  }

  static Future<List<NewsWebsiteLastId>?> readLatestArticleIds() async {
    final file = await _localFile(_fileLatestArticles);
    final contents = await _read(file);
    return NewsWebsiteLastId.fromJSON(contents);
  }
}