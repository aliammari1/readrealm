package tn.esprit.libraryapp.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import tn.esprit.libraryapp.models.Book
import tn.esprit.libraryapp.viewModel.BookViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyLibraryScreen(modifier: Modifier = Modifier, onBookClick: (Book) -> Unit = {}) {
    val viewModel: BookViewModel = viewModel()
    val bookmarks by viewModel.bookmarks.collectAsState()
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Currently Reading", "Completed", "Want to Read")

    LaunchedEffect(Unit) { viewModel.loadBookmarks() }

    Scaffold(
            topBar = {
                TopAppBar(
                        title = {
                            Column {
                                Text(
                                        "My Library",
                                        style = MaterialTheme.typography.headlineMedium,
                                        fontWeight = FontWeight.Bold
                                )
                                Text(
                                        "${bookmarks.size} books collected",
                                        style = MaterialTheme.typography.bodySmall
                                )
                            }
                        },
                        actions = {
                            IconButton(onClick = { /* Sort options */}) {
                                Icon(Icons.Default.Sort, "Sort")
                            }
                        }
                )
            }
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            // Reading Stats Card
            ReadingStatsCard(modifier = Modifier.fillMaxWidth().padding(16.dp))

            // Tabs
            ScrollableTabRow(
                    selectedTabIndex = selectedTab,
                    edgePadding = 16.dp,
                    containerColor = MaterialTheme.colorScheme.surface
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = { Text(title) }
                    )
                }
            }

            if (bookmarks.isEmpty()) {
                EmptyLibraryState()
            } else {
                BookGrid(books = bookmarks, onBookClick = onBookClick)
            }
        }
    }
}

@Composable
private fun ReadingStatsCard(modifier: Modifier = Modifier) {
    Card(
            modifier = modifier,
            colors =
                    CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
    ) {
        Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.SpaceAround
        ) {
            StatItem(icon = Icons.Default.Timer, value = "12.5h", label = "Read Time")
            StatItem(icon = Icons.Default.Book, value = "3", label = "Books Read")
            StatItem(icon = Icons.Default.Star, value = "4.5", label = "Avg Rating")
        }
    }
}

@Composable
private fun StatItem(
        icon: ImageVector,
        value: String,
        label: String,
        modifier: Modifier = Modifier
) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, contentDescription = null)
        Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
        )
        Text(text = label, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun BookGrid(books: List<Book>, onBookClick: (Book) -> Unit) {
    LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 150.dp),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
    ) { items(books) { book -> BookCard(book = book, onClick = { onBookClick(book) }) } }
}

@Composable
private fun BookCard(book: Book, onClick: () -> Unit) {
    Card(
            modifier = Modifier.fillMaxWidth().aspectRatio(0.7f).clickable(onClick = onClick),
            shape = RoundedCornerShape(12.dp)
    ) {
        Box {
            AsyncImage(
                    model = book.coverImage ?: "https://via.placeholder.com/150",
                    contentDescription = "Book cover",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
            )

            // Gradient overlay
            Box(
                    modifier =
                            Modifier.fillMaxSize()
                                    .background(
                                            Brush.verticalGradient(
                                                    colors =
                                                            listOf(
                                                                    Color.Transparent,
                                                                    Color.Black.copy(alpha = 0.7f)
                                                            )
                                            )
                                    )
            )

            Column(modifier = Modifier.align(Alignment.BottomStart).padding(12.dp)) {
                Text(
                        text = book.title,
                        style = MaterialTheme.typography.titleSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                )
                Text(
                        text = book.author,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.8f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun EmptyLibraryState() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
        ) {
            Icon(
                    Icons.Default.MenuBook,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                    "Your library is empty",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
            )
            Text(
                    "Start adding books to your collection",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
