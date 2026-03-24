package tn.esprit.libraryapp.screens

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.*
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.runtime.*
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions
import com.itextpdf.text.Document
import com.itextpdf.text.Paragraph
import com.itextpdf.text.pdf.PdfWriter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.internal.concurrent.formatDuration
import org.jsoup.Jsoup
import org.jsoup.parser.Parser
import tn.esprit.libraryapp.models.BookmarkPage
import tn.esprit.libraryapp.models.ReadingStatistics
import tn.esprit.libraryapp.models.ReadingTheme
import tn.esprit.libraryapp.ui.theme.*
import tn.esprit.libraryapp.utils.ReaderThemes
import tn.esprit.libraryapp.utils.VoiceCommand
import tn.esprit.libraryapp.utils.VoiceCommandHandler
import tn.esprit.libraryapp.viewModel.EPubReaderViewModel
import java.io.BufferedInputStream
import java.io.File
import java.io.InputStreamReader
import java.util.Locale
import java.util.zip.ZipFile
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.random.Random

private val languageToLocale =
    mapOf(
        "en" to Locale.ENGLISH,
        "fr" to Locale.FRENCH,
        "es" to Locale("es"),
        "de" to Locale.GERMAN,
        "it" to Locale.ITALIAN,
        "ar" to Locale("ar"),
    )

data class ReadingPreferences(
    val fontSize: Float = 16f,
    val lineHeight: Float = 1.5f,
    val isDarkMode: Boolean = false,
    val backgroundColor: Color = Color.White,
    val textColor: Color = Color.Black,
    val targetLanguage: String = "en",
    val theme: ReadingTheme = ReaderThemes.themes[0],
    val autoScroll: Boolean = false,
    val autoScrollSpeed: Float = 1f,
    val showReadingStats: Boolean = true,
    val enableGestures: Boolean = true,
)

// ═══════════════════════════════════════════════════════════════════
// ANCIENT TOME READING EXPERIENCE - Enchanted Reader Components
// ═══════════════════════════════════════════════════════════════════

/**
 * Mystical reading chamber background with floating dust particles
 * and candlelight ambiance
 */
@Composable
private fun AncientReadingChamber(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "chamber")
    
    // Candlelight flicker effect
    val candleFlicker by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "candle"
    )
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        DeepLibraryBrown,
                        Color(0xFF1A0D07),
                        DeepLibraryBrown.copy(alpha = 0.95f)
                    )
                )
            )
    ) {
        // Floating dust particles
        FloatingDustParticles()
        
        // Candlelight glow corners
        CandlelightCorners(flicker = candleFlicker)
        
        content()
    }
}

@Composable
private fun FloatingDustParticles() {
    // Simple static dust effect
    Canvas(modifier = Modifier.fillMaxSize()) {
        val dustPositions = listOf(
            Offset(size.width * 0.2f, size.height * 0.3f),
            Offset(size.width * 0.7f, size.height * 0.15f),
            Offset(size.width * 0.4f, size.height * 0.6f),
            Offset(size.width * 0.85f, size.height * 0.45f),
            Offset(size.width * 0.15f, size.height * 0.75f),
            Offset(size.width * 0.6f, size.height * 0.85f),
            Offset(size.width * 0.9f, size.height * 0.7f),
            Offset(size.width * 0.35f, size.height * 0.2f),
        )
        
        dustPositions.forEachIndexed { index, position ->
            drawCircle(
                color = CandlelightGlow.copy(alpha = 0.2f + (index % 3) * 0.1f),
                radius = 2f + (index % 4),
                center = position
            )
        }
    }
}

@Composable
private fun CandlelightCorners(flicker: Float) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        // Top left candle glow
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    CandlelightGlow.copy(alpha = 0.15f * flicker),
                    Color.Transparent
                ),
                center = Offset(0f, 0f),
                radius = size.width * 0.4f
            ),
            radius = size.width * 0.4f,
            center = Offset(0f, 0f)
        )
        
        // Top right candle glow
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    CandlelightGlow.copy(alpha = 0.12f * flicker),
                    Color.Transparent
                ),
                center = Offset(size.width, 0f),
                radius = size.width * 0.35f
            ),
            radius = size.width * 0.35f,
            center = Offset(size.width, 0f)
        )
    }
}

/**
 * Ancient tome page design with weathered parchment texture
 */
@Composable
private fun AncientTomePage(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 16.dp,
                shape = CutCornerShape(topEnd = 24.dp),
                ambientColor = Color.Black.copy(alpha = 0.4f)
            )
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        AncientParchment,
                        AncientParchment.copy(alpha = 0.95f),
                        Color(0xFFE8D4B0)
                    )
                ),
                shape = CutCornerShape(topEnd = 24.dp)
            )
            .border(
                width = 3.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        WarmLeather,
                        GildedGold.copy(alpha = 0.6f),
                        WarmLeather
                    )
                ),
                shape = CutCornerShape(topEnd = 24.dp)
            )
    ) {
        // Page aging texture
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Coffee stain effect
            drawCircle(
                color = WarmLeather.copy(alpha = 0.05f),
                radius = 80f,
                center = Offset(size.width * 0.8f, size.height * 0.3f)
            )
            
            // Edge darkening
            drawRect(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        WarmLeather.copy(alpha = 0.1f),
                        Color.Transparent,
                        Color.Transparent,
                        WarmLeather.copy(alpha = 0.1f)
                    )
                )
            )
        }
        
        // Decorative corner flourish
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(8.dp)
                .size(40.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val gold = GildedGold.copy(alpha = 0.4f)
                drawLine(
                    color = gold,
                    start = Offset(0f, size.height),
                    end = Offset(0f, 0f),
                    strokeWidth = 2f
                )
                drawLine(
                    color = gold,
                    start = Offset(0f, 0f),
                    end = Offset(size.width, 0f),
                    strokeWidth = 2f
                )
                // Decorative curl
                drawArc(
                    color = gold,
                    startAngle = 90f,
                    sweepAngle = 90f,
                    useCenter = false,
                    style = Stroke(width = 1.5f),
                    size = Size(30f, 30f),
                    topLeft = Offset(5f, 5f)
                )
            }
        }
        
        content()
    }
}

/**
 * Mystical progress arc showing reading journey
 */
