package tn.esprit.libraryapp.screens

import android.util.Log
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.ui.viewinterop.AndroidView
import tn.esprit.libraryapp.models.Book


@Composable
fun ReadBookScreen(book: Book) {
    Log.d("ReadBookScreen", "book: $book")
    Log.d("ReadBookScreen", "book.link: ${book.link}")
    if (!book.link.isNullOrEmpty()) {
        WebViewComponent(url = book.link)
    }
}

@Composable
fun WebViewComponent(url: String) {
    AndroidView(
        factory = { context ->
            WebView(context).apply {
                webViewClient = WebViewClient()
                settings.javaScriptEnabled = true
                loadUrl(url)
            }
        }
    )
}