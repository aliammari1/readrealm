package tn.esprit.libraryapp.utils

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import tn.esprit.libraryapp.models.experience.*
import java.util.UUID
import kotlin.random.Random

/**
 * ═══════════════════════════════════════════════════════════════════
 * EXPERIENCE GENERATOR
 * AI-powered text-to-world conversion engine
 * 
 * This generator analyzes book text and creates immersive literary
 * worlds with locations, characters, scenes, and interactive elements.
 * ═══════════════════════════════════════════════════════════════════
 */

class ExperienceGenerator {
    
    companion object {
        private const val TAG = "ExperienceGenerator"
        
        // Keywords for world analysis
        private val fantasyKeywords = listOf(
            "magic", "wizard", "dragon", "castle", "kingdom", "sword", "spell",
            "enchanted", "fairy", "mythical", "quest", "prophecy", "throne",
            "elves", "dwarves", "orcs", "wand", "potion", "curse", "sorcerer"
        )
        
        private val sciFiKeywords = listOf(
            "spaceship", "robot", "android", "galaxy", "planet", "laser",
            "cyborg", "alien", "starship", "reactor", "hologram", "AI",
            "technology", "future", "space", "station", "universe", "quantum"
        )
        
        private val historicalKeywords = listOf(
            "war", "empire", "king", "queen", "battle", "soldier", "century",
            "ancient", "medieval", "victorian", "renaissance", "colony", "revolution"
        )
        
        private val mysteryKeywords = listOf(
            "detective", "murder", "clue", "suspect", "crime", "investigation",
            "evidence", "witness", "secret", "hidden", "mystery", "puzzle"
        )
        
        private val romanceKeywords = listOf(
            "love", "heart", "passion", "desire", "kiss", "romance", "wedding",
            "affair", "relationship", "feelings", "embrace", "beloved"
        )
        
        private val horrorKeywords = listOf(
            "ghost", "haunted", "terror", "nightmare", "monster", "demon",
            "darkness", "fear", "death", "blood", "scream", "evil", "curse"
        )
    }
    
    /**
     * Generate a complete book experience from text content
     */
    suspend fun generateExperience(
        bookId: String,
        bookTitle: String,
        bookAuthor: String,
        chapters: List<ChapterContent>,
        coverImageUrl: String? = null
    ): BookExperience = withContext(Dispatchers.Default) {
        Log.d(TAG, "Starting experience generation for: $bookTitle")
        
        // Combine all chapter text for analysis
        val fullText = chapters.joinToString("\n") { it.content }
        
        // Analyze the book to determine world type and characteristics
        val worldType = analyzeWorldType(fullText)
        val era = analyzeEra(fullText)
        val mood = analyzeMood(fullText)
        
        Log.d(TAG, "Detected - Type: $worldType, Era: $era, Mood: $mood")
        
        // Generate the literary world
        val world = generateWorld(
            bookTitle = bookTitle,
            fullText = fullText,
            worldType = worldType,
            era = era,
            mood = mood
        )
        
        // Extract and generate characters
        val characters = extractCharacters(fullText, worldType)
        
        // Generate scenes from chapters
        val scenes = generateScenes(chapters, characters, mood, world.locations.firstOrNull())
        
        // Generate ambiance
        val ambiance = generateAmbiance(worldType, era, mood)
        
        // Generate interactive elements
        val interactiveElements = generateInteractiveElements(world.locations, worldType)
        
        BookExperience(
            id = UUID.randomUUID().toString(),
            bookId = bookId,
            bookTitle = bookTitle,
            bookAuthor = bookAuthor,
            coverUrl = coverImageUrl,
            world = world,
            scenes = scenes,
            characters = characters,
            ambiance = ambiance,
            interactiveElements = interactiveElements,
            createdAt = System.currentTimeMillis()
        )
    }
    
    /**
     * Analyze text to determine the world type
     */
    private fun analyzeWorldType(text: String): WorldType {
        val lowercaseText = text.lowercase()
        
        val scores = mapOf(
            WorldType.FANTASY to fantasyKeywords.count { lowercaseText.contains(it) },
            WorldType.SCIENCE_FICTION to sciFiKeywords.count { lowercaseText.contains(it) },
            WorldType.HISTORICAL to historicalKeywords.count { lowercaseText.contains(it) },
            WorldType.MYSTERY to mysteryKeywords.count { lowercaseText.contains(it) },
            WorldType.ROMANCE to romanceKeywords.count { lowercaseText.contains(it) },
            WorldType.HORROR to horrorKeywords.count { lowercaseText.contains(it) }
        )
        
        return scores.maxByOrNull { it.value }?.key ?: WorldType.CONTEMPORARY
    }
    
    /**
     * Analyze text to determine the era
     */
    private fun analyzeEra(text: String): WorldEra {
        val lowercaseText = text.lowercase()
        
        return when {
            lowercaseText.contains("ancient") || lowercaseText.contains("rome") ||
            lowercaseText.contains("egypt") || lowercaseText.contains("greece") -> WorldEra.ANCIENT
            
            lowercaseText.contains("medieval") || lowercaseText.contains("knight") ||
            lowercaseText.contains("castle") || lowercaseText.contains("kingdom") -> WorldEra.MEDIEVAL
            
            lowercaseText.contains("renaissance") || lowercaseText.contains("florence") -> WorldEra.RENAISSANCE
            
            lowercaseText.contains("victorian") || lowercaseText.contains("steam") ||
            lowercaseText.contains("1800s") -> WorldEra.VICTORIAN
            
            lowercaseText.contains("1920s") || lowercaseText.contains("gatsby") ||
            lowercaseText.contains("prohibition") -> WorldEra.EARLY_MODERN
            
            lowercaseText.contains("future") || lowercaseText.contains("2100") ||
            lowercaseText.contains("spaceship") -> WorldEra.FAR_FUTURE
            
            lowercaseText.contains("dystopia") || lowercaseText.contains("apocalypse") -> WorldEra.NEAR_FUTURE
            
            else -> WorldEra.CONTEMPORARY
        }
    }
    
