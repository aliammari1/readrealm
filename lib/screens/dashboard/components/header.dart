import 'package:flutter_library_app/constants.dart';
import 'package:flutter_library_app/controllers/menu_app_controller.dart';
import 'package:flutter_library_app/providers/auth_provider.dart';
import 'package:flutter_library_app/providers/search_provider.dart';
import 'package:flutter_library_app/responsive.dart';
import 'package:flutter/material.dart';
import 'package:flutter_svg/flutter_svg.dart';
import 'package:provider/provider.dart';
import 'package:flutter_library_app/screens/bookmarked_books/bookmarked_books_screen.dart';
import 'package:flutter_library_app/models/user_model.dart'; // Add this import

class Header extends StatelessWidget {
  const Header({Key? key}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return Row(
      children: [
        if (!Responsive.isDesktop(context))
          IconButton(
            icon: Icon(Icons.menu),
            onPressed: context.read<MenuAppController>().controlMenu,
          ),
        Text(
          "Dashboard",
          style: Theme.of(context).textTheme.titleLarge,
        ),
        Spacer(),
        Expanded(child: SearchField()),
        BookmarkIcon(), // Add this line
        Spacer(flex: 1),

        ProfileCard(),
      ],
    );
  }
}

class BookmarkIcon extends StatelessWidget {
  const BookmarkIcon({Key? key}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    final currentUser = context.watch<AuthProvider>().currentUser;
    final bookmarkCount = currentUser?.bookmarkCount ?? 0;

    return Container(
      margin: EdgeInsets.only(left: defaultPadding),
      padding: EdgeInsets.all(defaultPadding / 2),
      decoration: BoxDecoration(
        color: secondaryColor,
        borderRadius: BorderRadius.circular(10),
        border: Border.all(color: Colors.white10),
      ),
      child: InkWell(
        onTap: () {
          Navigator.push(
            context,
            MaterialPageRoute(builder: (context) => BookmarkedBooksScreen()),
          );
        },
        child: Stack(
          children: [
            Icon(Icons.bookmark, color: Colors.white),
            if (bookmarkCount > 0)
              Positioned(
                right: 0,
                top: 0,
                child: Container(
                  padding: EdgeInsets.all(2),
                  decoration: BoxDecoration(
                    color: Colors.red,
                    borderRadius: BorderRadius.circular(6),
                  ),
                  constraints: BoxConstraints(
                    minWidth: 14,
                    minHeight: 14,
                  ),
                  child: Text(
                    '$bookmarkCount',
                    style: TextStyle(
                      color: Colors.white,
                      fontSize: 8,
                    ),
                    textAlign: TextAlign.center,
                  ),
                ),
              ),
          ],
        ),
      ),
    );
  }
}

class ProfileCard extends StatelessWidget {
  const ProfileCard({Key? key}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    final authProvider = context.watch<AuthProvider>();
    final currentUser = authProvider.currentUser;

    return PopupMenuButton(
      offset: const Offset(0, 50),
      child: Container(
        margin: EdgeInsets.only(left: defaultPadding),
        padding: EdgeInsets.symmetric(
          horizontal: defaultPadding,
          vertical: defaultPadding / 2,
        ),
        decoration: BoxDecoration(
          color: secondaryColor,
          borderRadius: const BorderRadius.all(Radius.circular(10)),
          border: Border.all(color: Colors.white10),
        ),
        child: Row(
          children: [
            Icon(Icons.person, size: 38),
            if (!Responsive.isMobile(context))
              Padding(
                padding:
                    const EdgeInsets.symmetric(horizontal: defaultPadding / 2),
                child: Text(currentUser?.username ?? 'Guest'),
              ),
            const Icon(Icons.keyboard_arrow_down),
          ],
        ),
      ),
      itemBuilder: (context) => [
        PopupMenuItem(
          child: Row(
            children: [
              Icon(Icons.person, color: Colors.grey),
              SizedBox(width: 8),
              Text("My Profile"),
            ],
          ),
          onTap: () {},
        ),
        PopupMenuItem(
          child: Row(
            children: [
              Icon(Icons.logout, color: Colors.grey),
              SizedBox(width: 8),
              Text("Sign Out"),
            ],
          ),
          onTap: () {
            authProvider.logout();
            Navigator.of(context).pushReplacementNamed('/');
          },
        ),
      ],
    );
  }
}

class SearchField extends StatefulWidget {
  const SearchField({Key? key}) : super(key: key);

  @override
  _SearchFieldState createState() => _SearchFieldState();
}

class _SearchFieldState extends State<SearchField> {
  final TextEditingController _searchController = TextEditingController();

  @override
  void initState() {
    super.initState();
    // Initialize the controller's text with the current search query from the provider.
    _searchController.text = context.read<SearchProvider>().searchQuery;
  }

  @override
  Widget build(BuildContext context) {
    return TextField(
      controller: _searchController,
      onChanged: (query) {
        // Update the search query in the provider when the text changes.
        context.read<SearchProvider>().updateSearchQuery(query);
      },
      decoration: InputDecoration(
        hintText: "Search user",
        fillColor: secondaryColor,
        filled: true,
        border: OutlineInputBorder(
          borderSide: BorderSide.none,
          borderRadius: const BorderRadius.all(Radius.circular(10)),
        ),
        suffixIcon: InkWell(
          onTap: () {},
          child: Container(
            padding: EdgeInsets.all(defaultPadding * 0.75),
            margin: EdgeInsets.symmetric(horizontal: defaultPadding / 2),
            decoration: BoxDecoration(
              color: primaryColor,
              borderRadius: const BorderRadius.all(Radius.circular(10)),
            ),
            child: SvgPicture.asset("assets/icons/Search.svg"),
          ),
        ),
      ),
    );
  }

  @override
  void dispose() {
    _searchController.dispose();
    super.dispose();
  }
}
