//
//  ForgotView.swift
//  Application
//
//  Created by Apple Esprit on 7/11/2024.
//

import SwiftUI

struct ForgotView: View {
    @StateObject private var viewModel = AuthViewModel()
    @State var showOTPView = false
    var body: some View {
        if #available(iOS 16.0, *) {
            NavigationStack {
                VStack(spacing: 28) {
                    VStack(spacing: 28) {
                        Text("Forgot your password?").font(.title.bold())
                        Text ("enter your email adress and we will share a link to create a newpasswpord").fixedSize(horizontal: false, vertical:true).foregroundStyle(.secondary)
                    }
                    .multilineTextAlignment(.center)
                    
                    TextField("Email",text: $viewModel.email)
                        .padding(.leading)
                        .frame(maxWidth: .infinity)
                        .frame(height: 55)
                        .background(.gray.opacity(0.3))
                        .cornerRadius(15)
                    
                    SignButton(title: "send", action: {
                        showOTPView = true
                        viewModel.verifyEmail()
                    })
                    .navigationDestination(isPresented: $showOTPView) {
                        VerifCodeView(email: viewModel.email)
                    }
                    Spacer()
                }
                .padding()
                .padding(.top,20)
            }
        } else {
            // Fallback on earlier versions
        }
    }
}

struct ForgotView_Previews: PreviewProvider {
    static var previews: some View {
        ForgotView()
    }
}
