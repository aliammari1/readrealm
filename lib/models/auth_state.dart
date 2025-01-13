import 'package:flutter_library_app/models/user_model.dart';

class AuthState {
  final bool isAuthenticated;
  final String? userId;
  final String? accessToken;
  final String? refreshToken;
  final User? currentUser;
  final bool isLoading;
  final String? error;

  AuthState({
    this.isAuthenticated = false,
    this.userId,
    this.accessToken,
    this.refreshToken,
    this.currentUser,
    this.isLoading = false,
    this.error,
  });

  AuthState copyWith({
    bool? isAuthenticated,
    String? userId,
    String? accessToken,
    String? refreshToken,
    User? currentUser,
    bool? isLoading,
    String? error,
  }) {
    return AuthState(
      isAuthenticated: isAuthenticated ?? this.isAuthenticated,
      userId: userId ?? this.userId,
      accessToken: accessToken ?? this.accessToken,
      refreshToken: refreshToken ?? this.refreshToken,
      currentUser: currentUser ?? this.currentUser,
      isLoading: isLoading ?? this.isLoading,
      error: error ?? this.error,
    );
  }
}
