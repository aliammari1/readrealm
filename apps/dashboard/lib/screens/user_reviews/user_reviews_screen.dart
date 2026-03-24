import 'package:flutter/material.dart';
import 'package:flutter_library_app/constants.dart';
import 'package:flutter_library_app/services/book_service.dart';
import 'package:flutter_library_app/models/review_model.dart';
import 'package:flutter_library_app/services/emotion_analyzer.dart';
import 'package:fl_chart/fl_chart.dart';

class UserReviewsScreen extends StatefulWidget {
  final String userId;
  final String? username;

  const UserReviewsScreen({
    Key? key,
    required this.userId,
    this.username,
  }) : super(key: key);

  @override
  State<UserReviewsScreen> createState() => _UserReviewsScreenState();
}

class _UserReviewsScreenState extends State<UserReviewsScreen> {
  final BookService _bookService = BookService();
  late Future<List<Review>> _reviewsFuture;

  @override
  void initState() {
    super.initState();
    _reviewsFuture = _bookService.getUserReviews(widget.userId);
  }

  Widget _buildEmotionChart(List<Review> reviews) {
    // Count emotions
    Map<String, int> emotionCounts = {};
    for (var review in reviews) {
      final emotion = review.emotion.toLowerCase();
      emotionCounts[emotion] = (emotionCounts[emotion] ?? 0) + 1;
    }

    return Container(
      height: 200,
      padding: EdgeInsets.all(defaultPadding),
      decoration: BoxDecoration(
        color: secondaryColor,
        borderRadius: BorderRadius.circular(10),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(
            "Emotion Distribution",
            style: Theme.of(context).textTheme.titleMedium,
          ),
          Expanded(
            child: PieChart(
              PieChartData(
                sectionsSpace: 2,
                centerSpaceRadius: 40,
                sections: emotionCounts.entries.map((entry) {
                  return PieChartSectionData(
                    color: EmotionAnalyzer.getEmotionColor(entry.key),
                    value: entry.value.toDouble(),
                    title:
                        '${EmotionAnalyzer.getEmoji(entry.key)}\n${entry.value}',
                    radius: 50,
                    titleStyle:
                        TextStyle(fontSize: 16, fontWeight: FontWeight.bold),
                  );
                }).toList(),
              ),
            ),
          ),
        ],
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text('Reviews'),
        backgroundColor: secondaryColor,
        leading: IconButton(
          icon: Icon(Icons.arrow_back),
          onPressed: () => Navigator.of(context).pop(),
        ),
      ),
      body: Padding(
        padding: EdgeInsets.all(defaultPadding),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              children: [
                Icon(Icons.person, color: Colors.grey),
                SizedBox(width: 8),
                Text(
                  "${widget.username ?? 'User'}'s Reviews",
                  style: Theme.of(context).textTheme.titleLarge,
                ),
              ],
            ),
            SizedBox(height: defaultPadding),
            Expanded(
              child: FutureBuilder<List<Review>>(
                future: _reviewsFuture,
                builder: (context, snapshot) {
                  if (snapshot.connectionState == ConnectionState.waiting) {
                    return Center(
                      child: Column(
                        mainAxisSize: MainAxisSize.min,
                        children: [
                          CircularProgressIndicator(),
                          SizedBox(height: 16),
                          Text('Loading reviews...'),
                        ],
                      ),
                    );
                  }

                  if (snapshot.hasError) {
                    return Center(child: Text('Error: ${snapshot.error}'));
                  }

                  final reviews = snapshot.data!;
                  return Column(
                    children: [
                      _buildEmotionChart(reviews),
                      SizedBox(height: defaultPadding),
                      Expanded(
                        child: ListView.builder(
                          itemCount: reviews.length,
                          itemBuilder: (context, index) {
                            final review = reviews[index];
                            return Card(
                              margin: EdgeInsets.symmetric(vertical: 8),
                              child: ListTile(
                                title: Row(
                                  children: [
                                    Text('Book ID: ${review.bookId}'),
                                    Spacer(),
                                    Text(EmotionAnalyzer.getEmoji(
                                        review.emotion)),
                                  ],
                                ),
                                subtitle: Column(
                                  crossAxisAlignment: CrossAxisAlignment.start,
                                  children: [
                                    Row(
                                      children: List.generate(
                                        5,
                                        (i) => Icon(
                                          i < review.rating
                                              ? Icons.star
                                              : Icons.star_border,
                                          color: Colors.amber,
                                          size: 20,
                                        ),
                                      ),
                                    ),
                                    SizedBox(height: 8),
                                    Text(review.comment),
                                    SizedBox(height: 4),
                                    Text(
                                      'Posted on: ${review.createdAt.toLocal().toString().split('.')[0]}',
                                      style: TextStyle(
                                        color: Colors.grey,
                                        fontSize: 12,
                                      ),
                                    ),
                                  ],
                                ),
                              ),
                            );
                          },
                        ),
                      ),
                    ],
                  );
                },
              ),
            ),
          ],
        ),
      ),
    );
  }
}
