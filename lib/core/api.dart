import 'dart:convert';
import 'package:http/http.dart' as http;

import '../components/bug.dart';
import '../models/progress_model.dart';
import 'env.dart';

class API {
  static Future<String> getProgressAPIUrl() async {
    final response = await http.get(Uri.parse(Env.gatewayUrl + "/server"));
    if (response.statusCode != 200) return Env.progressFallbackUrl;
    return response.body;
  }

  static Future<http.Response> getProgress(ProgressModel account) async {
    String url = await API.getProgressAPIUrl();
    return await http.post(Uri.parse(account.geUrl(url)),
        body: json.encode(account.getAuth()),
        headers: {
          'Content-type': 'application/json',
          'Accept': 'application/json',
        });
  }

  static Future<bool> reportBug(Bug bug) async {
    String url = bug.getUrl(Env.progressFallbackUrl);
    final response =
        await http.post(Uri.parse(url), body: bug.getJson(), headers: {
      'Content-type': 'application/json',
      'Accept': 'application/json',
    });

    return response.statusCode == 200;
  }

  static Future<http.Response> getAvailableWebsites(String university) async {
    String url = "${Env.gohanUrl}/websites?university=$university";
    return await http.get(Uri.parse(url));
  }

  static Future<http.Response> getArticles(List<String> subscribedWebsites,
      {int? pageSize,
      int? pageNumber,
      List<String>? afterIds,
      List<String>? beforeIds}) async {
    String url =
        "${Env.gohanUrl}/articles?websites=${subscribedWebsites.join(',')}"
        "&pageSize=${pageSize ?? "0"}"
        "&pageNumber=${pageNumber ?? "0"}"
        "${afterIds != null ? "&after=${afterIds.join(",")}" : ""}"
        "${beforeIds != null ? "&before=${beforeIds.join(",")}" : ""}";

    return await http.get(Uri.parse(url));
  }
}
