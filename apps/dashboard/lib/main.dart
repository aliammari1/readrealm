import 'package:flutter/material.dart';
import 'package:flutter/foundation.dart'; // Fix the kIsWeb import
import 'package:google_fonts/google_fonts.dart';
import 'package:provider/provider.dart';
import 'package:path_provider/path_provider.dart';
import 'package:flutter_library_app/constants.dart';
import 'package:flutter_library_app/controllers/menu_app_controller.dart';
import 'package:flutter_library_app/providers/auth_provider.dart';
import 'package:flutter_library_app/providers/search_provider.dart'; // Import SearchProvider
import 'package:flutter_library_app/screens/login_screen.dart';
import 'package:flutter_library_app/screens/main/main_screen.dart';
import 'package:flutter_library_app/screens/register_screen.dart';
import 'package:flutter_library_app/screens/bookmarked_books/bookmarked_books_screen.dart';

void main() async {
  WidgetsFlutterBinding.ensureInitialized();

  if (!kIsWeb) {
    await getTemporaryDirectory();
  }

  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({Key? key}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return MultiProvider(
      providers: [
        ChangeNotifierProvider(create: (_) => AuthProvider()),
        ChangeNotifierProvider(create: (_) => MenuAppController()),
        ChangeNotifierProvider(
            create: (_) => SearchProvider()), // Add SearchProvider here
      ],
      child: MaterialApp(
        debugShowCheckedModeBanner: false,
        title: 'Flutter Admin Panel',
        theme: ThemeData.dark().copyWith(
          scaffoldBackgroundColor: bgColor,
          textTheme: GoogleFonts.poppinsTextTheme(Theme.of(context).textTheme)
              .apply(bodyColor: Colors.white),
          canvasColor: secondaryColor,
        ),
        home: LoginScreen(),
        routes: {
          '/register': (context) => RegisterScreen(),
          '/dashboard': (context) => MainScreen(),
          '/bookmarks': (context) => BookmarkedBooksScreen(),
        },
      ),
    );
  }
}
