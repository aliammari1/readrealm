package tn.esprit.libraryapp.viewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import tn.esprit.libraryapp.models.experience.*
import tn.esprit.libraryapp.utils.ChapterContent
import tn.esprit.libraryapp.utils.ExperienceGenerator
import java.util.UUID

/**
 * ═══════════════════════════════════════════════════════════════════
 * BOOK EXPERIENCE VIEW MODEL
 * Manages the state and logic for the immersive book experience
 * ═══════════════════════════════════════════════════════════════════
 */

class BookExperienceViewModel : ViewModel() {
    
    companion object {
        private const val TAG = "BookExperienceVM"
    }
    
    private val experienceGenerator = ExperienceGenerator()
    
    // UI State
    private val _uiState = MutableStateFlow(BookExperienceUiState())
    val uiState: StateFlow<BookExperienceUiState> = _uiState.asStateFlow()
    
    // Experience data
    private val _experience = MutableStateFlow<BookExperience?>(null)
    val experience: StateFlow<BookExperience?> = _experience.asStateFlow()
    
    // Current session
    private val _session = MutableStateFlow(createNewSession())
    val session: StateFlow<ExperienceSession> = _session.asStateFlow()
    
    // Current location
    private val _currentLocation = MutableStateFlow<WorldLocation?>(null)
    val currentLocation: StateFlow<WorldLocation?> = _currentLocation.asStateFlow()
    
    // Currently playing scene
    private val _currentScene = MutableStateFlow<ExperienceScene?>(null)
    val currentScene: StateFlow<ExperienceScene?> = _currentScene.asStateFlow()
    
    // Selected character for viewing
    private val _selectedCharacter = MutableStateFlow<ExperienceCharacter?>(null)
    val selectedCharacter: StateFlow<ExperienceCharacter?> = _selectedCharacter.asStateFlow()
    
    // Loading progress
    private val _loadingProgress = MutableStateFlow(0f)
    val loadingProgress: StateFlow<Float> = _loadingProgress.asStateFlow()
    
    private val _loadingTask = MutableStateFlow("")
    val loadingTask: StateFlow<String> = _loadingTask.asStateFlow()
    
    /**
     * Initialize and generate experience for a book
     */
    fun initializeExperience(
        bookId: String,
        bookTitle: String,
        bookAuthor: String,
        bookContent: String
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(screenState = ExperienceScreenState.LOADING) }
            
