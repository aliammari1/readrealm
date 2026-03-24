class User {
  final String id;
  final String username;
  final String email;
  final String role;
  final String? faceData; // Add this field
  final int bookmarkCount;

  User({
    required this.id,
    required this.username,
    required this.email,
    required this.role,
    this.faceData, // Add this parameter
    this.bookmarkCount = 0,
  });

  factory User.fromJson(Map<String, dynamic> json) {
    return User(
      id: json['_id'] ?? json['id'] ?? '',
      username: json['username'] ?? '',
      email: json['email'] ?? '',
      role: json['role'] ?? 'user',
      faceData: json['faceData'], // Add this field
      bookmarkCount: json['bookmarkCount'] ?? 0,
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'username': username,
      'email': email,
      'role': role,
      'faceData': faceData, // Add this field
      'bookmarkCount': bookmarkCount,
    };
  }

  @override
  String toString() {
    return 'User(id: $id, username: $username, email: $email, role: $role, faceData: $faceData, bookmarkCount: $bookmarkCount)';
  }
}
