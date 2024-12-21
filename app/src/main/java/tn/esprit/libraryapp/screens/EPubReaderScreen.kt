package tn.esprit.libraryapp.screens

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallTopAppBar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import com.itextpdf.text.Document
import com.itextpdf.text.Paragraph
import com.itextpdf.text.pdf.PdfWriter
import java.io.BufferedInputStream
import java.io.File
import java.io.InputStreamReader
import java.util.zip.ZipFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup

@Database(entities = [BookProgress::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun bookProgressDao(): BookProgressDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE
                    ?: synchronized(this) {
                        Room.databaseBuilder(
                                        context.applicationContext,
                                        AppDatabase::class.java,
                                        "book_progress_db"
                                )
                                .build()
                                .also { INSTANCE = it }
                    }
        }
    }
}

@Entity(tableName = "book_progress")
data class BookProgress(
        @PrimaryKey val bookUrl: String,
        val lastReadPage: Int = 0,
        val totalPages: Int = 0,
        val readingProgress: Float = 0f,
        val lastReadTimestamp: Long = System.currentTimeMillis()
)

@Dao
interface BookProgressDao {
    @Query("SELECT * FROM book_progress WHERE bookUrl = :url")
    fun getProgress(url: String): Flow<BookProgress?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveProgress(progress: BookProgress)

    @Query(
            "UPDATE book_progress SET lastReadPage = :page, readingProgress = :progress, lastReadTimestamp = :timestamp WHERE bookUrl = :url"
    )
    suspend fun updateProgress(url: String, page: Int, progress: Float, timestamp: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveInitialProgress(progress: BookProgress)
}

class BookProgressRepository(private val bookProgressDao: BookProgressDao) {
    fun getProgress(url: String) = bookProgressDao.getProgress(url)

    suspend fun updateProgress(url: String, currentPage: Int, totalPages: Int) {
        val progress = (currentPage.toFloat() / totalPages.toFloat()).coerceIn(0f, 1f)
        bookProgressDao.updateProgress(
                url = url,
                page = currentPage,
                progress = progress,
                timestamp = System.currentTimeMillis()
        )
    }

    suspend fun saveInitialProgress(url: String, totalPages: Int) {
        val progress = BookProgress(
            bookUrl = url,
            lastReadPage = 0,
            totalPages = totalPages,
            readingProgress = 0f,
            lastReadTimestamp = System.currentTimeMillis()
        )
        bookProgressDao.saveInitialProgress(progress)
    }
}

class EPubReaderViewModel(
        private val repository: BookProgressRepository,
        private val bookUrl: String
) : ViewModel() {

    val readingProgress =
            repository
                    .getProgress(bookUrl)
                    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)

    fun updateReadingProgress(currentPage: Int, totalPages: Int) {
        viewModelScope.launch { repository.updateProgress(bookUrl, currentPage, totalPages) }
    }

    fun initializeProgress(totalPages: Int) {
        viewModelScope.launch {
            repository.saveInitialProgress(bookUrl, totalPages)
        }
    }