@Composable
private fun MysticalProgressArc(
    progress: Float,
    currentPage: Int,
    totalPages: Int,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "arc")
    val runeGlow by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )
    
    Box(
        modifier = modifier
            .size(100.dp)
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val sweepAngle = 270f * progress
            
            // Background rune circle
            drawArc(
                color = WarmLeather.copy(alpha = 0.3f),
                startAngle = 135f,
                sweepAngle = 270f,
                useCenter = false,
                style = Stroke(width = 8f, cap = StrokeCap.Round)
            )
            
            // Progress arc with mystical gradient
            drawArc(
                brush = Brush.sweepGradient(
                    colors = listOf(
                        MysticPurple,
                        GildedGold,
                        MysticPurple
                    )
                ),
                startAngle = 135f,
                sweepAngle = sweepAngle,
                useCenter = false,
                style = Stroke(
                    width = 8f,
                    cap = StrokeCap.Round,
                    pathEffect = PathEffect.cornerPathEffect(4f)
                )
            )
            
            // Glowing endpoint
            if (progress > 0) {
                val angle = Math.toRadians((135 + sweepAngle).toDouble())
                val radius = size.width / 2 - 4
                val endX = center.x + radius * cos(angle).toFloat()
                val endY = center.y + radius * sin(angle).toFloat()
                
                drawCircle(
                    color = GildedGold.copy(alpha = runeGlow),
                    radius = 8f,
                    center = Offset(endX, endY)
                )
            }
        }
        
        // Center page indicator
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "${currentPage + 1}",
                color = GildedGold,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Serif
            )
            Text(
                text = "of $totalPages",
                color = AncientParchment.copy(alpha = 0.7f),
                fontSize = 10.sp,
                fontFamily = FontFamily.Serif
            )
        }
    }
}

/**
 * Enchanted Reader Control Runes - mystical floating action buttons
 */
@Composable
private fun EnchantedControlRune(
    icon: @Composable () -> Unit,
    onClick: () -> Unit,
    isActive: Boolean = false,
    isLoading: Boolean = false,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "rune")
    val glowPulse by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )
    
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotate"
    )
    
    Box(
        modifier = modifier
            .size(48.dp)
            .drawBehind {
                if (isActive) {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                GildedGold.copy(alpha = 0.4f * glowPulse),
                                Color.Transparent
                            )
                        ),
                        radius = size.width * 0.8f
                    )
                }
            }
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        if (isActive) MysticPurple else RichMahogany,
                        if (isActive) MysticPurple.copy(alpha = 0.7f) else WarmLeather.copy(alpha = 0.8f)
                    )
                ),
                shape = CircleShape
            )
            .border(
                width = 2.dp,
                brush = Brush.sweepGradient(
                    colors = listOf(
                        GildedGold.copy(alpha = if (isActive) 0.8f else 0.4f),
                        WarmLeather,
                        GildedGold.copy(alpha = if (isActive) 0.8f else 0.4f)
                    )
                ),
                shape = CircleShape
            )
            .clickable(enabled = !isLoading, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier
                    .size(24.dp)
                    .rotate(rotation),
                strokeWidth = 2.dp,
                color = GildedGold
            )
        } else {
            Box(
                modifier = Modifier.scale(if (isActive) 1.1f else 1f)
            ) {
                icon()
            }
        }
    }
}

/**
 * Enchanted Reader Header - Mystical scroll-like top bar
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EnchantedReaderHeader(
    progress: Float,
    onSettingsClick: () -> Unit,
    onLanguageSelect: (String) -> Unit,
    currentLanguage: String,
    isSpeaking: Boolean,
    onTtsClick: () -> Unit,
    isPdfLoading: Boolean,
    onPdfClick: () -> Unit,
) {
    var showLanguageMenu by remember { mutableStateOf(false) }
    val supportedLanguages = remember {
        mapOf(
            "en" to "🇬🇧 English",
            "fr" to "🇫🇷 French",
            "es" to "🇪🇸 Spanish",
            "de" to "🇩🇪 German",
            "it" to "🇮🇹 Italian",
            "ar" to "🇸🇦 Arabic",
        )
    }
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        DeepLibraryBrown,
                        DeepLibraryBrown.copy(alpha = 0.9f),
                        Color.Transparent
                    )
                )
            )
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // Decorative scroll ends
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Title with mystical styling
            Column {
                Text(
                    text = "📖 Ancient Tome",
                    color = GildedGold,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Serif
                )
                Text(
                    text = "Reading Chamber",
                    color = AncientParchment.copy(alpha = 0.7f),
                    fontSize = 12.sp,
                    fontStyle = FontStyle.Italic,
                    fontFamily = FontFamily.Serif
                )
            }
            
            // Control runes row
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // TTS Oracle Rune
                EnchantedControlRune(
                    icon = {
                        Icon(
                            imageVector = if (isSpeaking) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isSpeaking) "Silence Oracle" else "Summon Oracle",
                            tint = if (isSpeaking) GildedGold else AncientParchment,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    onClick = onTtsClick,
                    isActive = isSpeaking
                )
                
                // PDF Scroll Creation Rune
                EnchantedControlRune(
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = "Create Scroll",
                            tint = AncientParchment,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    onClick = onPdfClick,
                    isLoading = isPdfLoading
                )
                
                // Translation Rune
                Box {
                    EnchantedControlRune(
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Translate,
                                contentDescription = "Translate Runes",
                                tint = AncientParchment,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        onClick = { showLanguageMenu = true }
                    )
                    
                    DropdownMenu(
                        expanded = showLanguageMenu,
                        onDismissRequest = { showLanguageMenu = false },
                        modifier = Modifier.background(RichMahogany)
                    ) {
                        supportedLanguages.forEach { (code, name) ->
                            DropdownMenuItem(
                                text = { 
                                    Text(
                                        name, 
                                        color = if (code == currentLanguage) GildedGold else AncientParchment
                                    ) 
                                },
                                onClick = {
                                    onLanguageSelect(code)
                                    showLanguageMenu = false
                                },
                                leadingIcon = {
                                    if (code == currentLanguage) {
                                        Icon(
                                            Icons.Default.Check, 
                                            null,
                                            tint = GildedGold
                                        )
                                    }
                                },
                            )
                        }
                    }
                }
                
                // Arcane Settings Rune
                EnchantedControlRune(
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Arcane Settings",
                            tint = AncientParchment,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    onClick = onSettingsClick
                )
            }
        }
    }
}

/**
 * Arcane Settings Grimoire - Mystical settings panel
 */
