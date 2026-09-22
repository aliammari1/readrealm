import SwiftUI

struct ProfilView: View {
    @StateObject private var authViewModel = AuthViewModel()
    @State private var showDeleteConfirmation = false
    @State private var deletionError: String?
    @State private var accountDeleted = false

    var body: some View {
        if accountDeleted {
            ContentView()
        } else {
            NavigationView {
                ScrollView {
                    VStack(spacing: 20) {
                        Image("logouser")
                            .resizable()
                            .aspectRatio(contentMode: .fit)
                            .frame(width: 130, height: 130)
                            .clipShape(Circle())

                        Text("Your ReadRealm")
                            .font(.title2.bold())

                        if let privacyURL = URL(string: AppConfig.apiBaseURL + "/privacy") {
                            Link("Privacy Policy", destination: privacyURL)
                        }

                        Button(role: .destructive) {
                            showDeleteConfirmation = true
                        } label: {
                            Label("Delete Account", systemImage: "trash")
                                .frame(maxWidth: .infinity)
                        }
                        .buttonStyle(.bordered)
                        .padding(.top, 20)

                        if let deletionError {
                            Text(deletionError)
                                .font(.footnote)
                                .foregroundStyle(.red)
                        }
                    }
                    .padding()
                }
                .navigationTitle("Profile")
                .navigationBarTitleDisplayMode(.inline)
                .alert("Delete ReadRealm account?", isPresented: $showDeleteConfirmation) {
                    Button("Cancel", role: .cancel) {}
                    Button("Delete Permanently", role: .destructive) {
                        authViewModel.deleteAccount { result in
                            switch result {
                            case .success:
                                accountDeleted = true
                            case .failure(let error):
                                deletionError = error.localizedDescription
                            }
                        }
                    }
                } message: {
                    Text(
                        "This permanently deletes your account and associated ReadRealm data, including reviews, bookmarks, chat messages, and active sessions. This cannot be undone."
                    )
                }
            }
        }
    }
}

struct ProfilView_Previews: PreviewProvider {
    static var previews: some View {
        ProfilView()
    }
}
