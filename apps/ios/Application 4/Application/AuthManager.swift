import Foundation
import KeychainSwift

 class AuthManager: ObservableObject {
    static let shared = AuthManager()
    private let keychain = KeychainSwift()
    
    @Published var isAuthenticated = false
    
    private enum Keys {
        static let userId = ""
        static let accessToken = ""
        static let refreshToken = ""
    }
    
    func saveTokens(accessToken: String, refreshToken: String, userId: String) {
        keychain.set(accessToken, forKey: Keys.accessToken)
        keychain.set(refreshToken, forKey: Keys.refreshToken)
        keychain.set(userId, forKey: Keys.userId)
        isAuthenticated = true
    }
    
    func getAccessToken() -> String? {
        return keychain.get(Keys.accessToken)
    }
    
    func getUserId() -> String? {
        return keychain.get(Keys.userId)
    }
    
    func clearTokens() {
        keychain.delete(Keys.accessToken)
        keychain.delete(Keys.refreshToken)
        isAuthenticated = false
    }
}
