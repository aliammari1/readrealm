import 'package:http/http.dart' as http;
import 'dart:convert';

class ApiClient {
  final String baseUrl;
  final Map<String, String> _defaultHeaders = {
    'Content-Type': 'application/json',
  };

  ApiClient(this.baseUrl);

  Future<dynamic> post(String path, Map<String, dynamic> body, {String? token}) async {
    final headers = Map<String, String>.from(_defaultHeaders);
    if (token != null) {
      headers['Authorization'] = 'Bearer $token';
    }

    final response = await http.post(
      Uri.parse('$baseUrl$path'),
      headers: headers,
      body: json.encode(body),
    );

    if (response.statusCode != 200 && response.statusCode != 201) {
      throw Exception('API request failed with status ${response.statusCode}');
    }

    return json.decode(response.body);
  }
}
