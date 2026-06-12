import SwiftUI

struct HomeView: View {
    @StateObject private var viewModel = BookViewModel()
    @State private var selectedFilter: String = "Action"
    @State private var isLoading = true
    @State private var searchText = ""
    
    var body: some View {
        ScrollView(.vertical, showsIndicators: false) {
            VStack(spacing: 25) {
                // Modern Header with Search
                headerSection
                
                // Reading Stats Card
                statsSection
                
                // Trending Books
                trendingSection
                
                // Categories & Books Grid
                categoriesAndBooksSection
            }
            .padding(.top, getSafeArea().top)
        }
        .coordinateSpace(name: "SCROLL")
        .background(Color(.systemGray6))
        .ignoresSafeArea()
        .onAppear {
            viewModel.getBooks(genre: selectedFilter)
            // Simulate loading state
            DispatchQueue.main.asyncAfter(deadline: .now() + 2) {
                isLoading = false
            }
        }
    }
    
    private var headerSection: some View {
        VStack(spacing: 15) {
            HStack {
                VStack(alignment: .leading, spacing: 8) {
                    Text("Hi, Reader!")
                        .font(.title.bold())
                    Text("Explore today's books")
                        .foregroundColor(.gray)
                }
                
                Spacer()
                
                Button(action: {}) {
                    Image(systemName: "bell")
                        .font(.title2)
                        .foregroundColor(.black)
                        .overlay(
                            Circle()
                                .fill(Color.red)
                                .frame(width: 8, height: 8)
                                .offset(x: 10, y: -10)
                        )
                }
            }
            
            SearchBar(text: $searchText)
        }
        .padding(.horizontal)
    }
    
    private var statsSection: some View {
        HStack(spacing: 20) {
            StatCard(title: "Minutes", value: "125", icon: "clock.fill")
            StatCard(title: "Books", value: "05", icon: "book.fill")
            StatCard(title: "Authors", value: "12", icon: "person.fill")
        }
        .padding(.horizontal)
    }
    
    private var trendingSection: some View {
        VStack(alignment: .leading, spacing: 15) {
            SectionTitle(title: "Trending Now", action: {})
            
            ScrollView(.horizontal, showsIndicators: false) {
                HStack(spacing: 15) {
                    if isLoading {
                        ForEach(0..<3, id: \.self) { _ in
                            ShimmerBookCard()
                        }
                    } else {
                        ForEach(viewModel.books.prefix(5)) { book in
                            TrendingBookCard(book: book)
                        }
                    }
                }
                .padding(.horizontal)
            }
        }
    }
    
    private var categoriesAndBooksSection: some View {
        VStack(alignment: .leading, spacing: 15) {
            SectionTitle(title: "Categories", action: {})
            
            // Categories Pills
            ScrollView(.horizontal, showsIndicators: false) {
                HStack(spacing: 12) {
                    ForEach(["All", "Action", "Romance", "Mystery", "Horror"], id: \.self) { category in
                        CategoryPill(title: category, isSelected: selectedFilter == category) {
                            selectedFilter = category
                            isLoading = true
                            viewModel.getBooks(genre: category)
                            DispatchQueue.main.asyncAfter(deadline: .now() + 1.5) {
                                isLoading = false
                            }
                        }
                    }
                }
                .padding(.horizontal)
            }
            
            // Books Grid
            LazyVGrid(columns: Array(repeating: GridItem(.flexible(), spacing: 20), count: 2), spacing: 25) {
                if isLoading {
                    ForEach(0..<4, id: \.self) { _ in
                        ShimmerBookCard(isGrid: true)
                    }
                } else {
                    ForEach(viewModel.books) { book in
                        BookGridItem(book: book)
                    }
                }
            }
            .padding(.horizontal)
        }
    }
}

// MARK: - Supporting Views
struct ShimmerEffect: ViewModifier {
    @State private var phase: CGFloat = 0
    
    func body(content: Content) -> some View {
        content
            .overlay(
                GeometryReader { geometry in
                    LinearGradient(
                        gradient: Gradient(stops: [
                            .init(color: .clear, location: 0),
                            .init(color: .white.opacity(0.7), location: 0.3),
                            .init(color: .clear, location: 0.6)
                        ]),
                        startPoint: .leading,
                        endPoint: .trailing
                    )
                    .frame(width: geometry.size.width * 2)
                    .offset(x: -geometry.size.width)
                    .offset(x: phase * geometry.size.width)
                    .blendMode(.screen)
                }
            )
            .onAppear {
                withAnimation(.linear(duration: 1.5).repeatForever(autoreverses: false)) {
                    phase = 1
                }
            }
    }
}

