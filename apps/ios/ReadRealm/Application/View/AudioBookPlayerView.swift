import SwiftUI

struct AudioBookPlayerView: View {
    @StateObject private var viewModel = AudioStreamViewModel()
    var book: Book
    
    var body: some View {
        NavigationView {
            ZStack {
                Color.black.ignoresSafeArea()
                
                if viewModel.isLoading {
                    ProgressView()
                        .foregroundColor(.orange)
                } else {
                    VStack {
                        // Header Section
                        HStack {
                            NavigationLink(destination: BookDetailsView(book: book)){
                                Image(systemName: "chevron.left")
                                    .foregroundColor(.orange)
                                    .padding(.leading)
                            }
                            
                            //Spacer()
                            Text("Now Playing")
                                .foregroundColor(.orange)
                                .font(.headline)
                            Spacer()
                            
                            Text("1x")
                                .foregroundColor(.orange)
                                .font(.headline)
                            
                            Image(systemName: "line.horizontal.3")
                                .foregroundColor(.orange)
                                .padding(.trailing)
                        }
                        .padding(.top)
                        Spacer ()
                        
                        // Book cover
                        // Image(book.coverImage ?? "")
                        //     .resizable()
                        //     .aspectRatio(contentMode: .fill)
                        //     .frame(width: 300, height: 350)
                        //     .cornerRadius(12)
                        //     .padding(.vertical, 16)
                        AsyncImage(url: URL(string: book.coverImage!)) { image in
                    image
                        .resizable()
                        .cornerRadius(8)
            } placeholder: {
                Color.gray
            }
            .aspectRatio(contentMode: .fill)
            .frame(width: 300, height: 250)
                        Spacer()
                        // Title
                        VStack(spacing: 4) {
                            Text(book.title ?? "")
                                .font(.title3)
                                .bold()
                                .foregroundColor(.white)
                        }
                        .padding(.bottom, 12)
                        
                        // Progress section
                        VStack(spacing: 12) {
                            HStack {
                                Text(timeString(from: viewModel.currentTime))
                                    .font(.caption)
                                    .foregroundColor(.gray)
                                Spacer()
                                Text(timeString(from: viewModel.duration))
                                    .font(.caption)
                                    .foregroundColor(.gray)
                            }
                            
                            ZStack(alignment: .leading) {
                                Capsule()
                                    .fill(Color.gray.opacity(0.6))
                                    .frame(height: 4)
                                
                                Capsule()
                                    .fill(Color.orange)
                                    .frame(width: viewModel.progress * 200, height: 4)
                            }
                        }
                        .padding(.bottom, 16)
                        
                        // Controls
                        HStack(spacing: 32) {
                            Button(action: viewModel.skipBackward) {
                                Image(systemName: "backward.fill")
                                    .font(.title2)
                                    .foregroundColor(.orange)
                            }
                            
                            Button(action: viewModel.togglePlayback) {
                                Image(systemName: viewModel.isPlaying ? "pause.circle.fill" : "play.circle.fill")
                                    .font(.system(size: 50))
                                    .foregroundColor(.orange)
                            }
                            
                            Button(action: viewModel.skipForward) {
                                Image(systemName: "forward.fill")
                                    .font(.title2)
                                    .foregroundColor(.orange)
                            }
                        }
                        .padding(.bottom, 16)
                        
                        if let error = viewModel.error {
                            Text(error)
                                .foregroundColor(.red)
                                .font(.caption)
                        }
                    }
                    .padding()
                    .background(Color.black.opacity(0.8))
                    .cornerRadius(16)
                    .shadow(radius: 10)
                }
            }
        }
        .onAppear {
            viewModel.streamAudioBook(title: book.title ?? "")
        }
        .onDisappear {
            viewModel.stop()
        }
    }
    
    func timeString(from seconds: CGFloat) -> String {
        guard seconds.isFinite else { return "00:00" }
        let minutes = Int(seconds) / 60
        let remainingSeconds = Int(seconds) % 60
        return String(format: "%02d:%02d", minutes, remainingSeconds)
    }
}
