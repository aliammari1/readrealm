package tn.esprit.libraryapp.models.experience

import androidx.compose.ui.graphics.Color
import java.util.UUID

/**
 * ═══════════════════════════════════════════════════════════════════
 * BOOK TO EXPERIENCE - Core Domain Models
 * Immersive AR/VR Literary World Explorer
 * ═══════════════════════════════════════════════════════════════════
 */

/**
 * Represents a complete immersive experience generated from a book
 */
data class BookExperience(
    val id: String = UUID.randomUUID().toString(),
    val bookId: String,
    val bookTitle: String,
    val bookAuthor: String,
    val coverUrl: String?,
    val world: LiteraryWorld,
    val scenes: List<ExperienceScene>,
    val characters: List<ExperienceCharacter>,
    val ambiance: WorldAmbiance,
    val interactiveElements: List<InteractiveElement>,
    val createdAt: Long = System.currentTimeMillis(),
    val lastVisitedAt: Long? = null,
    val visitCount: Int = 0,
    val completionPercentage: Float = 0f
)

/**
 * Represents the literary world/setting of the book
 */
data class LiteraryWorld(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val era: WorldEra,
    val type: WorldType,
    val description: String,
    val locations: List<WorldLocation>,
    val atmosphere: WorldAtmosphere,
    val weatherPatterns: List<WeatherPattern>,
    val timeOfDay: TimeOfDay,
    val magicLevel: Float = 0f, // 0 = realistic, 1 = high fantasy
    val technologyLevel: Float = 0.5f, // 0 = primitive, 1 = futuristic
    val dangerLevel: Float = 0.3f
)

/**
 * A specific location within the literary world
 */
data class WorldLocation(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val type: LocationType,
    val description: String,
    val coordinates: WorldCoordinates,
    val connectedLocations: List<String>, // IDs of connected locations
    val pointsOfInterest: List<PointOfInterest>,
    val ambientSounds: List<AmbientSound>,
    val lightingCondition: LightingCondition,
    val isUnlocked: Boolean = true,
    val isVisited: Boolean = false,
    val secretsCount: Int = 0,
    val discoveredSecrets: Int = 0
)

/**
 * 3D coordinates in the virtual world
 */
data class WorldCoordinates(
    val x: Float,
    val y: Float,
    val z: Float,
    val rotation: Float = 0f
)

/**
 * A point of interest within a location
 */
data class PointOfInterest(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val type: PoiType,
    val description: String,
    val position: WorldCoordinates,
    val interactionType: InteractionType,
    val relatedChapter: Int?,
    val relatedQuote: String?,
    val isDiscovered: Boolean = false,
    val reward: DiscoveryReward? = null
)

/**
 * A scene from the book that can be experienced
 */
data class ExperienceScene(
    val id: String = UUID.randomUUID().toString(),
    val chapterNumber: Int,
    val chapterTitle: String,
    val sceneTitle: String,
    val description: String,
    val location: WorldLocation,
    val characters: List<String>, // Character IDs present in scene
    val mood: SceneMood,
    val keyEvents: List<KeyEvent>,
    val dialogues: List<SceneDialogue>,
    val duration: SceneDuration,
    val isKeyScene: Boolean = false,
    val isUnlocked: Boolean = true,
    val hasBeenExperienced: Boolean = false,
    val userRating: Float? = null
)

/**
 * A key event within a scene
 */
data class KeyEvent(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String,
    val timestamp: Float, // Position in scene (0-1)
    val importance: EventImportance,
    val visualEffect: VisualEffect?,
    val soundEffect: String?
)

/**
 * Dialogue that can be heard/seen in a scene
 */
data class SceneDialogue(
    val id: String = UUID.randomUUID().toString(),
    val speakerId: String,
    val speakerName: String,
    val text: String,
    val emotion: DialogueEmotion,
    val timestamp: Float,
    val isThought: Boolean = false
)

/**
 * A character that can be encountered in the experience
 */
