import SwiftUI
import Combine

class AuthViewModel: ObservableObject {
    @Published var username: String = ""
    @Published var email: String = "ali.ammari@esprit.tn"
    @Published var password: String = "password"
    @Published var errorMessage: String?
    @Published var isLoggedIn: Bool = false // Pour gérer l'état de la connexion
    @Published var isSignedUp: Bool = false
    @Published var profile: User? // Objet pour stocker les infos de profil
    @Published var OTPCode : String = ""
    @Published var newPassword: String = ""
    @Published var isOTPValid: Bool = false
    @Published var navigateToHome: Bool = false
    @Published var navigateToResetPassword: Bool = false
    @Published var navigateToChangePassword: Bool = false
    @Published var isEditingUser: Bool = false
    struct AuthDataModel: Codable {
        let accessToken: String
        let refreshToken: String
        let userId: String
    }
    
    private let baseURL = "https://c995-102-108-190-71.ngrok-free.app"
    func areCredentialsValid() -> Bool {
           return !username.isEmpty && !email.isEmpty && !password.isEmpty
    }
    
    func signUp() {
        guard let url = URL(string: "\(baseURL)/auth/register") else { return }
        var request = URLRequest(url: url)
        request.httpMethod = "POST"
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")
        
        let body: [String: Any] = [
            "username": username,
            "email": email,
            "password": password
        ]
        
        print(body)
        
        request.httpBody = try? JSONSerialization.data(withJSONObject: body)
        
        URLSession.shared.dataTask(with: request) { [weak self] data, response, error in
            guard let data = data, error == nil else { return }
            print(data,response)
            if let httpResponse = response as? HTTPURLResponse, httpResponse.statusCode == 201 {
                // Utilisateur créé, récupérer les infos de profil
                DispatchQueue.main.async {
                    self?.isSignedUp = true
                   // self?.fetchProfile()
                }
            } else {
                DispatchQueue.main.async {
                    self?.errorMessage = "Error signing up"
                }
            }
        }.resume()
    }
    
    func verifyOTP (){
        if OTPCode == "" {
            
        } else {
            
            guard let url = URL(string: "\(baseURL)/auth/verify-email") else { return }
            var request = URLRequest(url: url)
            request.httpMethod = "POST"
            request.setValue("application/json", forHTTPHeaderField: "Content-Type")
            
            let body: [String: Any] = [
                "email": email,
                "otp": OTPCode,
              //  "password": password
            ]
            print(body)
            request.httpBody = try? JSONSerialization.data(withJSONObject: body)
            
            URLSession.shared.dataTask(with: request) { [weak self] data, response, error in
                guard let data = data, error == nil else { return }
                
                if let httpResponse = response as? HTTPURLResponse, httpResponse.statusCode == 201 {
                    // Utilisateur créé, récupérer les infos de profil
                    DispatchQueue.main.async {
                        self?.isOTPValid = true
                    }
                } else {
                    DispatchQueue.main.async {
                        self?.errorMessage = "Error signing up"
                    }
                }
            }.resume()
        }
    }
    
    func verifyEmail() {
        guard let url = URL(string: "\(baseURL)/auth/generate-email") else { return }
        var request = URLRequest(url: url)
        request.httpMethod = "POST"
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")
        
        let body: [String: Any] = [
            "email": email,
          //  "password": password
        ]
        print(body)
        request.httpBody = try? JSONSerialization.data(withJSONObject: body)
        
        URLSession.shared.dataTask(with: request) { [weak self] data, response, error in
            guard let data = data, error == nil else { return }
            
            if let httpResponse = response as? HTTPURLResponse, httpResponse.statusCode == 201 {
                // Utilisateur créé, récupérer les infos de profil
                DispatchQueue.main.async {
                
                  //  self?.fetchProfile()
                }
            } else {
                DispatchQueue.main.async {
                    self?.errorMessage = "Error signing up"
                }
            }
        }.resume()
    }
    
    
    func resetPassword() {
        guard let url = URL(string: "\(baseURL)/auth/forgot-password") else { return }
        var request = URLRequest(url: url)
        request.httpMethod = "POST"
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")
        
        let body: [String: Any] = [
            "email": email,
            "password": newPassword
          //  "password": password
        ]
        
        request.httpBody = try? JSONSerialization.data(withJSONObject: body)
        
        URLSession.shared.dataTask(with: request) { [weak self] data, response, error in
            guard let data = data, error == nil else { return }
            
            if let httpResponse = response as? HTTPURLResponse, httpResponse.statusCode == 201 {
                // Utilisateur créé, récupérer les infos de profil
                DispatchQueue.main.async {
                  //  self?.fetchProfile()
                }
            } else {
                DispatchQueue.main.async {
                    self?.errorMessage = "Error signing up"
                }
            }
        }.resume()
    }
    
    
    
    func signin() {
        guard let url = URL(string: "\(baseURL)/auth/login") else { return }
        var request = URLRequest(url: url)
        request.httpMethod = "POST"
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")
        
        let body: [String: Any] = [
            "email": email,
            "password": password
        ]
        request.httpBody = try? JSONSerialization.data(withJSONObject: body)
        
        URLSession.shared.dataTask(with: request) { [weak self] data, response, error in
            guard let self = self else { return }
            
            if let error = error {
                DispatchQueue.main.async {
                    self.errorMessage = error.localizedDescription
                }
                return
            }
            
            if let httpResponse = response as? HTTPURLResponse,
               httpResponse.statusCode == 201,
               let data = data {
                do {
                    let authData = try JSONDecoder().decode(AuthDataModel.self, from: data)
                    DispatchQueue.main.async {
                        AuthManager.shared.saveTokens(
                            accessToken: authData.accessToken,
                            refreshToken: authData.refreshToken,
                            userId: authData.userId
                        )
                        self.errorMessage = nil
                        self.fetchProfile()
                        self.isLoggedIn = true  // Move this after successful profile fetch
                    }
                } catch {
                    DispatchQueue.main.async {
                        self.errorMessage = "Failed to decode response"
                    }
                }
            } else {
                DispatchQueue.main.async {
                    self.errorMessage = "Invalid credentials"
                }
            }
        }.resume()
    }

    // Méthode pour récupérer le profil
    func fetchProfile() {
        let userId = AuthManager.shared.getUserId()
        let url = URL(string: "\(baseURL)/user/userId/\(userId!)")
        var request = URLRequest(url: url!)
        request.httpMethod = "GET"
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")
        // request.setValue("Bearer \(accessToken)", forHTTPHeaderField: "Authorization")
        do {
            URLSession.shared.dataTask(with: request) {data,response,error in
                let httpResponse = response as? HTTPURLResponse
                print(httpResponse?.statusCode)
                let profile = try? JSONSerialization.jsonObject(with: data!) as? NSDictionary
                let userModel = User(id: profile!["_id"] as! String, username: profile!["username"] as! String, email: profile!["email"] as! String)
                print(userModel)
                DispatchQueue.main.sync {
                    self.profile = userModel
                    self.isEditingUser = true
                }
            }.resume()
        } catch  {
            
        }
    }
}
