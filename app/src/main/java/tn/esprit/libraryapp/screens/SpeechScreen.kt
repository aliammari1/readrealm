// SpeechScreen.kt
package tn.esprit.libraryapp.screens

import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.collectLatest
import tn.esprit.libraryapp.viewModel.SpeechViewModel
import java.nio.ByteBuffer
import java.nio.ByteOrder

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

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Speech Assistant") },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                ),
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Configuration Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(
                        "Configuration",
                        style = MaterialTheme.typography.titleMedium,
                    )
                    OutlinedTextField(
                        value = systemMessage,
                        onValueChange = { systemMessage = it },
                        label = { Text("System Message") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                    )
                    OutlinedTextField(
                        value = temperature,
                        onValueChange = { temperature = it },
                        label = { Text("Temperature") },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }

            // Transcript Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxSize(),
                ) {
                    Text(
                        "Transcript",
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        transcript,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .background(
                                MaterialTheme.colorScheme.surfaceVariant,
                                RoundedCornerShape(8.dp),
                            )
                            .padding(12.dp),
                    )
                }
            }

            // Recording Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
            ) {
                FilledTonalButton(
                    onClick = {
                        if (isRecording) {
                            speechViewModel.stopRecording()
                        } else {
                            speechViewModel.startRecording(
                                systemMessage,
                                temperature.toFloatOrNull() ?: 0.7f,
                            )
                        }
                    },
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = if (isRecording) {
                            MaterialTheme.colorScheme.errorContainer
                        } else {
                            MaterialTheme.colorScheme.primaryContainer
                        },
                    ),
                ) {
                    Icon(
                        if (isRecording) Icons.Default.Close else Icons.Default.Add,
                        contentDescription = if (isRecording) "Stop Recording" else "Start Recording",
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (isRecording) "Stop Recording" else "Start Recording")
                }
            }

            // Error Snackbar
            errorState.value?.let { error ->
                Snackbar(
                    modifier = Modifier.padding(16.dp),
                    action = {
                        TextButton(onClick = { /* Clear error */ }) {
                            Text("Dismiss")
                        }
                    },
                ) {
                    Text(error)
                }
            }
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
                .setBufferSizeInBytes(minBufferSize * 8) // Increased buffer size
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build()

            currentAudioTrack?.play()
        }

        // Convert and write audio data
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