@Composable
private fun ArcaneSettingsGrimoire(
    preferences: ReadingPreferences,
    onPreferencesChanged: (ReadingPreferences) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DeepLibraryBrown,
        titleContentColor = GildedGold,
        title = { 
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("⚙️", fontSize = 24.sp)
                Column {
                    Text(
                        "Arcane Settings",
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Customize your reading experience",
                        fontSize = 12.sp,
                        color = AncientParchment.copy(alpha = 0.7f),
                        fontStyle = FontStyle.Italic
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                // Font Size Rune
                ArcaneSettingItem(
                    icon = "📜",
                    title = "Script Size",
                    subtitle = "${preferences.fontSize.toInt()}pt"
                ) {
                    Slider(
                        value = preferences.fontSize,
                        onValueChange = {
                            onPreferencesChanged(preferences.copy(fontSize = it))
                        },
                        valueRange = 12f..24f,
                        colors = SliderDefaults.colors(
                            thumbColor = GildedGold,
                            activeTrackColor = MysticPurple,
                            inactiveTrackColor = WarmLeather.copy(alpha = 0.3f)
                        )
                    )
                }

                // Line Height Rune
                ArcaneSettingItem(
                    icon = "📏",
                    title = "Line Spacing",
                    subtitle = "×${String.format("%.1f", preferences.lineHeight)}"
                ) {
                    Slider(
                        value = preferences.lineHeight,
                        onValueChange = {
                            onPreferencesChanged(preferences.copy(lineHeight = it))
                        },
                        valueRange = 1f..2f,
                        colors = SliderDefaults.colors(
                            thumbColor = GildedGold,
                            activeTrackColor = MysticPurple,
                            inactiveTrackColor = WarmLeather.copy(alpha = 0.3f)
                        )
                    )
                }

                HorizontalDivider(color = WarmLeather.copy(alpha = 0.3f))

                // Dark Mode Toggle
                ArcaneToggleItem(
                    icon = "🌙",
                    title = "Night Reading",
                    subtitle = "Dark chamber mode",
                    checked = preferences.isDarkMode,
                    onCheckedChange = {
                        onPreferencesChanged(preferences.copy(isDarkMode = it))
                    }
                )

                // Auto-scroll Toggle
                ArcaneToggleItem(
                    icon = "🔄",
                    title = "Auto-Scroll Spell",
                    subtitle = "Enchanted page turning",
                    checked = preferences.autoScroll,
                    onCheckedChange = {
                        onPreferencesChanged(preferences.copy(autoScroll = it))
                    }
                )

                // Auto-scroll Speed
                AnimatedVisibility(visible = preferences.autoScroll) {
                    ArcaneSettingItem(
                        icon = "⚡",
                        title = "Scroll Speed",
                        subtitle = "×${String.format("%.1f", preferences.autoScrollSpeed)}"
                    ) {
                        Slider(
                            value = preferences.autoScrollSpeed,
                            onValueChange = {
                                onPreferencesChanged(preferences.copy(autoScrollSpeed = it))
                            },
                            valueRange = 0.5f..3f,
                            colors = SliderDefaults.colors(
                                thumbColor = GildedGold,
                                activeTrackColor = PhoenixOrange,
                                inactiveTrackColor = WarmLeather.copy(alpha = 0.3f)
                            )
                        )
                    }
                }

                HorizontalDivider(color = WarmLeather.copy(alpha = 0.3f))

                // Gesture Control Toggle
                ArcaneToggleItem(
                    icon = "👆",
                    title = "Gesture Magic",
                    subtitle = "Swipe to turn pages",
                    checked = preferences.enableGestures,
                    onCheckedChange = {
                        onPreferencesChanged(preferences.copy(enableGestures = it))
                    }
                )

                // Reading Stats Toggle
                ArcaneToggleItem(
                    icon = "📊",
                    title = "Reading Wisdom",
                    subtitle = "Show statistics overlay",
                    checked = preferences.showReadingStats,
                    onCheckedChange = {
                        onPreferencesChanged(preferences.copy(showReadingStats = it))
                    }
                )
            }
        },
        confirmButton = { 
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(contentColor = GildedGold)
            ) { 
                Text("✨ Apply", fontFamily = FontFamily.Serif) 
            } 
        },
    )
}

@Composable
private fun ArcaneSettingItem(
    icon: String,
    title: String,
    subtitle: String,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(icon, fontSize = 20.sp)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    title,
                    color = AncientParchment,
                    fontWeight = FontWeight.Medium,
                    fontFamily = FontFamily.Serif
                )
                Text(
                    subtitle,
                    color = GildedGold.copy(alpha = 0.7f),
                    fontSize = 12.sp
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        content()
    }
}

@Composable
private fun ArcaneToggleItem(
    icon: String,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(icon, fontSize = 20.sp)
        Column(modifier = Modifier.weight(1f)) {
            Text(
                title,
                color = AncientParchment,
                fontWeight = FontWeight.Medium,
                fontFamily = FontFamily.Serif
            )
            Text(
                subtitle,
                color = GildedGold.copy(alpha = 0.7f),
                fontSize = 12.sp
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = GildedGold,
                checkedTrackColor = MysticPurple,
                uncheckedThumbColor = WarmLeather,
                uncheckedTrackColor = DeepLibraryBrown
            )
        )
    }
}

@Composable
private fun calculateWordsPerPage(content: String, fontSize: Int = 16): Int {
    val density = LocalDensity.current
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val lineHeight = 24.sp
    val paddingTop = 16.dp
    val paddingBottom = 16.dp
    val navigationHeight = 56.dp
    val progressIndicatorHeight = 48.dp

    val availableHeight =
        screenHeight - paddingTop - paddingBottom - navigationHeight - progressIndicatorHeight

    val linesPerPage = with(density) { (availableHeight.toPx() / lineHeight.toPx()).toInt() }

    // Estimate average characters per line based on screen width and font size
    val screenWidth = configuration.screenWidthDp.dp
    val averageCharWidth =
        with(density) { fontSize.dp.toPx() * 0.6f } // Approximate character width
    val charsPerLine = with(density) { (screenWidth.toPx() / averageCharWidth).toInt() }

    // Estimate words per line (average word length of 5 characters plus space)
    val averageWordsPerLine = charsPerLine / 6

    return linesPerPage * averageWordsPerLine
}

