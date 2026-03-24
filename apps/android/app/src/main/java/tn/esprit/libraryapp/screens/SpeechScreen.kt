// SpeechScreen.kt
package tn.esprit.libraryapp.screens

import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.util.Log
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.collectLatest
import tn.esprit.libraryapp.ui.theme.*
import tn.esprit.libraryapp.viewModel.SpeechViewModel
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

// ═══════════════════════════════════════════════════════════════════
// ✨ ORACLE'S VOICE - MYSTICAL SPEECH ASSISTANT ✨
// A magical crystal ball interface for voice interaction
// ═══════════════════════════════════════════════════════════════════

private var currentAudioTrack: AudioTrack? = null

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpeechScreen(speechViewModel: SpeechViewModel = viewModel()) {
    val isRecording by speechViewModel.isRecording
    val transcript by speechViewModel.transcript
    val context = LocalContext.current
    var systemMessage by remember { mutableStateOf("speak tunisian arabic dialect") }
    var temperature by remember { mutableStateOf("0.7") }
    val errorState = speechViewModel.errorMessages.collectAsState(initial = null)

    Box(modifier = Modifier.fillMaxSize()) {
        // Mystical background
        OracleBackground()
        
        // Floating mystical particles
        FloatingOracleParticles()
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Mystical header
            OracleHeader()
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Configuration as ancient spell scroll
            AncientSpellScroll(
                systemMessage = systemMessage,
                onSystemMessageChange = { systemMessage = it },
                temperature = temperature,
                onTemperatureChange = { temperature = it }
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Transcript as crystal ball vision
            CrystalBallVision(
                transcript = transcript,
                modifier = Modifier.weight(1f)
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Recording control as mystical orb
            MysticalRecordingOrb(
                isRecording = isRecording,
                onToggle = {
                    if (isRecording) {
                        speechViewModel.stopRecording()
                    } else {
                        speechViewModel.startRecording(
                            systemMessage,
                            temperature.toFloatOrNull() ?: 0.7f,
                        )
                    }
                }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        // Error display as magical warning
        errorState.value?.let { error ->
            MagicalErrorBanner(
                error = error,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
            )
        }
    }

    LaunchedEffect(Unit) {
        speechViewModel.receivedAudio.collectLatest { audioBytes ->
            playAudio(context, audioBytes)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            currentAudioTrack?.apply {
                stop()
                release()
            }
            currentAudioTrack = null
        }
    }
}

// ═══════════════════════════════════════════════════════════════════
// ORACLE BACKGROUND
// ═══════════════════════════════════════════════════════════════════

@Composable
private fun OracleBackground() {
    Box(modifier = Modifier.fillMaxSize()) {
        // Deep mystical gradient
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF0A0510), // Deep void
                            MysticPurple,
                            Color(0xFF1A0A20), // Dark purple
                            DeepLibraryBrown
                        )
                    )
                )
        )
        
        // Mystical circles pattern
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Draw concentric mystical circles
            val centerX = size.width / 2
            val centerY = size.height / 2
            
            for (i in 1..5) {
                val radius = 100f * i
                drawCircle(
                    color = MysticPurple.copy(alpha = 0.1f / i),
                    radius = radius,
                    center = Offset(centerX, centerY),
                    style = Stroke(width = 1.dp.toPx())
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════
// FLOATING ORACLE PARTICLES
// ═══════════════════════════════════════════════════════════════════

@Composable
private fun FloatingOracleParticles() {
    val infiniteTransition = rememberInfiniteTransition(label = "particles")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(15000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "particleTime"
    )
    
    Canvas(modifier = Modifier.fillMaxSize()) {
        val particles = 30
        for (i in 0 until particles) {
            val phase = i.toFloat() / particles
            val x = size.width * ((sin(time * 2 * Math.PI + phase * Math.PI * 2) + 1) / 2).toFloat()
            val y = size.height * ((phase + time) % 1f)
            val alpha = (sin(time * 4 * Math.PI + i) + 1) / 4
            
            drawCircle(
                color = GildedGold.copy(alpha = alpha.toFloat()),
                radius = 2f + (i % 3),
                center = Offset(x, y)
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════════
// ORACLE HEADER
// ═══════════════════════════════════════════════════════════════════

@Composable
private fun OracleHeader() {
    val glowAnimation = rememberInfiniteTransition(label = "headerGlow")
    val glowAlpha by glowAnimation.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "🔮",
            fontSize = 48.sp,
            modifier = Modifier.graphicsLayer {
                shadowElevation = 20f
            }
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Oracle's Voice",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = GildedGold.copy(alpha = glowAlpha)
        )
        
        Text(
            text = "Speak and be heard by the ancients",
            fontSize = 12.sp,
            fontStyle = FontStyle.Italic,
            color = CandlelightGlow.copy(alpha = 0.7f)
        )
    }
}

// ═══════════════════════════════════════════════════════════════════
// ANCIENT SPELL SCROLL (Configuration)
// ═══════════════════════════════════════════════════════════════════

@Composable
private fun AncientSpellScroll(
    systemMessage: String,
    onSystemMessageChange: (String) -> Unit,
    temperature: String,
    onTemperatureChange: (String) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        AncientParchment.copy(alpha = 0.15f),
                        WarmLeather.copy(alpha = 0.1f)
                    )
                ),
                shape = RoundedCornerShape(16.dp)
            )
            .border(
                width = 1.dp,
                color = GildedGold.copy(alpha = 0.4f),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(16.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "📜", fontSize = 20.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Spell Configuration",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = GildedGold
                )
            }
            
            // System message input
            OutlinedTextField(
                value = systemMessage,
                onValueChange = onSystemMessageChange,
                label = { 
                    Text(
                        "✧ Incantation Words",
                        color = CandlelightGlow
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = GildedGold,
                    unfocusedBorderColor = WarmLeather.copy(alpha = 0.5f),
                    focusedTextColor = AncientParchment,
                    unfocusedTextColor = AncientParchment.copy(alpha = 0.8f),
                    cursorColor = GildedGold
                )
            )
            
            // Temperature input
            OutlinedTextField(
                value = temperature,
                onValueChange = onTemperatureChange,
                label = { 
                    Text(
                        "🌡 Magical Intensity",
                        color = CandlelightGlow
                    ) 
                },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = GildedGold,
                    unfocusedBorderColor = WarmLeather.copy(alpha = 0.5f),
                    focusedTextColor = AncientParchment,
                    unfocusedTextColor = AncientParchment.copy(alpha = 0.8f),
                    cursorColor = GildedGold
                )
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════════
// CRYSTAL BALL VISION (Transcript display)
// ═══════════════════════════════════════════════════════════════════

@Composable
private fun CrystalBallVision(
    transcript: String,
    modifier: Modifier = Modifier
) {
    val glowAnimation = rememberInfiniteTransition(label = "crystalGlow")
    val innerGlow by glowAnimation.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "innerGlow"
    )
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
    ) {
        // Outer glow
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            MysticPurple.copy(alpha = innerGlow),
                            MysticPurple.copy(alpha = 0.1f),
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                )
        )
        
        // Crystal ball
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF2A1B3D).copy(alpha = 0.9f),
                            MysticPurple.copy(alpha = 0.7f),
                            InkBlue.copy(alpha = 0.8f)
                        )
                    ),
                    shape = CircleShape
                )
                .border(
                    width = 3.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            GildedGold,
                            WarmLeather,
                            GildedGold
                        )
                    ),
                    shape = CircleShape
                )
                .clip(CircleShape),
            contentAlignment = Alignment.Center
        ) {
            // Inner mystical swirl
            Canvas(modifier = Modifier.fillMaxSize()) {
                val path = Path()
                for (i in 0..360 step 30) {
                    val angle = Math.toRadians(i.toDouble())
                    val radius = size.minDimension / 3 * (0.5f + 0.5f * sin(angle * 3))
                    val x = center.x + (radius * cos(angle)).toFloat()
                    val y = center.y + (radius * sin(angle)).toFloat()
                    if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                }
                path.close()
                drawPath(
                    path = path,
                    color = GildedGold.copy(alpha = 0.2f),
                    style = Stroke(width = 1.dp.toPx())
                )
            }
            
            // Transcript text
            Column(
                modifier = Modifier
                    .padding(32.dp)
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                if (transcript.isEmpty()) {
                    Text(
                        text = "The crystal awaits\nyour voice...",
                        fontSize = 16.sp,
                        fontStyle = FontStyle.Italic,
                        color = CandlelightGlow.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center,
                        lineHeight = 24.sp
                    )
                } else {
                    Text(
                        text = transcript,
                        fontSize = 14.sp,
                        color = AncientParchment,
                        textAlign = TextAlign.Center,
                        lineHeight = 22.sp
                    )
                }
            }
            
            // Crystal highlight
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .align(Alignment.TopStart)
                    .offset(x = 30.dp, y = 30.dp)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.3f),
                                Color.Transparent
                            )
                        ),
                        shape = CircleShape
                    )
            )
        }
        
        // Crystal stand
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .offset(y = 20.dp)
                .size(100.dp, 30.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            WarmLeather,
                            RichMahogany,
                            DeepLibraryBrown
                        )
                    ),
                    shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)
                )
                .border(
                    width = 1.dp,
                    color = GildedGold.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)
                )
        )
    }
}

