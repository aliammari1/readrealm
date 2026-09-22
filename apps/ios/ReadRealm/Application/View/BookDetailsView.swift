import Foundation
import SwiftUI

struct BookDetailsView: View {
    @Environment(\.dismiss) private var dismiss
    @StateObject private var bookViewModel = BookViewModel()
    var book: Book?

    var body: some View {
        NavigationView {
            ZStack {
                LinearGradient(
                    gradient: Gradient(
                        colors: [Color.purple.opacity(0.2), Color.purple.opacity(0.1)]
                    ),
                    startPoint: .top,
                    endPoint: .bottom
                )
                .ignoresSafeArea()

                ScrollView {
                    VStack(spacing: 20) {
                        HStack {
                            Button(action: { dismiss() }) {
                                Image(systemName: "arrow.left")
                                    .foregroundColor(.purple)
                                    .font(.title2)
                            }

                            Spacer()

                            Text("Book Details")
                                .foregroundColor(.black)
                                .font(.headline)

                            Spacer()

                            Color.clear.frame(width: 28, height: 28)
                        }

                        AsyncImage(url: URL(string: book?.coverImage ?? "")) { image in
                            image
                                .resizable()
                                .scaledToFill()
                        } placeholder: {
                            RoundedRectangle(cornerRadius: 8)
                                .fill(Color.gray.opacity(0.2))
                                .overlay {
                                    Image(systemName: "book.closed")
                                        .foregroundColor(.gray)
                                }
                        }
                        .frame(width: 120, height: 180)
                        .clipShape(RoundedRectangle(cornerRadius: 8))

                        VStack(spacing: 6) {
                            Text(book?.title ?? "Unknown Book")
                                .font(.title2)
                                .bold()
                                .multilineTextAlignment(.center)
                                .foregroundColor(.black)

                            Text(book?.author ?? "Unknown Author")
                                .font(.subheadline)
                                .foregroundColor(.gray)
                        }

                        if let currentBook = book {
                            HStack(spacing: 36) {
                                BookStatView(
                                    value: currentBook.averageRating.map { String(format: "%.1f", $0) } ?? "—",
                                    label: "rating"
                                )
                                BookStatView(
                                    value: currentBook.numOfPages > 0 ? String(currentBook.numOfPages) : "—",
                                    label: "pages"
                                )
                                BookStatView(
                                    value: currentBook.publicationYear > 0 ? String(currentBook.publicationYear) : "—",
                                    label: "published"
                                )
                            }

                            HStack(spacing: 16) {
                                if let link = currentBook.link, !link.isEmpty {
                                    NavigationLink(
                                        destination: EpubReader(urlString: link)
                                            .navigationBarBackButtonHidden(true)
                                    ) {
                                        BookActionLabel(title: "Read")
                                    }
                                }

                                NavigationLink(
                                    destination: AudioBookPlayerView(book: currentBook)
                                        .navigationBarBackButtonHidden(true)
                                ) {
                                    BookActionLabel(title: "Listen")
                                }
                            }
                        }

                        VStack(alignment: .leading, spacing: 8) {
                            Text("Description")
                                .font(.headline)
                                .foregroundColor(.black)

                            Text(descriptionText)
                                .font(.body)
                                .foregroundColor(.gray)
                                .lineSpacing(4)
                        }
                        .frame(maxWidth: .infinity, alignment: .leading)
                    }
                    .padding()
                }
            }
            .navigationBarHidden(true)
        }
        .onAppear {
            guard bookViewModel.bookDescription.isEmpty,
                  let title = book?.title,
                  !title.isEmpty else {
                return
            }
            bookViewModel.getBookSummary(title: title)
        }
    }

    private var descriptionText: String {
        if !bookViewModel.bookDescription.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty {
            return bookViewModel.bookDescription
        }
        if let description = book?.description,
           !description.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty {
            return description
        }
        return "No description is available for this book."
    }
}

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

struct BookActionLabel: View {
    let title: String

    var body: some View {
        Text(title)
            .font(.headline)
            .padding()
            .frame(maxWidth: .infinity)
            .foregroundColor(.purple)
            .overlay(
                RoundedRectangle(cornerRadius: 8)
                    .stroke(Color.purple, lineWidth: 2)
            )
    }
}
