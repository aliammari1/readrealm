//
//  BookViewModel.swift
//  Application
//

import SwiftUI
import Combine

@MainActor
final class BookViewModel: ObservableObject {
    private let baseURL = "https://libraryapp-nest-back.vercel.app"

    @Published var books: [Book] = []
    @Published var bookDescription: String = ""
    @Published var searchQuery: String = ""

    func searchBooks() {
        let query = searchQuery.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !query.isEmpty,
              var components = URLComponents(string: "\(baseURL)/book/search") else {
            books = []
            return
        }

        components.queryItems = [URLQueryItem(name: "q", value: query)]
        guard let url = components.url else { return }

        Task {
            await loadBooks(from: url)
        }
    }

    func getBooks(genre: String = "all") {
        guard let encodedGenre = genre.addingPercentEncoding(withAllowedCharacters: .urlPathAllowed),
              let url = URL(string: "\(baseURL)/book/genre/\(encodedGenre)") else {
            return
        }

        Task {
            await loadBooks(from: url)
        }
    }

    func getBookSummary(title: String) {
        guard let encodedTitle = title.addingPercentEncoding(withAllowedCharacters: .urlPathAllowed),
              let url = URL(string: "\(baseURL)/book/summary/\(encodedTitle)") else {
            return
        }

        Task {
            do {
                let (data, response) = try await URLSession.shared.data(from: url)
                guard let httpResponse = response as? HTTPURLResponse,
                      (200...299).contains(httpResponse.statusCode) else {
                    return
                }

                bookDescription = String(data: data, encoding: .utf8) ?? ""
            } catch {
                print("Failed to fetch book summary: \(error.localizedDescription)")
            }
        }
    }

    private func loadBooks(from url: URL) async {
        books = []

        do {
            let (data, response) = try await URLSession.shared.data(from: url)
            guard let httpResponse = response as? HTTPURLResponse,
                  (200...299).contains(httpResponse.statusCode) else {
                print("Book request failed")
                return
            }

            books = try JSONDecoder().decode([Book].self, from: data)
        } catch {
            print("Failed to load books: \(error.localizedDescription)")
        }
    }
}
