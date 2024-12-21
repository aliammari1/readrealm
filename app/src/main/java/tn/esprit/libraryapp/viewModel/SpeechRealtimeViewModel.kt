package tn.esprit.libraryapp.viewModel

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Base64
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.socket.client.IO
import io.socket.client.Socket
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.net.URISyntaxException
import java.nio.ByteBuffer
import java.nio.ByteOrder

@SuppressLint("MissingPermission")
class SpeechViewModel : ViewModel() {

    val isRecording = mutableStateOf(false)
    val transcript = mutableStateOf("")
    val latestInputSpeechBlock = mutableStateOf("")

    private var mSocket: Socket? = null
    private val coroutineScope = viewModelScope

    private val sampleRate = 24000
    private val bufferSize = AudioRecord.getMinBufferSize(
        sampleRate,
        AudioFormat.CHANNEL_IN_MONO,
        AudioFormat.ENCODING_PCM_16BIT
    ) * 2 // Double the minimum buffer size

    private val audioRecord: AudioRecord by lazy {
        AudioRecord(
            MediaRecorder.AudioSource.VOICE_RECOGNITION, // Changed to VOICE_RECOGNITION
            sampleRate,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            bufferSize
        )
    }

    // SharedFlow to emit received audio data as ByteArray
    private val _receivedAudio = MutableSharedFlow<ByteArray>()
    val receivedAudio = _receivedAudio.asSharedFlow()

    // SharedFlow to emit error messages
    private val _errorMessages = MutableSharedFlow<String>()
    val errorMessages = _errorMessages.asSharedFlow()

    init {
        // Initialize Socket.IO connection
        initSocket()
    }

    private fun initSocket() {
        try {
            val options = IO.Options().apply {
                transports = arrayOf("websocket")
                reconnection = true
                forceNew = true
            }
            mSocket = IO.socket("http://192.168.98.105:3000", options)
            mSocket?.apply {
                connect()

                on(Socket.EVENT_CONNECT) {
                    Log.d("SpeechViewModel", "Socket connected")
                }

                on("sessionStatus") { args ->
                    if (args.isNotEmpty()) {
                        val status = args[0] as JSONObject
                        Log.d("SpeechViewModel", "Session status: $status")
                    }
                }

                on("error") { args ->
                    if (args.isNotEmpty()) {
                        val errorMsg = args[0] as String
                        Log.e("SpeechViewModel", "Socket error: $errorMsg")
                        coroutineScope.launch {
                            _errorMessages.emit(errorMsg)
                        }
                    }
                }

                on("ack") { args ->
                    if (args.isNotEmpty()) {
                        Log.d("SpeechViewModel", "Acknowledgment from server: ${args[0]}")
                    }
                }

                on("transcript") { args ->
                    if (args.isNotEmpty()) {
                        val transcriptUpdate = args[0] as String
                        Log.d("SpeechViewModel", "Received transcript: $transcriptUpdate")
                        transcript.value += "$transcriptUpdate"  // Remove newline to keep continuous text
                    }
                }

                on("transcription") { args ->
                    if (args.isNotEmpty()) {
                        val transcriptionText = args[0] as String
                        Log.d("SpeechViewModel", "Received transcription: $transcriptionText")
                        transcript.value += "\n$transcriptionText\n"  // Add newlines for completed utterances
                    }
                }

                on("audio") { args ->
                    if (args.isNotEmpty()) {
                        when (val audioData = args[0]) {
                            is String -> {
                                if (audioData == "clear" || audioData == "Session start") {
                                    // Handle control messages
                                    Log.d("SpeechViewModel", "Received control message: $audioData")
                                } else {
                                    try {
                                        // Decode base64 audio data
                                        val audioBytes = Base64.decode(audioData, Base64.DEFAULT)
                                        Log.d(
                                            "SpeechViewModel",
                                            "Received audio data: ${audioBytes.size} bytes"
                                        )
                                        coroutineScope.launch {
                                            _receivedAudio.emit(audioBytes)
                                        }
                                    } catch (e: Exception) {
                                        Log.e("SpeechViewModel", "Error decoding audio data", e)
                                        coroutineScope.launch {
                                            _errorMessages.emit("Error decoding audio: ${e.message}")
                                        }
                                    }
                                }
                            }

                            else -> Log.e(
                                "SpeechViewModel",
                                "Unexpected audio data type: ${audioData?.javaClass}"
                            )
                        }
                    }
                }

                on("speech") { args ->
                    if (args.isNotEmpty()) {
                        val speechMessage = args[0] as String
                        latestInputSpeechBlock.value = speechMessage
                    }
                }

                on("done") {
                    latestInputSpeechBlock.value += " << Session Done >>"
                }

                on(Socket.EVENT_DISCONNECT) {
                    Log.d("SpeechViewModel", "Socket disconnected")
                    coroutineScope.launch {
                        isRecording.value = false
                        _errorMessages.emit("Connection lost")
                    }
                }

                on(Socket.EVENT_CONNECT_ERROR) { args ->
                    emit("stop")
                }
            }
        } catch (e: URISyntaxException) {
            Log.e("SpeechViewModel", "Socket URI error: ${e.message}")
            coroutineScope.launch {
                _errorMessages.emit("Socket URI error: ${e.message}")
            }
        }
    }

    fun startRecording(systemMessage: String, temperature: Float) {
        if (isRecording.value) return

        // Set isRecording to true immediately
        isRecording.value = true

        // Clear transcript when starting new session
        transcript.value = ""

        mSocket?.emit("start", JSONObject().apply {
            put("systemMessage", systemMessage)
            put("temperature", temperature)
        })

        // Remove isRecording assignment from here
        mSocket?.once("sessionStatus") { args ->
            if (args.isNotEmpty() && (args[0] as JSONObject).optBoolean("active", false)) {
                coroutineScope.launch(Dispatchers.IO) {
                    try {
                        audioRecord.startRecording()
                        val buffer = ShortArray(bufferSize / 2)

                        while (isRecording.value) {
                            val read = audioRecord.read(buffer, 0, buffer.size)
                            if (read > 0) {
                                val byteBuffer = ByteBuffer.allocate(read * 2)
                                    .order(ByteOrder.LITTLE_ENDIAN)
                                for (i in 0 until read) {
                                    byteBuffer.putShort(buffer[i])
                                }

                                val audioData = byteBuffer.array()
                                val base64Audio = Base64.encodeToString(audioData, Base64.NO_WRAP)

                                mSocket?.emit("sendAudio", JSONObject().apply {
                                    put("audio", base64Audio)
                                })
                            }
                            kotlinx.coroutines.delay(10)
                        }
                    } catch (e: Exception) {
                        Log.e("SpeechViewModel", "Recording error", e)
                    } finally {
                        audioRecord.stop()
                    }
                }
            } else {
                // Handle inactive session
                isRecording.value = false
            }
        }
    }

    fun stopRecording() {
        if (!isRecording.value) return

        isRecording.value = false
        coroutineScope.launch(Dispatchers.IO) {
            audioRecord.stop()
            mSocket?.emit("stop")
            mSocket?.disconnect()
            mSocket?.off()
        }
    }

    override fun onCleared() {
        super.onCleared()
        mSocket?.off()
        coroutineScope.cancel()
        audioRecord.release()
        mSocket?.disconnect()
        mSocket?.off()
    }
}