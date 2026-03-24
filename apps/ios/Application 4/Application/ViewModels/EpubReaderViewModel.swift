import SwiftUI
import AVFoundation

class EpubReaderViewModel: NSObject, ObservableObject, AVSpeechSynthesizerDelegate {
    @Published var content: String = ""
    @Published var currentPage: Int = 0
    @Published var pages: [String] = []
    @Published var isLoading: Bool = true
    @Published var error: String? = nil
    @Published var preferences = ReadingPreferences()
    @Published var statistics = ReadingStatistics()
    @Published var isSpeaking: Bool = false
    @Published var currentWordIndex: Int = 0
    @Published var bookmarks: [BookmarkPage] = []
    @Published var autoScrollProgress: Double = 0
    @Published var translationProgress: Double = 0
    @Published var loadingProgress: Double = 0
    @Published var pdfExportProgress: Double = 0
    @Published var currentOperation: String = ""
    @Published var isImmersiveModeEnabled: Bool = false
    @Published var translationManager: TranslationManager
    @Published var highlightedWordRange: NSRange?
    @Published var currentHighlightedWord: String?
    @Published var currentUtterance: AVSpeechUtterance?
    @Published var hasTranslatedContent: Bool = false
    @Published private(set) var translatedPages: Set<Int> = []
    @Published private(set) var isTranslatingInBackground: Bool = false
    private var backgroundTranslationTask: Task<Void, Never>?
    
    private var synthesizer: AVSpeechSynthesizer = {
        let synth = AVSpeechSynthesizer()
        // Ensure background playback
        try? AVAudioSession.sharedInstance().setCategory(.playback, mode: .spokenAudio, options: .mixWithOthers)
        return synth
    }()

    private var startTime: Date?
    private var timer: Timer?
    private var autoScrollTimer: Timer?
    
    private let voiceCommandHandler: VoiceCommandHandler
    
    // Add state management enums
    enum ViewState {
        case loading
        case ready
        case error(String)
    }
    
    enum TranslationState {
        case idle
        case translating(progress: Double)
        case background
        case error(String)
    }
    
    @Published private(set) var viewState: ViewState = .loading
    @Published private(set) var translationState: TranslationState = .idle
    @Published private(set) var exportState: ViewState = .ready
    
    private var pageStates: [Int: String] = [:] // Cache original pages
    private var pendingTranslations: Set<Int> = []
    
    override init() {
        self.translationManager = TranslationManager()
        self.voiceCommandHandler = VoiceCommandHandler()
        self.synthesizer = AVSpeechSynthesizer()
        super.init()
        setupSpeechSynthesizer()
        setupVoiceCommands()
        setupSynthesizerDelegate()
    }
    
    private func setupSpeechSynthesizer() {
        synthesizer.delegate = self
        
        // Configure default voice
        if let voice = AVSpeechSynthesisVoice(language: "en-US") {
            let utterance = AVSpeechUtterance(string: "")
            utterance.voice = voice
            utterance.rate = preferences.speakingRate
            utterance.pitchMultiplier = 1.0
            currentUtterance = utterance
        }
    }
    
    private func setupVoiceCommands() {
        guard preferences.enableVoiceCommands else { return }
        
        Task {
            do {
                try await voiceCommandHandler.startListening { [weak self] command in
                    guard let self = self else { return }
                    Task { @MainActor in
                        switch command {
                        case .next:
                            self.moveToNextPage()
                        case .previous:
                            self.moveToPreviousPage()
                        case .start:
                            self.startSpeaking(self.pages[self.currentPage])
                        case .stop:
                            self.stopSpeaking()
                        }
                    }
                }
            } catch {
                await MainActor.run {
                    print("Voice command error: \(error.localizedDescription)")
                    self.error = "Voice commands not available: \(error.localizedDescription)"
                }
            }
        }
    }

    private func setupSynthesizerDelegate() {
        synthesizer.delegate = self
    }
    
    @MainActor
    func updateProgress(_ progress: Double) {
        self.loadingProgress = progress
    }
    
