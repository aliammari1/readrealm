//
//  EpubReader.swift
//  Application
//
//  Created by Mac2021 on 3/1/2025.
//

import Foundation
import SwiftUI
import WebKit

struct EpubReader: View {
    @StateObject private var viewModel = EpubReaderViewModel()
    @State private var showSettings = false
    @State private var showThemes = false
    @State private var showStats = false
    @State private var showBookmarks = false
    @State private var isPdfExporting = false
    @State private var showLanguageMenu = false
    
    let urlString: String
    
    var body: some View {
        NavigationView {
            ZStack {
                if viewModel.isLoading {
                    ProgressView()
                } else if let error = viewModel.error {
                    Text("Error: \(error)")
                } else {
                    ReaderContent(viewModel: viewModel)
                }
                
                VStack {
                    Spacer()
                    ImmersiveControls(viewModel: viewModel)
                        .padding(.bottom)
                }
            }
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarLeading) {
                    HStack {
                        Button(action: { showLanguageMenu.toggle() }) {
                            Image(systemName: "globe")
                        }
                        
                        Button(action: { showBookmarks.toggle() }) {
                            Image(systemName: "bookmark")
                        }
                    }
                }
                
                ToolbarItem(placement: .navigationBarTrailing) {
                    HStack {
                        Button(action: { viewModel.isSpeaking ? viewModel.stopSpeaking() : viewModel.startSpeaking(viewModel.pages[viewModel.currentPage]) }) {
                            Image(systemName: viewModel.isSpeaking ? "pause.fill" : "play.fill")
                        }
                        
                        Button(action: exportToPDF) {
                            Image(systemName: "arrow.down.doc")
                        }
                        .disabled(isPdfExporting)
                        
                        Button(action: { showSettings.toggle() }) {
                            Image(systemName: "gear")
                        }
                    }
                }
            }
            .sheet(isPresented: $showSettings) {
                SettingsView(preferences: $viewModel.preferences)
            }
            .sheet(isPresented: $showBookmarks) {
                NavigationView {
                    BookmarkListView(bookmarks: viewModel.bookmarks) { page in
                        viewModel.currentPage = page
                        showBookmarks = false
                    }
                    .navigationTitle("Bookmarks")
                    .navigationBarItems(trailing: Button("Done") { showBookmarks = false })
                }
            }
            .sheet(isPresented: $showLanguageMenu) {
                LanguageMenuView(
                    currentLanguage: viewModel.preferences.targetLanguage,
                    onLanguageSelect: { language in
                        viewModel.preferences.targetLanguage = language
                        showLanguageMenu = false
                    }
                )
            }
        }
        .task {
            if let url = URL(string: urlString) {
                await viewModel.loadEpub(from: url)
            }
        }
        .onAppear {
            viewModel.startSession()
        }
        .onDisappear {
            viewModel.endSession()
        }
    }
    
    private func exportToPDF() {
        isPdfExporting = true
        Task {
            do {
                let url = try await viewModel.exportToPDF()
                await MainActor.run {
                    isPdfExporting = false
                    // Show share sheet
                    let activityVC = UIActivityViewController(
                        activityItems: [url],
                        applicationActivities: nil
                    )
                    if let windowScene = UIApplication.shared.connectedScenes.first as? UIWindowScene,
                       let window = windowScene.windows.first,
                       let rootVC = window.rootViewController {
                        rootVC.present(activityVC, animated: true)
                    }
                }
            } catch {
                await MainActor.run {
                    isPdfExporting = false
                    viewModel.error = error.localizedDescription
                }
            }
        }
    }
    
    struct ReaderWebView: View {
        let urlString: String
        
        var body: some View {
            if let url = URL(string: urlString) {
                AsyncView(url: url)
            } else {
                Text("Invalid URL")
            }
        }
    }
    
    struct AsyncView: View {
        let url: URL
        @State private var content: String = ""
        @State private var isLoading = true
        
        var body: some View {
            Group {
                if isLoading {
                    ProgressView()
                } else {
                    ScrollView {
                        Text(content)
                            .padding()
                    }
                }
            }
            .task {
                do {
                    let (data, _) = try await URLSession.shared.data(from: url)
                    if let text = String(data: data, encoding: .utf8) {
                        content = text
                    }
                    isLoading = false
                } catch {
                    content = "Error loading content"
                    isLoading = false
                }
            }
        }
    }
}

struct ReaderContent: View {
    @ObservedObject var viewModel: EpubReaderViewModel
    @Environment(\.colorScheme) var colorScheme
    @State private var showControls = true
    @State private var lastTapTime = Date()
    
