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
      id: json['_id'] as String? ?? json['id'] as String? ?? '',
      username: json['username'] as String? ?? '',
      email: json['email'] as String? ?? '',
      role: json['role'] as String? ?? 'user',
      faceData: json['faceData'] as String?,
      bookmarkCount: (json['bookmarkCount'] as num?)?.toInt() ?? 0,
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
