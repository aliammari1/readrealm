import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:flutter_library_app/controllers/menu_app_controller.dart';
import 'package:flutter_library_app/responsive.dart';
import 'package:flutter_library_app/screens/dashboard/dashboard_screen.dart';
import 'package:flutter_library_app/screens/main/components/side_menu.dart';

class MainScreen extends StatelessWidget {
  const MainScreen({Key? key}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    final menuController = Provider.of<MenuAppController>(context);
    // Create a single instance of SideMenu
    final sideMenuInstance = SideMenu();

    return Scaffold(
      key: menuController.scaffoldKey,
      drawer: sideMenuInstance,
      body: SafeArea(
        child: Row(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            // Only show side menu on desktop mode
            if (Responsive.isDesktop(context))
              Expanded(child: sideMenuInstance),
            // Main content
            Expanded(
              flex: 5,
              child: DashboardScreen(),
            ),
          ],
        ),
      ),
    );
  }
}
