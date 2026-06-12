import Foundation
import AVFoundation
import Combine

class AudioStreamViewModel: ObservableObject {
    @Published var isPlaying = false
    @Published var isLoading = false
    @Published var error: String?
    @Published var currentTime: CGFloat = 0
    @Published var duration: CGFloat = 0
    @Published var progress: CGFloat = 0

    private var player: AVPlayer?
    private var timeObserver: Any?
    private var cancellables = Set<AnyCancellable>()
    
    private let baseURL = "https://libraryapp-nest-back.vercel.app"
    private let skipInterval: Double = 15
    
    func streamAudioBook(title: String) {
        isLoading = true
        error = nil
        
        guard let encodedTitle = title.addingPercentEncoding(withAllowedCharacters: .urlPathAllowed),
              let url = URL(string: "\(baseURL)/book/tts/stream/\(encodedTitle)") else {
            error = "Invalid URL"
            isLoading = false
            return
        }
        
        let playerItem = AVPlayerItem(url: url)
        player = AVPlayer(playerItem: playerItem)
        
        // Observe player status
        player?.currentItem?.publisher(for: \.status)
            .receive(on: DispatchQueue.main)
            .sink { [weak self] status in
                self?.isLoading = false
                switch status {
                case .readyToPlay:
                    self?.setupTimeObserver()
                    self?.player?.play()
                    self?.isPlaying = true
                    self?.duration = CGFloat(self?.player?.currentItem?.duration.seconds ?? 0)
                case .failed:
                    self?.error = "Failed to load audio"
                    self?.isPlaying = false
                default:
                    break
                }
            }
            .store(in: &cancellables)
    }
    
    private func setupTimeObserver() {
        let interval = CMTime(seconds: 0.5, preferredTimescale: CMTimeScale(NSEC_PER_SEC))
        timeObserver = player?.addPeriodicTimeObserver(forInterval: interval, queue: .main) { [weak self] time in
            guard let self = self else { return }
            
            let currentTimeValue = time.seconds
            let durationValue = self.player?.currentItem?.duration.seconds ?? 0
            
            // Validate that the values are finite numbers
            guard currentTimeValue.isFinite, durationValue.isFinite else { return }
            
            self.currentTime = CGFloat(currentTimeValue)
            self.duration = CGFloat(durationValue)
            
            // Calculate progress only if duration is valid and greater than 0
            if self.duration > 0 {
                self.progress = self.currentTime / self.duration
            } else {
                self.progress = 0
            }
        }
    }
    
    func togglePlayback() {
        if isPlaying {
            player?.pause()
        } else {
            player?.play()
        }
        isPlaying.toggle()
    }
    
    func skipForward() {
        guard let player = player else { return }
        let newTime = player.currentTime() + CMTime(seconds: skipInterval, preferredTimescale: 1)
        player.seek(to: newTime)
    }
    
    func skipBackward() {
        guard let player = player else { return }
        let newTime = player.currentTime() - CMTime(seconds: skipInterval, preferredTimescale: 1)
        player.seek(to: newTime)
    }
    
    func stop() {
        if let timeObserver = timeObserver {
            player?.removeTimeObserver(timeObserver)
        }
        player?.pause()
        player = nil
        isPlaying = false
        currentTime = 0
        duration = 0
        progress = 0
        timeObserver = nil
    }
    
    deinit {
        if let timeObserver = timeObserver {
            player?.removeTimeObserver(timeObserver)
        }
    }
}