data class ExperienceCharacter(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val role: CharacterRole,
    val description: String,
    val physicalDescription: PhysicalDescription,
    val personality: List<PersonalityTrait>,
    val relationships: List<CharacterRelationship>,
    val currentLocation: String?, // Location ID
    val quotes: List<CharacterQuote>,
    val isMainCharacter: Boolean = false,
    val hasMetPlayer: Boolean = false,
    val affinity: Float = 0.5f // 0 = hostile, 1 = friendly
)

/**
 * Physical description for character visualization
 */
data class PhysicalDescription(
    val height: HeightCategory,
    val build: BuildCategory,
    val hairColor: String,
    val eyeColor: String,
    val skinTone: String,
    val age: AgeCategory,
    val distinctiveFeatures: List<String>,
    val clothing: String,
    val accessories: List<String>
)

/**
 * A memorable quote from a character
 */
data class CharacterQuote(
    val text: String,
    val context: String,
    val chapter: Int,
    val mood: DialogueEmotion
)

/**
 * Relationship between characters
 */
data class CharacterRelationship(
    val characterId: String,
    val characterName: String,
    val relationshipType: RelationshipType,
    val description: String
)

/**
 * The overall ambiance/atmosphere of the world
 */
data class WorldAmbiance(
    val primaryColor: Long,
    val secondaryColor: Long,
    val accentColor: Long,
    val fogDensity: Float = 0f,
    val particleEffects: List<ParticleEffect>,
    val globalLighting: LightingCondition,
    val musicTheme: MusicTheme,
    val ambientSoundscape: List<AmbientSound>
)

/**
 * Ambient sounds in the environment
 */
data class AmbientSound(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val type: SoundType,
    val volume: Float = 0.5f,
    val isLooping: Boolean = true,
    val spatialPosition: WorldCoordinates? = null
)

/**
 * Interactive elements users can engage with
 */
data class InteractiveElement(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val type: InteractiveType,
    val description: String,
    val position: WorldCoordinates,
    val locationId: String,
    val interactionPrompt: String,
    val reward: DiscoveryReward?,
    val isAvailable: Boolean = true,
    val hasBeenInteracted: Boolean = false
)

/**
 * Rewards for discovering secrets or completing interactions
 */
data class DiscoveryReward(
    val type: RewardType,
    val title: String,
    val description: String,
    val points: Int = 0,
    val unlocksContent: String? = null
)

/**
 * Visual effects that can be displayed
 */
data class VisualEffect(
    val type: EffectType,
    val intensity: Float = 1f,
    val duration: Long = 2000,
    val color: Long? = null
)

/**
 * Particle effects in the environment
 */
data class ParticleEffect(
    val type: ParticleType,
    val density: Float = 0.5f,
    val color: Long,
    val speed: Float = 1f,
    val size: Float = 1f
)

/**
 * Music theme for the world
 */
data class MusicTheme(
    val name: String,
    val genre: MusicGenre,
    val tempo: Tempo,
    val instruments: List<String>,
    val mood: SceneMood
)

/**
 * Weather pattern in the world
 */
data class WeatherPattern(
    val type: WeatherType,
    val intensity: Float = 0.5f,
    val duration: Long = 0, // 0 = permanent
    val probability: Float = 1f
)

// ═══════════════════════════════════════════════════════════════════
// ENUMS
// ═══════════════════════════════════════════════════════════════════

enum class WorldEra {
    ANCIENT,
    MEDIEVAL,
    RENAISSANCE,
    VICTORIAN,
    EARLY_MODERN,
    CONTEMPORARY,
    NEAR_FUTURE,
    FAR_FUTURE,
    TIMELESS,
    ALTERNATE_HISTORY
}

enum class WorldType {
    FANTASY,
    SCIENCE_FICTION,
    HISTORICAL,
    CONTEMPORARY,
    DYSTOPIAN,
    UTOPIAN,
    HORROR,
    MYSTERY,
    ROMANCE,
    ADVENTURE,
    MYTHOLOGICAL
}

enum class LocationType {
    CASTLE,
    MANSION,
    COTTAGE,
    FOREST,
    MOUNTAIN,
    OCEAN,
    CITY,
    VILLAGE,
    DESERT,
    CAVE,
    TEMPLE,
    SCHOOL,
    LIBRARY,
    GARDEN,
    BATTLEFIELD,
    SHIP,
    SPACE_STATION,
    UNDERGROUND,
    ISLAND,
    PALACE,
    DUNGEON,
    TAVERN,
    MARKET,
    BRIDGE,
    TOWER,
    RUINS
}

