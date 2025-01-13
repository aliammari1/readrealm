class Review {
  final String id;
  final String userId;
  final int bookId;
  final String comment;
  final int rating;
  final DateTime createdAt;
  final DateTime updatedAt;
  final String emotion;

  Review({
    required this.id,
    required this.bookId,
    required this.userId,
    required this.comment,
    required this.rating,
    required this.createdAt,
    required this.updatedAt,
    required this.emotion,
  });

  factory Review.fromJson(Map<String, dynamic> json) {
    return Review(
      id: json['_id'] ?? '',
      bookId: json['bookId'] ?? 0,
      userId: json['userId'] ?? '',
      comment: json['comment'] ?? '',
      rating: json['rating'] ?? 0,
      emotion: json['emotion'] ?? 'neutral',
      createdAt: DateTime.parse(json['createdAt']),
      updatedAt: DateTime.parse(json['updatedAt']),
    );
  }
}
