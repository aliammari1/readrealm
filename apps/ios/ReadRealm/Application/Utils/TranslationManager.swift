import Foundation

class TranslationManager: ObservableObject {
    @Published private(set) var progress: Double = 0
    @Published private(set) var isTranslating: Bool = false
    private let baseURL = "https://translate.googleapis.com/translate_a/single"
    private let maxRetries = 3
    
    enum TranslationError: Error {
        case translationFailed
        case invalidLanguage
        case networkError
        case emptyResponse
    }
    
    @MainActor
    func setProgress(_ value: Double) {
        progress = value
    }
    
    @MainActor
    func setTranslating(_ value: Bool) {
        isTranslating = value
    }
    
    func translateText(_ text: String, from sourceLanguage: String = "auto", to targetLanguage: String, progressHandler: @escaping (Double) -> Void) async throws -> String {
        print("📍 Starting translation from '\(sourceLanguage)' to '\(targetLanguage)'")
        print("📝 Text length: \(text.count) characters")
        
        await setTranslating(true)
        defer { Task { @MainActor in await setTranslating(false) } }
        
        let chunks = text.split(separator: ".").map(String.init)
        var translatedChunks: [String] = []
        
        print("🔄 Processing \(chunks.count) chunks")
        
        for (index, chunk) in chunks.enumerated() {
            if !chunk.trimmingCharacters(in: .whitespaces).isEmpty {
                do {
                    let translated = try await translateChunk(chunk.trimmingCharacters(in: .whitespaces),
                                                           from: sourceLanguage,
                                                           to: targetLanguage)
                    translatedChunks.append(translated)
                    let progress = Double(index + 1) / Double(chunks.count)
                    await MainActor.run {
                        progressHandler(progress)
                    }
                    print("✓ Chunk \(index + 1)/\(chunks.count) translated")
                } catch {
                    print("❌ Failed to translate chunk \(index + 1): \(error.localizedDescription)")
                    throw error
                }
            }
        }
        
        let result = translatedChunks.joined(separator: ". ")
        print("✅ Translation completed successfully")
        return result
    }
    
    private func withRetries<T>(_ operation: () async throws -> T) async throws -> T {
        var lastError: Error?
        
        for attempt in 1...maxRetries {
            do {
                if attempt > 1 {
                    try await Task.sleep(nanoseconds: UInt64(attempt) * 1_000_000_000) // Exponential backoff
                }
                return try await operation()
            } catch {
                lastError = error
                print("❌ Attempt \(attempt) failed: \(error.localizedDescription)")
                continue
            }
        }
        
        throw lastError ?? TranslationError.translationFailed
    }
    
    private func translateChunk(_ text: String, from source: String, to target: String) async throws -> String {
        var components = URLComponents(string: baseURL)
        
        // These parameters mimic a browser request
        components?.queryItems = [
            URLQueryItem(name: "client", value: "gtx"),
            URLQueryItem(name: "sl", value: source),
            URLQueryItem(name: "tl", value: target),
            URLQueryItem(name: "dt", value: "t"),
            URLQueryItem(name: "q", value: text)
        ]
        
        guard let url = components?.url else {
            throw TranslationError.invalidLanguage
        }
        
        print("🌐 Requesting translation from Google: \(url.absoluteString)")
        
        var request = URLRequest(url: url)
        request.setValue("Mozilla/5.0", forHTTPHeaderField: "User-Agent")
        request.setValue("*/*", forHTTPHeaderField: "Accept")
        
        let (data, response) = try await URLSession.shared.data(for: request)
        
        if let httpResponse = response as? HTTPURLResponse {
            print("📡 Status: \(httpResponse.statusCode)")
            guard (200...299).contains(httpResponse.statusCode) else {
                print("❌ HTTP Error: \(httpResponse.statusCode)")
                throw TranslationError.translationFailed
            }
        }
        
        guard let jsonArray = try? JSONSerialization.jsonObject(with: data) as? [Any],
              let translationsArray = jsonArray.first as? [Any],
              let firstTranslation = translationsArray.first as? [Any],
              let translatedText = firstTranslation.first as? String else {
            print("❌ JSON Parse failed")
            print("📄 Raw response: \(String(data: data, encoding: .utf8) ?? "invalid")")
            throw TranslationError.translationFailed
        }
        
        print("✅ Translation received: \(translatedText)")
        return translatedText
    }
}
