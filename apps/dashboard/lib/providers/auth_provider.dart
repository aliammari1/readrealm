import 'package:flutter/material.dart';
import 'package:flutter_library_app/models/auth_state.dart';
import 'package:flutter_library_app/models/user_model.dart';
import 'package:http/http.dart' as http;
import 'dart:convert';
import '../constants.dart';

class AuthProvider with ChangeNotifier {
  final String _baseUrl = apiBaseUrl;
  AuthState _state = AuthState();

  AuthState get state => _state;
  bool get isAuthenticated => _state.isAuthenticated;
  User? get currentUser => _state.currentUser;

  void _setState(AuthState newState) {
    _state = newState;
    notifyListeners();
  }

  Future<void> login(String email, String password) async {
    _setState(_state.copyWith(isLoading: true, error: null));

    try {
      final response = await http.post(
        Uri.parse('$_baseUrl/auth/login'),
        headers: {'Content-Type': 'application/json'},
        body: json.encode({'email': email, 'password': password}),
      );

      if (response.statusCode == 200 || response.statusCode == 201) {
        final data = json.decode(response.body);
        if (data['accessToken'] != null && data['userId'] != null) {
          _setState(
            _state.copyWith(
              isAuthenticated: true,
              userId: data['userId'],
              accessToken: data['accessToken'],
              refreshToken: data['refreshToken'],
              isLoading: false,
              error: null,
            ),
          );
          await _fetchCurrentUser();
        } else {
          throw Exception('Invalid response format');
        }
      } else {
        throw Exception('Authentication failed: ${response.statusCode}');
      }
    } catch (e) {
      _setState(
        _state.copyWith(
          isLoading: false,
          error: e.toString(),
          isAuthenticated: false,
        ),
      );
      rethrow;
    }
  }

  Future<void> register(String username, String email, String password) async {
    _setState(_state.copyWith(isLoading: true, error: null));

    try {
      final response = await http.post(
        Uri.parse('$_baseUrl/auth/register'),
        headers: {'Content-Type': 'application/json'},
        body: json.encode({
          'username': username,
          'email': email,
          'password': password,
        }),
      );

      if (response.statusCode != 200 && response.statusCode != 201) {
        throw Exception('Registration failed: ${response.statusCode}');
      }

      _setState(_state.copyWith(isLoading: false));
    } catch (e) {
      _setState(_state.copyWith(isLoading: false, error: e.toString()));
      rethrow;
    }
  }

  Future<void> _fetchCurrentUser() async {
    if (_state.userId == null) return;

    try {
      final response = await http.get(
        Uri.parse('$_baseUrl/user/userId/${_state.userId}'),
        headers: {
          'Content-Type': 'application/json',
          'Authorization': 'Bearer ${_state.accessToken}',
        },
      );

      if (response.statusCode == 200) {
        final dynamic userData = json.decode(response.body);
        final user = User.fromJson(userData['user'] ?? userData);
        _setState(_state.copyWith(currentUser: user));
      }
    } catch (e) {
      logout();
      rethrow;
    }
    await fetchUsers(); // Add this line
  }

  void logout() {
    _setState(AuthState());
  }

  List<User> _users = [];
  List<User> get users => _users;

  Future<void> fetchUsers() async {
    try {
      final response = await http.get(
        Uri.parse('$_baseUrl/user'),
        headers: {
          'Content-Type': 'application/json',
          'Authorization': 'Bearer ${_state.accessToken}',
        },
      );

      if (response.statusCode == 200) {
        final List<dynamic> usersJson = json.decode(response.body);
        _users = usersJson.map((json) => User.fromJson(json)).toList();
        notifyListeners();
      } else {
        throw Exception('Failed to load users');
      }
    } catch (e) {
      throw Exception('Error fetching users: $e');
    }
  }

  Future<void> addUser(String username, String email, String password) async {
    try {
      final response = await http.post(
        Uri.parse('$_baseUrl/user'),
        headers: {
          'Content-Type': 'application/json',
          'Authorization': 'Bearer ${_state.accessToken}',
        },
        body: json.encode({
          'username': username,
          'email': email,
          'password': password,
        }),
      );

      if (response.statusCode == 201) {
        await fetchUsers();
      } else {
        throw Exception('Failed to add user: ${response.body}');
      }
    } catch (e) {
      throw Exception('Error adding user: $e');
    }
  }

  Future<void> updateUser(
    String id,
    String username,
    String email, {
    String? password,
  }) async {
    try {
      final Map<String, dynamic> body = {'username': username, 'email': email};
      if (password != null && password.isNotEmpty) {
        body['password'] = password;
      }

      final response = await http.put(
        Uri.parse('$_baseUrl/user/$id'),
        headers: {
          'Content-Type': 'application/json',
          'Authorization': 'Bearer ${_state.accessToken}',
        },
        body: json.encode(body),
      );

      if (response.statusCode == 200) {
        await fetchUsers();
      } else {
        throw Exception('Failed to update user: ${response.body}');
      }
    } catch (e) {
      throw Exception('Error updating user: $e');
    }
  }

  Future<void> deleteUser(String id) async {
    try {
      final response = await http.delete(
        Uri.parse('$_baseUrl/user/$id'),
        headers: {'Authorization': 'Bearer ${_state.accessToken}'},
      );

      if (response.statusCode == 200) {
        await fetchUsers();
      } else {
        throw Exception('Failed to delete user');
      }
    } catch (e) {
      throw Exception('Error deleting user: $e');
    }
  }
}
