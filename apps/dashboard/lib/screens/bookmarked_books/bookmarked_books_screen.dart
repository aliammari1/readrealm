import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:flutter_library_app/constants.dart';
import 'package:flutter_library_app/services/book_service.dart';
import 'package:flutter_library_app/models/book_model.dart';
import 'package:flutter_library_app/providers/auth_provider.dart';
import 'package:fl_chart/fl_chart.dart'; // Import fl_chart

class BookmarkedBooksScreen extends StatefulWidget {
  final String? userId;
  final String? username;

  const BookmarkedBooksScreen({
    Key? key,
    this.userId,
    this.username,
  }) : super(key: key);

  @override
  State<BookmarkedBooksScreen> createState() => _BookmarkedBooksScreenState();
}

class _BookmarkedBooksScreenState extends State<BookmarkedBooksScreen> {
  final BookService _bookService = BookService();
  late Future<List<Book>> _bookmarksFuture;

  @override
  void initState() {
    super.initState();
    final userId =
        widget.userId ?? context.read<AuthProvider>().currentUser?.id;
    _bookmarksFuture = _bookService.getUserBookmarks(userId!);
  }

  Map<String, int> _calculateGenreDistribution(List<Book> books) {
    Map<String, int> genreCount = {};
    for (var book in books) {
      genreCount[book.genre] = (genreCount[book.genre] ?? 0) + 1;
    }
    return genreCount;
  }

  @override
  Widget build(BuildContext context) {
    final bool isViewingOwnBookmarks = widget.userId == null;
    final String title = isViewingOwnBookmarks
        ? 'My Bookmarked Books'
        : '${widget.username}\'s Bookmarks';

    return Scaffold(
      appBar: AppBar(
        title: Text(
          title,
          style: const TextStyle(fontWeight: FontWeight.bold),
        ),
        backgroundColor: secondaryColor,
        leading: IconButton(
          icon: const Icon(Icons.arrow_back, color: Colors.white),
          onPressed: () => Navigator.of(context).pop(),
        ),
        elevation: 5,
      ),
      body: Padding(
        padding: const EdgeInsets.all(defaultPadding),
        child: FutureBuilder<List<Book>>(
          future: _bookmarksFuture,
          builder: (context, snapshot) {
            if (snapshot.connectionState == ConnectionState.waiting) {
              return Center(
                child: Column(
                  mainAxisSize: MainAxisSize.min,
                  children: const [
                    CircularProgressIndicator(),
                    SizedBox(height: 16),
                    Text('Loading bookmarks...'),
                  ],
                ),
              );
            }

            if (snapshot.hasError) {
              return Center(
                child: Text(
                  'Error: ${snapshot.error}',
                  style: const TextStyle(color: Colors.red),
                ),
              );
            }

            final books = snapshot.data!;
            final genreData = _calculateGenreDistribution(books);

            return Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                const Text(
                  "Genre Distribution",
                  style: TextStyle(
                    fontWeight: FontWeight.bold,
                    fontSize: 20,
                  ),
                ),
                const SizedBox(height: defaultPadding),
                // Chart Widget
                Expanded(
                  flex: 2,
                  child: genreData.isEmpty
                      ? const Center(
                          child: Text(
                            'No bookmarks to display.',
                            style: TextStyle(color: Colors.grey),
                          ),
                        )
                      : BarChart(
                          BarChartData(
                            alignment: BarChartAlignment.spaceAround,
                            barTouchData: BarTouchData(
                              enabled: true,
                            ),
                            titlesData: FlTitlesData(
                              leftTitles: AxisTitles(
                                axisNameWidget: Text('Count'),
                                sideTitles: SideTitles(showTitles: true),
                              ),
                              bottomTitles: AxisTitles(
                                axisNameWidget: Text('Genres'),
                                sideTitles: SideTitles(
                                  showTitles: true,
                                  getTitlesWidget: (value, TitleMeta meta) {
                                    final genres = genreData.keys.toList();
                                    if (value.toInt() < genres.length) {
                                      return Text(
                                        genres[value.toInt()],
                                        style: TextStyle(
                                          fontSize: 12,
                                          fontWeight: FontWeight.bold,
                                        ),
                                      );
                                    }
                                    return const Text('');
                                  },
                                ),
                              ),
                            ),
                            borderData: FlBorderData(show: false),
                            barGroups: genreData.entries
                                .toList()
                                .asMap() // Fixed this line
                                .map((index, entry) {
                                  return MapEntry(
                                    index,
                                    BarChartGroupData(
                                      x: index, // Use index directly
                                      barRods: [
                                        BarChartRodData(
                                          toY: entry.value.toDouble(),
                                          color: Colors
                                              .blue, // Changed colors to color
                                          width: 16,
                                        ),
                                      ],
                                    ),
                                  );
                                })
                                .values
                                .toList(),
                          ),
                        ),
                ),
                const SizedBox(height: defaultPadding),
                const Text(
                  "Bookmarked Books",
                  style: TextStyle(
                    fontWeight: FontWeight.bold,
                    fontSize: 20,
                  ),
                ),
                const SizedBox(height: defaultPadding),
                // List of Bookmarked Books
                Expanded(
                  flex: 3,
                  child: ListView.builder(
                    itemCount: books.length,
                    itemBuilder: (context, index) {
                      final book = books[index];
                      return ListTile(
                        title: Text(book.title),
                        subtitle: Text(book.author),
                        trailing: book.coverUrl != null
                            ? Image.network(book.coverUrl!)
                            : null,
                        onTap: () {
                          // Handle book tap event if needed
                        },
                      );
                    },
                  ),
                ),
              ],
            );
          },
        ),
      ),
    );
  }
}
