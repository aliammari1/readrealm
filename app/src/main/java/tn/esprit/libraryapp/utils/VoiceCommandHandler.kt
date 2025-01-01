package tn.esprit.libraryapp.utils

import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class VoiceCommandHandler(private val activity: Activity) {
    private val speechRecognizer = SpeechRecognizer.createSpeechRecognizer(activity)
    private val _isListening = MutableStateFlow(false)
    val isListening = _isListening.asStateFlow()

    private val commands =
        mapOf(
            listOf("start", "play", "begin", "read") to VoiceCommand.START,
            listOf("stop", "pause", "halt") to VoiceCommand.STOP,
            listOf("next", "forward", "continue") to VoiceCommand.NEXT,
            listOf("previous", "back", "return") to VoiceCommand.PREVIOUS
        )

    fun startListening(onCommand: (VoiceCommand) -> Unit) {
        val intent =
            Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(
                    RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                )
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
                // Add continuous listening
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            }

        speechRecognizer.setRecognitionListener(
            object : VoiceRecognitionListener() {
                override fun onResults(results: List<String>) {
                    val command = results.firstOrNull()?.lowercase()?.trim() ?: return
                    commands.forEach { (triggers, voiceCommand) ->
                        if (triggers.any { command.contains(it) }) {
                            onCommand(voiceCommand)
                            // Restart listening for next command
                            speechRecognizer.startListening(intent)
                            return
                        }
                    }
                    // If no command matched, restart listening
                    speechRecognizer.startListening(intent)
                }

                override fun onStartListening() {
                    _isListening.value = true
                }

                override fun onStopListening() {
                    // Don't set isListening to false here, as we want to continue
                    speechRecognizer.startListening(intent)
                }

                override fun onError(error: Int) {
                    // Restart listening on error unless it's a fatal error
                    if (error !in
                        listOf(
                            SpeechRecognizer.ERROR_CLIENT,
                            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS
                        )
                    ) {
                        speechRecognizer.startListening(intent)
                    } else {
                        _isListening.value = false
                    }
                }
            }
        )

        speechRecognizer.startListening(intent)
    }

    fun stopListening() {
        speechRecognizer.stopListening()
        _isListening.value = false
    }

    fun destroy() {
        speechRecognizer.destroy()
    }
}

enum class VoiceCommand {
    START,
    STOP,
    NEXT,
    PREVIOUS
}

abstract class VoiceRecognitionListener : android.speech.RecognitionListener {
    abstract fun onResults(results: List<String>)
    abstract fun onStartListening()
    abstract fun onStopListening()

    override fun onResults(results: android.os.Bundle?) {
        results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.let {
            onResults(it)
        }
    }

    override fun onReadyForSpeech(params: android.os.Bundle?) = onStartListening()
    override fun onBeginningOfSpeech() {}
    override fun onRmsChanged(rmsdB: Float) {}
    override fun onBufferReceived(buffer: ByteArray?) {}
    override fun onEndOfSpeech() = onStopListening()
    override fun onError(error: Int) = onStopListening()
    override fun onPartialResults(partialResults: android.os.Bundle?) {}
    override fun onEvent(eventType: Int, params: android.os.Bundle?) {}
}