@Composable
fun EnchantedHighlightedText(
    text: String,
    currentWordIndex: Int,
    fontSize: TextUnit = 16.sp,
    lineHeight: TextUnit = 24.sp,
    textColor: Color = DeepLibraryBrown,
) {
    val words = text.split(Regex("(?<=\\s)|(?=\\s)"))
    var currentIndex = 0

    Text(
        text =
        buildAnnotatedString {
            words.forEach { word ->
                val style =
                    if (currentIndex == currentWordIndex) {
                        SpanStyle(
                            background = MysticPurple.copy(alpha = 0.3f),
                            color = MysticPurple,
                            fontWeight = FontWeight.Bold
                        )
                    } else {
                        SpanStyle(color = textColor)
                    }
                withStyle(style) { append(word) }
                if (!word.isBlank()) currentIndex++
            }
        },
        modifier = Modifier.verticalScroll(rememberScrollState()),
        fontSize = fontSize,
        lineHeight = lineHeight,
        color = textColor,
        fontFamily = FontFamily.Serif
    )
}

class TranslationManager(private val context: Context) {
    private val translators = mutableMapOf<String, Translator>()
    private val _downloadProgress = MutableStateFlow(0f)
    val downloadProgress = _downloadProgress.asStateFlow()

    fun getTranslator(targetLanguage: String): Translator {
        return translators.getOrPut(targetLanguage) {
            val options =
                TranslatorOptions.Builder()
                    .setSourceLanguage(TranslateLanguage.ENGLISH)
                    .setTargetLanguage(targetLanguage)
                    .build()
            Translation.getClient(options)
        }
    }

    suspend fun translateText(text: String, targetLanguage: String): String {
        val translator = getTranslator(targetLanguage)

        try {
            // Check if model needs downloading
            val conditions = DownloadConditions.Builder().requireWifi().build()

            try {
                _downloadProgress.value = 0.1f
                translator.downloadModelIfNeeded(conditions).await()
                _downloadProgress.value = 0.5f
            } catch (e: Exception) {
                throw Exception("Failed to download language model: ${e.message}")
            }

            // Break text into smaller chunks for translation
            val chunks = text.split(". ").filter { it.isNotBlank() }
            val translatedChunks =
                chunks.mapIndexed { index, chunk ->
                    _downloadProgress.value = 0.5f + (0.5f * index / chunks.size)
                    translator.translate("$chunk.").await()
                }

            _downloadProgress.value = 1f
            return translatedChunks.joinToString(" ")
        } catch (e: Exception) {
            throw Exception("Translation failed: ${e.message}")
        }
    }