    /**
     * Analyze text to determine the overall mood
     */
    private fun analyzeMood(text: String): SceneMood {
        val lowercaseText = text.lowercase()
        
        val moodScores = mutableMapOf<SceneMood, Int>()
        
        // Tense mood indicators
        val tenseWords = listOf("danger", "threat", "chase", "escape", "desperate", "urgent")
        moodScores[SceneMood.TENSE] = tenseWords.count { lowercaseText.contains(it) }
        
        // Romantic mood indicators
        val romanticWords = listOf("love", "heart", "kiss", "embrace", "desire", "passion")
        moodScores[SceneMood.ROMANTIC] = romanticWords.count { lowercaseText.contains(it) }
        
        // Mysterious mood indicators
        val mysteriousWords = listOf("secret", "hidden", "strange", "unknown", "puzzle", "clue")
        moodScores[SceneMood.MYSTERIOUS] = mysteriousWords.count { lowercaseText.contains(it) }
        
        // Joyful mood indicators
        val joyfulWords = listOf("happy", "joy", "laugh", "celebrate", "wonderful", "beautiful")
        moodScores[SceneMood.JOYFUL] = joyfulWords.count { lowercaseText.contains(it) }
        
        // Epic mood indicators
        val epicWords = listOf("battle", "victory", "glory", "hero", "legendary", "triumph")
        moodScores[SceneMood.EPIC] = epicWords.count { lowercaseText.contains(it) }
        
        // Melancholic mood indicators
        val melancholicWords = listOf("sad", "grief", "loss", "tears", "sorrow", "lonely")
        moodScores[SceneMood.MELANCHOLIC] = melancholicWords.count { lowercaseText.contains(it) }
        
        return moodScores.maxByOrNull { it.value }?.key ?: SceneMood.MYSTERIOUS
    }
    
    /**
     * Generate a complete literary world
     */
    private fun generateWorld(
        bookTitle: String,
        fullText: String,
        worldType: WorldType,
        era: WorldEra,
        mood: SceneMood
    ): LiteraryWorld {
        val worldId = UUID.randomUUID().toString()
        
        // Generate locations based on world type
        val locations = generateLocations(fullText, worldType, era)
        
        // Generate weather patterns
        val weather = generateWeatherPatterns(worldType, mood)
        
        // Calculate magic level based on world type
        val magicLevel = when (worldType) {
            WorldType.FANTASY -> 0.8f + Random.nextFloat() * 0.2f
            WorldType.MYTHOLOGICAL -> 0.7f + Random.nextFloat() * 0.3f
            WorldType.HORROR -> 0.3f + Random.nextFloat() * 0.3f
            WorldType.SCIENCE_FICTION -> 0.1f + Random.nextFloat() * 0.2f
            else -> 0f
        }
        
        // Determine time of day based on mood
        val timeOfDay = when (mood) {
            SceneMood.MYSTERIOUS, SceneMood.TERRIFYING -> TimeOfDay.MIDNIGHT
            SceneMood.ROMANTIC -> TimeOfDay.DUSK
            SceneMood.EPIC -> TimeOfDay.NOON
            SceneMood.MELANCHOLIC -> TimeOfDay.EVENING
            SceneMood.JOYFUL -> TimeOfDay.MORNING
            else -> TimeOfDay.AFTERNOON
        }
        
        // Determine atmosphere based on mood
        val atmosphere = when (mood) {
            SceneMood.MYSTERIOUS -> WorldAtmosphere.MYSTICAL
            SceneMood.TERRIFYING -> WorldAtmosphere.DARK
            SceneMood.JOYFUL -> WorldAtmosphere.BRIGHT
            SceneMood.MELANCHOLIC -> WorldAtmosphere.GLOOMY
            SceneMood.EPIC -> WorldAtmosphere.ETHEREAL
            SceneMood.ROMANTIC -> WorldAtmosphere.SERENE
            SceneMood.CHAOTIC -> WorldAtmosphere.CHAOTIC
            else -> WorldAtmosphere.MYSTICAL
        }
        
        return LiteraryWorld(
            id = worldId,
            name = generateWorldName(bookTitle, worldType),
            description = generateWorldDescription(worldType, era),
            type = worldType,
            era = era,
            locations = locations,
            atmosphere = atmosphere,
            weatherPatterns = weather,
            timeOfDay = timeOfDay,
            magicLevel = magicLevel,
            technologyLevel = when (worldType) {
                WorldType.SCIENCE_FICTION -> 0.9f
                WorldType.CONTEMPORARY -> 0.7f
                WorldType.HISTORICAL -> 0.2f
                WorldType.FANTASY -> 0.3f
                else -> 0.5f
            },
            dangerLevel = when (mood) {
                SceneMood.TERRIFYING -> 0.9f
                SceneMood.TENSE -> 0.7f
                SceneMood.EPIC -> 0.6f
                SceneMood.MYSTERIOUS -> 0.4f
                else -> 0.2f
            }
        )
    }
    
    /**
     * Generate world name from book title
     */
    private fun generateWorldName(bookTitle: String, worldType: WorldType): String {
        val suffix = when (worldType) {
            WorldType.FANTASY -> "Realm"
            WorldType.SCIENCE_FICTION -> "System"
            WorldType.HISTORICAL -> "Era"
            WorldType.HORROR -> "Shadows"
            WorldType.MYSTERY -> "Mysteries"
            WorldType.ROMANCE -> "Hearts"
            else -> "World"
        }
        
        // Extract key words from title
        val keyWord = bookTitle.split(" ")
            .filter { it.length > 3 && !it.lowercase().matches(Regex("the|and|of|in|to|a")) }
            .firstOrNull() ?: bookTitle.take(10)
        
        return "The $keyWord $suffix"
    }
    
