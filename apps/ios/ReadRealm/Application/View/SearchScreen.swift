//
//  SearchScreen.swift
//  Application
//
//  Created by Ala Din Habibi on 11/29/24.
//


import SwiftUI
import Combine

struct SearchScreen: View {
    @StateObject private var viewModel = BookViewModel()
    var body: some View {
        if #available(iOS 16.0, *) {
            NavigationStack {
                List {
                    ForEach(viewModel.books) {book in
                   //     BookThumbnail(title: book.title, author: book.author, coverImage: book.coverImage ?? "")
                    }
                }
                .searchable(text: $viewModel.searchQuery)
                .onChange(of: viewModel.searchQuery) { _ in
                    viewModel.searchBooks()
                }
            }
        }
    }
}

struct SearchScreen_Previews: PreviewProvider {
    static var previews: some View {
        SearchScreen()
    }
}