enum class PoiType {
    ARTIFACT,
    MONUMENT,
    HIDDEN_PASSAGE,
    TREASURE,
    JOURNAL_ENTRY,
    CHARACTER_MEMORY,
    PLOT_CLUE,
    SCENIC_VIEW,
    INTERACTIVE_OBJECT,
    PORTAL,
    SECRET_ROOM
}

enum class InteractionType {
    OBSERVE,
    READ,
    TOUCH,
    COLLECT,
    UNLOCK,
    ACTIVATE,
    CONVERSE,
    SOLVE_PUZZLE
}

enum class SceneMood {
    JOYFUL,
    MELANCHOLIC,
    TENSE,
    ROMANTIC,
    MYSTERIOUS,
    TERRIFYING,
    EPIC,
    PEACEFUL,
    CHAOTIC,
    TRIUMPHANT,
    TRAGIC,
    HOPEFUL,
    OMINOUS
}

enum class SceneDuration {
    BRIEF,      // < 2 minutes
    SHORT,      // 2-5 minutes
    MEDIUM,     // 5-10 minutes
    LONG,       // 10-20 minutes
    EXTENDED    // > 20 minutes
}

enum class EventImportance {
    MINOR,
    MODERATE,
    MAJOR,
    CRITICAL,
    CLIMACTIC
}

enum class DialogueEmotion {
    NEUTRAL,
    HAPPY,
    SAD,
    ANGRY,
    FEARFUL,
    SURPRISED,
    DISGUSTED,
    LOVING,
    SUSPICIOUS,
    DETERMINED,
    DEFEATED,
    SARCASTIC,
    WISE,
    MENACING
}

enum class CharacterRole {
    PROTAGONIST,
    ANTAGONIST,
    DEUTERAGONIST,
    MENTOR,
    SIDEKICK,
    LOVE_INTEREST,
    COMIC_RELIEF,
    GUARDIAN,
    HERALD,
    SHAPESHIFTER,
    SHADOW,
    ALLY,
    TRICKSTER
}

enum class HeightCategory {
    VERY_SHORT,
    SHORT,
    AVERAGE,
    TALL,
    VERY_TALL
}

enum class BuildCategory {
    SLIM,
    AVERAGE,
    ATHLETIC,
    MUSCULAR,
    HEAVY
}

enum class AgeCategory {
    CHILD,
    TEENAGER,
    YOUNG_ADULT,
    ADULT,
    MIDDLE_AGED,
    ELDERLY,
    ANCIENT,
    AGELESS
}

enum class PersonalityTrait {
    BRAVE,
    COWARDLY,
    INTELLIGENT,
    NAIVE,
    KIND,
    CRUEL,
    LOYAL,
    TREACHEROUS,
    HONEST,
    DECEPTIVE,
    AMBITIOUS,
    HUMBLE,
    PASSIONATE,
    STOIC,
    CURIOUS,
    CAUTIOUS,
    OPTIMISTIC,
    PESSIMISTIC,
    CHARISMATIC,
    RESERVED
}

enum class RelationshipType {
    FRIEND,
    ENEMY,
    LOVER,
    FAMILY,
    MENTOR,
    STUDENT,
    RIVAL,
    ALLY,
    ACQUAINTANCE,
    SERVANT,
    MASTER
}

enum class WorldAtmosphere {
    BRIGHT,
    DARK,
    MYSTICAL,
    GLOOMY,
    SERENE,
    CHAOTIC,
    ETHEREAL,
    GRITTY,
    WHIMSICAL,
    OPPRESSIVE,
    LIBERATING
}

enum class TimeOfDay {
    DAWN,
    MORNING,
    NOON,
    AFTERNOON,
    DUSK,
    EVENING,
    NIGHT,
    MIDNIGHT,
    DYNAMIC
}