    var body: some View {
        GeometryReader { geometry in
            ZStack {
                // Background layer
                viewModel.preferences.backgroundColor
                    .opacity(viewModel.isImmersiveModeEnabled ? 0.95 : 1.0)
                    .ignoresSafeArea()
                
                VStack(spacing: 0) {
                    // Translation Progress Bar at top with higher z-index
                    ZStack {
                        if viewModel.translationProgress > 0 {
                            GlobalProgressBar(
                                progress: viewModel.translationProgress,
                                text: "Translating page \(viewModel.currentPage + 1)...",
                                accentColor: viewModel.preferences.accentColor
                            )
                            .transition(.move(edge: .top))
                            .zIndex(1)
                        }
                    }
                    .animation(.easeInOut, value: viewModel.translationProgress > 0)
                    
                    // Content Area
                    ScrollView {
                        webViewContent
                            .frame(minHeight: geometry.size.height)
                    }
                    // ...rest of existing content...
                }
                
                // Controls overlay
                if showControls {
                    VStack {
                        // Top controls
                        HStack {
                            if viewModel.preferences.showReadingStats {
                                ReadingStatsView(statistics: viewModel.statistics)
                                    .transition(.move(edge: .top))
                            }
                        }
                        .padding(.top)
                        
                        Spacer()
                        
                        // Bottom controls
                        VStack(spacing: 15) {
                            // Translation progress
                            if viewModel.translationProgress > 0 && viewModel.translationProgress < 1 {
                                TranslationProgressView(progress: viewModel.translationProgress)
                            }
                            
                            // Navigation controls
                            HStack {
                                PageButton(action: viewModel.moveToPreviousPage,
                                         icon: "chevron.left",
                                         isEnabled: viewModel.currentPage > 0)
                                
                                Spacer()
                                
                                PageCounter(current: viewModel.currentPage + 1,
                                          total: viewModel.pages.count)
                                
                                Spacer()
                                
                                PageButton(action: viewModel.moveToNextPage,
                                         icon: "chevron.right",
                                         isEnabled: viewModel.currentPage < viewModel.pages.count - 1)
                            }
                            .padding(.horizontal)
                        }
                        .padding(.bottom)
                        .background(
                            Rectangle()
                                .fill(viewModel.preferences.backgroundColor)
                                .opacity(0.8)
                                .blur(radius: 10)
                        )
                    }
                }
                
                // Add background translation indicator
                if viewModel.isTranslatingInBackground {
                    VStack {
                        HStack {
                            Spacer()
                            Label("Translating upcoming pages...", systemImage: "globe")
                                .font(.caption)
                                .foregroundColor(.secondary)
                                .padding(8)
                                .background(.ultraThinMaterial)
                                .cornerRadius(8)
                                .padding()
                        }
                        Spacer()
                    }
                }
            }
        }
        .gesture(
            TapGesture()
                .onEnded { _ in
                    withAnimation(.easeInOut(duration: 0.3)) {
                        showControls.toggle()
                    }
                }
        )
        .gesture(pageSwipeGesture)
    }
    
    private var webViewContent: some View {
        WebView(content: viewModel.pages[viewModel.currentPage],
               preferences: viewModel.preferences,
               highlightedRange: viewModel.highlightedWordRange)
            .frame(maxWidth: .infinity, maxHeight: .infinity)
            .if(viewModel.preferences.enablePageTransitions) { view in
                view.transition(.slide)
            }
            .overlay(
                Group {
                    if viewModel.preferences.showTranslationPreview && viewModel.translationProgress > 0 {
                        TranslationPreviewOverlay(progress: viewModel.translationProgress)
                    }
                }
            )
    }
    
    private var pageSwipeGesture: some Gesture {
        DragGesture(minimumDistance: 20)
            .onEnded { gesture in
                guard viewModel.preferences.enableGestures else { return }
                if gesture.translation.width > 50 {
                    viewModel.moveToPreviousPage()
                } else if gesture.translation.width < -50 {
                    viewModel.moveToNextPage()
                }
            }
    }
}

// New supporting views
struct TranslationProgressView: View {
    let progress: Double
    
    var body: some View {
        VStack(spacing: 4) {
            ProgressView(value: progress)
                .progressViewStyle(.linear)
                .tint(.blue)
            Text("Translating... \(Int(progress * 100))%")
                .font(.caption2)
                .foregroundColor(.secondary)
        }
        .padding(.horizontal)
        .padding(.vertical, 8)
        .background(.ultraThinMaterial)
        .cornerRadius(8)
    }
}

struct PageButton: View {
    let action: () -> Void
    let icon: String
    let isEnabled: Bool
    
    var body: some View {
        Button(action: action) {
            Image(systemName: icon)
                .imageScale(.large)
                .frame(width: 44, height: 44)
                .background(
                    Circle()
                        .fill(.ultraThinMaterial)
                )
                .contentShape(Circle())
        }
        .disabled(!isEnabled)
        .opacity(isEnabled ? 1.0 : 0.3)
    }
}

