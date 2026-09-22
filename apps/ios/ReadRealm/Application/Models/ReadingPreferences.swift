import SwiftUI

struct ReadingPreferences: Equatable {
    // Text settings
    var fontSize: CGFloat = 16
    var lineHeight: CGFloat = 1.5
    var fontFamily: String = "-apple-system"
    var textAlignment: String = "left"
    var marginSize: CGFloat = 20
    var letterSpacing: CGFloat = 0
    var paragraphSpacing: CGFloat = 1.2
    
    // Display settings
    var isDarkMode: Bool = false
    var backgroundColor: Color = .white
    var textColor: Color = .black
    var accentColor: Color = .blue
    
    // Language settings
    var sourceLanguage: String = "en"
    var targetLanguage: String = "en"
    var autoDetectLanguage: Bool = true
    
    // Reading features
    var autoScroll: Bool = false
    var autoScrollSpeed: Float = 1.0
    var showReadingStats: Bool = true
    var enableGestures: Bool = true
    var showTranslationPreview: Bool = true
    var highlightTranslatedWords: Bool = true
    
    // Accessibility
    var enableTextToSpeech: Bool = true
    var speakingRate: Float = 0.5
    var enableVoiceCommands: Bool = false
    
    // Performance
    var enablePageTransitions: Bool = true
    var preloadNextPage: Bool = true
    var maxCachedPages: Int = 5
    
    // Layout
    enum LayoutMode: String, CaseIterable {
        case single, double, continuous
    }
    var layoutMode: LayoutMode = .single
}

struct ReadingTheme {
    let name: String
    let backgroundColor: Color
    let textColor: Color
}

enum ReaderThemes {
    static let themes = [
        ReadingTheme(name: "Light", backgroundColor: .white, textColor: .black),
        ReadingTheme(name: "Dark", backgroundColor: .black, textColor: .white),
        ReadingTheme(name: "Sepia", backgroundColor: Color(red: 0.98, green: 0.95, blue: 0.9), textColor: .brown)
    ]
}
