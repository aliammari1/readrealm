package tn.esprit.libraryapp.utils

import androidx.compose.ui.graphics.Color
import tn.esprit.libraryapp.models.ReadingTheme

object ReaderThemes {
    val themes = listOf(
        ReadingTheme(
            name = "Light",
            backgroundColor = Color.White,
            textColor = Color.Black,
            accentColor = Color(0xFF2196F3),
            isDark = false,
        ),
        ReadingTheme(
            name = "Dark",
            backgroundColor = Color(0xFF121212),
            textColor = Color.White,
            accentColor = Color(0xFF64B5F6),
            isDark = true,
        ),
        ReadingTheme(
            name = "Sepia",
            backgroundColor = Color(0xFFF7E9D7),
            textColor = Color(0xFF5B4636),
            accentColor = Color(0xFFB68D4C),
            isDark = false,
        ),
        ReadingTheme(
            name = "Night",
            backgroundColor = Color(0xFF1A1B1E),
            textColor = Color(0xFFE2E2E2),
            accentColor = Color(0xFF6B8E23),
            isDark = true,
        ),
    )
}