struct PageCounter: View {
    let current: Int
    let total: Int
    
    var body: some View {
        Text("\(current)/\(total)")
            .font(.system(.caption, design: .rounded))
            .padding(.horizontal, 12)
            .padding(.vertical, 6)
            .background(.ultraThinMaterial)
            .cornerRadius(12)
    }
}

// Update ReadingStatsView
struct ReadingStatsView: View {
    let statistics: ReadingStatistics
    
    var body: some View {
        HStack(spacing: 16) {
            StatItem(value: Int(statistics.timeSpentReading / 60), unit: "min", icon: "clock")
            StatItem(value: statistics.pagesRead, unit: "pages", icon: "book")
            StatItem(value: Int(statistics.averageReadingSpeed), unit: "wpm", icon: "speedometer")
        }
        .padding(8)
        .background(.ultraThinMaterial)
        .cornerRadius(16)
        .padding(.horizontal)
    }
}

struct StatItem: View {
    let value: Int
    let unit: String
    let icon: String
    
    var body: some View {
        HStack(spacing: 4) {
            Image(systemName: icon)
                .imageScale(.small)
                .foregroundColor(.secondary)
            Text("\(value)")
                .bold()
            Text(unit)
                .foregroundColor(.secondary)
                .font(.caption2)
        }
    }
}

struct SettingsView: View {
    @Binding var preferences: ReadingPreferences
    @Environment(\.dismiss) var dismiss
    
    var body: some View {
        NavigationView {
            Form {
                Section("Text Appearance") {
                    Slider(value: $preferences.fontSize, in: 12...24, step: 1) {
                        Text("Font Size: \(Int(preferences.fontSize))")
                    }
                    
                    Slider(value: $preferences.lineHeight, in: 1...2, step: 0.1) {
                        Text("Line Height: \(preferences.lineHeight, specifier: "%.1f")")
                    }
                    
                    Slider(value: $preferences.letterSpacing, in: 0...2, step: 0.1) {
                        Text("Letter Spacing: \(preferences.letterSpacing, specifier: "%.1f")")
                    }
                    
                    Picker("Text Alignment", selection: $preferences.textAlignment) {
                        Text("Left").tag("left")
                        Text("Center").tag("center")
                        Text("Right").tag("right")
                        Text("Justify").tag("justify")
                    }
                    
                    Picker("Font Family", selection: $preferences.fontFamily) {
                        Text("System").tag("-apple-system")
                        Text("Serif").tag("Georgia")
                        Text("Sans-serif").tag("Helvetica")
                        Text("Monospace").tag("Courier")
                    }
                }
                
                Section("Layout") {
                    Picker("Layout Mode", selection: $preferences.layoutMode) {
                        ForEach(ReadingPreferences.LayoutMode.allCases, id: \.self) { mode in
                            Text(mode.rawValue.capitalized).tag(mode)
                        }
                    }
                    
                    Slider(value: $preferences.marginSize, in: 10...40, step: 2) {
                        Text("Margin Size: \(Int(preferences.marginSize))")
                    }
                    
                    Slider(value: $preferences.paragraphSpacing, in: 1...2, step: 0.1) {
                        Text("Paragraph Spacing: \(preferences.paragraphSpacing, specifier: "%.1f")")
                    }
                }
                
                Section("Theme") {
                    Toggle("Dark Mode", isOn: $preferences.isDarkMode)
                    ColorPicker("Background Color", selection: $preferences.backgroundColor)
                    ColorPicker("Text Color", selection: $preferences.textColor)
                    ColorPicker("Accent Color", selection: $preferences.accentColor)
                }
                
                Section("Features") {
                    Toggle("Show Reading Stats", isOn: $preferences.showReadingStats)
                    Toggle("Enable Gestures", isOn: $preferences.enableGestures)
                    Toggle("Page Transitions", isOn: $preferences.enablePageTransitions)
                    Toggle("Preload Next Page", isOn: $preferences.preloadNextPage)
                    
                    Picker("Max Cached Pages", selection: $preferences.maxCachedPages) {
                        ForEach([3, 5, 10, 15], id: \.self) { num in
                            Text("\(num) pages").tag(num)
                        }
                    }
                }
                
                Section("Translation") {
                    Toggle("Auto-detect Language", isOn: $preferences.autoDetectLanguage)
                    Toggle("Show Translation Preview", isOn: $preferences.showTranslationPreview)
                    Toggle("Highlight Translated Words", isOn: $preferences.highlightTranslatedWords)
                }
                
                Section("Auto-Scroll") {
                    Toggle("Enable", isOn: $preferences.autoScroll)
                    if preferences.autoScroll {
                        Slider(value: $preferences.autoScrollSpeed, in: 0.5...3) {
                            Text("Speed: \(preferences.autoScrollSpeed, specifier: "%.1f")x")
                        }
                    }
                }
                
                Section("Accessibility") {
                    Toggle("Text-to-Speech", isOn: $preferences.enableTextToSpeech)
                    if preferences.enableTextToSpeech {
                        Slider(value: $preferences.speakingRate, in: 0.1...1.0) {
                            Text("Speaking Rate: \(preferences.speakingRate, specifier: "%.1f")x")
                        }
                    }
                    Toggle("Voice Commands", isOn: $preferences.enableVoiceCommands)
                }
            }
            .navigationTitle("Settings")
            .navigationBarItems(trailing: Button("Done") { dismiss() })
        }
    }
}

