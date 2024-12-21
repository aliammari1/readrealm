package tn.esprit.libraryapp.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import tn.esprit.libraryapp.NavigationItem


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    navController: NavHostController,
) {
    var isEditing by remember { mutableStateOf(false) }
    LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Profile Header
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                modifier = Modifier.size(50.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Profile Info
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                ProfileInfoItem(
                    icon = Icons.Default.Person,
                    label = "Username",
                    value = "John Doe",
                    isEditing = isEditing
                )

                Divider(modifier = Modifier.padding(vertical = 8.dp))

                ProfileInfoItem(
                    icon = Icons.Default.Email,
                    label = "Email",
                    value = "john.doe@example.com",
                    isEditing = isEditing
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Actions
        Button(
            onClick = { isEditing = !isEditing },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = if (isEditing) Icons.Default.Save else Icons.Default.Edit,
                contentDescription = null,
                modifier = Modifier.padding(end = 8.dp)
            )
            Text(if (isEditing) "Save Changes" else "Edit Profile")
        }

        OutlinedButton(
            onClick = {
                navController.navigate(NavigationItem.Login.route)
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Logout,
                contentDescription = null,
                modifier = Modifier.padding(end = 8.dp)
            )
            Text("Logout")
        }
    }
}

@Composable
private fun ProfileInfoItem(
    icon: ImageVector,
    label: String,
    value: String,
    isEditing: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(end = 16.dp)
        )

        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            if (isEditing) {
                OutlinedTextField(
                    value = value,
                    onValueChange = { },
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}