import Speech
import AVFoundation

enum VoiceCommand {
    case start
    case stop
    case next
    case previous
}

enum VoiceCommandError: Error {
    case notAuthorized
    case noRecognizer
    case audioEngineFailed
}

class VoiceCommandHandler: NSObject, SFSpeechRecognizerDelegate {
    private var speechRecognizer: SFSpeechRecognizer?
    private var recognitionRequest: SFSpeechAudioBufferRecognitionRequest?
    private var recognitionTask: SFSpeechRecognitionTask?
    private let audioEngine = AVAudioEngine()
    private var commandHandler: ((VoiceCommand) -> Void)?
    
    @Published var isListening: Bool = false
    @Published var error: VoiceCommandError?
    
    override init() {
        super.init()
        setupSpeechRecognizer()
    }
    
    private func setupSpeechRecognizer() {
        speechRecognizer = SFSpeechRecognizer(locale: Locale(identifier: "en-US"))
        speechRecognizer?.delegate = self
    }
    
    func startListening(commandHandler: @escaping (VoiceCommand) -> Void) {
        guard let speechRecognizer = speechRecognizer else {
            error = .noRecognizer
            return
        }

        let audioSession = AVAudioSession.sharedInstance()
        do {
            try audioSession.setCategory(.record, mode: .measurement, options: .duckOthers)
            try audioSession.setActive(true, options: .notifyOthersOnDeactivation)
        } catch {
            print("Failed to set up audio session: \(error)")
            self.error = .audioEngineFailed
            return
        }

        self.commandHandler = commandHandler
        recognitionRequest = SFSpeechAudioBufferRecognitionRequest()
        
        guard let recognitionRequest = recognitionRequest else { return }
        recognitionRequest.shouldReportPartialResults = true

        recognitionTask = speechRecognizer.recognitionTask(with: recognitionRequest) { [weak self] result, error in
            guard let self = self else { return }
            if let error = error {
                print("Recognition error: \(error)")
                self.stopListening()
                return
            }
            
            if let result = result {
                self.processCommand(result.bestTranscription.formattedString)
            }
        }

        let inputNode = audioEngine.inputNode
        let recordingFormat = inputNode.outputFormat(forBus: 0)
        
        inputNode.installTap(onBus: 0, bufferSize: 1024, format: recordingFormat) { [weak self] buffer, _ in
            self?.recognitionRequest?.append(buffer)
        }

        audioEngine.prepare()
        do {
            try audioEngine.start()
            isListening = true
        } catch {
            print("Could not start audio engine: \(error)")
            self.error = .audioEngineFailed
            stopListening()
        }
    }
    
    private func startRecognition() async throws {
        // Ensure we're on the main thread for audio setup
        await MainActor.run {
            // Cancel any existing task
            recognitionTask?.cancel()
            recognitionTask = nil
            
            // Stop any existing audio
            audioEngine.stop()
            audioEngine.inputNode.removeTap(onBus: 0)
        }
        
        // Configure audio session
        let audioSession = AVAudioSession.sharedInstance()
        try await audioSession.setCategory(.record, mode: .measurement, options: .duckOthers)
        try await audioSession.setActive(true, options: .notifyOthersOnDeactivation)
        
        let recognitionRequest = SFSpeechAudioBufferRecognitionRequest()
        self.recognitionRequest = recognitionRequest
        recognitionRequest.shouldReportPartialResults = true
        
        // Configure audio engine input
        let inputNode = audioEngine.inputNode
        let recordingFormat = inputNode.outputFormat(forBus: 0)
        
        inputNode.installTap(onBus: 0, bufferSize: 1024, format: recordingFormat) { [weak self] buffer, _ in
            self?.recognitionRequest?.append(buffer)
        }
        
        audioEngine.prepare()
        try audioEngine.start()
        
        // Start recognition
        try await withCheckedThrowingContinuation { (continuation: CheckedContinuation<Void, Error>) in
            recognitionTask = speechRecognizer?.recognitionTask(with: recognitionRequest) { [weak self] result, error in
                guard let self = self else { return }
                
                if let error = error {
                    print("Recognition error: \(error.localizedDescription)")
                    self.stopListening()
                    return
                }
                
                if let result = result {
                    self.processCommand(result.bestTranscription.formattedString)
                }
                
                if result?.isFinal == true {
                    continuation.resume()
                }
            }
            
            // Set listening state
            DispatchQueue.main.async {
                self.isListening = true
            }
        }
    }
    
    private func processCommand(_ text: String) {
        let command = text.lowercased()
        if command.contains("start") {
            commandHandler?(.start)
        } else if command.contains("stop") {
            commandHandler?(.stop)
        } else if command.contains("next") {
            commandHandler?(.next)
        } else if command.contains("previous") {
            commandHandler?(.previous)
        }
    }
    
    func stopListening() {
        audioEngine.stop()
        audioEngine.inputNode.removeTap(onBus: 0)
        recognitionRequest?.endAudio()
        recognitionTask?.cancel()
        
        // Deactivate audio session
        do {
            try? AVAudioSession.sharedInstance().setActive(false, options: .notifyOthersOnDeactivation)
        } catch {
            print("❌ Audio session configuration failed: \(error)")
            return
        }
        
        isListening = false
    }
    
    // Add proper cleanup
    deinit {
        stopListening()
    }
}
