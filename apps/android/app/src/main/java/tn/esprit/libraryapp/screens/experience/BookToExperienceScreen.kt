package tn.esprit.libraryapp.screens.experience

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import tn.esprit.libraryapp.components.experience.*
import tn.esprit.libraryapp.models.experience.*
import tn.esprit.libraryapp.viewModel.BookExperienceViewModel
import tn.esprit.libraryapp.viewModel.BookExperienceUiState
import tn.esprit.libraryapp.viewModel.ExperienceScreenState

// Theme colors
private val DeepLibraryBrown = Color(0xFF2C1810)
private val GildedGold = Color(0xFFD4AF37)
private val MysticPurple = Color(0xFF4A1942)
private val AncientParchment = Color(0xFFF5E6D3)

/**
 * ═══════════════════════════════════════════════════════════════════
 *                   BOOK TO EXPERIENCE SCREEN
 * ═══════════════════════════════════════════════════════════════════
 * 
 * Main immersive screen that transforms books into explorable worlds.
 * Users can walk through literary settings, meet characters, and
 * experience key scenes from their favorite books.
 * 
 * ═══════════════════════════════════════════════════════════════════
 */
@Composable
fun BookToExperienceScreen(
    bookId: String,
    bookTitle: String,
    bookContent: String,
    onNavigateBack: () -> Unit,
    viewModel: BookExperienceViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val experience by viewModel.experience.collectAsState()
    val session by viewModel.session.collectAsState()
    val currentLocation by viewModel.currentLocation.collectAsState()
    val loadingProgress by viewModel.loadingProgress.collectAsState()
    val loadingTask by viewModel.loadingTask.collectAsState()

    // Initialize experience when screen loads
    LaunchedEffect(bookId) {
        if (experience == null && uiState.screenState != ExperienceScreenState.LOADING) {
            viewModel.initializeExperience(bookId, bookTitle, "", bookContent)
        }
    }

    // UI state variables
    var selectedTab by remember { mutableStateOf(ExperienceTab.WORLD) }
    var showSettings by remember { mutableStateOf(false) }
    var showWorldInfo by remember { mutableStateOf(false) }
    var showExitDialog by remember { mutableStateOf(false) }
    var isQuickActionsExpanded by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(DeepLibraryBrown, MysticPurple, DeepLibraryBrown)
                )
            )
    ) {
        when (uiState.screenState) {
            ExperienceScreenState.LOADING, ExperienceScreenState.IDLE -> {
                ExperienceLoadingScreen(
                    bookTitle = bookTitle,
                    loadingProgress = loadingProgress,
                    loadingMessage = loadingTask
                )
            }

            ExperienceScreenState.ERROR -> {
                ExperienceErrorScreen(
                    error = uiState.errorMessage ?: "Unknown error",
                    onRetry = { viewModel.initializeExperience(bookId, bookTitle, "", bookContent) },
                    onBack = onNavigateBack
                )
            }

            else -> {
                if (experience != null) {
                    val exp = experience!!
                    val world = exp.world

                    // Immersive background
                    ImmersiveBackground(
                        worldType = world.type.name,
                        timeOfDay = world.timeOfDay.name,
                        weatherType = world.weatherPatterns.firstOrNull()?.type?.name ?: "CLEAR",
                        particlesEnabled = true
                    )

                    // Main content
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Header
                        ExperienceHeader(
                            bookTitle = bookTitle,
                            worldName = world.name,
                            progress = uiState.experienceProgress,
                            onBackClick = { showExitDialog = true },
                            onSettingsClick = { showSettings = true },
                            onInfoClick = { showWorldInfo = true }
                        )

                        // Tab content
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                        ) {
                            ExperienceTabContent(
                                tab = selectedTab,
                                experience = exp,
                                session = session,
                                currentLocation = currentLocation,
                                uiState = uiState,
                                viewModel = viewModel
                            )
                        }

                        // Navigation tabs
                        ExperienceNavigationTabs(
                            selectedTab = selectedTab,
                            onTabSelected = { selectedTab = it }
                        )
                    }

                    // Quick actions FAB
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.BottomEnd
                    ) {
                        ExperienceQuickActions(
                            isExpanded = isQuickActionsExpanded,
                            onToggle = { isQuickActionsExpanded = !isQuickActionsExpanded },
                            onMapClick = { selectedTab = ExperienceTab.MAP },
                            onCharactersClick = { selectedTab = ExperienceTab.CHARACTERS },
                            onScenesClick = { selectedTab = ExperienceTab.SCENES },
                            onJournalClick = { selectedTab = ExperienceTab.JOURNAL },
                            modifier = Modifier.padding(bottom = 80.dp)
                        )
                    }

                    // Overlays and dialogs
                    ExperienceOverlays(
                        showSettings = showSettings,
                        showWorldInfo = showWorldInfo,
                        showExitDialog = showExitDialog,
                        uiState = uiState,
                        world = world,
                        onDismissSettings = { showSettings = false },
                        onDismissWorldInfo = { showWorldInfo = false },
                        onDismissExitDialog = { showExitDialog = false },
                        onConfirmExit = onNavigateBack,
                        onToggleAudio = { viewModel.toggleAudio() }
                    )
                }
            }
        }
    }
}

