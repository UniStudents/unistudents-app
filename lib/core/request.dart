import 'package:http/http.dart' as http;
import 'dart:convert';

request(String username, String password, String university, String? cookies, String? system, bool isAndroid) async {
  if(isAndroid) {
    await requestHttp(username, password, university, cookies, system);
  } else {
    await requestHeroku(username, password, university, cookies, system);
  }
}

requestHttp(String username, String password, String university, String? cookies, String? system) async {

}

requestHeroku(String username, String password, String university, String? cookies, String? system) async {
    String url = "https://unistudents-prod-1.herokuapp.com/api/student/$university";
    if(system != null) url += "/$system";

    Map<String, String?> data = {
      'username': username,
      'password': password,
      'cookies': cookies,
    };

    final response = await http.post(
        Uri.parse(url),
        body: json.encode(data),
        headers: {
          'Content-type' : 'application/json',
          'Accept': 'application/json',
        }
    );

    final resJson = json.decode(response.body);
    print(resJson);
}