    /**
     * Generate world description
     */
    private fun generateWorldDescription(worldType: WorldType, era: WorldEra): String {
        val typeDescriptions = mapOf(
            WorldType.FANTASY to "A realm where magic weaves through the very fabric of reality, where ancient powers slumber and legends walk among mortals.",
            WorldType.SCIENCE_FICTION to "A universe of technological marvels, where the boundaries of human achievement stretch beyond the stars.",
            WorldType.HISTORICAL to "A carefully recreated period of history, brought to life with authentic details and atmospheric immersion.",
            WorldType.HORROR to "A world shrouded in darkness, where unspeakable terrors lurk in every shadow and fear is a constant companion.",
            WorldType.MYSTERY to "An intricate web of secrets and hidden truths, where every clue leads deeper into the unknown.",
            WorldType.ROMANCE to "A world painted in the colors of passion and longing, where hearts intertwine and destinies merge.",
            WorldType.CONTEMPORARY to "The modern world as we know it, yet seen through a lens that reveals its hidden depths.",
            WorldType.DYSTOPIAN to "A fractured society struggling against oppression, where hope survives against all odds.",
            WorldType.UTOPIAN to "A seemingly perfect society where all appears well, but what lies beneath the surface?",
            WorldType.ADVENTURE to "A world full of excitement and discovery, where every corner holds new wonders.",
            WorldType.MYTHOLOGICAL to "A world where gods and mortals intertwine, and ancient myths come to life."
        )
        
        return typeDescriptions[worldType] 
            ?: "An immersive literary world awaiting your exploration."
    }
    
    /**
     * Generate locations from text
     */
    private fun generateLocations(
        text: String,
        worldType: WorldType,
        era: WorldEra
    ): List<WorldLocation> {
        // Location templates based on world type
        val locationTemplates = when (worldType) {
            WorldType.FANTASY -> listOf(
                LocationTemplate("The Great Hall", LocationType.CASTLE, "A magnificent hall where heroes gather and destinies are forged."),
                LocationTemplate("Enchanted Forest", LocationType.FOREST, "Ancient trees whisper secrets of ages past, their branches heavy with magic."),
                LocationTemplate("Dragon's Peak", LocationType.MOUNTAIN, "A treacherous mountain where legend says a dragon guards its hoard."),
                LocationTemplate("The Hidden Village", LocationType.VILLAGE, "A peaceful settlement nestled in a valley, hidden from the outside world."),
                LocationTemplate("Crystal Caves", LocationType.CAVE, "Underground chambers glittering with mystical crystals."),
                LocationTemplate("The Tower of Sages", LocationType.TOWER, "A soaring spire where ancient knowledge is preserved.")
            )
            
            WorldType.SCIENCE_FICTION -> listOf(
                LocationTemplate("Command Bridge", LocationType.SPACE_STATION, "The nerve center of the starship, filled with holographic displays."),
                LocationTemplate("Space Station Nexus", LocationType.SPACE_STATION, "A massive orbital station serving as a hub for interstellar travel."),
                LocationTemplate("Alien Marketplace", LocationType.MARKET, "A bustling bazaar where species from across the galaxy trade goods."),
                LocationTemplate("Research Laboratory", LocationType.LIBRARY, "State-of-the-art facilities where the boundaries of science are pushed."),
                LocationTemplate("Cryogenic Bay", LocationType.UNDERGROUND, "Rows of pods where travelers sleep through the void between stars."),
                LocationTemplate("Observation Deck", LocationType.TOWER, "A transparent dome offering breathtaking views of the cosmos.")
            )
            
            WorldType.MYSTERY -> listOf(
                LocationTemplate("The Study", LocationType.LIBRARY, "A room filled with clues, where secrets hide in plain sight."),
                LocationTemplate("Hidden Passage", LocationType.UNDERGROUND, "A secret corridor known only to few."),
                LocationTemplate("Crime Scene", LocationType.MANSION, "Where the mystery began, frozen in time for investigation."),
                LocationTemplate("Detective's Office", LocationType.CITY, "A cluttered space where brilliant minds piece together puzzles."),
                LocationTemplate("The Vault", LocationType.DUNGEON, "A secure room holding evidence of dark deeds."),
                LocationTemplate("Abandoned Warehouse", LocationType.RUINS, "An empty building with more stories to tell.")
            )
            
            WorldType.HORROR -> listOf(
                LocationTemplate("The Haunted Manor", LocationType.MANSION, "A decaying estate where restless spirits dwell."),
                LocationTemplate("Cursed Cemetery", LocationType.RUINS, "Ancient tombstones mark the resting place of the unquiet dead."),
                LocationTemplate("The Dark Cellar", LocationType.DUNGEON, "A place of unspeakable horrors beneath the surface."),
                LocationTemplate("Twisted Forest", LocationType.FOREST, "Trees that seem to watch and branches that reach like claws."),
                LocationTemplate("The Shrine", LocationType.TEMPLE, "A place of dark worship, stained with ancient rituals."),
                LocationTemplate("Nightmare Realm", LocationType.UNDERGROUND, "A dimension where fears become reality.")
            )
            
            WorldType.ROMANCE -> listOf(
                LocationTemplate("The Garden", LocationType.GARDEN, "A romantic paradise of blooming flowers and secluded paths."),
                LocationTemplate("Moonlit Balcony", LocationType.PALACE, "Where whispered promises are made under starlight."),
                LocationTemplate("The Ballroom", LocationType.PALACE, "A grand hall where hearts meet across a crowded floor."),
                LocationTemplate("Secret Rendezvous", LocationType.GARDEN, "A private place known only to lovers."),
                LocationTemplate("Seaside Cliff", LocationType.MOUNTAIN, "Where waves crash below and hearts soar above."),
                LocationTemplate("Cozy Café", LocationType.TAVERN, "A warm haven where love stories begin over coffee.")
            )
            
            else -> listOf(
                LocationTemplate("Main Street", LocationType.CITY, "The heart of town, bustling with life and stories."),
                LocationTemplate("The Old House", LocationType.MANSION, "A building with history etched into every wall."),
                LocationTemplate("Town Square", LocationType.MARKET, "Where the community gathers and events unfold."),
                LocationTemplate("The Park", LocationType.GARDEN, "A peaceful green space amid urban life."),
                LocationTemplate("Hidden Alley", LocationType.CITY, "A forgotten corner of the city with its own secrets."),
                LocationTemplate("The Bridge", LocationType.BRIDGE, "A crossing point between worlds, real and metaphorical.")
            )
        }
        
        // Generate location IDs first so we can reference them
        val locationIds = locationTemplates.map { UUID.randomUUID().toString() }
        
        return locationTemplates.mapIndexed { index, template ->
            val connectedIds = listOfNotNull(
                locationIds.getOrNull(index - 1),
                locationIds.getOrNull(index + 1)
            )
            
            WorldLocation(
                id = locationIds[index],
                name = template.name,
                description = template.description,
                type = template.type,
                coordinates = WorldCoordinates(
                    x = 10f + index * 15f + Random.nextFloat() * 10f,
                    y = 20f + (index % 3) * 25f + Random.nextFloat() * 10f,
                    z = 0f
                ),
                pointsOfInterest = generatePOIs(template.type, worldType),
                connectedLocations = connectedIds,
                ambientSounds = generateLocationSounds(template.type),
                lightingCondition = getLightingForLocationType(template.type)
            )
        }
    }
    
