import 'package:http/http.dart' as http;
import 'dart:convert';

import 'package:unistudents_app/models/progress_account_model.dart';

Future<bool> request(ProgressAccountModel account, bool isAndroid) async {
  if(isAndroid) {
    return await requestHttp(account);
  } else {
    return  await requestHeroku(account);
  }
}

Future<bool> requestHttp(ProgressAccountModel account) async {
  return false;
}

Future<bool> requestHeroku(ProgressAccountModel account) async {
    final response = await http.post(
        Uri.parse(account.geHerokutUrl()),
        body: json.encode(account.getAuth()),
        headers: {
          'Content-type' : 'application/json',
          'Accept': 'application/json',
        }
    );

    if(response.statusCode != 200) return false;
    return account.assignFromHeroku(response.body);
}