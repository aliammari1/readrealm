package tn.esprit.libraryapp.services

import com.google.mlkit.nl.smartreply.SmartReply
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.tasks.await

class MLKitService {
    private val textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    private val imageLabeler = ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS)
    private val barcodeScanner = BarcodeScanning.getClient()
    private val faceDetector = FaceDetection.getClient()
    private val smartReplyGenerator = SmartReply.getClient()

    suspend fun recognizeText(image: InputImage?) = image?.let {
        textRecognizer.process(it).await()
    }

    suspend fun labelImage(image: InputImage?) = image?.let {
        imageLabeler.process(it).await()
    } ?: emptyList()

    suspend fun scanBarcode(image: InputImage?) = image?.let {
        barcodeScanner.process(it).await()
    } ?: emptyList()

    suspend fun detectFaces(image: InputImage?) = image?.let {
        faceDetector.process(it).await()
    } ?: emptyList()

    suspend fun getSmartReplies(conversation: List<String>): List<String> {
        val messages = conversation.map {
            com.google.mlkit.nl.smartreply.TextMessage.createForRemoteUser(
                it, System.currentTimeMillis(), "user"
            )
        }
        return smartReplyGenerator.suggestReplies(messages).await()
            .suggestions.map { it.text }
    }

    suspend fun translateText(text: String, targetLang: String): String {
        val options = TranslatorOptions.Builder()
            .setTargetLanguage(targetLang)
            .setSourceLanguage("auto")
            .build()
        val translator = Translation.getClient(options)
        return try {
            translator.downloadModelIfNeeded().await()
            translator.translate(text).await()
        } catch (e: Exception) {
            text
        } finally {
            translator.close()
        }
    }

    fun close() {
        textRecognizer.close()
        imageLabeler.close()
        barcodeScanner.close()
        faceDetector.close()
        smartReplyGenerator.close()
    }
}
