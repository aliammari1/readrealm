package tn.esprit.libraryapp.models

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.label.ImageLabel
import com.google.mlkit.vision.text.Text
import tn.esprit.libraryapp.components.WheelPickerType
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

data class LibrarianState(
    val isVisible: Boolean = false,
    val message: String = "",
    val isTyping: Boolean = false,
    val suggestions: List<String> = emptyList(),
    val isInteractive: Boolean = false,
    val currentMode: LibrarianMode = LibrarianMode.IDLE,
    val context: LibrarianContext = LibrarianContext(),
    val recognizedText: Text? = null,
    val detectedFaces: List<Face> = emptyList(),
    val imageLabels: List<ImageLabel?> = emptyList(), // Changed type to List<String>
    val translatedText: String? = null,
    val smartReplies: List<String> = emptyList(),
    val detectedBarcode: String? = null
)

data class LibrarianContext(
    val lastGenre: String? = null,
    val lastQuery: String? = null,
    val lastBookId: Int? = null, // Add this field
    val conversationHistory: List<String> = emptyList(),
    val userPreferences: Map<String, Int> = emptyMap() // Genre preferences
)

enum class LibrarianMode {
    IDLE,
    RECOMMENDING,
    HELPING,
    SEARCHING,
    CHATTING,
    SCANNING,
    VOICE_SEARCH,
    IMAGE_ANALYSIS,
    SMART_RECOMMEND,
    TEXT_RECOGNITION,
    BARCODE_SCANNING,
    FACE_DETECTION,
    SMART_REPLY,
    TRANSLATION
}

sealed class LibrarianAction {
    object Welcome : LibrarianAction()
    object Dismiss : LibrarianAction()
    data class Recommend(val genre: String? = null) : LibrarianAction()
    data class Help(val topic: String) : LibrarianAction()
    data class Search(val query: String) : LibrarianAction()
    data class Chat(val message: String) : LibrarianAction()
    data class ViewDetails(val bookId: Int) : LibrarianAction()
    data class ReadBook(val bookId: Int) : LibrarianAction()
    data class ScanText(val image: InputImage) : LibrarianAction()
    data class VoiceSearch(val query: String) : LibrarianAction()
    data class ImageLabel(val image: InputImage) : LibrarianAction()
    data class SmartRecommend(val preferences: Map<String, Float>) : LibrarianAction()
    data class RecognizeText(val image: InputImage) : LibrarianAction()
    data class ScanBarcode(val image: InputImage) : LibrarianAction()
    data class DetectFaces(val image: InputImage) : LibrarianAction()
    data class TranslateText(val text: String, val targetLang: String) : LibrarianAction()
    data class GetSmartReply(val conversation: List<String>) : LibrarianAction()
    data class ProcessBookCover(val image: InputImage?) : LibrarianAction()
    data class Navigation(val route: String, val params: Map<String, String> = emptyMap()) :
        LibrarianAction()
}


data class LibrarianAnimations(
    val floatAnim: Float,
    val glowAnim: Float,
    val lightningAnim: Float,
    val energyAnim: Float,
    val particleAnim: Float,
    val pulseAnim: Float,
    val verticalOffset: Float,
    val horizontalOffset: Float
) {
    companion object {
        @Composable
        operator fun invoke(): LibrarianAnimations {
            val transition = rememberInfiniteTransition(label = "")

            val floatAnim by transition.animateFloat(
                initialValue = 0f,
                targetValue = 2f * PI.toFloat(),
                animationSpec = infiniteRepeatable(
                    animation = tween(3000, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                ),
                label = "float"
            )

            val glowAnim by transition.animateFloat(
                initialValue = 0.2f,
                targetValue = 0.8f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1500, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "glow"
            )

            val lightningAnim by transition.animateFloat(
                initialValue = 0f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(2000, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "lightning"
            )

            val energyAnim by transition.animateFloat(
                initialValue = 0f,
                targetValue = 360f,
                animationSpec = infiniteRepeatable(
                    animation = tween(8000, easing = LinearEasing)
                ),
                label = "energy"
            )

            val particleAnim by transition.animateFloat(
                initialValue = 0f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(2000, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "particle"
            )

            val pulseAnim by transition.animateFloat(
                initialValue = 0.95f,
                targetValue = 1.05f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1000, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "pulse"
            )

            return LibrarianAnimations(
                floatAnim = floatAnim,
                glowAnim = glowAnim,
                lightningAnim = lightningAnim,
                energyAnim = energyAnim,
                particleAnim = particleAnim,
                pulseAnim = pulseAnim,
                verticalOffset = sin(floatAnim) * 8,
                horizontalOffset = cos(floatAnim) * 4
            )
        }
    }
}

data class SelectorProperties(
    val enabled: Boolean = true,
    val shape: Shape = RoundedCornerShape(8.dp),
    val color: Color = Color.Gray.copy(alpha = 0.2f),
    val border: BorderStroke? = null
)


data class PickerData(
    val type: WheelPickerType,
    val title: String,
    val options: List<String>,
    val current: String? = null
)

enum class WheelType {
    GENRE,
    LANGUAGE,
    SORT,
    ACTION
}

data class QuickActionItem(
    val icon: ImageVector,
    val label: String,
    val wheelType: WheelType? = null,
    val action: LibrarianAction? = null
)