    /**
     * Get lighting condition based on location type
     */
    private fun getLightingForLocationType(locationType: LocationType): LightingCondition {
        return when (locationType) {
            LocationType.CAVE, LocationType.DUNGEON, LocationType.UNDERGROUND -> LightingCondition.DARKNESS
            LocationType.FOREST -> LightingCondition.TWILIGHT
            LocationType.LIBRARY -> LightingCondition.CANDLELIT
            LocationType.PALACE, LocationType.CASTLE -> LightingCondition.GOLDEN_HOUR
            LocationType.GARDEN -> LightingCondition.BRIGHT_DAYLIGHT
            LocationType.TEMPLE -> LightingCondition.MAGICAL_GLOW
            LocationType.SPACE_STATION -> LightingCondition.NEON
            LocationType.TAVERN -> LightingCondition.FIRELIGHT
            else -> LightingCondition.BRIGHT_DAYLIGHT
        }
    }
    
    /**
     * Generate ambient sounds for a location
     */
    private fun generateLocationSounds(locationType: LocationType): List<AmbientSound> {
        val sounds = mutableListOf<AmbientSound>()
        
        when (locationType) {
            LocationType.FOREST -> {
                sounds.add(AmbientSound(name = "Bird Songs", type = SoundType.NATURE, volume = 0.4f))
                sounds.add(AmbientSound(name = "Rustling Leaves", type = SoundType.NATURE, volume = 0.3f))
            }
            LocationType.CAVE, LocationType.DUNGEON -> {
                sounds.add(AmbientSound(name = "Dripping Water", type = SoundType.AMBIENT, volume = 0.5f))
                sounds.add(AmbientSound(name = "Echoing Whispers", type = SoundType.AMBIENT, volume = 0.2f))
            }
            LocationType.OCEAN, LocationType.SHIP -> {
                sounds.add(AmbientSound(name = "Ocean Waves", type = SoundType.NATURE, volume = 0.6f))
                sounds.add(AmbientSound(name = "Seagulls", type = SoundType.CREATURE, volume = 0.3f))
            }
            LocationType.CITY, LocationType.MARKET -> {
                sounds.add(AmbientSound(name = "Crowd Chatter", type = SoundType.CROWD, volume = 0.5f))
                sounds.add(AmbientSound(name = "City Ambiance", type = SoundType.AMBIENT, volume = 0.4f))
            }
            LocationType.CASTLE, LocationType.PALACE -> {
                sounds.add(AmbientSound(name = "Stone Echoes", type = SoundType.AMBIENT, volume = 0.3f))
                sounds.add(AmbientSound(name = "Distant Music", type = SoundType.MUSIC, volume = 0.2f))
            }
            LocationType.TEMPLE -> {
                sounds.add(AmbientSound(name = "Mystical Hum", type = SoundType.MAGICAL, volume = 0.4f))
                sounds.add(AmbientSound(name = "Chanting", type = SoundType.AMBIENT, volume = 0.3f))
            }
            else -> {
                sounds.add(AmbientSound(name = "General Ambiance", type = SoundType.AMBIENT, volume = 0.3f))
            }
        }
        
        return sounds
    }
    
    /**
     * Generate Points of Interest for a location
     */
    private fun generatePOIs(
        locationType: LocationType,
        worldType: WorldType
    ): List<PointOfInterest> {
        val poiCount = 2 + Random.nextInt(4)
        
        val poiTypes = when (locationType) {
            LocationType.LIBRARY, LocationType.TEMPLE -> 
                listOf(PoiType.JOURNAL_ENTRY, PoiType.ARTIFACT, PoiType.PLOT_CLUE)
            LocationType.CASTLE, LocationType.PALACE, LocationType.MANSION ->
                listOf(PoiType.MONUMENT, PoiType.HIDDEN_PASSAGE, PoiType.TREASURE)
            LocationType.FOREST, LocationType.MOUNTAIN, LocationType.CAVE ->
                listOf(PoiType.SCENIC_VIEW, PoiType.INTERACTIVE_OBJECT, PoiType.SECRET_ROOM)
            LocationType.RUINS, LocationType.DUNGEON ->
                listOf(PoiType.CHARACTER_MEMORY, PoiType.ARTIFACT, PoiType.PLOT_CLUE)
            else ->
                listOf(PoiType.INTERACTIVE_OBJECT, PoiType.SCENIC_VIEW, PoiType.JOURNAL_ENTRY)
        }
        
        return (0 until poiCount).map { index ->
            val poiType = poiTypes[index % poiTypes.size]
            
            PointOfInterest(
                id = UUID.randomUUID().toString(),
                name = generatePOIName(poiType, worldType),
                description = generatePOIDescription(poiType),
                type = poiType,
                position = WorldCoordinates(
                    x = 50f + Random.nextFloat() * 200f,
                    y = 200f + Random.nextFloat() * 300f,
                    z = 0f
                ),
                interactionType = InteractionType.OBSERVE,
                relatedChapter = 1 + Random.nextInt(10),
                relatedQuote = null,
                reward = if (Random.nextFloat() > 0.5f) {
                    DiscoveryReward(
                        type = RewardType.LORE,
                        title = "Discovery: ${generatePOIName(poiType, worldType)}",
                        description = "You've uncovered something interesting!"
                    )
                } else null
            )
        }
    }
    
