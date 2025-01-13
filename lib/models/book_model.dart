class Book {
  final int id;
  final String title;
  final String author;
  final String? coverUrl;
  final bool isBookmarked;
  final String genre; // Ensure genre is required

  Book({
    required this.id,
    required this.title,
    required this.author,
    this.coverUrl,
    this.isBookmarked = false,
    required this.genre, // Ensure genre is required in the constructor
  });

  // Updated fromJson factory to include 'genre'
  factory Book.fromJson(Map<String, dynamic> json) {
    return Book(
      id: json['id'],
      title: json['title'],
      author: json['author'],
      coverUrl: json['coverUrl'],
      isBookmarked: json['isBookmarked'] ?? false,
      genre: json['genre'], // Ensure genre is passed here
    );
  }
}