    fun cleanup() {
        translators.values.forEach { it.close() }
        translators.clear()
    }
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EPubReaderScreen(bookUrl: String) {
    var content by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var currentPage by remember { mutableIntStateOf(0) }
    var pages by remember { mutableStateOf<List<String>>(emptyList()) }
    var isPdfLoading by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    var epubContent by remember { mutableStateOf<String>("") }
    val wordsPerPage = calculateWordsPerPage(epubContent)
    val scope = rememberCoroutineScope()
    rememberScrollState()
    var downloadProgress by remember { mutableFloatStateOf(0f) }
    var pdfProgress by remember { mutableFloatStateOf(0f) }
    val context = LocalContext.current
    val viewModel: EPubReaderViewModel =
        viewModel(factory = EPubReaderViewModel.Factory(bookUrl, context))
    val progress by viewModel.readingProgress.collectAsState()
    val isSpeaking by viewModel.isSpeaking
    val currentWordIndex by viewModel.currentWordIndex

    var showSettings by remember { mutableStateOf(false) }
    var preferences by remember { mutableStateOf(ReadingPreferences(isDarkMode = true)) }
    var isTranslating by remember { mutableStateOf(false) }
    var translatedContent by remember { mutableStateOf<String?>(null) }

    val translationManager = remember { TranslationManager(context) }
    var translationError by remember { mutableStateOf<String?>(null) }
    val translationProgress by translationManager.downloadProgress.collectAsState()

    // Translation function
    suspend fun translateContent(text: String) {
        isTranslating = true
        translationError = null
        try {
            translatedContent = translationManager.translateText(text, preferences.targetLanguage)
        } catch (e: Exception) {
            translationError = e.message
        } finally {
            isTranslating = false
        }
    }

    val currentContent = translatedContent ?: content

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

    LaunchedEffect(bookUrl) {
        scope.launch {
            try {
                downloadProgress = 0f
                epubContent =
                    withContext(Dispatchers.IO) {
                        downloadAndParseEpub(bookUrl) { progress ->
                            downloadProgress = progress
                        }
                    }
                // Recalculate pages with dynamic wordsPerPage
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

    LaunchedEffect(Unit) {
        viewModel.setOnPageComplete { _ ->
            if (currentPage < pages.size - 1) {
                currentPage++
                // Get next page content
                pages.getOrNull(currentPage)?.let { nextPageContent ->
                    // First translate the content if needed
                    if (preferences.targetLanguage != "en") {
                        scope.launch {
                            try {
                                val translatedNextPage =
                                    translationManager.translateText(
                                        nextPageContent,
                                        preferences.targetLanguage,
                                    )
                                viewModel.startSpeaking(translatedNextPage)
                            } catch (e: Exception) {
                                // Fallback to original content if translation fails
                                viewModel.startSpeaking(nextPageContent)
                            }
                        }
                    } else {
                        viewModel.startSpeaking(nextPageContent)
                    }
                }
            }
        }
    }

    DisposableEffect(Unit) { onDispose { translationManager.cleanup() } }

    fun updateTtsLanguage(languageCode: String) {
        viewModel.pauseSpeaking() // Stop current speech
        viewModel.updateTtsLanguage(languageToLocale[languageCode] ?: Locale.ENGLISH)
    }

    var showThemeSelector by remember { mutableStateOf(false) }
    var showBookmarks by remember { mutableStateOf(false) }
    var showStats by remember { mutableStateOf(false) }

    val activity = LocalContext.current as Activity
    val voiceCommandHandler = remember { VoiceCommandHandler(activity) }
    val isListeningForCommands by voiceCommandHandler.isListening.collectAsState()

    DisposableEffect(Unit) { onDispose { voiceCommandHandler.destroy() } }

    fun handleVoiceCommand(command: VoiceCommand) {
        when (command) {
            VoiceCommand.START -> {
                if (!isSpeaking) {
                    (translatedContent ?: content)?.let { textToSpeak ->
                        viewModel.startSpeaking(textToSpeak)
                    }
                }
            }

            VoiceCommand.STOP -> {
                if (isSpeaking) {
                    viewModel.pauseSpeaking()
                }
            }

            VoiceCommand.NEXT -> {
                if (currentPage < pages.size - 1) {
                    currentPage++
                }
            }

            VoiceCommand.PREVIOUS -> {
                if (currentPage > 0) {
                    currentPage--
                }
            }
        }
    }

    // Add periodic stats update
    LaunchedEffect(Unit) {
        while (true) {
            delay(30000) // Update every 30 seconds
            viewModel.updateReadingStats(content, currentPage)
        }
    }

    // Add cleanup
    DisposableEffect(Unit) {
        onDispose {
            viewModel.endReadingSession()
            voiceCommandHandler.destroy()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = DeepLibraryBrown,
        floatingActionButton = {
            // Enchanted bookmark ribbon FAB
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(DragonsBlood, PhoenixOrange)
                        ),
                        shape = CircleShape
                    )
                    .border(
                        2.dp,
                        GildedGold.copy(alpha = 0.5f),
                        CircleShape
                    )
                    .clickable {
                        content?.let { currentText ->
                            viewModel.addBookmark(currentPage, currentText.take(50) + "...")
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.BookmarkAdd, 
                    "Add Bookmark",
                    tint = AncientParchment
                )
            }
        },
    ) { paddingValues ->
        AncientReadingChamber(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                isLoading -> {
                    MysticalLoadingScroll(downloadProgress)
                }

                error != null -> {
                    AncientErrorRune(error!!)
                }

                currentContent != null -> {
                    Column(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // Enchanted Header
                        EnchantedReaderHeader(
                            progress = currentPage.toFloat() / pages.size,
                            onSettingsClick = { showSettings = true },
                            onLanguageSelect = { newLang ->
                                preferences = preferences.copy(targetLanguage = newLang)
                                updateTtsLanguage(newLang)
                                scope.launch { content?.let { translateContent(it) } }
                            },
                            currentLanguage = preferences.targetLanguage,
                            isSpeaking = isSpeaking,
                            onTtsClick = {
                                if (isSpeaking) {
                                    viewModel.pauseSpeaking()
                                } else {
                                    (translatedContent ?: content)?.let { textToSpeak ->
                                        viewModel.startSpeaking(textToSpeak)
                                    }
                                }
                            },
                            isPdfLoading = isPdfLoading,
                            onPdfClick = {
                                scope.launch {
                                    isPdfLoading = true
                                    pdfProgress = 0f
                                    try {
                                        createAndOpenPdf(epubContent, context) { progress ->
                                            pdfProgress = progress
                                        }
                                        snackbarHostState.showSnackbar("📜 Scroll created and opened!")
                                    } catch (e: Exception) {
                                        snackbarHostState.showSnackbar("⚠️ Spell failed: ${e.message}")
                                    } finally {
                                        isPdfLoading = false
                                        pdfProgress = 0f
                                    }
                                }
                            },
                        )
                        
                        // Main reading area
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 16.dp)
                        ) {
                            // Mystical Progress Arc
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                MysticalProgressArc(
                                    progress = currentPage.toFloat() / pages.size.coerceAtLeast(1),
                                    currentPage = currentPage,
                                    totalPages = pages.size
                                )
                            }

                            // Translation progress
                            if (isTranslating) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(4.dp)
                                            .background(
                                                WarmLeather.copy(alpha = 0.3f),
                                                RoundedCornerShape(2.dp)
                                            )
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxHeight()
                                                .fillMaxWidth(translationProgress)
                                                .background(
                                                    brush = Brush.horizontalGradient(
                                                        colors = listOf(MysticPurple, GildedGold)
                                                    ),
                                                    RoundedCornerShape(2.dp)
                                                )
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "🔮 Translating runes... ${(translationProgress * 100).toInt()}%",
                                        fontSize = 12.sp,
                                        color = MysticPurple,
                                        fontStyle = FontStyle.Italic
                                    )
                                }
                            }

                            translationError?.let { errorMsg ->
                                Text(
                                    text = "⚠️ $errorMsg",
                                    color = DragonsBlood,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                            }

                            // Ancient Tome Page with content
                            AncientTomePage(
                                modifier = Modifier
                                    .weight(1f)
                                    .pointerInput(Unit) {
                                        if (preferences.enableGestures) {
                                            detectDragGestures { change, dragAmount ->
                                                change.consume()
                                                if (abs(dragAmount.x) > abs(dragAmount.y)) {
                                                    if (dragAmount.x > 0 && currentPage > 0) {
                                                        scope.launch { currentPage-- }
                                                    } else if (dragAmount.x < 0 && currentPage < pages.size - 1) {
                                                        scope.launch { currentPage++ }
                                                    }
                                                }
                                            }
                                        }
                                    }
                            ) {
                                Box(modifier = Modifier.fillMaxSize()) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(20.dp)
                                    ) {
                                        // Reading stats at top
                                        if (preferences.showReadingStats) {
                                            MysticalReadingWisdom(
                                                statistics = viewModel.readingStats.value,
                                                modifier = Modifier.padding(bottom = 12.dp)
                                            )
                                        }

                                        // Content with highlighted text
                                        if (isTranslating) {
                                            Box(
                                                modifier = Modifier.fillMaxSize(),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Column(
                                                    horizontalAlignment = Alignment.CenterHorizontally
                                                ) {
                                                    Text("🔮", fontSize = 48.sp)
                                                    Spacer(modifier = Modifier.height(16.dp))
                                                    Text(
                                                        "Deciphering ancient runes...",
                                                        color = WarmLeather,
                                                        fontStyle = FontStyle.Italic
                                                    )
                                                }
                                            }
                                        } else {
                                            EnchantedHighlightedText(
                                                text = currentContent!!,
                                                currentWordIndex = currentWordIndex,
                                                fontSize = preferences.fontSize.sp,
                                                lineHeight = preferences.lineHeight.em,
                                                textColor = DeepLibraryBrown
                                            )
                                        }
                                    }

                                    // Auto-scroll magic trail
                                    if (preferences.autoScroll) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(3.dp)
                                                .align(Alignment.BottomCenter)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxHeight()
                                                    .fillMaxWidth(viewModel.autoScrollProgress.value)
                                                    .background(
                                                        brush = Brush.horizontalGradient(
                                                            colors = listOf(
                                                                MysticPurple,
                                                                GildedGold,
                                                                PhoenixOrange
                                                            )
                                                        )
                                                    )
                                            )
                                        }
                                    }
                                }
                            }

                            // Mystical Page Turner
                            AnimatedVisibility(
                                visible = true,
                                enter = slideInVertically() + fadeIn(),
                                exit = slideOutVertically() + fadeOut(),
                            ) {
                                MysticalPageTurner(
                                    currentPage = currentPage,
                                    totalPages = pages.size,
                                    onPrevious = { if (currentPage > 0) currentPage-- },
                                    onNext = { if (currentPage < pages.size - 1) currentPage++ },
                                )
                            }
                        }
                        
                        // Enchanted Bottom Controls
                        EnchantedControlsBar(
                            preferences = preferences,
                            onAutoScrollToggle = { enabled ->
                                preferences = preferences.copy(autoScroll = enabled)
                                viewModel.toggleAutoScroll(enabled, preferences.autoScrollSpeed)
                            },
                            onSpeedChange = { speed ->
                                preferences = preferences.copy(autoScrollSpeed = speed)
                                if (preferences.autoScroll) {
                                    viewModel.toggleAutoScroll(true, speed)
                                }
                            },
                            onBookmarkClick = { showBookmarks = true },
                            onThemeClick = { showThemeSelector = true },
                            onStatsClick = { showStats = true },
                            onVoiceCommandClick = {
                                if (isListeningForCommands) {
                                    voiceCommandHandler.stopListening()
                                } else {
                                    voiceCommandHandler.startListening { command ->
                                        handleVoiceCommand(command)
                                    }
                                }
                            },
                            isListeningForCommands = isListeningForCommands,
                        )
                    }
                }
            }
        }
    }

    // Arcane Settings Grimoire
    if (showSettings) {
        ArcaneSettingsGrimoire(
            preferences = preferences,
            onPreferencesChanged = { preferences = it },
            onDismiss = { showSettings = false },
        )
    }

    // Enchanted Theme Selector
    if (showThemeSelector) {
        EnchantedThemeSelector(
            currentTheme = preferences.theme,
            onThemeSelect = { theme ->
                preferences = preferences.copy(theme = theme)
                showThemeSelector = false
            },
            onDismiss = { showThemeSelector = false },
        )
    }

    // Magical Bookmarks Dialog
    if (showBookmarks) {
        AlertDialog(
            onDismissRequest = { showBookmarks = false },
            containerColor = DeepLibraryBrown,
            titleContentColor = GildedGold,
            title = { 
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("🔖", fontSize = 24.sp)
                    Text(
                        "Magical Bookmarks",
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                MagicalBookmarkScroll(
                    bookmarks = viewModel.bookmarks.value,
                    onBookmarkClick = { page ->
                        currentPage = page
                        showBookmarks = false
                    },
                )
            },
            confirmButton = {
                TextButton(
                    onClick = { showBookmarks = false },
                    colors = ButtonDefaults.textButtonColors(contentColor = GildedGold)
                ) { 
                    Text("Close", fontFamily = FontFamily.Serif) 
                }
            },
        )
    }

    // Reading Statistics Dialog
    if (showStats) {
        AlertDialog(
            onDismissRequest = { showStats = false },
            containerColor = DeepLibraryBrown,
            titleContentColor = GildedGold,
            title = { 
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("📊", fontSize = 24.sp)
                    Text(
                        "Reading Wisdom",
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                MysticalReadingWisdom(
                    statistics = viewModel.readingStats.value,
                    modifier = Modifier.fillMaxWidth(),
                )
            },
            confirmButton = { 
                TextButton(
                    onClick = { showStats = false },
                    colors = ButtonDefaults.textButtonColors(contentColor = GildedGold)
                ) { 
                    Text("Close", fontFamily = FontFamily.Serif) 
                } 
            },
        )
    }
}

/**
 * Mystical Loading Scroll - Ancient tome materializing
 */
@Composable
private fun MysticalLoadingScroll(progress: Float) {
    val infiniteTransition = rememberInfiniteTransition(label = "loading")
    
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )
    
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )
    
    AncientReadingChamber {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            // Magical tome appearing
            Box(
                modifier = Modifier
                    .size(150.dp)
                    .scale(pulseScale),
                contentAlignment = Alignment.Center
            ) {
                // Outer mystical circle
                Canvas(modifier = Modifier.fillMaxSize()) {
                    rotate(rotation) {
                        drawCircle(
                            brush = Brush.sweepGradient(
                                colors = listOf(
                                    MysticPurple,
                                    GildedGold,
                                    MysticPurple.copy(alpha = 0.5f),
                                    GildedGold.copy(alpha = 0.5f),
                                    MysticPurple
                                )
                            ),
                            style = Stroke(
                                width = 4f,
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 10f))
                            )
                        )
                    }
                    
                    // Rune symbols around circle
                    val runeAngles = listOf(0f, 60f, 120f, 180f, 240f, 300f)
                    runeAngles.forEach { angle ->
                        rotate(angle + rotation * 0.5f) {
                            drawCircle(
                                color = GildedGold.copy(alpha = 0.7f),
                                radius = 6f,
                                center = Offset(center.x, center.y - size.width / 2 + 20f)
                            )
                        }
                    }
                }
                
                // Center book icon
                Text(
                    text = "📖",
                    fontSize = 48.sp,
                    modifier = Modifier.alpha(pulseScale)
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Progress indicator
            if (progress > 0f) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Summoning Ancient Text...",
                        color = AncientParchment,
                        fontSize = 16.sp,
                        fontFamily = FontFamily.Serif,
                        fontStyle = FontStyle.Italic
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Custom mystical progress bar
                    Box(
                        modifier = Modifier
                            .width(200.dp)
                            .height(8.dp)
                            .background(
                                WarmLeather.copy(alpha = 0.3f),
                                RoundedCornerShape(4.dp)
                            )
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(progress)
                                .background(
                                    brush = Brush.horizontalGradient(
                                        colors = listOf(MysticPurple, GildedGold)
                                    ),
                                    RoundedCornerShape(4.dp)
                                )
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        "${(progress * 100).toInt()}%",
                        color = GildedGold,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Serif
                    )
                }
            } else {
                Text(
                    "Opening the Ancient Tome...",
                    color = AncientParchment,
                    fontSize = 16.sp,
                    fontFamily = FontFamily.Serif,
                    fontStyle = FontStyle.Italic
                )
            }
        }
    }
}