// ═══════════════════════════════════════════════════════════════════
// MYSTICAL RECORDING ORB
// ═══════════════════════════════════════════════════════════════════

@Composable
private fun MysticalRecordingOrb(
    isRecording: Boolean,
    onToggle: () -> Unit
) {
    val pulseAnimation = rememberInfiniteTransition(label = "pulse")
    val pulseScale by pulseAnimation.animateFloat(
        initialValue = 1f,
        targetValue = if (isRecording) 1.2f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )
    
    val rotateAnimation by pulseAnimation.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (isRecording) 2000 else 10000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotate"
    )
    
    val glowColor = if (isRecording) DragonsBlood else EnchantedGreen
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            contentAlignment = Alignment.Center
        ) {
            // Outer rotating runes
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .rotate(rotateAnimation)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val runeCount = 8
                    for (i in 0 until runeCount) {
                        val angle = (360f / runeCount) * i
                        val radian = Math.toRadians(angle.toDouble())
                        val x = center.x + (size.minDimension / 2 - 10) * cos(radian).toFloat()
                        val y = center.y + (size.minDimension / 2 - 10) * sin(radian).toFloat()
                        drawCircle(
                            color = glowColor.copy(alpha = 0.6f),
                            radius = 4.dp.toPx(),
                            center = Offset(x, y)
                        )
                    }
                }
            }
            
            // Glow effect
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .scale(pulseScale)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                glowColor.copy(alpha = 0.4f),
                                Color.Transparent
                            )
                        ),
                        shape = CircleShape
                    )
            )
            
            // Main orb button
            Button(
                onClick = onToggle,
                modifier = Modifier.size(80.dp),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isRecording) DragonsBlood else EnchantedGreen
                )
            ) {
                Icon(
                    imageVector = if (isRecording) Icons.Default.Close else Icons.Default.Add,
                    contentDescription = if (isRecording) "Stop Recording" else "Start Recording",
                    tint = GildedGold,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Text(
            text = if (isRecording) "✦ Listening... ✦" else "Tap to Speak",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = if (isRecording) DragonsBlood else GildedGold
        )
    }
}

