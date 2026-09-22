import 'dart:convert';

import 'package:flutter_library_app/constants.dart';
import 'package:flutter_library_app/models/book_model.dart';
import 'package:flutter_library_app/models/review_model.dart';
import 'package:http/http.dart' as http;

class BookService {
  static const String baseUrl = '$apiBaseUrl/book';

  Future<List<Book>> getUserBookmarks(String userId) async {
    final response = await http.get(Uri.parse('$baseUrl/bookmarks/$userId'));

    if (response.statusCode == 200) {
      final jsonData = jsonDecode(response.body) as List<dynamic>;
      return jsonData
          .map((item) => Book.fromJson(item as Map<String, dynamic>))
          .toList();
    }
    throw Exception('Failed to load bookmarks');
  }

  Future<List<Review>> getBookReviews(int bookId) async {
    final response = await http.get(Uri.parse('$baseUrl/reviews/$bookId'));

    if (response.statusCode == 200) {
      final jsonData = jsonDecode(response.body) as List<dynamic>;
      return jsonData
          .map((item) => Review.fromJson(item as Map<String, dynamic>))
          .toList();
    }
    throw Exception('Failed to load reviews');
  }

  Future<void> toggleBookmark(int bookId, String userId) async {
    final response = await http.put(
      Uri.parse('$baseUrl/$bookId/bookmark'),
      headers: {'Content-Type': 'application/json'},
      body: jsonEncode({'userId': userId}),
    );

    if (response.statusCode != 200 && response.statusCode != 201) {
      throw Exception('Failed to toggle bookmark');
    }
  }

  Future<List<Review>> getUserReviews(String userId) async {
    final response = await http.get(Uri.parse('$baseUrl/user-reviews/$userId'));

    if (response.statusCode == 200) {
      final jsonData = jsonDecode(response.body) as List<dynamic>;
      return jsonData
          .map((item) => Review.fromJson(item as Map<String, dynamic>))
          .toList();
    }
    throw Exception('Failed to load user reviews');
  }
}