    @MainActor
    func updateTranslationProgress(_ progress: Double) {
        self.translationProgress = progress
    }
    
    @MainActor
    func updateOperation(_ operation: String) {
        self.currentOperation = operation
    }
    
    func loadEpub(from url: URL) async {
        await updateOperation("Downloading...")
        do {
            let (data, response) = try await URLSession.shared.data(from: url)
            
            if let text = String(data: data, encoding: .utf8) {
                await MainActor.run {
                    self.content = text
                    self.loadingProgress = 0.5
                    self.currentOperation = "Processing..."
                }
                
                calculatePages()
                
                await MainActor.run {
                    self.isLoading = false
                    self.loadingProgress = 1.0
                    self.currentOperation = ""
                }
            }
        } catch {
            await MainActor.run {
                self.error = error.localizedDescription
                self.isLoading = false
                self.currentOperation = "Error"
            }
        }
    }
    
    private func calculatePages() {
        let words = content.split(separator: " ")
        let wordsPerPage = 500 // Adjust based on screen size and preferences
        pages = stride(from: 0, to: words.count, by: wordsPerPage).map {
            words[$0..<min($0 + wordsPerPage, words.count)].joined(separator: " ")
        }
    }
    
    func startSpeaking(_ text: String) {
        guard !text.isEmpty else { return }
        stopSpeaking() // Stop any existing speech

        let utterance = AVSpeechUtterance(string: text)
        utterance.rate = preferences.speakingRate
        utterance.voice = AVSpeechSynthesisVoice(language: preferences.targetLanguage)
        utterance.pitchMultiplier = 1.0
        
        if #available(iOS 16.0, *) {
            utterance.prefersAssistiveTechnologySettings = true
        }