struct BookmarkListView: View {
    let bookmarks: [BookmarkPage]
    let onBookmarkSelected: (Int) -> Void
    
    var body: some View {
        List(bookmarks) { bookmark in
            Button(action: { onBookmarkSelected(bookmark.pageNumber) }) {
                VStack(alignment: .leading) {
                    Text("Page \(bookmark.pageNumber + 1)")
                        .font(.headline)
                    Text(bookmark.snippet)
                        .font(.subheadline)
                        .lineLimit(2)
                    Text(bookmark.timestamp, style: .date)
                        .font(.caption)
                }
            }
        }
    }
}

struct ThemeSelectorView: View {
    @Binding var preferences: ReadingPreferences
    
    var body: some View {
        List {
            ForEach(ReaderThemes.themes, id: \.name) { theme in
                Button(action: {
                    preferences.backgroundColor = theme.backgroundColor
                    preferences.textColor = theme.textColor
                }) {
                    HStack {
                        Text(theme.name)
                        Spacer()
                        if preferences.backgroundColor == theme.backgroundColor {
                            Image(systemName: "checkmark")
                        }
                    }
                }
            }
        }
    }
}

struct LanguageMenuView: View {
    let currentLanguage: String
    let onLanguageSelect: (String) -> Void
    
    private let languages = [
        ("en", "English"),
        ("fr", "French"),
        ("es", "Spanish"),
        ("de", "German"),
        ("it", "Italian"),
        ("ar", "Arabic")
    ]
    
    var body: some View {
        NavigationView {
            List {
                ForEach(languages, id: \.0) { code, name in
                    Button(action: { onLanguageSelect(code) }) {
                        HStack {
                            Text(name)
                            Spacer()
                            if code == currentLanguage {
                                Image(systemName: "checkmark")
                            }
                        }
                    }
                }
            }
            .navigationTitle("Select Language")
        }
    }
}

struct ImmersiveControls: View {
    @ObservedObject var viewModel: EpubReaderViewModel
    @Environment(\.colorScheme) var colorScheme
    
    var body: some View {
        HStack {
            Button(action: { viewModel.toggleImmersiveMode() }) {
                Image(systemName: viewModel.isImmersiveModeEnabled ? "eye.slash" : "eye")
            }
            
            Button(action: { viewModel.toggleAutoScroll() }) {
                Image(systemName: viewModel.preferences.autoScroll ? "pause.circle" : "play.circle")
            }
            
            if viewModel.currentOperation.isEmpty == false {
                Text(viewModel.currentOperation)
                    .font(.caption)
                    .foregroundColor(.secondary)
            }
            
            if viewModel.loadingProgress > 0 && viewModel.loadingProgress < 1 {
                ProgressView(value: viewModel.loadingProgress)
                    .frame(width: 100)
            }
        }
        .padding(.horizontal)
    }
}

struct TranslationPreviewOverlay: View {
    let progress: Double
    
    var body: some View {
        VStack {
            Spacer()
            HStack {
                Spacer()
                TranslationProgressView(progress: progress)
                    .padding()
            }
        }
    }
}

// Add View extension for conditional modifiers
extension View {
    @ViewBuilder
    func `if`<Transform: View>(_ condition: Bool, transform: (Self) -> Transform) -> some View {
        if condition {
            transform(self)
        } else {
            self
        }
    }
}

// Add new view for global progress bar
struct GlobalProgressBar: View {
    let progress: Double
    let text: String
    let accentColor: Color
    
    var body: some View {
        VStack(spacing: 2) {
            GeometryReader { geometry in
                Rectangle()
                    .fill(accentColor)
                    .frame(width: geometry.size.width * progress)
                    .animation(.linear(duration: 0.2), value: progress)
            }
            .frame(height: 3)
            
            Text(text)
                .font(.caption2)
                .foregroundColor(.secondary)
                .padding(.bottom, 4)
        }
        .background(.ultraThinMaterial)
    }
}