/**
 * Ancient Error Rune - When dark magic fails
 */
@Composable
private fun AncientErrorRune(errorMessage: String) {
    val infiniteTransition = rememberInfiniteTransition(label = "error")
    
    val flicker by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(500),
            repeatMode = RepeatMode.Reverse
        ),
        label = "flicker"
    )
    
    AncientReadingChamber {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            // Broken seal symbol
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .alpha(flicker),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    // Cracked circle
                    drawCircle(
                        color = DragonsBlood.copy(alpha = 0.6f),
                        style = Stroke(
                            width = 4f,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(20f, 10f, 5f, 10f))
                        )
                    )
                    
                    // X mark
                    drawLine(
                        color = DragonsBlood,
                        start = Offset(size.width * 0.3f, size.height * 0.3f),
                        end = Offset(size.width * 0.7f, size.height * 0.7f),
                        strokeWidth = 6f,
                        cap = StrokeCap.Round
                    )
                    drawLine(
                        color = DragonsBlood,
                        start = Offset(size.width * 0.7f, size.height * 0.3f),
                        end = Offset(size.width * 0.3f, size.height * 0.7f),
                        strokeWidth = 6f,
                        cap = StrokeCap.Round
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                "The Spell Has Failed",
                color = DragonsBlood,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Serif
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        DragonsBlood.copy(alpha = 0.1f),
                        RoundedCornerShape(12.dp)
                    )
                    .border(
                        1.dp,
                        DragonsBlood.copy(alpha = 0.3f),
                        RoundedCornerShape(12.dp)
                    )
                    .padding(16.dp)
            ) {
                Text(
                    errorMessage,
                    color = AncientParchment.copy(alpha = 0.9f),
                    fontSize = 14.sp,
                    fontFamily = FontFamily.Serif,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                "🔮 Try summoning the tome again",
                color = MysticPurple,
                fontSize = 14.sp,
                fontStyle = FontStyle.Italic
            )
        }
    }
}

