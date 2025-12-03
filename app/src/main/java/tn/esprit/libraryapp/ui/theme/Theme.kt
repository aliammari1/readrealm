package tn.esprit.libraryapp.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryLight,
    onPrimary = Color.White,
    primaryContainer = PrimaryDark,
    onPrimaryContainer = SecondaryLight,
    secondary = Secondary,
    onSecondary = TextColor,
    secondaryContainer = SecondaryDark,
    onSecondaryContainer = WhiteColor,
    tertiary = Accent,
    onTertiary = Color.White,
    tertiaryContainer = WoodAccent,
    onTertiaryContainer = OldGold,
    background = LibraryBackgroundDark,
    onBackground = WhiteColor,
    surface = Color(0xFF2C2218),
    onSurface = WhiteColor,
    surfaceVariant = Color(0xFF3D3228),
    onSurfaceVariant = SecondaryLight,
    outline = GrayColor,
    error = VelvetRed,
    onError = Color.White,
)

private val LightColorScheme = lightColorScheme(
    primary = Primary,
    onPrimary = Color.White,
    primaryContainer = SecondaryLight,
    onPrimaryContainer = PrimaryDark,
    secondary = Secondary,
    onSecondary = TextColor,
    secondaryContainer = SecondaryLight,
    onSecondaryContainer = TextColor,
    tertiary = Accent,
    onTertiary = Color.White,
    tertiaryContainer = AccentLight,
    onTertiaryContainer = TextColor,
    background = LibraryBackground,
    onBackground = TextColor,
    surface = WhiteColor,
    onSurface = TextColor,
    surfaceVariant = SecondaryLight,
    onSurfaceVariant = TextSecondary,
    outline = GrayColor,
    error = VelvetRed,
    onError = Color.White,
)

@Suppress("UNUSED_PARAMETER")
@Composable
fun LibraryAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false, // Disabled to use our custom library theme
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content,
    )
}
