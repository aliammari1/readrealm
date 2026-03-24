import 'package:flutter/material.dart';
import 'package:flutter_library_app/screens/dashboard/components/header.dart';
import 'package:provider/provider.dart';
import 'package:flutter_library_app/constants.dart';
import 'package:flutter_library_app/providers/auth_provider.dart';
import 'package:flutter_library_app/models/user_model.dart';
import 'package:flutter_library_app/screens/bookmarked_books/bookmarked_books_screen.dart';
import 'package:flutter_library_app/screens/user_reviews/user_reviews_screen.dart';
import 'package:flutter_library_app/providers/search_provider.dart'; // Import SearchProvider

class UserTable extends StatefulWidget {
  const UserTable({Key? key}) : super(key: key);

  @override
  State<UserTable> createState() => _UserTableState();
}

class _UserTableState extends State<UserTable> {
  final TextEditingController _usernameController = TextEditingController();
  final TextEditingController _emailController = TextEditingController();
  final TextEditingController _passwordController = TextEditingController();
  bool _isLoading = false;
  bool _obscurePassword = true;

  @override
  void dispose() {
    _usernameController.dispose();
    _emailController.dispose();
    _passwordController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final authProvider = context.watch<AuthProvider>();
    final searchQuery =
        context.watch<SearchProvider>().searchQuery; // Get the search query
    List<User> users = authProvider.users;

    // Filter users based on the search query
    if (searchQuery.isNotEmpty) {
      users = users
          .where((user) =>
              user.username.toLowerCase().contains(searchQuery.toLowerCase()) ||
              user.email.toLowerCase().contains(searchQuery.toLowerCase()))
          .toList();
    }

    return Card(
      elevation: 4,
      child: Container(
        width: double.infinity,
        padding: EdgeInsets.all(defaultPadding),
        decoration: BoxDecoration(
          color: secondaryColor,
          borderRadius: const BorderRadius.all(Radius.circular(10)),
        ),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                Text(
                  "Users Management",
                  style: Theme.of(context).textTheme.titleMedium,
                ),
                ElevatedButton.icon(
                  icon: Icon(Icons.add),
                  label: Text("Add New User"),
                  onPressed: () => _showAddEditDialog(context),
                ),
              ],
            ),
            SizedBox(height: defaultPadding),
            // Add Search Field here

            PaginatedDataTable(
              header: Text("User List"),
              columns: [
                DataColumn(label: Text("Username")),
                DataColumn(label: Text("Email")),
                DataColumn(label: Text("Actions")),
              ],
              source: UserDataSource(
                  context, users, _showAddEditDialog, _showDeleteDialog),
              rowsPerPage: 5,
              columnSpacing: 300,
              horizontalMargin: 0,
              showCheckboxColumn: false,
            ),
          ],
        ),
      ),
    );
  }

  Future<void> _showAddEditDialog(BuildContext context, {User? user}) async {
    // Same as before
  }

  Future<void> _showDeleteDialog(BuildContext context, User user) async {
    // Same as before
  }
}

class UserDataSource extends DataTableSource {
  final BuildContext context;
  final List<User> users;
  final Function(BuildContext, {User? user}) onEdit;
  final Function(BuildContext, User user) onDelete;

  UserDataSource(this.context, this.users, this.onEdit, this.onDelete);

  @override
  DataRow? getRow(int index) {
    if (index >= users.length) return null;

    final user = users[index];

    return DataRow(
      cells: [
        DataCell(Text(user.username)),
        DataCell(Text(user.email)),
        DataCell(
          Row(
            mainAxisSize: MainAxisSize.min,
            children: [
              IconButton(
                icon: Icon(Icons.bookmark, color: Colors.blue),
                tooltip: "View Bookmarks (${user.bookmarkCount})",
                onPressed: () {
                  Navigator.push(
                    context,
                    MaterialPageRoute(
                      builder: (context) => BookmarkedBooksScreen(
                        userId: user.id,
                        username: user.username,
                      ),
                    ),
                  );
                },
              ),
              IconButton(
                icon: Icon(Icons.rate_review, color: Colors.green),
                tooltip: "View Reviews",
                onPressed: () {
                  Navigator.push(
                    context,
                    MaterialPageRoute(
                      builder: (context) => UserReviewsScreen(
                        userId: user.id,
                        username: user.username,
                      ),
                    ),
                  );
                },
              ),
              IconButton(
                icon: Icon(Icons.edit, color: Colors.orange),
                tooltip: "Edit User",
                onPressed: () => onEdit(context, user: user),
              ),
              IconButton(
                icon: Icon(Icons.delete, color: Colors.red),
                tooltip: "Delete User",
                onPressed: () => onDelete(context, user),
              ),
            ],
          ),
        ),
      ],
    );
  }

  @override
  bool get isRowCountApproximate => false;

  @override
  int get rowCount => users.length;

  @override
  int get selectedRowCount => 0;
}
