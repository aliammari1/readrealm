import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:flutter_library_app/providers/auth_provider.dart';

class SideMenu extends StatelessWidget {
  const SideMenu({Key? key}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    final authProvider = context.watch<AuthProvider>();
    final currentUser = authProvider.currentUser;

    return Container(
      width: 256, // Fixed width for side menu
      child: Drawer(
        child: ListView(
          children: [
            DrawerHeader(
              child: Column(
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  CircleAvatar(
                    radius: 30,
                    child: Icon(Icons.person),
                  ),
                  SizedBox(height: 10),
                  Text(currentUser?.username ?? 'User'),
                  Text(
                    currentUser?.email ?? 'email@example.com',
                    style: TextStyle(fontSize: 12),
                  ),
                ],
              ),
            ),
            DrawerListTile(
              title: "Dashboard",
              icon: Icons.dashboard,
              onTap: () {},
            ),
            DrawerListTile(
              title: "Logout",
              icon: Icons.logout,
              onTap: () {
                authProvider.logout();
                Navigator.of(context).pushReplacementNamed('/');
              },
            ),
          ],
        ),
      ),
    );
  }
}

class DrawerListTile extends StatelessWidget {
  final String title;
  final IconData icon;
  final VoidCallback onTap;

  const DrawerListTile({
    Key? key,
    required this.title,
    required this.icon,
    required this.onTap,
  }) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return ListTile(
      onTap: onTap,
      horizontalTitleGap: 0.0,
      leading: Icon(icon, color: Colors.white54),
      title: Text(
        title,
        style: TextStyle(color: Colors.white54),
      ),
    );
  }
}
