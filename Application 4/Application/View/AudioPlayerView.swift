import SwiftUI

struct AudioPlayerView: View {
    @StateObject private var viewModel = AudioStreamViewModel()
    let bookTitle: String
    
    var body: some View {
        VStack(spacing: 20) {
            Text(bookTitle)
                .font(.title2)
            
            if viewModel.isLoading {
                ProgressView()
            } else {
                Button(action: viewModel.togglePlayback) {
                    Image(systemName: viewModel.isPlaying ? "pause.circle.fill" : "play.circle.fill")
                        .resizable()
                        .frame(width: 64, height: 64)
                        .foregroundColor(.blue)
                }
            }
            
            if let error = viewModel.error {
                Text(error)
                    .foregroundColor(.red)
                    .font(.caption)
            }
        }
        .padding()
        .onAppear {
            viewModel.streamAudioBook(title: bookTitle)
        }
        .onDisappear {
            viewModel.stop()
        }
    }
}


struct MBooksView_Previews: PreviewProvider {
    static var previews: some View {
        AudioPlayerView(bookTitle: "Romeo and Juliet")
    }
}
