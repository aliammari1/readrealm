//
//  BookViewModel.swift
//  Application
//
//  Created by Ala Din Habibi on 11/28/24.
//

import SwiftUI
import Combine


class BookViewModel: ObservableObject {
    private let baseURL = "https://libraryapp-nest-back.vercel.app"
    @Published var books: [Book] = [Book]()
    @Published var bookDescription: String = ""
    @Published var searchQuery: String = ""
    func searchBooks() {
        self.books = []
        let url = URL(string: "\(self.baseURL)/book/search/\(searchQuery)")
        var request = URLRequest(url: url!)
        request.httpMethod = "GET"
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")
        // request.setValue("Bearer \(accessToken)", forHTTPHeaderField: "Authorization")
        do {
            URLSession.shared.dataTask(with: request) {data,response,error in
                let httpResponse = response as? HTTPURLResponse
                print(httpResponse?.statusCode)
                let bookData = try? JSONDecoder().decode([Book].self, from: data!)
                print(bookData)
                
                DispatchQueue.main.sync {
                    self.books = bookData ?? []
                }
            }.resume()
        } catch  {
            
        }
    }
    func getBooks(genre: String = "all") {
        print("📚 Starting book fetch for genre: \(genre)")
        self.books = []
        guard let url = URL(string: "\(self.baseURL)/book/genre/\(genre)") else {
            print("❌ Invalid URL constructed for genre: \(genre)")
            return
        }
        
        var request = URLRequest(url: url)
        request.httpMethod = "GET"
        
        let session = URLSession(configuration: .default)
        let task = session.dataTask(with: request) { [weak self] data, response, error in
            if let error = error {
                print("❌ Network error: \(error.localizedDescription)")
                return
            }
            
            guard let httpResponse = response as? HTTPURLResponse else {
                print("❌ Invalid response type")
                return
            }
            
            print("📥 Response status: \(httpResponse.statusCode)")
            
            guard httpResponse.statusCode == 200,
                  let data = data else {
                print("❌ Invalid response: Status \(httpResponse.statusCode)")
                return
            }
            
            print("📦 Received data size: \(data.count) bytes")
            
            // Split the received data by double newlines to separate events
            let stringData = String(data: data, encoding: .utf8) ?? ""
            let events = stringData.components(separatedBy: "\n\n")
            print("🔍 Found \(events.count) potential events")
            
            var processedCount = 0
            var errorCount = 0
            
            for event in events {
                if event.hasPrefix("data: ") {
                    let jsonString = String(event.dropFirst(6)) // Remove "data: " prefix
                    if let eventData = jsonString.data(using: .utf8) {
                        do {
                            let book = try JSONDecoder().decode(Book.self, from: eventData)
                            DispatchQueue.main.async {
                                self?.books.append(book)
                                processedCount += 1
                                print("✅ Processed book: \(book.title)")
                            }
                        } catch {
                            errorCount += 1
                            print("❌ JSON decode error: \(error.localizedDescription)")
                            print("📄 Raw JSON: \(jsonString)")
                        }
                    }
                }
            }
            
            print("📊 Processing complete - Success: \(processedCount), Errors: \(errorCount)")
        }
        task.resume()
        print("🚀 Network request started")
    }
    func getBookSummary(title: String) {
        let url = URL(string: "\(self.baseURL)/book/summary/\(title)")
        var request = URLRequest(url: url!)
        request.httpMethod = "GET"
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")
        // request.setValue("Bearer \(accessToken)", forHTTPHeaderField: "Authorization")
        do {
            URLSession.shared.dataTask(with: request) {data,response,error in
                let httpResponse = response as? HTTPURLResponse
                print(httpResponse?.statusCode)
                print(data)
                let bookData = String(data: data!, encoding: .utf8)
                print(bookData)
                
                DispatchQueue.main.sync {
                    self.bookDescription = bookData!
                }
            }.resume()
        } catch  {
            
        }
    }
}