/**
 * Mystical Page Turner - Navigation with magical gestures
 */
@Composable
private fun MysticalPageTurner(
    currentPage: Int,
    totalPages: Int,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Previous page button - Left scroll edge
        Box(
            modifier = Modifier
                .weight(1f)
                .height(48.dp)
                .background(
                    brush = Brush.horizontalGradient(
                        colors = if (currentPage > 0) 
                            listOf(MysticPurple.copy(alpha = 0.3f), Color.Transparent)
                        else 
                            listOf(WarmLeather.copy(alpha = 0.2f), Color.Transparent)
                    ),
                    shape = RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp)
                )
                .clickable(enabled = currentPage > 0) { onPrevious() },
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.ChevronLeft,
                    contentDescription = "Previous",
                    tint = if (currentPage > 0) GildedGold else WarmLeather.copy(alpha = 0.5f)
                )
                Text(
                    "Previous",
                    color = if (currentPage > 0) AncientParchment else WarmLeather.copy(alpha = 0.5f),
                    fontFamily = FontFamily.Serif
                )
            }
        }

        // Page indicator - Ancient seal
        Box(
            modifier = Modifier
                .size(56.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(RichMahogany, DeepLibraryBrown)
                    ),
                    shape = CircleShape
                )
                .border(
                    2.dp,
                    Brush.sweepGradient(
                        colors = listOf(GildedGold, WarmLeather, GildedGold)
                    ),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "${currentPage + 1}",
                    color = GildedGold,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "/ $totalPages",
                    color = AncientParchment.copy(alpha = 0.7f),
                    fontSize = 9.sp
                )
            }
        }

        // Next page button - Right scroll edge
        Box(
            modifier = Modifier
                .weight(1f)
                .height(48.dp)
                .background(
                    brush = Brush.horizontalGradient(
                        colors = if (currentPage < totalPages - 1)
                            listOf(Color.Transparent, MysticPurple.copy(alpha = 0.3f))
                        else
                            listOf(Color.Transparent, WarmLeather.copy(alpha = 0.2f))
                    ),
                    shape = RoundedCornerShape(topStart = 24.dp, bottomStart = 24.dp)
                )
                .clickable(enabled = currentPage < totalPages - 1) { onNext() },
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    "Next",
                    color = if (currentPage < totalPages - 1) AncientParchment else WarmLeather.copy(alpha = 0.5f),
                    fontFamily = FontFamily.Serif
                )
                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = "Next",
                    tint = if (currentPage < totalPages - 1) GildedGold else WarmLeather.copy(alpha = 0.5f)
                )
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

        val doc = Jsoup.parse(xhtmlContent.toString(), "", Parser.xmlParser())
        doc.text()
    }
}

private suspend fun createAndOpenPdf(
    content: String,
    context: Context,
    onProgress: (Float) -> Unit,
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
                contentValues,
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

/**
 * Mystical Reading Wisdom - Enchanted statistics overlay
 */
@Composable
private fun MysticalReadingWisdom(statistics: ReadingStatistics, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        DeepLibraryBrown.copy(alpha = 0.9f),
                        RichMahogany.copy(alpha = 0.8f),
                        DeepLibraryBrown.copy(alpha = 0.9f)
                    )
                ),
                shape = RoundedCornerShape(12.dp)
            )
            .border(
                1.dp,
                GildedGold.copy(alpha = 0.3f),
                RoundedCornerShape(12.dp)
            )
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        // Time crystal
        WisdomCrystal(
            icon = "⏳",
            value = formatDuration(statistics.timeSpentReading),
            label = "Time"
        )
        
        // Pages read scroll
        WisdomCrystal(
            icon = "📜",
            value = "${statistics.pagesRead}",
            label = "Pages"
        )
        
        // Speed feather
        WisdomCrystal(
            icon = "🪶",
            value = "${statistics.averageReadingSpeed.roundToInt()}",
            label = "Words/min"
        )
    }
}

@Composable
private fun WisdomCrystal(
    icon: String,
    value: String,
    label: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(icon, fontSize = 20.sp)
        Text(
            value,
            color = GildedGold,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Serif
        )
        Text(
            label,
            color = AncientParchment.copy(alpha = 0.7f),
            fontSize = 10.sp
        )
    }
}

/**
 * Magical Bookmark Scroll - List of enchanted bookmarks
 */
