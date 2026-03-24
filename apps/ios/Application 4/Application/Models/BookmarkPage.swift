import Foundation

struct BookmarkPage: Identifiable, Codable {
    let id: UUID
    let pageNumber: Int
    let snippet: String
    let timestamp: Date
    
    init(pageNumber: Int, snippet: String) {
        self.id = UUID()
        self.pageNumber = pageNumber
        self.snippet = snippet
        self.timestamp = Date()
    }
}