    private fun generatePOIName(type: PoiType, worldType: WorldType): String {
        val names = when (type) {
            PoiType.ARTIFACT -> listOf("Ancient Relic", "Mysterious Object", "Lost Heirloom", "Enchanted Item")
            PoiType.MONUMENT -> listOf("Stone Memorial", "Carved Statue", "Historic Marker", "Grand Monument")
            PoiType.HIDDEN_PASSAGE -> listOf("Secret Door", "Hidden Lever", "Concealed Path", "False Wall")
            PoiType.TREASURE -> listOf("Treasure Chest", "Hidden Cache", "Precious Gems", "Golden Hoard")
            PoiType.JOURNAL_ENTRY -> listOf("Worn Journal", "Faded Letter", "Ancient Scroll", "Personal Diary")
            PoiType.CHARACTER_MEMORY -> listOf("Lingering Memory", "Echoes of the Past", "Ghostly Presence", "Memory Fragment")
            PoiType.PLOT_CLUE -> listOf("Crucial Evidence", "Hidden Message", "Mysterious Clue", "Revealing Document")
            PoiType.SCENIC_VIEW -> listOf("Breathtaking Vista", "Panoramic View", "Hidden Overlook", "Scenic Spot")
            PoiType.INTERACTIVE_OBJECT -> listOf("Strange Device", "Curious Object", "Interactive Element", "Mechanical Wonder")
            PoiType.PORTAL -> listOf("Mystical Gateway", "Dimensional Rift", "Magic Portal", "Ethereal Door")
            PoiType.SECRET_ROOM -> listOf("Hidden Chamber", "Secret Alcove", "Concealed Room", "Private Sanctuary")
        }
        return names.random()
    }
    
    private fun generatePOIDescription(type: PoiType): String {
        return when (type) {
            PoiType.ARTIFACT -> "An object of mysterious origin, humming with potential significance."
            PoiType.MONUMENT -> "A testament to events long past, standing as a silent witness to history."
            PoiType.HIDDEN_PASSAGE -> "A concealed path known only to those who know where to look."
            PoiType.TREASURE -> "Valuables hidden away, waiting to be discovered by a worthy explorer."
            PoiType.JOURNAL_ENTRY -> "Personal writings that shed light on the minds and hearts of those who came before."
            PoiType.CHARACTER_MEMORY -> "A powerful emotional resonance lingers here, imprinted by someone's experience."
            PoiType.PLOT_CLUE -> "A piece of the puzzle, essential to understanding the larger picture."
            PoiType.SCENIC_VIEW -> "A moment of beauty and wonder, offering perspective on the world below."
            PoiType.INTERACTIVE_OBJECT -> "Something that responds to touch, inviting exploration and experimentation."
            PoiType.PORTAL -> "A threshold between worlds, crackling with otherworldly energy."
            PoiType.SECRET_ROOM -> "A private space hidden from casual discovery, preserving its secrets."
        }
    }
    
    /**
     * Generate ambiance for the world
     */
    private fun generateAmbiance(
        worldType: WorldType,
        era: WorldEra,
        mood: SceneMood
    ): WorldAmbiance {
        val (primaryColor, secondaryColor, accentColor) = when (worldType) {
            WorldType.FANTASY -> Triple(0xFF1A1A4DL, 0xFF2D2D6BL, 0xFFD4AF37L)
            WorldType.SCIENCE_FICTION -> Triple(0xFF0A1929L, 0xFF0D47A1L, 0xFF00E5FFL)
            WorldType.HORROR -> Triple(0xFF0A0A0AL, 0xFF1A0A0AL, 0xFF8B0000L)
            WorldType.ROMANCE -> Triple(0xFF2D1A1AL, 0xFF4A2A3AL, 0xFFFF69B4L)
            WorldType.MYSTERY -> Triple(0xFF1A1A2DL, 0xFF2D2D4AL, 0xFFDAA520L)
            WorldType.HISTORICAL -> Triple(0xFF2D2A1AL, 0xFF4A3A2AL, 0xFFD4AF37L)
            else -> Triple(0xFF1A1A1AL, 0xFF2D2D2DL, 0xFFFFD700L)
        }
        
        val lightingCondition = when (mood) {
            SceneMood.MYSTERIOUS -> LightingCondition.CANDLELIT
            SceneMood.ROMANTIC -> LightingCondition.GOLDEN_HOUR
            SceneMood.TERRIFYING -> LightingCondition.DARKNESS
            SceneMood.EPIC -> LightingCondition.BRIGHT_DAYLIGHT
            SceneMood.MELANCHOLIC -> LightingCondition.OVERCAST
            else -> LightingCondition.BRIGHT_DAYLIGHT
        }
        
        val particleEffects = mutableListOf<ParticleEffect>()
        when (worldType) {
            WorldType.FANTASY -> {
                particleEffects.add(ParticleEffect(type = ParticleType.MAGIC_ORBS, density = 0.6f, color = 0xFFE6E6FAL))
                particleEffects.add(ParticleEffect(type = ParticleType.FIREFLIES, density = 0.4f, color = 0xFFFFFF00L))
            }
            WorldType.HORROR -> {
                particleEffects.add(ParticleEffect(type = ParticleType.ASH, density = 0.5f, color = 0xFF808080L))
                particleEffects.add(ParticleEffect(type = ParticleType.DUST, density = 0.5f, color = 0xFF696969L))
            }
            WorldType.ROMANCE -> {
                particleEffects.add(ParticleEffect(type = ParticleType.PETALS, density = 0.5f, color = 0xFFFFB6C1L))
            }
            else -> {
                particleEffects.add(ParticleEffect(type = ParticleType.DUST, density = 0.3f, color = 0xFFFFE4B5L))
            }
        }
        
        val musicTheme = generateMusicTheme(worldType, mood)
        
        return WorldAmbiance(
            primaryColor = primaryColor,
            secondaryColor = secondaryColor,
            accentColor = accentColor,
            fogDensity = if (worldType == WorldType.HORROR || mood == SceneMood.MYSTERIOUS) 0.5f else 0.1f,
            particleEffects = particleEffects,
            globalLighting = lightingCondition,
            musicTheme = musicTheme,
            ambientSoundscape = generateAmbientSounds(worldType, mood)
        )
    }
    