    class Factory(private val bookUrl: String, private val context: Context) :
            ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val database = AppDatabase.getDatabase(context)
            val repository = BookProgressRepository(database.bookProgressDao())
            return EPubReaderViewModel(repository, bookUrl) as T
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun EPubReaderScreen(epubUrl: String) {
    var content by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var currentPage by remember { mutableIntStateOf(0) }
    var pages by remember { mutableStateOf<List<String>>(emptyList()) }
    var isPdfLoading by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val wordsPerPage = 500
    var epubContent by remember { mutableStateOf<String>("") }
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    var downloadProgress by remember { mutableFloatStateOf(0f) }
    var pdfProgress by remember { mutableFloatStateOf(0f) }
    val context = LocalContext.current
    val viewModel: EPubReaderViewModel =
            viewModel(factory = EPubReaderViewModel.Factory(epubUrl, context))
    val progress by viewModel.readingProgress.collectAsState()

    LaunchedEffect(progress, pages) {
        if (pages.isNotEmpty() && progress != null) {
            currentPage = progress?.lastReadPage ?: 0
        }
    }

    LaunchedEffect(currentPage) {
        content = pages.getOrNull(currentPage)
        if (pages.isNotEmpty()) {
            viewModel.updateReadingProgress(currentPage, pages.size)
        }
    }

    LaunchedEffect(epubUrl) {
        scope.launch {
            try {
                downloadProgress = 0f
                epubContent =
                        withContext(Dispatchers.IO) {
                            downloadAndParseEpub(epubUrl) { progress ->
                                downloadProgress = progress
                            }
                        }
                pages = epubContent.split(" ").chunked(wordsPerPage).map { it.joinToString(" ") }
                
                // Initialize progress if this is the first time loading the book
                if (progress == null) {
                    viewModel.initializeProgress(pages.size)
                } else {
                    currentPage = progress?.lastReadPage ?: 0
                }
                
                content = pages.getOrNull(currentPage)
                isLoading = false
            } catch (e: Exception) {
                error = e.message
                isLoading = false
            }
        }
    }

    LaunchedEffect(currentPage) { content = pages.getOrNull(currentPage) }

    Scaffold(
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            topBar = {
                SmallTopAppBar(
                        title = { Text("EPUB Reader") },
                        actions = {
                            IconButton(
                                    onClick = {
                                        scope.launch {
                                            isPdfLoading = true
                                            pdfProgress = 0f
                                            try {
                                                val pdfFile =
                                                        createAndOpenPdf(
                                                                epubContent ?: "",
                                                                context
                                                        ) { progress -> pdfProgress = progress }
                                                snackbarHostState.showSnackbar(
                                                        "PDF saved and opened"
                                                )
                                            } catch (e: Exception) {
                                                snackbarHostState.showSnackbar(
                                                        "Error: ${e.message}"
                                                )
                                            } finally {
                                                isPdfLoading = false
                                                pdfProgress = 0f
                                            }
                                        }
                                    },
                                    enabled = !isLoading && epubContent != null && !isPdfLoading
                            ) {
                                if (isPdfLoading) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                                        if (pdfProgress > 0f) {
                                            Text(
                                                    text = "${(pdfProgress * 100).toInt()}%",
                                                    style = MaterialTheme.typography.labelSmall
                                            )
                                        }
                                    }
                                } else {
                                    Icon(
                                            imageVector = Icons.Default.Download,
                                            contentDescription = "Download PDF"
                                    )
                                }
                            }
                        }
                )
            }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            when {
                isLoading -> {
                    Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator()
                        if (downloadProgress > 0f) {
                            Spacer(modifier = Modifier.height(16.dp))
                            LinearProgressIndicator(
                                    progress = downloadProgress,
                                    modifier = Modifier.width(200.dp).padding(horizontal = 16.dp)
                            )
                            Text(
                                    text = "${(downloadProgress * 100).toInt()}%",
                                    modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }
                }
                error != null -> {
                    Text(
                            "Error: $error",
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(16.dp)
                    )
                }
                content != null -> {
                    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                        LinearProgressIndicator(
                                progress = currentPage.toFloat() / pages.size.coerceAtLeast(1),
                                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                        )
                        Text(
                                text =
                                        "Reading Progress: ${(currentPage.toFloat() / pages.size * 100).toInt()}%",
                                style = MaterialTheme.typography.labelMedium,
                                modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Box(
                                modifier =
                                        Modifier.weight(1f)
                                                .background(
                                                        MaterialTheme.colorScheme.surface,
                                                        shape = MaterialTheme.shapes.medium
                                                )
                                                .padding(16.dp)
                        ) {
                            Text(
                                    text = content!!,
                                    modifier = Modifier.verticalScroll(scrollState),
                                    fontSize = 16.sp,
                                    lineHeight = 24.sp
                            )
                        }

                        Row(
                                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                        ) {
                            FilledTonalButton(
                                    onClick = { if (currentPage > 0) currentPage-- },
                                    enabled = currentPage > 0
                            ) { Text("Previous") }

                            Text(
                                    "Page ${currentPage + 1} of ${pages.size}",
                                    style = MaterialTheme.typography.labelLarge
                            )

                            FilledTonalButton(
                                    onClick = { if (currentPage < pages.size - 1) currentPage++ },
                                    enabled = currentPage < pages.size - 1
                            ) { Text("Next") }
                        }
                    }
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
private suspend fun downloadAndParseEpub(epubUrl: String, onProgress: (Float) -> Unit): String {
    return withContext(Dispatchers.IO) {
        val tempFile = File.createTempFile("book", ".epub")
        val client = OkHttpClient()

        val request = Request.Builder().url(epubUrl).build()

        client.newCall(request).execute().use { response ->
            val body = response.body
            val contentLength = body?.contentLength() ?: -1L
            val input = BufferedInputStream(body?.byteStream())

            tempFile.outputStream().use { output ->
                val buffer = ByteArray(8192)
                var bytesRead: Int
                var totalBytesRead = 0L

                while (input.read(buffer).also { bytesRead = it } != -1) {
                    output.write(buffer, 0, bytesRead)
                    totalBytesRead += bytesRead
                    if (contentLength > 0) {
                        val progress = totalBytesRead.toFloat() / contentLength.toFloat()
                        withContext(Dispatchers.Main) { onProgress(progress) }
                    }
                }
            }
        }

        // Rest of the parsing code remains the same
        val xhtmlContent = StringBuilder()
        ZipFile(tempFile).use { zip ->
            zip.entries().asIterator().forEach { entry ->
                if (entry.name.endsWith(".xhtml")) {
                    zip.getInputStream(entry).use { stream ->
                        InputStreamReader(stream).use { reader ->
                            reader.readLines().forEach { line -> xhtmlContent.append(line) }
                        }
                    }
                }
            }
        }

        tempFile.delete()

        val doc = Jsoup.parse(xhtmlContent.toString(), "", org.jsoup.parser.Parser.xmlParser())
        doc.text()
    }
}

@RequiresApi(Build.VERSION_CODES.Q)
private suspend fun createAndOpenPdf(
        content: String,
        context: Context,
        onProgress: (Float) -> Unit
): File {
    return withContext(Dispatchers.IO) {
        val fileName = "book_${System.currentTimeMillis()}.pdf"
        val contentValues =
                ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                }

        val uri =
                context.contentResolver.insert(
                        MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                        contentValues
                )
                        ?: throw IllegalStateException("Failed to create PDF file")

        context.contentResolver.openOutputStream(uri)?.use { outputStream ->
            Document().apply {
                PdfWriter.getInstance(this, outputStream)
                open()

                // Split content into chunks for progress tracking
                val chunks = content.chunked(1000)
                chunks.forEachIndexed { index, chunk ->
                    add(Paragraph(chunk))
                    withContext(Dispatchers.Main) {
                        onProgress((index + 1).toFloat() / chunks.size)
                    }
                }
                close()
            }
        }

        // Open PDF viewer
        try {
            val intent =
                    Intent(Intent.ACTION_VIEW).apply {
                        setDataAndType(uri, "application/pdf")
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
            val chooserIntent = Intent.createChooser(intent, "Open PDF with...")
            chooserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooserIntent)
        } catch (e: Exception) {
            val marketIntent =
                    Intent(Intent.ACTION_VIEW).apply {
                        data = Uri.parse("market://search?q=pdf+viewer")
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
            context.startActivity(marketIntent)
        }

        File(uri.path!!)
    }
}
