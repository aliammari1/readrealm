import 'package:flutter/material.dart';

class UserProvider extends ChangeNotifier {
  String? _userId;
  String? _accessToken;
  String? _refreshToken;

  String? get userId => _userId;
  String? get accessToken => _accessToken;

  Future<void> saveTokens(
      {required String userId,
      required String accessToken,
      required String refreshToken}) async {
    _userId = userId;
    _accessToken = accessToken;
    _refreshToken = refreshToken;
    notifyListeners();
  }

  void logout() {
    _userId = null;
    _accessToken = null;
    _refreshToken = null;
    notifyListeners();
  }
}