            try {
                // Simulate progressive loading with tasks
                val chapters = parseChapters(bookContent)
                
                updateLoadingProgress(0.1f, "Analyzing book structure...")
                delay(500)
                
                updateLoadingProgress(0.3f, "Detecting world type...")
                delay(500)
                
                updateLoadingProgress(0.5f, "Generating locations...")
                delay(500)
                
                updateLoadingProgress(0.7f, "Creating characters...")
                delay(500)
                
                updateLoadingProgress(0.85f, "Building scenes...")
                delay(500)
                
                updateLoadingProgress(0.95f, "Finalizing experience...")
                
                // Generate the actual experience
                val generatedExperience = experienceGenerator.generateExperience(
                    bookId = bookId,
                    bookTitle = bookTitle,
                    bookAuthor = bookAuthor,
                    chapters = chapters
                )
                
                _experience.value = generatedExperience
                
                // Set initial location
                generatedExperience.world.locations.firstOrNull()?.let { firstLocation ->
                    _currentLocation.value = firstLocation
                    _session.update { it.copy(
                        currentLocationId = firstLocation.id,
                        visitedLocations = it.visitedLocations + firstLocation.id
                    )}
                }
                
                updateLoadingProgress(1f, "Experience ready!")
                delay(500)
                
                _uiState.update { it.copy(screenState = ExperienceScreenState.EXPLORATION) }
                
            } catch (e: Exception) {
                Log.e(TAG, "Failed to generate experience", e)
                _uiState.update { it.copy(
                    screenState = ExperienceScreenState.ERROR,
                    errorMessage = e.message ?: "Failed to generate experience"
                )}
            }
        }
    }
    
    /**
     * Parse book content into chapters
     */
    private fun parseChapters(content: String): List<ChapterContent> {
        // Simple chapter detection - look for "Chapter" markers
        val chapterPattern = Regex("(?i)chapter\\s*(\\d+|[IVXLCDM]+)[:\\s]*([^\\n]*)")
        val matches = chapterPattern.findAll(content).toList()
        
        if (matches.isEmpty()) {
            // No chapters found, treat whole content as one chapter
            return listOf(ChapterContent(
                chapterNumber = 1,
                title = "The Story",
                content = content
            ))
        }
        
        return matches.mapIndexed { index, match ->
            val chapterNumber = match.groupValues[1].toIntOrNull() 
                ?: (index + 1)
            val title = match.groupValues[2].trim().ifEmpty { "Chapter $chapterNumber" }
            
            val startIndex = match.range.last + 1
            val endIndex = matches.getOrNull(index + 1)?.range?.first ?: content.length
            val chapterContent = content.substring(startIndex, endIndex).trim()
            
            ChapterContent(
                chapterNumber = chapterNumber,
                title = title,
                content = chapterContent
            )
        }
    }
    
    private fun updateLoadingProgress(progress: Float, task: String) {
        _loadingProgress.value = progress
        _loadingTask.value = task
    }
    
    /**
     * Navigate to a different location
     */
    fun navigateToLocation(location: WorldLocation) {
        viewModelScope.launch {
            _uiState.update { it.copy(isTransitioning = true) }
            
            val previousLocationId = _currentLocation.value?.id ?: ""
            
            // Simulate travel time
            delay(2000)
            
            _currentLocation.value = location
            _session.update { session ->
                val movementRecord = if (previousLocationId.isNotEmpty()) {
                    listOf(MovementRecord(
                        fromLocationId = previousLocationId,
                        toLocationId = location.id,
                        timestamp = System.currentTimeMillis(),
                        teleported = false
                    ))
                } else {
                    emptyList()
                }
                
                session.copy(
                    currentLocationId = location.id,
                    visitedLocations = session.visitedLocations + location.id,
                    movementHistory = session.movementHistory + movementRecord
                )
            }
            
            _uiState.update { it.copy(isTransitioning = false) }
        }
    }
    
    /**
     * Interact with a Point of Interest
     */
    fun interactWithPoi(poi: PointOfInterest) {
        viewModelScope.launch {
            // Mark as discovered
            if (!poi.isDiscovered) {
                _session.update { session ->
                    session.copy(
                        discoveredPois = session.discoveredPois + poi.id,
                        completedInteractions = session.completedInteractions + poi.id
                    )
                }
                
                // Update the POI in the experience
                _experience.update { exp ->
                    exp?.let { experience ->
                        val updatedLocations = experience.world.locations.map { location ->
                            val updatedPois = location.pointsOfInterest.map { p ->
                                if (p.id == poi.id) p.copy(isDiscovered = true) else p
                            }
                            location.copy(pointsOfInterest = updatedPois)
                        }
                        experience.copy(
                            world = experience.world.copy(locations = updatedLocations)
                        )
                    }
                }
            }
            
            // Show POI detail
            _uiState.update { it.copy(
                selectedPoi = poi,
                showPoiDetail = true
            )}
        }
    }
    
    /**
     * Start playing a scene
     */
    fun playScene(scene: ExperienceScene) {
        _currentScene.value = scene
        _uiState.update { it.copy(screenState = ExperienceScreenState.SCENE_PLAYING) }
    }
    
    /**
     * Complete current scene
     */
    fun completeScene() {
        _currentScene.value?.let { scene ->
            _session.update { session ->
                session.copy(
                    completedInteractions = session.completedInteractions + scene.id,
                    currentSceneId = null
                )
            }
        }
        
        _currentScene.value = null
        _uiState.update { it.copy(screenState = ExperienceScreenState.EXPLORATION) }
        
        // Update progress
        updateProgress()
    }
    
    /**
     * View character details
     */
    fun viewCharacter(character: ExperienceCharacter) {
        _selectedCharacter.value = character
        _uiState.update { it.copy(screenState = ExperienceScreenState.CHARACTER_VIEW) }
    }
    
    /**
     * Close character view
     */
    fun closeCharacterView() {
        _selectedCharacter.value = null
        _uiState.update { it.copy(screenState = ExperienceScreenState.EXPLORATION) }
    }
    
    /**
     * Toggle map view
     */
    fun toggleMap() {
        _uiState.update { it.copy(
            showMap = !it.showMap
        )}
    }
    
    /**
     * Close map view
     */
    fun closeMap() {
        _uiState.update { it.copy(showMap = false) }
    }
    
    /**
     * Toggle journal view
     */
    fun toggleJournal() {
        _uiState.update { it.copy(
            showJournal = !it.showJournal
        )}
    }
    
    /**
     * Close journal view
     */
    fun closeJournal() {
        _uiState.update { it.copy(showJournal = false) }
    }
    
    /**
     * Toggle characters list
     */
    fun toggleCharactersList() {
        _uiState.update { it.copy(
            showCharactersList = !it.showCharactersList
        )}
    }
    
    /**
     * Close characters list
     */
    fun closeCharactersList() {
        _uiState.update { it.copy(showCharactersList = false) }
    }
    
    /**
     * Toggle audio
     */
    fun toggleAudio() {
        _uiState.update { it.copy(
            isAudioEnabled = !it.isAudioEnabled
        )}
    }
    
    /**
     * Toggle settings
     */
    fun toggleSettings() {
        _uiState.update { it.copy(
            showSettings = !it.showSettings
        )}
    }
    
    /**
     * Close POI detail
     */
    fun closePoiDetail() {
        _uiState.update { it.copy(
            showPoiDetail = false,
            selectedPoi = null
        )}
    }
    
    /**
     * Update overall progress
     */
    private fun updateProgress() {
        _experience.value?.let { exp ->
            val totalLocations = exp.world.locations.size
            val visitedLocations = _session.value.visitedLocations.size
            
            val totalPois = exp.world.locations.sumOf { it.pointsOfInterest.size }
            val discoveredPois = _session.value.discoveredPois.size
            
            val totalScenes = exp.scenes.size
            val completedInteractions = _session.value.completedInteractions.size
            
            val locationProgress = if (totalLocations > 0) visitedLocations.toFloat() / totalLocations else 0f
            val poiProgress = if (totalPois > 0) discoveredPois.toFloat() / totalPois else 0f
            val sceneProgress = if (totalScenes > 0) completedInteractions.toFloat() / totalScenes else 0f
            
            val overallProgress = (locationProgress * 0.3f + poiProgress * 0.3f + sceneProgress * 0.4f)
            
            // Store progress in UI state since ExperienceSession doesn't have a progress field
            _uiState.update { it.copy(experienceProgress = overallProgress) }
        }
    }
    
    /**
     * Update session time
     */
    fun updateSessionTime(deltaTime: Long) {
        _session.update { it.copy(
            totalTimeSpent = it.totalTimeSpent + deltaTime
        )}
    }
    
    /**
     * Get available scenes for current location
     */
    fun getAvailableScenes(): List<ExperienceScene> {
        return _experience.value?.scenes?.filter { scene ->
            scene.location.id == _currentLocation.value?.id
        } ?: emptyList()
    }
    
    /**
     * Create a new session
     */
    private fun createNewSession(): ExperienceSession {
        return ExperienceSession(
            id = UUID.randomUUID().toString(),
            experienceId = "",
            userId = "default_user",
            currentLocationId = "",
            currentSceneId = null,
            visitedLocations = emptySet(),
            discoveredPois = emptySet(),
            metCharacters = emptySet(),
            completedInteractions = emptySet(),
            collectedRewards = emptyList(),
            totalTimeSpent = 0L,
            sessionStartTime = System.currentTimeMillis(),
            movementHistory = emptyList()
        )
    }
    
    /**
     * Reset experience
     */
    fun resetExperience() {
        _experience.value = null
        _session.value = createNewSession()
        _currentLocation.value = null
        _currentScene.value = null
        _selectedCharacter.value = null
        _uiState.value = BookExperienceUiState()
    }
}

/**
 * UI State for the experience screen
 */
data class BookExperienceUiState(
    val screenState: ExperienceScreenState = ExperienceScreenState.IDLE,
    val isTransitioning: Boolean = false,
    val showMap: Boolean = false,
    val showJournal: Boolean = false,
    val showCharactersList: Boolean = false,
    val showSettings: Boolean = false,
    val showPoiDetail: Boolean = false,
    val selectedPoi: PointOfInterest? = null,
    val isAudioEnabled: Boolean = true,
    val errorMessage: String? = null,
    val experienceProgress: Float = 0f
)

/**
 * Screen states for the experience
 */
enum class ExperienceScreenState {
    IDLE,
    LOADING,
    EXPLORATION,
    SCENE_PLAYING,
    CHARACTER_VIEW,
    ERROR
}

/**
 * Extension to update flow values
 */
private fun <T> MutableStateFlow<T>.update(transform: (T) -> T) {
    value = transform(value)
}