    private fun generateAmbientSounds(worldType: WorldType, mood: SceneMood): List<AmbientSound> {
        val sounds = mutableListOf<AmbientSound>()
        
        when (worldType) {
            WorldType.FANTASY -> {
                sounds.add(AmbientSound(name = "Mystical Hum", type = SoundType.MAGICAL, volume = 0.3f))
                sounds.add(AmbientSound(name = "Distant Magic", type = SoundType.MAGICAL, volume = 0.2f))
            }
            WorldType.HORROR -> {
                sounds.add(AmbientSound(name = "Creaking Floorboards", type = SoundType.AMBIENT, volume = 0.4f, isLooping = false))
                sounds.add(AmbientSound(name = "Whispers in the Dark", type = SoundType.AMBIENT, volume = 0.2f))
            }
            WorldType.SCIENCE_FICTION -> {
                sounds.add(AmbientSound(name = "Ship Hum", type = SoundType.MECHANICAL, volume = 0.3f))
                sounds.add(AmbientSound(name = "Electronic Beeps", type = SoundType.MECHANICAL, volume = 0.2f, isLooping = false))
            }
            else -> {
                sounds.add(AmbientSound(name = "General Atmosphere", type = SoundType.AMBIENT, volume = 0.3f))
            }
        }
        
        return sounds
    }
    
    private fun generateMusicTheme(worldType: WorldType, mood: SceneMood): MusicTheme {
        val genre = when (worldType) {
            WorldType.FANTASY -> MusicGenre.ORCHESTRAL
            WorldType.SCIENCE_FICTION -> MusicGenre.ELECTRONIC
            WorldType.HORROR -> MusicGenre.AMBIENT
            WorldType.ROMANCE -> MusicGenre.CLASSICAL
            WorldType.HISTORICAL -> MusicGenre.CLASSICAL
            else -> MusicGenre.ORCHESTRAL
        }
        
        val tempo = when (mood) {
            SceneMood.EPIC -> Tempo.FAST
            SceneMood.TENSE -> Tempo.MODERATE
            SceneMood.ROMANTIC -> Tempo.SLOW
            SceneMood.MELANCHOLIC -> Tempo.VERY_SLOW
            SceneMood.CHAOTIC -> Tempo.VERY_FAST
            else -> Tempo.MODERATE
        }
        
        val instruments = when (genre) {
            MusicGenre.ORCHESTRAL -> listOf("Strings", "Brass", "Woodwinds", "Percussion")
            MusicGenre.ELECTRONIC -> listOf("Synthesizer", "Drum Machine", "Bass")
            MusicGenre.AMBIENT -> listOf("Pads", "Drones", "Nature Sounds")
            MusicGenre.CLASSICAL -> listOf("Piano", "Violin", "Cello")
            else -> listOf("Various Instruments")
        }
        
        return MusicTheme(
            name = "${worldType.name.lowercase().replaceFirstChar { it.uppercase() }} Theme",
            genre = genre,
            tempo = tempo,
            instruments = instruments,
            mood = mood
        )
    }
    
    /**
     * Generate weather patterns
     */
    private fun generateWeatherPatterns(
        worldType: WorldType,
        mood: SceneMood
    ): List<WeatherPattern> {
        val patterns = mutableListOf<WeatherPattern>()
        
        when {
            mood == SceneMood.MELANCHOLIC -> {
                patterns.add(WeatherPattern(type = WeatherType.RAIN, intensity = 0.6f))
                patterns.add(WeatherPattern(type = WeatherType.CLOUDY, intensity = 0.4f))
            }
            mood == SceneMood.TERRIFYING -> {
                patterns.add(WeatherPattern(type = WeatherType.STORM, intensity = 0.7f))
                patterns.add(WeatherPattern(type = WeatherType.FOG, intensity = 0.5f))
            }
            mood == SceneMood.EPIC -> {
                patterns.add(WeatherPattern(type = WeatherType.WIND, intensity = 0.6f))
            }
            mood == SceneMood.ROMANTIC -> {
                patterns.add(WeatherPattern(type = WeatherType.CLEAR, intensity = 0.8f))
            }
            else -> {
                patterns.add(WeatherPattern(type = WeatherType.CLEAR, intensity = 0.7f))
            }
        }
        
        return patterns
    }
    
