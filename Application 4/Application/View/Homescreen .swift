import SwiftUI

struct Homescreen: View {
    
    @State var currentTab: Tab = .Home
    @State private var isNavigatingToProfile = false // State to control navigation
    
    init() {
        UITabBar.appearance().isHidden = true // Hide system TabBar
    }
    
    @Namespace var animation
    
    var body: some View {
        NavigationView {
            ZStack {
                
                // Main TabView content
                TabView(selection: $currentTab) {
                    // Home tab - Use NavigationView for each tab
                    NavigationView {
                        HomeView() // HomeView content
                            .frame(maxWidth: .infinity, maxHeight: .infinity)
                            .background(Color.white)
                            .navigationBarHidden(true) // Hide the navigation bar for this tab
                        //.edgesIgnoringSafeArea(.top) // Ignore top safe area to remove any extra space
                            .tag(Tab.Home)
                    }
                    
                    // Book tab
                    SearchScreen()
                                .frame(maxWidth: .infinity, maxHeight: .infinity)
                                .background(Color.white)
                                .tag(Tab.Book)
                     
                    // Heart tab
                    EpubReader(urlString: "https://www.gutenberg.org/cache/epub/1513/pg1513.txt")
                        .frame(maxWidth: .infinity, maxHeight: .infinity)
                        .background(Color.white)
                        .tag(Tab.Heart)
                    
                    // Person tab: Show Profile View when tapped
                    NavigationView {
                        ProfilView() // ProfilView content
                            .frame(maxWidth: .infinity, maxHeight: .infinity)
                            .background(Color.white)
                            .navigationBarHidden(false) // Show navigation bar for ProfilView
                            .tag(Tab.Person)
                    }
                    .tag(Tab.Person)
                }
                .overlay(
                    HStack(spacing: 0) {
                        // Custom TabBar buttons
                        ForEach(Tab.allCases, id: \.rawValue) { tab in
                            TabButton(tab: tab, currentTab: $currentTab, animation: animation)
                        }
                    }
                        .padding(.vertical, 10)
                        .padding(.bottom, getSafeArea().bottom == 0 ? 10 : (getSafeArea().bottom - 5))
                        .background(Color.white.shadow(color: .black.opacity(0.1), radius: 8, x: 0, y: -2))
                    , alignment: .bottom)
                .ignoresSafeArea(.all, edges: .bottom)
            }
        }
        .navigationBarBackButtonHidden(true)
    }
        
        
}

    struct TabButton: View {
        let tab: Tab
        @Binding var currentTab: Tab
        var animation: Namespace.ID
        
        var body: some View {
            Button(action: {
                withAnimation(.spring()) {
                    currentTab = tab
                }
            }) {
                VStack(spacing: 4) {
                    Image(systemName: currentTab == tab ? "\(tab.rawValue).fill" : tab.rawValue)
                        .resizable()
                        .aspectRatio(contentMode: .fit)
                        .frame(width: 24, height: 24)
                        .foregroundColor(currentTab == tab ? Color.blue : Color.gray)
                    
                    if currentTab == tab {
                        Circle()
                            .fill(Color.blue)
                            .frame(width: 6, height: 6)
                            .offset(y: 2)
                            .matchedGeometryEffect(id: "tab", in: animation)
                    }
                }
                .frame(maxWidth: .infinity)
            }
        }
    
    
}

enum Tab: String, CaseIterable {
    case Home = "house"
    case Book = "book"
    case Heart = "heart"
    case Person = "person"
    
    var Tabname: String {
        switch self {
        case .Home:
            return "Home"
        case .Book:
            return "Book"
        case .Heart:
            return "Heart"
        case .Person:
            return "Person"
        }
    }
}

struct Homescreen_Previews: PreviewProvider {
    static var previews: some View {
        Homescreen()
    }
}

extension View {
    func getSafeArea() -> UIEdgeInsets {
        guard let screen = UIApplication.shared.connectedScenes.first as? UIWindowScene else {
            return .zero
        }
        guard let safeArea = screen.windows.first?.safeAreaInsets else {
            return .zero
        }
        return safeArea
    }
}