struct ShimmerBookCard: View {
    var isGrid: Bool = false
    
    var body: some View {
        VStack(alignment: .leading) {
            RoundedRectangle(cornerRadius: 15)
                .fill(Color.gray.opacity(0.3))
                .frame(width: isGrid ? nil : 140, height: isGrid ? 200 : 180)
                .modifier(ShimmerEffect())
            
            RoundedRectangle(cornerRadius: 4)
                .fill(Color.gray.opacity(0.3))
                .frame(width: 100, height: 16)
                .modifier(ShimmerEffect())
            
            RoundedRectangle(cornerRadius: 4)
                .fill(Color.gray.opacity(0.3))
                .frame(width: 60, height: 12)
                .modifier(ShimmerEffect())
        }
    }
}

// Add these structures to the HomeView.swift file

struct SearchBar: View {
    @Binding var text: String
    
    var body: some View {
        HStack {
            Image(systemName: "magnifyingglass")
                .foregroundColor(.gray)
            
            TextField("Search books, authors...", text: $text)
                .font(.system(size: 14))
            
            if !text.isEmpty {
                Button(action: { text = "" }) {
                    Image(systemName: "xmark.circle.fill")
                        .foregroundColor(.gray)
                }
            }
        }
        .padding(12)
        .background(Color.white)
        .cornerRadius(15)
        .shadow(color: Color.black.opacity(0.1), radius: 5, x: 0, y: 2)
    }
}

struct StatCard: View {
    let title: String
    let value: String
    let icon: String
    
    var body: some View {
        VStack {
            Image(systemName: icon)
                .font(.title2)
                .foregroundColor(.purple)
                .frame(width: 40, height: 40)
                .background(Color.purple.opacity(0.2))
                .clipShape(Circle())
            
            Text(value)
                .font(.title3.bold())
            
            Text(title)
                .font(.caption)
                .foregroundColor(.gray)
        }
        .frame(maxWidth: .infinity)
        .padding(.vertical, 20)
        .background(Color.white)
        .cornerRadius(15)
        .shadow(color: Color.black.opacity(0.1), radius: 5, x: 0, y: 2)
    }
}

struct SectionTitle: View {
    let title: String
    let action: () -> Void
    
    var body: some View {
        HStack {
            Text(title)
                .font(.title3.bold())
            Spacer()
            Button("See All", action: action)
                .font(.subheadline)
                .foregroundColor(.purple)
        }
        .padding(.horizontal)
    }
}

struct CategoryPill: View {
    let title: String
    let isSelected: Bool
    let action: () -> Void
    
    var body: some View {
        Button(action: action) {
            Text(title)
                .font(.system(size: 14, weight: .medium))
                .padding(.horizontal, 20)
                .padding(.vertical, 8)
                .background(isSelected ? Color.purple : Color.white)
                .foregroundColor(isSelected ? .white : .black)
                .cornerRadius(20)
                .shadow(color: Color.black.opacity(0.1), radius: 5, x: 0, y: 2)
        }
    }
}

struct TrendingBookCard: View {
    let book: Book
    
    var body: some View {
        VStack(alignment: .leading) {
            AsyncImage(url: URL(string: book.coverImage ?? "")) { image in
                image
                    .resizable()
                    .aspectRatio(contentMode: .fill)
            } placeholder: {
                Color.gray.opacity(0.3)
            }
            .frame(width: 140, height: 180)
            .cornerRadius(15)
            
            Text(book.title)
                .font(.system(size: 14, weight: .medium))
                .lineLimit(1)
            
            Text(book.author)
                .font(.system(size: 12))
                .foregroundColor(.gray)
        }
    }
}

struct BookGridItem: View {
    let book: Book
    
    var body: some View {
        VStack(alignment: .leading) {
            AsyncImage(url: URL(string: book.coverImage ?? "")) { image in
                image
                    .resizable()
                    .aspectRatio(contentMode: .fill)
            } placeholder: {
                Color.gray.opacity(0.3)
            }
            .frame(height: 200)
            .cornerRadius(15)
            
            Text(book.title)
                .font(.system(size: 14, weight: .medium))
                .lineLimit(2)
            
            Text(book.author)
                .font(.system(size: 12))
                .foregroundColor(.gray)
        }
    }
}