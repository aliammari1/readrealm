import SwiftUI

struct ResetPasswordView: View {
    @StateObject private var viewModel = AuthViewModel()
    @State var email: String
    @State var Newpassword = ""
    @State var confirmpassword = ""
    @FocusState var isActive
    @State var checkMinChars = false
    @State var checkLetter = false
    @State var checkPunctuation = false
    @State var checkNumber = false
    @State var showPassword = false
    @State var checkpasswordmatch = false
    
    var progressColor: Color {
        let containsLetters = Newpassword.rangeOfCharacter(from: .letters) != nil
        let containsNumbers = Newpassword.rangeOfCharacter(from: .decimalDigits) != nil
        let containsPunctuation = Newpassword.rangeOfCharacter(from: CharacterSet(charactersIn:"!@#%^&§/.?,")) != nil
        let containspasswordmatch = confirmpassword == Newpassword
        if containsLetters && containsNumbers && containsPunctuation && Newpassword.count >= 8 {
            return Color.green
        }
        else if containsLetters && !containsNumbers && !containsPunctuation {
            return Color.red
        }
        else if containsNumbers && !containsLetters && !containsPunctuation {
            return Color.red
        }
        else if containsLetters && containsNumbers && !containsPunctuation {
            return Color.yellow
        }
        else if containsLetters && containsNumbers && containsPunctuation {
            return Color.blue
        }
        else {
            return .white
        }
    }
    
    var body: some View {
        ScrollView {
            VStack (alignment: .leading, spacing: 24) {
                VStack {
                    ZStack (alignment: .leading) {
                        ZStack {
                            SecureField("", text: $Newpassword)
                                .padding(.leading)
                                .frame(maxWidth: .infinity)
                                .frame(height: 55).focused($isActive)
                                .background(.gray.opacity(0.3))
                                .cornerRadius(16)
                                .opacity(showPassword ? 0 : 1)
                            
                            TextField("", text: $Newpassword)
                                .padding(.leading)
                                .frame(maxWidth: .infinity)
                                .frame(height: 55).focused($isActive)
                                .background(.gray.opacity(0.3))
                                .cornerRadius(16)
                                .opacity(showPassword ? 1 : 0)
                        }
                        
                        Text("Newpassword").padding(.horizontal)
                            .offset(y: (isActive || !Newpassword.isEmpty) ? -50 : 0)
                            .foregroundStyle(isActive ? .primary: .secondary)
                            .foregroundColor(Color.white)
                            .animation(.spring(), value: isActive)
                            .onTapGesture { isActive = true }
                            .onChange(of: Newpassword) { newValue in
                                withAnimation {
                                    checkMinChars = newValue.count >= 8
                                    checkLetter = newValue.rangeOfCharacter(from: .letters) != nil
                                    checkNumber = newValue.rangeOfCharacter(from: .decimalDigits) != nil
                                    checkPunctuation = newValue.rangeOfCharacter(from: CharacterSet(charactersIn: "1@#%^&§/.?,")) != nil
                                    checkpasswordmatch = Newpassword == confirmpassword
                                }
                            }
                    }
                    .overlay (alignment: .trailing) {
                        Image(systemName: showPassword ? "eye.fill" : "eye.slash.fill")
                            .foregroundStyle(showPassword ? .primary : .secondary)
                            .padding(16)
                            .contentShape(Rectangle())
                            .onTapGesture {
                                showPassword.toggle()
                            }
                    }
                }
                
                ZStack (alignment: .leading) {
                    ZStack {
                        SecureField("", text: $confirmpassword)
                            .padding(.leading)
                            .frame(maxWidth: .infinity)
                            .frame(height: 55).focused($isActive)
                            .background(.gray.opacity(0.3))
                            .cornerRadius(16)
                            .opacity(showPassword ? 0 : 1)
                        
                        TextField("", text: $confirmpassword)
                            .padding(.leading)
                            .frame(maxWidth: .infinity)
                            .frame(height: 55).focused($isActive)
                            .background(.gray.opacity(0.3))
                            .cornerRadius(16)
                            .opacity(showPassword ? 1 : 0)
                    }

                    Text("Confirm password").padding(.horizontal)
                        .offset(y: (isActive || !confirmpassword.isEmpty) ? -50 : 0)
                        .foregroundStyle(isActive ? .primary: .secondary)
                        .animation(.spring(), value: isActive)
                        .onTapGesture { isActive = true }
                        .onChange(of: confirmpassword) { newValue in
                            withAnimation {
                                checkpasswordmatch = Newpassword == newValue
                            }
                        }
                }
                .overlay(alignment: .trailing) {
                    Image(systemName: showPassword ? "eye.fill" : "eye.slash.fill")
                        .foregroundStyle(showPassword ? .primary : .secondary)
                        .padding(16)
                        .contentShape(Rectangle())
                        .onTapGesture {
                            showPassword.toggle()
                        }
                }
                
                VStack (alignment: .leading, spacing: 10) {
                    CheckText(text: "At least Minimum 8 characters", check: $checkMinChars)
                    CheckText(text: "At least one letter", check: $checkLetter)
                    CheckText(text: "At least one special character", check: $checkPunctuation)
                    CheckText(text: "At least one Number", check: $checkNumber)
                    CheckText(text: "Passwords do match", check: $checkpasswordmatch)
                }
                Button("Reset Password") {
                    viewModel.email = email
                    viewModel.newPassword = Newpassword
                    viewModel.resetPassword()
                }
                
            }
            .padding()
            
        }
    }
}

struct ResetPasswordView_Previews: PreviewProvider {
    static var previews: some View {
        ResetPasswordView(email: "")
    }
}
