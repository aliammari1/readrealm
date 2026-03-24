import 'package:flutter/material.dart';
import 'package:flutter_secure_storage/flutter_secure_storage.dart';
import 'package:flutter_library_app/models/auth_state.dart';
import 'package:flutter_library_app/models/user_model.dart';
import 'package:http/http.dart' as http;
import 'dart:convert';
import '../services/api_client.dart';
import '../services/face_recognition_service.dart';
import 'package:path_provider/path_provider.dart';

class AuthProvider with ChangeNotifier {
  final String _baseUrl = 'http://localhost:3000';
  //final String _baseUrl = 'http://192.168.159.105:3000';
  final _secureStorage = const FlutterSecureStorage();
  late final ApiClient _apiClient;
  final FaceRecognitionService _faceService = FaceRecognitionService();
  AuthState _state = AuthState();

  AuthProvider() {
    _apiClient = ApiClient(_baseUrl);
  }

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
        body: json.encode({
          'email': email,
          'password': password,
        }),
      );

      if (response.statusCode == 200 || response.statusCode == 201) {
        final data = json.decode(response.body);
        print('Login response: ${response.body}'); // Debug line

        if (data['accessToken'] != null && data['userId'] != null) {
          _setState(_state.copyWith(
            isAuthenticated: true,
            userId: data['userId'],
            accessToken: data['accessToken'],
            refreshToken: data['refreshToken'],
            isLoading: false,
            error: null,
          ));
          await _fetchCurrentUser();
        } else {
          throw Exception('Invalid response format');
        }
      } else {
        throw Exception('Authentication failed: ${response.statusCode}');
      }
    } catch (e) {
      _setState(_state.copyWith(
        isLoading: false,
        error: e.toString(),
        isAuthenticated: false,
      ));
      throw e;
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

      if (response.statusCode != 200) {
        throw Exception('Registration failed');
      }

      _setState(_state.copyWith(isLoading: false));
    } catch (e) {
      _setState(_state.copyWith(
        isLoading: false,
        error: e.toString(),
      ));
      throw e;
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
      throw e;
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

  Future<void> updateUser(String id, String username, String email,
      {String? password}) async {
    try {
      final Map<String, dynamic> body = {
        'username': username,
        'email': email,
      };
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
        headers: {
          'Authorization': 'Bearer ${_state.accessToken}',
        },
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

  Future<void> signInWithFace() async {
    try {
      _setState(_state.copyWith(isLoading: true));

      // Get stored credentials
      final storedPersonId = await _secureStorage.read(key: 'azure_person_id');
      final storedEmail = await _secureStorage.read(key: 'last_email');

      if (storedPersonId == null || storedEmail == null) {
        throw Exception(
            'No stored face data found. Please login with password first');
      }

      // Capture and verify face
      final tempDir = await getTemporaryDirectory();
      final imagePath = '${tempDir.path}/face_auth.jpg';
      final success =
          await _faceService.authenticate(storedPersonId, imagePath);

      if (!success) {
        throw Exception('Face authentication failed');
      }

      // Login with stored email
      final response = await http.post(
        Uri.parse('$_baseUrl/auth/face-login'),
        headers: {'Content-Type': 'application/json'},
        body: json.encode({
          'email': storedEmail,
          'personId': storedPersonId,
        }),
      );

      if (response.statusCode != 200) {
        throw Exception('Authentication failed');
      }

      final data = json.decode(response.body);
      final user = User.fromJson(data['user']);
      final token = data['token'];

      _setState(_state.copyWith(
        isAuthenticated: true,
        currentUser: user,
        accessToken: token,
      ));
    } catch (e) {
      _setState(_state.copyWith(error: e.toString()));
      throw e;
    } finally {
      _setState(_state.copyWith(isLoading: false));
    }
  }

  // After successful password login, store credentials for Face ID
  Future<void> _storeCredentialsForFaceId(String email, String personId) async {
    await _secureStorage.write(key: 'last_email', value: email);
    await _secureStorage.write(key: 'azure_person_id', value: personId);
  }
}