    /**
     * Generate interactive elements
     */
    private fun generateInteractiveElements(
        locations: List<WorldLocation>,
        worldType: WorldType
    ): List<InteractiveElement> {
        val elements = mutableListOf<InteractiveElement>()
        
        locations.forEach { location ->
            // Add 1-3 interactive elements per location
            val count = 1 + Random.nextInt(3)
            repeat(count) { index ->
                val type = when (worldType) {
                    WorldType.FANTASY -> listOf(InteractiveType.MAGICAL_OBJECT, InteractiveType.SCROLL, InteractiveType.ARTIFACT).random()
                    WorldType.MYSTERY -> listOf(InteractiveType.BOOK, InteractiveType.PUZZLE, InteractiveType.DOOR).random()
                    WorldType.HORROR -> listOf(InteractiveType.MIRROR, InteractiveType.PORTRAIT, InteractiveType.DOOR).random()
                    else -> listOf(InteractiveType.BOOK, InteractiveType.ARTIFACT, InteractiveType.NPC).random()
                }
                
                elements.add(
                    InteractiveElement(
                        id = UUID.randomUUID().toString(),
                        name = "Interactive ${type.name.lowercase().replace("_", " ")}",
                        type = type,
                        description = "An intriguing ${type.name.lowercase().replace("_", " ")} that beckons your attention.",
                        position = WorldCoordinates(
                            x = Random.nextFloat() * 100f,
                            y = Random.nextFloat() * 100f,
                            z = 0f
                        ),
                        locationId = location.id,
                        interactionPrompt = "Examine ${type.name.lowercase().replace("_", " ")}",
                        reward = if (Random.nextFloat() > 0.5f) {
                            DiscoveryReward(
                                type = RewardType.LORE,
                                title = "Discovery Reward",
                                description = "You've uncovered something interesting!",
                                points = 10
                            )
                        } else null
                    )
                )
            }
        }
        
        return elements
    }
    
    /**
     * Extract characters from text
     */
    private fun extractCharacters(
        text: String,
        worldType: WorldType
    ): List<ExperienceCharacter> {
        val characterTemplates = when (worldType) {
            WorldType.FANTASY -> listOf(
                CharacterTemplate("The Chosen One", CharacterRole.PROTAGONIST, "A young hero destined for greatness."),
                CharacterTemplate("The Wise Mentor", CharacterRole.MENTOR, "An ancient sage whose wisdom guides the hero."),
                CharacterTemplate("The Dark Lord", CharacterRole.ANTAGONIST, "A powerful being of shadow seeking dominion."),
                CharacterTemplate("The Loyal Friend", CharacterRole.SIDEKICK, "A steadfast companion providing humor and heart.")
            )
            WorldType.ROMANCE -> listOf(
                CharacterTemplate("The Romantic Lead", CharacterRole.PROTAGONIST, "A passionate soul searching for true love."),
                CharacterTemplate("The Love Interest", CharacterRole.LOVE_INTEREST, "The one who captures hearts with a single glance."),
                CharacterTemplate("The Rival", CharacterRole.SHADOW, "Someone who stands between love and its fulfillment."),
                CharacterTemplate("The Best Friend", CharacterRole.ALLY, "A confidant offering advice and support.")
            )
            WorldType.MYSTERY -> listOf(
                CharacterTemplate("The Detective", CharacterRole.PROTAGONIST, "A brilliant mind dedicated to uncovering truth."),
                CharacterTemplate("The Suspect", CharacterRole.SHAPESHIFTER, "Someone whose motives remain unclear."),
                CharacterTemplate("The Criminal", CharacterRole.ANTAGONIST, "The mastermind behind the mystery."),
                CharacterTemplate("The Witness", CharacterRole.HERALD, "Someone who holds the key to the puzzle.")
            )
            else -> listOf(
                CharacterTemplate("The Main Character", CharacterRole.PROTAGONIST, "The central figure whose journey we follow."),
                CharacterTemplate("The Companion", CharacterRole.ALLY, "A trusted friend who shares in the adventure."),
                CharacterTemplate("The Obstacle", CharacterRole.SHADOW, "Someone or something that stands in the way.")
            )
        }
        
        return characterTemplates.mapIndexed { index, template ->
            ExperienceCharacter(
                id = UUID.randomUUID().toString(),
                name = template.name,
                role = template.role,
                description = template.description,
                physicalDescription = generatePhysicalDescription(),
                personality = generatePersonalityTraits(template.role),
                relationships = emptyList(),
                currentLocation = null,
                quotes = generateQuotes(template.role),
                isMainCharacter = template.role == CharacterRole.PROTAGONIST,
                hasMetPlayer = false,
                affinity = when (template.role) {
                    CharacterRole.ANTAGONIST, CharacterRole.SHADOW -> 0.2f
                    CharacterRole.ALLY, CharacterRole.SIDEKICK, CharacterRole.LOVE_INTEREST -> 0.8f
                    CharacterRole.MENTOR -> 0.9f
                    else -> 0.5f
                }
            )
        }
    }
    
    private fun generatePhysicalDescription(): PhysicalDescription {
        val hairColors = listOf("Dark Brown", "Blonde", "Silver", "Auburn", "Black", "Red")
        val eyeColors = listOf("Deep Blue", "Emerald Green", "Warm Brown", "Gray", "Golden", "Hazel")
        val skinTones = listOf("Fair", "Olive", "Tan", "Dark", "Pale", "Bronze")
        
        return PhysicalDescription(
            height = HeightCategory.values().random(),
            build = BuildCategory.values().random(),
            hairColor = hairColors.random(),
            eyeColor = eyeColors.random(),
            skinTone = skinTones.random(),
            age = AgeCategory.values().filter { it != AgeCategory.CHILD }.random(),
            distinctiveFeatures = listOf(
                "A distinctive scar",
                "An unusual birthmark",
                "Piercing gaze",
                "Warm smile"
            ).shuffled().take(Random.nextInt(1, 3)),
            clothing = "Characteristic garb befitting their station",
            accessories = listOf("Ring", "Necklace", "Brooch").shuffled().take(Random.nextInt(0, 2))
        )
    }
    
