//
//  Book.swift
//  Application
//
//  Created by Ala Din Habibi on 11/28/24.
//

struct Book: Identifiable, Decodable {
    let id: Int
    let author: String
    let title: String
    let coverImage: String?
    let genre: String
    let numOfPages: Int
    let publicationYear: Int    // Changed from publicationDate
    
    // New optional fields from the API response
    let textData: String?
    let link: String?
    let bookmarks: [String]?    // Adjust type if needed
    let reviews: [String]?      // Adjust type if needed
    let averageRating: Double?
    let totalReviews: Int?
    let total: Int?
    let description: String?
    let offset: Int?
    let limit: Int?
    
    // Add coding keys if the JSON keys don't match our property names exactly
    enum CodingKeys: String, CodingKey {
        case id, title, author, publicationYear, numOfPages, coverImage
        case genre, textData, link, bookmarks, reviews, averageRating
        case totalReviews, total, description, offset, limit
    }
}
