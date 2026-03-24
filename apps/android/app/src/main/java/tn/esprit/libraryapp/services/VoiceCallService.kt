package tn.esprit.libraryapp.services

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.AudioTrack
import android.media.MediaRecorder
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class VoiceCallService(private val chatSocketService: ChatSocketService) {
    private var audioRecord: AudioRecord? = null
    private var audioTrack: AudioTrack? = null
    private var isRecording = false
    private val scope = CoroutineScope(Dispatchers.IO)

    private val _callState = MutableStateFlow<CallState>(CallState.Idle)
    val callState: StateFlow<CallState> = _callState

    private val bufferSize = AudioRecord.getMinBufferSize(
        SAMPLE_RATE,
        AudioFormat.CHANNEL_IN_MONO,
        AudioFormat.ENCODING_PCM_16BIT,
    )

    private var currentRoomId: String? = null
    private var currentUserId: String? = null

    init {
        observeVoiceData()
    }

    private fun observeVoiceData() {
        scope.launch {
            chatSocketService.voiceData.collect { audioData ->
                handleIncomingVoiceData(audioData)
            }
        }
    }

    fun startCall(bookId: String, userId: String, username: String) {
        Log.d("VoiceCall", "Starting call for book $bookId as user $username")
        currentUserId = userId
        chatSocketService.startVoiceCall(bookId, userId, username)
        _callState.value = CallState.Calling
    }

    fun joinCall(roomId: String, userId: String, username: String) {
        Log.d("VoiceCall", "Joining call in room $roomId as user $username")
        try {
            currentRoomId = roomId
            currentUserId = userId
            initializeAudio()
            chatSocketService.joinVoiceCall(roomId, userId, username)
            startStreaming(roomId, userId)
            _callState.value = CallState.Connected
        } catch (e: Exception) {
            Log.e("VoiceCall", "Error joining call", e)
            endCall()
        }
    }

    private fun initializeAudio() {
        try {
            val minBuffer = AudioRecord.getMinBufferSize(
                SAMPLE_RATE,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
            )

            // Use a larger buffer for better streaming
            val bufferSize = minBuffer * 2

            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.VOICE_COMMUNICATION, // Changed from MIC
                SAMPLE_RATE,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                bufferSize,
            )

            audioTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .build(),
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(SAMPLE_RATE)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build(),
                )
                .setBufferSizeInBytes(bufferSize)
                .setTransferMode(AudioTrack.MODE_STREAM)
                .setPerformanceMode(AudioTrack.PERFORMANCE_MODE_LOW_LATENCY) // Add this
                .build()

            audioTrack?.play()
            audioRecord?.startRecording()
            isRecording = true
            Log.d("VoiceCall", "Audio initialized with buffer size: $bufferSize")
        } catch (e: Exception) {
            Log.e("VoiceCall", "Error initializing audio", e)
            throw e
        }
    }

    private fun startStreaming(roomId: String, userId: String) {
        scope.launch {
            try {
                val buffer = ByteArray(bufferSize)
                while (isRecording) {
                    val readSize = audioRecord?.read(buffer, 0, bufferSize) ?: 0
                    if (readSize > 0 && !isSilent(buffer, readSize)) {
                        chatSocketService.sendVoiceData(roomId, userId, buffer.copyOf(readSize))
                    }
                }
            } catch (e: Exception) {
                Log.e("VoiceCall", "Error in streaming", e)
                endCall()
            }
        }
    }

    private fun isSilent(buffer: ByteArray, size: Int): Boolean {
        var sum = 0.0
        for (i in 0 until size step 2) {
            val sample = (buffer[i + 1].toInt() shl 8) or (buffer[i].toInt() and 0xFF)
            sum += Math.abs(sample.toDouble())
        }
        val average = sum / (size / 2)
        return average < SILENCE_THRESHOLD
    }

    fun handleIncomingVoiceData(audioChunk: ByteArray) {
        try {
            audioTrack?.write(audioChunk, 0, audioChunk.size)
        } catch (e: Exception) {
            Log.e("VoiceCall", "Error playing audio", e)
        }
    }

    fun endCall() {
        isRecording = false
        audioRecord?.stop()
        audioRecord?.release()
        audioTrack?.stop()
        audioTrack?.release()
        currentRoomId?.let { roomId ->
            chatSocketService.endVoiceCall(roomId)
        }
        currentRoomId = null
        currentUserId = null
        _callState.value = CallState.Idle
    }

    companion object {
        const val SAMPLE_RATE = 16000 // Changed from 44100 for voice
        const val SILENCE_THRESHOLD = 50.0
    }

    sealed class CallState {
        object Idle : CallState()
        object Calling : CallState()
        object Connected : CallState()
    }
}