    private fun generatePersonalityTraits(role: CharacterRole): List<PersonalityTrait> {
        val traits = when (role) {
            CharacterRole.PROTAGONIST -> listOf(PersonalityTrait.BRAVE, PersonalityTrait.LOYAL, PersonalityTrait.KIND)
            CharacterRole.ANTAGONIST -> listOf(PersonalityTrait.AMBITIOUS, PersonalityTrait.DECEPTIVE, PersonalityTrait.CRUEL)
            CharacterRole.MENTOR -> listOf(PersonalityTrait.INTELLIGENT, PersonalityTrait.CAUTIOUS, PersonalityTrait.HONEST)
            CharacterRole.SIDEKICK -> listOf(PersonalityTrait.LOYAL, PersonalityTrait.OPTIMISTIC, PersonalityTrait.BRAVE)
            CharacterRole.LOVE_INTEREST -> listOf(PersonalityTrait.PASSIONATE, PersonalityTrait.CHARISMATIC, PersonalityTrait.KIND)
            else -> listOf(PersonalityTrait.CURIOUS, PersonalityTrait.RESERVED, PersonalityTrait.STOIC)
        }
        return traits.shuffled().take(3)
    }
    
    private fun generateQuotes(role: CharacterRole): List<CharacterQuote> {
        val quotes = when (role) {
            CharacterRole.PROTAGONIST -> listOf(
                "I will not let darkness prevail.",
                "Hope is the light that guides us home."
            )
            CharacterRole.ANTAGONIST -> listOf(
                "Power is not given. It is taken.",
                "The world will remember my name."
            )
            CharacterRole.MENTOR -> listOf(
                "The journey of a thousand miles begins with a single step.",
                "Knowledge is the greatest weapon."
            )
            else -> listOf(
                "Together we are stronger.",
                "Every ending is a new beginning."
            )
        }
        
        return quotes.mapIndexed { index, text ->
            CharacterQuote(
                text = text,
                context = "A defining moment in chapter ${index + 1}",
                chapter = index + 1,
                mood = DialogueEmotion.DETERMINED
            )
        }
    }
    
    /**
     * Generate scenes from chapters
     */
    private fun generateScenes(
        chapters: List<ChapterContent>,
        characters: List<ExperienceCharacter>,
        overallMood: SceneMood,
        defaultLocation: WorldLocation?
    ): List<ExperienceScene> {
        val sceneLocation = defaultLocation ?: WorldLocation(
            id = UUID.randomUUID().toString(),
            name = "The Beginning",
            type = LocationType.CASTLE,
            description = "Where the story begins",
            coordinates = WorldCoordinates(0f, 0f, 0f),
            connectedLocations = emptyList(),
            pointsOfInterest = emptyList(),
            ambientSounds = emptyList(),
            lightingCondition = LightingCondition.BRIGHT_DAYLIGHT
        )
        
        return chapters.mapIndexed { index, chapter ->
            val sceneMood = if (index == chapters.size - 1) SceneMood.EPIC else overallMood
            
            ExperienceScene(
                id = UUID.randomUUID().toString(),
                chapterNumber = index + 1,
                chapterTitle = chapter.title.ifEmpty { "Chapter ${index + 1}" },
                sceneTitle = chapter.title.ifEmpty { "Scene ${index + 1}" },
                description = chapter.content.take(200),
                location = sceneLocation,
                characters = characters.take(2).map { it.id },
                mood = sceneMood,
                keyEvents = extractKeyEvents(chapter.content),
                dialogues = generateDialogues(chapter.content, characters),
                duration = calculateDuration(chapter.content),
                isKeyScene = index == 0 || index == chapters.size - 1
            )
        }
    }
    
    private fun extractKeyEvents(content: String): List<KeyEvent> {
        return listOf(
            KeyEvent(
                id = UUID.randomUUID().toString(),
                title = "Key Moment",
                description = "A pivotal moment unfolds",
                timestamp = 0.5f,
                importance = EventImportance.MAJOR,
                visualEffect = null,
                soundEffect = null
            )
        )
    }
    
    private fun generateDialogues(
        content: String,
        characters: List<ExperienceCharacter>
    ): List<SceneDialogue> {
        val dialogues = mutableListOf<SceneDialogue>()
        
        characters.take(2).forEachIndexed { index, character ->
            dialogues.add(
                SceneDialogue(
                    speakerId = character.id,
                    speakerName = character.name,
                    text = "Words that echo through the ages.",
                    emotion = DialogueEmotion.DETERMINED,
                    timestamp = index * 0.3f
                )
            )
        }
        
        dialogues.add(
            SceneDialogue(
                speakerId = "narrator",
                speakerName = "Narrator",
                text = "The scene unfolds before us.",
                emotion = DialogueEmotion.NEUTRAL,
                timestamp = 0.8f,
                isThought = false
            )
        )
        
        return dialogues
    }
    
    private fun calculateDuration(content: String): SceneDuration {
        val wordCount = content.split("\\s+".toRegex()).size
        val readingTimeMinutes = wordCount / 200.0
        
        return when {
            readingTimeMinutes < 2 -> SceneDuration.BRIEF
            readingTimeMinutes < 5 -> SceneDuration.SHORT
            readingTimeMinutes < 10 -> SceneDuration.MEDIUM
            readingTimeMinutes < 20 -> SceneDuration.LONG
            else -> SceneDuration.EXTENDED
        }
    }
}

/**
 * Helper data classes
 */
data class ChapterContent(
    val chapterNumber: Int,
    val title: String,
    val content: String
)

private data class LocationTemplate(
    val name: String,
    val type: LocationType,
    val description: String
)

private data class CharacterTemplate(
    val name: String,
    val role: CharacterRole,
    val description: String
)
