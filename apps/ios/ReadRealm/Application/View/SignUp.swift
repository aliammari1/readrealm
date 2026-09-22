[Reading 57 lines from start (total: 57 lines, 0 remaining)]

//
//  SignUp.swift
//  Application
//
//  Created by Apple Esprit on 7/11/2024.
//

import SwiftUI

struct SignUp: View {
    @StateObject private var viewModel = AuthViewModel()
    @FocusState var isAvtive
    @Binding var Remenber : Bool
    @Binding var showSignIn : Bool
    var action:() -> Void
    var body: some View {
        
        VStack(spacing:45){
            
            TopView(title: "Create New Account", details: "please sign up in to your account")
            VStack(spacing: 10) {
                InfoTF(title: "Username", text: $viewModel.username)
                
                InfoTF(title: "Email", text: $viewModel.email)
                
                PasswordCheckField(text: $viewModel.password)
            }
            if #available(iOS 16.0, *) {
                SignButton(title: "sign up", action: { viewModel.signUp()})
                    .navigationDestination(isPresented: $viewModel.isSignedUp, destination: {ProfilView()})
            } else {
                // Fallback on earlier versions
            }
            
            
            Button {
                withAnimation{
                    showSignIn.toggle()
                }
            } label: {
                Text("Already have an Account? ***sign in*** ")
            }
            .tint(.white)
            
        }
        .padding(.horizontal)
    }
}


struct SignUp_Previews: PreviewProvider {
    static var previews: some View {
       //ForgotView()
     //  SignUp()
        ContentView()
   }
}

[executed on device: thethirdone (5346b035-2835-4d41-8950-4054b200ff7b)]