        Task { @MainActor in
            self.isSpeaking = true
            self.synthesizer.speak(utterance)
        }
    }
    
    func stopSpeaking() {
        synthesizer.stopSpeaking(at: .immediate)
        Task { @MainActor in
            isSpeaking = false
            currentHighlightedWord = nil
        }
    }
    
    func updateSpeakingRate(_ rate: Float) {
        preferences.speakingRate = rate
        if isSpeaking, let text = currentUtterance?.speechString {
            startSpeaking(text) // Restart with new rate
        }
    }
    
    func updateReadingStats() {
        guard let start = startTime else {
            startTime = Date()
            return
        }
        
        let timeElapsed = Date().timeIntervalSince(start)
        statistics.updateStats(
            currentPage: currentPage,
            wordsRead: pages[currentPage].split(separator: " ").count,
            timeElapsed: timeElapsed
        )
        startTime = Date()
    }
    
    @MainActor
    func startSession() {
        startTime = Date()
        timer = Timer.scheduledTimer(withTimeInterval: 30, repeats: true) { [weak self] _ in
            self?.updateReadingStats()
        }
    }
    
    @MainActor
    func endSession() {
        timer?.invalidate()
        updateReadingStats()
    }
    
    func addBookmark(pageNumber: Int, snippet: String) {
        let bookmark = BookmarkPage(pageNumber: pageNumber, snippet: snippet)
        bookmarks.append(bookmark)
    }
    
    func toggleAutoScroll(enabled: Bool, speed: Float) {
        autoScrollTimer?.invalidate()
        
        guard enabled else { return }
        
        let interval = 1.0 / Double(speed)
        autoScrollTimer = Timer.scheduledTimer(withTimeInterval: interval, repeats: true) { [weak self] _ in
            guard let self = self else { return }
            Task { @MainActor in
                self.autoScrollProgress += 0.01
                if self.autoScrollProgress >= 1.0 {
                    self.autoScrollProgress = 0
                    self.moveToNextPage()
                }
            }
        }
    }
    
    @MainActor
    func exportToPDF() async throws -> URL {
        currentOperation = "Exporting PDF..."
        defer { currentOperation = "" }
        
        let contentToExport = preferences.targetLanguage != preferences.sourceLanguage && hasTranslatedContent 
            ? pages.joined(separator: "\n\n") 
            : content
        
        return try await PDFExporter.createPDF(
            from: contentToExport,
            title: "Book Export",
            sourceLanguage: preferences.sourceLanguage,
            targetLanguage: preferences.targetLanguage,
            isTranslated: hasTranslatedContent,
            preferences: preferences, // Pass preferences to PDF exporter
            progress: { [weak self] progress in
                Task { @MainActor in
                    self?.pdfExportProgress = Double(progress)
                }
            }
        )
    }
    
    @MainActor
    func moveToNextPage() {
        guard currentPage < pages.count - 1 else { return }
        currentPage += 1
        updateReadingStats()
        if preferences.targetLanguage != preferences.sourceLanguage {
            Task {
                await translateCurrentPage()
            }
        }
    }
    
    @MainActor
    func moveToPreviousPage() {
        guard currentPage > 0 else { return }
        currentPage -= 1
        updateReadingStats()
        if preferences.targetLanguage != preferences.sourceLanguage {
            Task {
                await translateCurrentPage()
            }
        }
    }
    
    func updateTtsLanguage(_ locale: Locale) {
        synthesizer.stopSpeaking(at: .immediate)
        let utterance = AVSpeechUtterance(string: "")
        utterance.voice = AVSpeechSynthesisVoice(language: locale.identifier)
        synthesizer.speak(utterance)
    }
    
    @MainActor
    func updateWebViewContent() {
        let jsCode = """
            document.body.style.fontSize = '\(preferences.fontSize)px';
            document.body.style.lineHeight = '\(preferences.lineHeight)';
            document.body.style.backgroundColor = '\(preferences.backgroundColor.description)';
            document.body.style.color = '\(preferences.textColor.description)';
            document.body.style.fontFamily = '\(preferences.fontFamily)';
            document.body.style.textAlign = '\(preferences.textAlignment)';
            document.body.style.padding = '\(preferences.marginSize)px';
        """
        // Execute JavaScript in WebView
    }
    
    @MainActor
    func updatePreferences(_ newPreferences: ReadingPreferences) {
        let oldSourceLang = preferences.sourceLanguage
        let oldTargetLang = preferences.targetLanguage
        
        // Handle voice commands
        if newPreferences.enableVoiceCommands != preferences.enableVoiceCommands {
            if newPreferences.enableVoiceCommands {
                setupVoiceCommands()
            } else {
                voiceCommandHandler.stopListening()
            }
        }
        
        // Update preferences
        self.preferences = newPreferences
        
        // Handle language changes
        if oldSourceLang != newPreferences.sourceLanguage || 
           oldTargetLang != newPreferences.targetLanguage {
            Task {
                resetTranslation()
                if newPreferences.targetLanguage != newPreferences.sourceLanguage {
                    await translateCurrentPage()
                }
            }
        }
        
        updateWebViewContent()
    }
    
    @MainActor
    func translateCurrentPage() async {
        guard preferences.targetLanguage != preferences.sourceLanguage else { return }
        
        do {
            // Save original content if not cached
            if pageStates[currentPage] == nil {
                pageStates[currentPage] = pages[currentPage]
            }
            
            updateTranslationState(.translating(progress: 0.1))
            
            // Check if page was already translated
            if translatedPages.contains(currentPage) {
                print("ℹ️ Page \(currentPage + 1) already translated")
                updateTranslationState(.idle)
                return
            }
            
            let translated = try await translationManager.translateText(
                pages[currentPage],
                from: preferences.sourceLanguage,
                to: preferences.targetLanguage
            ) { [weak self] progress in
                self?.updateTranslationState(.translating(progress: progress))
            }
            
            pages[currentPage] = translated
            translatedPages.insert(currentPage)
            hasTranslatedContent = true
            
            // Start background translations if needed
            if preferences.preloadNextPage {
                Task {
                    await translateUpcomingPages()
                }
            }
            
            updateTranslationState(.idle)
            
        } catch {
            updateTranslationState(.error(error.localizedDescription))
        }
    }
    
    @MainActor
    private func translateUpcomingPages() async {
        updateTranslationState(.background)
        
        let pagesToPreload = min(preferences.maxCachedPages, pages.count - currentPage - 1)
        for offset in 1...pagesToPreload where !Task.isCancelled {
            let pageIndex = currentPage + offset
            
            // Skip if already translated or pending
            guard !translatedPages.contains(pageIndex) && 
                  !pendingTranslations.contains(pageIndex) else { continue }
            
            pendingTranslations.insert(pageIndex)
            
            do {
                // Cache original content
                pageStates[pageIndex] = pages[pageIndex]
                
                let translated = try await translationManager.translateText(
                    pages[pageIndex],
                    from: preferences.sourceLanguage,
                    to: preferences.targetLanguage
                ) { _ in }
                
                pages[pageIndex] = translated
                translatedPages.insert(pageIndex)
                pendingTranslations.remove(pageIndex)
                print("✅ Background translated page \(pageIndex + 1)")
            } catch {
                pendingTranslations.remove(pageIndex)
                print("❌ Failed to translate page \(pageIndex + 1): \(error.localizedDescription)")
            }
        }
        
        updateTranslationState(.idle)
    }
    
    @MainActor
    func resetTranslation() {
        // Restore original content
        for (page, originalContent) in pageStates {
            pages[page] = originalContent
        }
        translatedPages.removeAll()
        pendingTranslations.removeAll()
        pageStates.removeAll()
        hasTranslatedContent = false
        updateTranslationState(.idle)
    }
    
    private func updateViewState(_ state: ViewState) {
        Task { @MainActor in
            self.viewState = state
   //         self.isLoading = (state == .loading)
            if case .error(let message) = state {
                self.error = message
            } else {
                self.error = nil
            }
        }
    }
    
    private func updateTranslationState(_ state: TranslationState) {
        Task { @MainActor in
            self.translationState = state
            if case .translating(let progress) = state {
                self.translationProgress = progress
            } else {
                self.translationProgress = 0
            }
        }
    }
    
    func toggleImmersiveMode() {
        isImmersiveModeEnabled.toggle()
    }
    
    func toggleAutoScroll() {
        toggleAutoScroll(enabled: !preferences.autoScroll, speed: preferences.autoScrollSpeed)
    }
    
    // Handle cleanup
    deinit {
        backgroundTranslationTask?.cancel()
        stopSpeaking()
        voiceCommandHandler.stopListening()
        timer?.invalidate()
        autoScrollTimer?.invalidate()
    }

    // MARK: - AVSpeechSynthesizerDelegate
    func speechSynthesizer(_ synthesizer: AVSpeechSynthesizer, didFinish utterance: AVSpeechUtterance) {
        Task { @MainActor in
            isSpeaking = false
            currentHighlightedWord = nil
            
            // Automatically move to next page if at the end
            if preferences.autoScroll && currentPage < pages.count - 1 {
                moveToNextPage()
                startSpeaking(pages[currentPage])
            }
        }
    }
    
    func speechSynthesizer(_ synthesizer: AVSpeechSynthesizer, didPause utterance: AVSpeechUtterance) {
        Task { @MainActor in
            isSpeaking = false
        }
    }
    
    func speechSynthesizer(_ synthesizer: AVSpeechSynthesizer, didContinue utterance: AVSpeechUtterance) {
        Task { @MainActor in
            isSpeaking = true
        }
    }
    
    func speechSynthesizer(_ synthesizer: AVSpeechSynthesizer, didCancel utterance: AVSpeechUtterance) {
        Task { @MainActor in
            isSpeaking = false
            currentHighlightedWord = nil
        }
    }

    func speechSynthesizer(_ synthesizer: AVSpeechSynthesizer, willSpeakRangeOfSpeechString characterRange: NSRange, utterance: AVSpeechUtterance) {
        Task { @MainActor in
            self.highlightedWordRange = characterRange
        }
    }
}
