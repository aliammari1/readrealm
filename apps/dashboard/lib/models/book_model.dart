class Book {
  final int id;
  final String title;
  final String author;
  final String? coverUrl;
  final bool isBookmarked;
  final String genre;

  Book({
    required this.id,
    required this.title,
    required this.author,
    this.coverUrl,
    this.isBookmarked = false,
    required this.genre,
  });

  factory Book.fromJson(Map<String, dynamic> json) {
    return Book(
      id: (json['id'] as num).toInt(),
      title: json['title']?.toString() ?? 'Untitled',
      author: json['author']?.toString() ?? 'Unknown',
      coverUrl: (json['coverUrl'] ?? json['coverImage'])?.toString(),
      isBookmarked: json['isBookmarked'] == true,
      genre: json['genre']?.toString() ?? 'Unknown',
    );
  }
}