@Composable
private fun MagicalBookmarkScroll(
    bookmarks: List<BookmarkPage>,
    onBookmarkClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (bookmarks.isEmpty()) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("🔖", fontSize = 48.sp, modifier = Modifier.alpha(0.5f))
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "No bookmarks yet",
                color = AncientParchment.copy(alpha = 0.7f),
                fontFamily = FontFamily.Serif
            )
            Text(
                "Tap the ribbon to mark your place",
                color = WarmLeather,
                fontSize = 12.sp,
                fontStyle = FontStyle.Italic
            )
        }
    } else {
        LazyColumn(modifier = modifier) {
            items(bookmarks.size) { index ->
                val bookmark = bookmarks[index]
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onBookmarkClick(bookmark.pageNumber) }
                        .padding(vertical = 8.dp, horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Ribbon icon
                    Box(
                        modifier = Modifier
                            .width(4.dp)
                            .height(40.dp)
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(DragonsBlood, PhoenixOrange)
                                ),
                                shape = RoundedCornerShape(2.dp)
                            )
                    )
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Page ${bookmark.pageNumber + 1}",
                            color = GildedGold,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Serif
                        )
                        Text(
                            bookmark.snippet,
                            color = AncientParchment.copy(alpha = 0.8f),
                            fontSize = 12.sp,
                            maxLines = 2
                        )
                    }
                    
                    Icon(
                        Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = WarmLeather
                    )
                }
                
                if (index < bookmarks.size - 1) {
                    HorizontalDivider(
                        color = WarmLeather.copy(alpha = 0.2f),
                        modifier = Modifier.padding(start = 20.dp)
                    )
                }
            }
        }
    }
}

/**
 * Enchanted Controls Bar - Bottom mystical controls
 */
@Composable
private fun EnchantedControlsBar(
    preferences: ReadingPreferences,
    onAutoScrollToggle: (Boolean) -> Unit,
    onSpeedChange: (Float) -> Unit,
    onBookmarkClick: () -> Unit,
    onThemeClick: () -> Unit,
    onStatsClick: () -> Unit,
    onVoiceCommandClick: () -> Unit,
    isListeningForCommands: Boolean,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.Transparent,
                        DeepLibraryBrown.copy(alpha = 0.95f),
                        DeepLibraryBrown
                    )
                )
            )
            .padding(horizontal = 8.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Bookmark rune
            EnchantedControlRune(
                icon = {
                    Icon(
                        Icons.Default.Bookmark,
                        "Bookmarks",
                        tint = AncientParchment,
                        modifier = Modifier.size(20.dp)
                    )
                },
                onClick = onBookmarkClick
            )
            
            // Auto-scroll rune
            EnchantedControlRune(
                icon = {
                    Icon(
                        if (preferences.autoScroll) Icons.Default.Pause else Icons.Default.PlayArrow,
                        "Auto-scroll",
                        tint = if (preferences.autoScroll) GildedGold else AncientParchment,
                        modifier = Modifier.size(20.dp)
                    )
                },
                onClick = { onAutoScrollToggle(!preferences.autoScroll) },
                isActive = preferences.autoScroll
            )
            
            // Speed slider (visible when auto-scroll is on)
            AnimatedVisibility(visible = preferences.autoScroll) {
                Box(
                    modifier = Modifier
                        .width(80.dp)
                        .background(
                            WarmLeather.copy(alpha = 0.3f),
                            RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 8.dp)
                ) {
                    Slider(
                        value = preferences.autoScrollSpeed,
                        onValueChange = onSpeedChange,
                        valueRange = 0.5f..3f,
                        colors = SliderDefaults.colors(
                            thumbColor = GildedGold,
                            activeTrackColor = MysticPurple,
                            inactiveTrackColor = WarmLeather.copy(alpha = 0.3f)
                        ),
                        modifier = Modifier.height(24.dp)
                    )
                }
            }
            
            // Theme palette rune
            EnchantedControlRune(
                icon = {
                    Icon(
                        Icons.Default.Palette,
                        "Themes",
                        tint = AncientParchment,
                        modifier = Modifier.size(20.dp)
                    )
                },
                onClick = onThemeClick
            )
            
            // Stats rune
            EnchantedControlRune(
                icon = {
                    Icon(
                        Icons.Default.Timeline,
                        "Reading Stats",
                        tint = AncientParchment,
                        modifier = Modifier.size(20.dp)
                    )
                },
                onClick = onStatsClick
            )
            
            // Voice command rune
            EnchantedControlRune(
                icon = {
                    Icon(
                        if (isListeningForCommands) Icons.Default.Mic else Icons.Default.MicNone,
                        if (isListeningForCommands) "Stop voice" else "Voice commands",
                        tint = if (isListeningForCommands) PhoenixOrange else AncientParchment,
                        modifier = Modifier.size(20.dp)
                    )
                },
                onClick = onVoiceCommandClick,
                isActive = isListeningForCommands
            )
        }
    }
}

/**
 * Enchanted Theme Selector - Mystical reading ambiance chooser
 */
@Composable
private fun EnchantedThemeSelector(
    currentTheme: ReadingTheme,
    onThemeSelect: (ReadingTheme) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DeepLibraryBrown,
        titleContentColor = GildedGold,
        title = { 
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("🎨", fontSize = 24.sp)
                Text(
                    "Reading Ambiance",
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            LazyColumn {
                items(ReaderThemes.themes.size) { index ->
                    val theme = ReaderThemes.themes[index]
                    val isSelected = theme == currentTheme
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onThemeSelect(theme) }
                            .background(
                                if (isSelected) MysticPurple.copy(alpha = 0.2f) 
                                else Color.Transparent,
                                RoundedCornerShape(8.dp)
                            )
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Theme preview circle
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(
                                        theme.backgroundColor,
                                        CircleShape
                                    )
                                    .border(
                                        2.dp,
                                        theme.textColor.copy(alpha = 0.5f),
                                        CircleShape
                                    )
                            )
                            
                            Text(
                                theme.name,
                                color = AncientParchment,
                                fontFamily = FontFamily.Serif
                            )
                        }
                        
                        if (isSelected) {
                            Icon(
                                Icons.Default.Check,
                                null,
                                tint = GildedGold
                            )
                        }
                    }
                }
            }
        },
        confirmButton = { 
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(contentColor = GildedGold)
            ) { 
                Text("✨ Done", fontFamily = FontFamily.Serif) 
            } 
        },
    )
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Preview(showBackground = true)
@Composable
fun EPubReaderScreenPreview() {
    EPubReaderScreen("https://www.gutenberg.org/cache/epub/1513/pg1513.txt")
}
