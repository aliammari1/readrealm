import 'dart:convert';
import 'package:http/http.dart' as http;
import '../constants.dart';
import '../models/book_model.dart';
import '../models/review_model.dart';

class BookService {
  static const String baseUrl = '$apiBaseUrl/book';

  Future<List<Book>> getUserBookmarks(String userId) async {
    final response = await http.get(Uri.parse('$baseUrl/bookmarks/$userId'));

    if (response.statusCode == 200) {
      final List<dynamic> jsonData = json.decode(response.body);
      return jsonData.map((json) => Book.fromJson(json)).toList();
    }
    throw Exception('Failed to load bookmarks');
  }

  Future<List<Review>> getBookReviews(int bookId) async {
    final response = await http.get(Uri.parse('$baseUrl/reviews/$bookId'));

    if (response.statusCode == 200) {
      final List<dynamic> jsonData = json.decode(response.body);
      return jsonData.map((json) => Review.fromJson(json)).toList();
    }
    throw Exception('Failed to load reviews');
  }

  Future<void> toggleBookmark(int bookId, String userId) async {
    final response = await http.put(
      Uri.parse('$baseUrl/$bookId/bookmark'),
      headers: {'Content-Type': 'application/json'},
      body: json.encode({'userId': userId}),
    );

    if (response.statusCode != 200 && response.statusCode != 201) {
      throw Exception('Failed to toggle bookmark');
    }
  }

  Future<List<Review>> getUserReviews(String userId) async {
    final response = await http.get(Uri.parse('$baseUrl/user-reviews/$userId'));

    if (response.statusCode == 200) {
      final List<dynamic> jsonData = json.decode(response.body);
      return jsonData.map((json) => Review.fromJson(json)).toList();
    }
    throw Exception('Failed to load user reviews');
  }
}
