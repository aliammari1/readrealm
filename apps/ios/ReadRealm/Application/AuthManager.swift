import Foundation
import KeychainSwift

enum AppConfig {
    static let apiBaseURL: String = {
        let configured = Bundle.main.object(forInfoDictionaryKey: "READREALM_API_BASE_URL") as? String
        let fallback = "https://libraryapp-nest-back.vercel.app"
        return (configured?.isEmpty == false ? configured! : fallback)
            .trimmingCharacters(in: CharacterSet(charactersIn: "/"))
    }()
}

final class AuthManager: ObservableObject {
    static let shared = AuthManager()
    private let keychain = KeychainSwift()

    @Published var isAuthenticated = false

    private enum Keys {
        static let userId = "readrealm.userId"
        static let accessToken = "readrealm.accessToken"
        static let refreshToken = "readrealm.refreshToken"
    }

    private init() {
        isAuthenticated = getAccessToken() != nil
    }

    func saveTokens(accessToken: String, refreshToken: String, userId: String) {
        keychain.set(accessToken, forKey: Keys.accessToken)
        keychain.set(refreshToken, forKey: Keys.refreshToken)
        keychain.set(userId, forKey: Keys.userId)
        isAuthenticated = true
    }

    func getAccessToken() -> String? {
        keychain.get(Keys.accessToken)
    }

    func getRefreshToken() -> String? {
        keychain.get(Keys.refreshToken)
    }

    func getUserId() -> String? {
        keychain.get(Keys.userId)
    }

    func clearTokens() {
        keychain.delete(Keys.accessToken)
        keychain.delete(Keys.refreshToken)
        keychain.delete(Keys.userId)
        isAuthenticated = false
    }
}
