import 'package:http/http.dart' as http;
import 'dart:convert';

import 'package:unistudents_app/models/progress_model.dart';

import 'api.dart';

Future<bool> request(ProgressModel account, bool isAndroid) async {
  if(isAndroid) {
    return await requestHttp(account);
  } else {
    return  await requestHeroku(account);
  }
}

Future<bool> requestHttp(ProgressModel account) async {
  return false;
}

Future<bool> requestHeroku(ProgressModel account) async {
  String? url = await API.getAPIUrl();
  if(url == null) return false;

  final response = await http.post(
        Uri.parse(account.geHerokuUrl(url)),
        body: json.encode(account.getAuth()),
        headers: {
          'Content-type' : 'application/json',
          'Accept': 'application/json',
        }
    );

    if(response.statusCode != 200) return false;
    return account.assignFromHeroku(response.body);
}