@Composable
private fun ExperienceTabContent(
    tab: ExperienceTab,
    experience: BookExperience,
    session: ExperienceSession,
    currentLocation: WorldLocation?,
    uiState: BookExperienceUiState,
    viewModel: BookExperienceViewModel
) {
    val world = experience.world

    AnimatedContent(
        targetState = tab,
        transitionSpec = {
            fadeIn() + slideInHorizontally() togetherWith 
            fadeOut() + slideOutHorizontally()
        },
        label = "tabContent"
    ) { currentTab ->
        when (currentTab) {
            ExperienceTab.WORLD -> {
                WorldTabContent(
                    world = world,
                    currentLocation = currentLocation,
                    onLocationSelected = { viewModel.navigateToLocation(it) }
                )
            }

            ExperienceTab.SCENES -> {
                ScenesTabContent(
                    scenes = experience.scenes,
                    currentSceneId = session.currentSceneId,
                    completedSceneIds = session.completedInteractions,
                    onSceneClick = { viewModel.playScene(it) }
                )
            }

            ExperienceTab.CHARACTERS -> {
                CharactersTabContent(
                    characters = experience.characters,
                    selectedCharacterId = null, // Not currently selected
                    metCharacterIds = session.metCharacters,
                    onCharacterClick = { viewModel.viewCharacter(it) }
                )
            }

            ExperienceTab.MAP -> {
                WorldMapNavigator(
                    world = world,
                    currentLocation = currentLocation ?: world.locations.first(),
                    visitedLocations = session.visitedLocations,
                    onLocationSelected = { viewModel.navigateToLocation(it) },
                    onClose = { /* Close handled by tab change */ },
                    modifier = Modifier.fillMaxSize()
                )
            }

            ExperienceTab.JOURNAL -> {
                val journalEntries = buildJournalEntries(session, experience)
                ExperienceJournal(
                    entries = journalEntries,
                    onEntryClick = { /* Navigate to related content */ }
                )
            }
        }
    }
}

@Composable
private fun ExperienceOverlays(
    showSettings: Boolean,
    showWorldInfo: Boolean,
    showExitDialog: Boolean,
    uiState: BookExperienceUiState,
    world: LiteraryWorld,
    onDismissSettings: () -> Unit,
    onDismissWorldInfo: () -> Unit,
    onDismissExitDialog: () -> Unit,
    onConfirmExit: () -> Unit,
    onToggleAudio: () -> Unit
) {
    // Settings panel
    AnimatedVisibility(
        visible = showSettings,
        enter = fadeIn() + slideInVertically { -it },
        exit = fadeOut() + slideOutVertically { -it }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.6f)),
            contentAlignment = Alignment.Center
        ) {
            ExperienceSettingsPanel(
                ambientSoundEnabled = uiState.isAudioEnabled,
                onAmbientSoundToggle = { onToggleAudio() },
                particleEffectsEnabled = true,
                onParticleEffectsToggle = { /* TODO */ },
                narratorSpeed = 1f,
                onNarratorSpeedChange = { /* TODO */ },
                autoPlayScenes = false,
                onAutoPlayToggle = { /* TODO */ },
                onClose = onDismissSettings
            )
        }
    }

    // World info panel
    AnimatedVisibility(
        visible = showWorldInfo,
        enter = fadeIn() + scaleIn(),
        exit = fadeOut() + scaleOut()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.6f)),
            contentAlignment = Alignment.Center
        ) {
            WorldInfoPanel(
                worldName = world.name,
                worldType = world.type.name.replace("_", " "),
                era = world.era.name.replace("_", " "),
                magicLevel = world.magicLevel,
                dangerLevel = world.dangerLevel,
                onClose = onDismissWorldInfo
            )
        }
    }

    // Exit dialog
    if (showExitDialog) {
        ExitExperienceDialog(
            onConfirm = onConfirmExit,
            onDismiss = onDismissExitDialog,
            progress = uiState.experienceProgress
        )
    }
}

private fun buildJournalEntries(
    session: ExperienceSession,
    experience: BookExperience
): List<JournalEntry> {
    val entries = mutableListOf<JournalEntry>()
    val now = System.currentTimeMillis()

    // Add visited locations
    session.visitedLocations.forEachIndexed { index, locationId ->
        val location = experience.world.locations.find { it.id == locationId }
        location?.let {
            entries.add(
                JournalEntry(
                    id = "loc_$locationId",
                    type = JournalEntryType.LOCATION_VISITED,
                    title = "Visited ${it.name}",
                    description = it.description.take(100) + if (it.description.length > 100) "..." else "",
                    timestamp = now - (index * 60000L),
                    relatedId = locationId
                )
            )
        }
    }

    // Add met characters
    session.metCharacters.forEachIndexed { index, characterId ->
        val character = experience.characters.find { it.id == characterId }
        character?.let {
            entries.add(
                JournalEntry(
                    id = "char_$characterId",
                    type = JournalEntryType.CHARACTER_MET,
                    title = "Met ${it.name}",
                    description = it.role.name.replace("_", " "),
                    timestamp = now - (index * 120000L),
                    relatedId = characterId
                )
            )
        }
    }

    // Add completed scenes
    session.completedInteractions.filter { it.startsWith("scene_") || experience.scenes.any { s -> s.id == it } }.forEachIndexed { index, sceneId ->
        val scene = experience.scenes.find { it.id == sceneId }
        scene?.let {
            entries.add(
                JournalEntry(
                    id = "scene_$sceneId",
                    type = JournalEntryType.SCENE_WITNESSED,
                    title = "Witnessed: ${it.sceneTitle}",
                    description = "A ${it.mood.name.lowercase().replace("_", " ")} moment",
                    timestamp = now - (index * 180000L),
                    relatedId = sceneId
                )
            )
        }
    }

    return entries.sortedByDescending { it.timestamp }
}