enum class LightingCondition {
    BRIGHT_DAYLIGHT,
    OVERCAST,
    GOLDEN_HOUR,
    TWILIGHT,
    MOONLIT,
    STARLIGHT,
    CANDLELIT,
    FIRELIGHT,
    MAGICAL_GLOW,
    DARKNESS,
    NEON,
    BIOLUMINESCENT
}

enum class SoundType {
    NATURE,
    WEATHER,
    AMBIENT,
    MUSIC,
    CREATURE,
    MECHANICAL,
    MAGICAL,
    CROWD,
    SILENCE
}

enum class InteractiveType {
    BOOK,
    SCROLL,
    ARTIFACT,
    DOOR,
    CHEST,
    MIRROR,
    PORTRAIT,
    STATUE,
    LEVER,
    PUZZLE,
    NPC,
    VEHICLE,
    WEAPON,
    MAGICAL_OBJECT
}

enum class RewardType {
    LORE,
    ACHIEVEMENT,
    COSMETIC,
    CHARACTER_INSIGHT,
    HIDDEN_CHAPTER,
    ALTERNATE_ENDING,
    CONCEPT_ART,
    AUTHOR_NOTE
}

enum class EffectType {
    GLOW,
    SPARKLE,
    LIGHTNING,
    FIRE,
    SMOKE,
    MIST,
    PORTAL,
    EXPLOSION,
    MAGIC_CIRCLE,
    TRANSFORMATION,
    FADE,
    SHAKE
}

enum class ParticleType {
    DUST,
    SNOW,
    RAIN,
    LEAVES,
    PETALS,
    EMBERS,
    FIREFLIES,
    MAGIC_ORBS,
    STARS,
    BUBBLES,
    ASH,
    POLLEN
}

enum class MusicGenre {
    ORCHESTRAL,
    AMBIENT,
    ELECTRONIC,
    FOLK,
    CLASSICAL,
    JAZZ,
    CELTIC,
    EASTERN,
    TRIBAL,
    INDUSTRIAL,
    CHORAL
}

enum class Tempo {
    VERY_SLOW,
    SLOW,
    MODERATE,
    FAST,
    VERY_FAST,
    DYNAMIC
}

enum class WeatherType {
    CLEAR,
    CLOUDY,
    RAIN,
    STORM,
    SNOW,
    FOG,
    WIND,
    MAGICAL,
    APOCALYPTIC
}

// ═══════════════════════════════════════════════════════════════════
// USER EXPERIENCE STATE
// ═══════════════════════════════════════════════════════════════════

/**
 * Tracks user's progress and state within an experience
 */
data class ExperienceSession(
    val id: String = UUID.randomUUID().toString(),
    val experienceId: String,
    val userId: String,
    val currentLocationId: String,
    val currentSceneId: String?,
    val visitedLocations: Set<String> = emptySet(),
    val discoveredPois: Set<String> = emptySet(),
    val metCharacters: Set<String> = emptySet(),
    val completedInteractions: Set<String> = emptySet(),
    val collectedRewards: List<DiscoveryReward> = emptyList(),
    val totalTimeSpent: Long = 0,
    val sessionStartTime: Long = System.currentTimeMillis(),
    val movementHistory: List<MovementRecord> = emptyList()
)

/**
 * Records movement through the world
 */
data class MovementRecord(
    val fromLocationId: String,
    val toLocationId: String,
    val timestamp: Long,
    val teleported: Boolean = false
)

/**
 * User preferences for the experience
 */
data class ExperiencePreferences(
    val enableVoiceNarration: Boolean = true,
    val enableAmbientSounds: Boolean = true,
    val enableMusic: Boolean = true,
    val enableParticleEffects: Boolean = true,
    val enableWeatherEffects: Boolean = true,
    val graphicsQuality: GraphicsQuality = GraphicsQuality.HIGH,
    val narratorVoice: NarratorVoice = NarratorVoice.DRAMATIC,
    val subtitlesEnabled: Boolean = true,
    val autoAdvanceScenes: Boolean = false,
    val hapticFeedback: Boolean = true
)

enum class GraphicsQuality {
    LOW,
    MEDIUM,
    HIGH,
    ULTRA
}

enum class NarratorVoice {
    CALM,
    DRAMATIC,
    MYSTERIOUS,
    WARM,
    ROBOTIC,
    NONE
}
