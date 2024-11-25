//
//  VerifCodeView.swift
//  Application
//
//  Created by Mac2021 on 7/11/2024.
//

import SwiftUI

struct VerifCodeView: View {
    @StateObject private var viewModel = AuthViewModel()
    @State var email: String
    @State var otoText:String = ""
    @FocusState private var iskeyboardshowing: Bool
    var body: some View {
        NavigationStack {
            ZStack{
                Color.black .ignoresSafeArea()
                VStack(spacing: 28) {
                    VStack(spacing: 28){
                        Text("Enter your code ").font(.title.bold()).foregroundColor(.red)
                        Text ("enter your email address and we will share a link to create a new password").foregroundColor(.red).fixedSize(horizontal: false, vertical:true).foregroundStyle(.secondary)
                        
                        
                        HStack (spacing: 0){
                            ForEach(0..<6, id: \.self) { index in
                                OTPTextBox(index)
                            }
                        }
                        .background(content :{
                            TextField("",text: $otoText.limit(6))
                                .keyboardType(.numberPad)
                                .textContentType(.oneTimeCode)
                                .frame(width: 1 , height: 1)
                                .opacity(0.001)
                                .blendMode(.screen)
                                .focused($iskeyboardshowing)
                        })
                        .contentShape(Rectangle())
                        .onTapGesture {
                            iskeyboardshowing.toggle()
                        }
                        .padding(.bottom,20)
                        .padding(.top , 10)
                    }
                    .multilineTextAlignment(.center)
                    SignButton(title: "Verify", action: {
                        viewModel.email = email
                        viewModel.OTPCode = otoText
                        viewModel.verifyOTP()

                    })
                    .navigationDestination(isPresented: $viewModel.isOTPValid) {
                        ResetPasswordView(email: viewModel.email)
                        }
                    Spacer()
                        .disableWithOpacity(otoText.count < 6 )
                }
                .padding()
                .padding(.top,20)
            }
        }
    }
    
    func OTPTextBox(_ index : Int )->some View{
        ZStack {
            if otoText.count > index {
                // finding char
                let startIndex = otoText.startIndex
                let charIndex = otoText.index(startIndex , offsetBy: index)
                let charToString = String(otoText[charIndex])
                Text (charToString)
                    .foregroundColor(Color.red)
            }else{
                Text("")
                    .foregroundColor(Color.red)
            }
        }
        .frame(width:45 , height: 45)
        .background{
            RoundedRectangle(cornerRadius: 6 , style: .continuous)
                .stroke(.gray,lineWidth: 0.5)
        }
        .frame(maxWidth: .infinity)
    }
}


struct VerifCodeView_Previews: PreviewProvider {
    static var previews: some View {
        VerifCodeView(email: "")
    }
}

extension View {
    func disableWithOpacity(_ condition: Bool )->some View {
        self
            .disabled(condition)
            .opacity(condition ? 0.6 : 1 )
    }
}

extension Binding where Value == String{
    func limit(_ length : Int) -> Self {
        if self.wrappedValue.count > length {
            DispatchQueue.main.async {
                self.wrappedValue = String(self.wrappedValue.prefix(length))
            }
        }
        return self
    }
}
