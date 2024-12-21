package tn.esprit.libraryapp.services

import android.content.Context
import android.media.MediaPlayer
import android.util.Log
import okhttp3.ResponseBody
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class AudioStreamManager(private val context: Context) {
    private var mediaPlayer: MediaPlayer? = null
    private var currentFile: File? = null

    fun playStream(responseBody: ResponseBody, onPlayingStateChanged: (Boolean) -> Unit) {
        try {
            // Stop any existing playback
            stop()

            // Create temporary file
            val tempFile = File.createTempFile("audio", ".mp3", context.cacheDir)
            currentFile = tempFile

            // Write stream to temporary file
            FileOutputStream(tempFile).use { outputStream ->
                responseBody.byteStream().use { inputStream ->
                    inputStream.copyTo(outputStream)
                }
            }

            // Initialize and start MediaPlayer
            mediaPlayer = MediaPlayer().apply {
                setDataSource(tempFile.path)
                setOnCompletionListener {
                    stop()
                    onPlayingStateChanged(false)
                }
                setOnPreparedListener {
                    start()
                    onPlayingStateChanged(true)
                }
                prepareAsync()
            }

        } catch (e: IOException) {
            Log.e("AudioStreamManager", "Error playing audio stream", e)
            stop()
            onPlayingStateChanged(false)
        }
    }

    fun stop() {
        mediaPlayer?.apply {
            if (isPlaying) {
                stop()
            }
            reset()
            release()
        }
        mediaPlayer = null

        // Clean up temporary file
        currentFile?.delete()
        currentFile = null
    }
}