// ═══════════════════════════════════════════════════════════════════
// MAGICAL ERROR BANNER
// ═══════════════════════════════════════════════════════════════════

@Composable
private fun MagicalErrorBanner(
    error: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                Brush.horizontalGradient(
                    colors = listOf(
                        DragonsBlood.copy(alpha = 0.9f),
                        RichMahogany.copy(alpha = 0.9f)
                    )
                ),
                shape = RoundedCornerShape(12.dp)
            )
            .border(
                width = 1.dp,
                color = GildedGold.copy(alpha = 0.5f),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "⚠️", fontSize = 24.sp)
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = error,
                color = AncientParchment,
                fontSize = 14.sp
            )
        }
    }
}

private fun playAudio(context: Context, audioBytes: ByteArray) {
    val sampleRate = 24000
    val channelConfig = AudioFormat.CHANNEL_OUT_MONO
    val audioFormat = AudioFormat.ENCODING_PCM_16BIT

    try {
        if (currentAudioTrack?.state != AudioTrack.STATE_INITIALIZED) {
            val minBufferSize = AudioTrack.getMinBufferSize(sampleRate, channelConfig, audioFormat)
            if (minBufferSize == AudioTrack.ERROR || minBufferSize == AudioTrack.ERROR_BAD_VALUE) {
                Log.e("SpeechScreen", "Invalid buffer size")
                return
            }

            currentAudioTrack?.release()
            currentAudioTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .build(),
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setSampleRate(sampleRate)
                        .setEncoding(audioFormat)
                        .setChannelMask(channelConfig)
                        .build(),
                )
                .setBufferSizeInBytes(minBufferSize * 8)
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build()

            currentAudioTrack?.play()
        }

        val shortBuffer = ShortArray(audioBytes.size / 2)
        ByteBuffer.wrap(audioBytes)
            .order(ByteOrder.LITTLE_ENDIAN)
            .asShortBuffer()
            .get(shortBuffer)

        currentAudioTrack?.write(shortBuffer, 0, shortBuffer.size, AudioTrack.WRITE_BLOCKING)
    } catch (e: Exception) {
        Log.e("SpeechScreen", "Error playing audio", e)
        currentAudioTrack?.release()
        currentAudioTrack = null
    }
}
