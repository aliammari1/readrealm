import SwiftUI

struct BookDetailsView: View {
    @State private var selectedTab: String = "Description"
    @StateObject private var bookViewModel = BookViewModel()
    var book: Book?
    var body: some View {
        NavigationView {
            ZStack {
                LinearGradient(
                    gradient: Gradient(colors: [Color.purple.opacity(0.2), Color.purple.opacity(0.1)]),
                    startPoint: .top,
                    endPoint: .bottom
                )
                .ignoresSafeArea()
        ScrollView {
            VStack(spacing: 16) {
                HStack (spacing: 16){
                    NavigationLink(destination: Homescreen())
                            {
                                Image(systemName: "arrow.left")
                                    .foregroundColor(.purple)
                                    .font(.title)  // Adjust the size of the back icon
                                    .padding(.leading)
                            }
                            
                            Spacer()  // This will push the Book Title to the center
                            Text("Book Details")
                        .foregroundColor(.black)
                                .font(.headline)
                            
                            Spacer()  // This will push the favorite icon to the right
                            
                            // Favorite Icon (Heart)
                            Image(systemName: "heart.fill")  // Or use "heart" for an empty heart
                        .foregroundColor(.purple)
                                .font(.title)  // Adjust the size of the heart icon
                                .padding(.trailing)
                        }
                        .padding(.top)
                        // Book Cover
                        VStack {
                            Image(book?.coverImage ?? "") // Replace with actual asset name
                                .resizable()
                                .frame(width: 100, height: 150)
                                .cornerRadius(8)
                        }
                        // Book Title and Author
                        VStack(spacing: 4) {
                            Text(book?.title ?? "")
                                .font(.title2)
                                .bold()
                                .multilineTextAlignment(.center)
                                .foregroundColor(.black)
                            Text(book?.author ?? "")
                                .font(.subheadline)
                                .foregroundColor(.gray)
                        }
                        // Book Stats
                        HStack(spacing: 60) {
                            BookStatView(value: "4.5", label: "rating")
                            BookStatView(value: "302", label: "pages")
                            BookStatView(value: "1997", label: "published")
                            BookStatView(value: "12.7k", label: "read")
                        }
                        // Progress Bar
                        VStack {
                            ZStack(alignment: .leading) {
                                Rectangle()
                                    .frame(height: 4)
                                    .foregroundColor(Color.gray.opacity(0.6))
                                Rectangle()
                                    .frame(width: 100, height: 4) // 23% of the max width
                                    .foregroundColor(Color.purple)
                            }
                            
                            HStack {
                                Spacer()
                                Text("23%")
                                    .foregroundColor(.gray)
                                    .font(.caption)
                            }
                        }
                        
                        // Action Buttons
                        HStack(spacing: 16) {
                            ActionButton(title: "Read", action: {
                                // Action for "Read" button
                                print("Read button tapped")
                            })
                            
                            // NavigationLink to AudioBookPlayerView
                            NavigationLink(destination: AudioBookPlayerView(book: book!)
                                .navigationBarBackButtonHidden(true)) {
                                Text("Listen")
                                    .font(.headline)
                                    .padding()
                                    .frame(maxWidth: .infinity)
                                    .background(Color.black.opacity(0))
                                    .foregroundColor(Color.purple)
                                    .overlay(
                                        RoundedRectangle(cornerRadius: 8)
                                            .stroke(Color.purple, lineWidth: 2)
                                    )
                                    .cornerRadius(8)
                            }
                        }
                        
                        Button(action: {}) {
                            Text("Download")
                                .font(.callout)
                                .bold()
                                .padding()
                                .frame(maxWidth: .infinity)
                                .background(Color.purple.opacity(0.4))
                                .foregroundColor(.white.opacity(0.7))
                                .cornerRadius(8)
                        }
                        
                        // Tab Navigation
                        HStack(spacing: 16) {
                            TabItem(title: "Description", isSelected: selectedTab == "Description") {
                                selectedTab = "Description"
                                bookViewModel.getBookSummary(title: book?.title ?? "")
                            }
                            TabItem(title: "About author", isSelected: selectedTab == "About author") {
                                selectedTab = "About author"
                            }
                            TabItem(title: "Reviews", isSelected: selectedTab == "Reviews") {
                                selectedTab = "Reviews"
                            }
                        }
                        .padding(.vertical)
                        
                        // Content for the selected tab
                        VStack(alignment: .leading, spacing: 8) {
                            if selectedTab == "Description" {
                                Text("Description")
                                    .font(.headline)
                                    .foregroundColor(.black)
                                Text(bookViewModel.bookDescription)
                                    .font(.body)
                                    .foregroundColor(.gray)
                                    .lineSpacing(4)
                                .font(.body)
                                .foregroundColor(.gray)
                                .lineSpacing(4)
                            } else if selectedTab == "About author" {
                                Text("About Author")
                                    .font(.headline)
                                    .foregroundColor(.black)
                                Text("""
                                Agatha Christie was an English writer known for her detective novels, particularly those featuring Hercule Poirot and Miss Marple.
                                """)
                                .font(.body)
                                .foregroundColor(.gray)
                                .lineSpacing(4)
                            } else if selectedTab == "Reviews" {
                                Text("Reviews")
                                    .font(.headline)
                                    .foregroundColor(.black)
                                Text("""
                                Reviews will be displayed here.
                                """)
                                .font(.body)
                                .foregroundColor(.gray)
                                .lineSpacing(4)
                            }
                        }
                    }.padding()
                }
            }
        }
        .navigationBarHidden(true)
    }
    
}

// Subviews

struct BookStatView: View {
    let value: String
    let label: String
    
    var body: some View {
        VStack {
            Text(value)
                .font(.headline)
                .foregroundColor(.black)
            Text(label)
                .font(.caption)
                .bold()
                .foregroundColor(.gray)
        }
    }
}

struct ActionButton: View {
    let title: String
    let action: () -> Void // Accepts action as a closure
    
    var body: some View {
        Button(action: action) {
            Text(title)
                .font(.headline)
                .padding()
                .frame(maxWidth: .infinity)
                .background(Color.black.opacity(0))
                .foregroundColor(Color.purple)
                .overlay(
                    RoundedRectangle(cornerRadius: 8)
                        .stroke(Color.purple, lineWidth: 2)
                )
                .cornerRadius(8)
        }
    }
}

// Subview for TabItem
struct TabItem: View {
    let title: String
    var isSelected: Bool = false
    var action: () -> Void
    
    var body: some View {
        Button(action: action) {
            VStack {
                Text(title)
                    .font(.body)
                    .foregroundColor(isSelected ? .white : .gray)
                if isSelected {
                    Rectangle()
                        .frame(height: 2)
                        .foregroundColor(.white)
                }
            }
        }
    }
}
