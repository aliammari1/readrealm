//
//  SignIn.swift
//  Application
//
//  Created by Apple Esprit on 7/11/2024.
//

import SwiftUI

struct SignIn: View {
    @StateObject private var viewModel = AuthViewModel()
    @FocusState var isAvtive
    @Binding var Remenber : Bool
    @Binding var showSignUp : Bool
    @State var showForgotView = false
    var action : () -> Void

    var body: some View {

        VStack(spacing:45){
            TopView(title: "welcome back ", details: "please sign up into your account")
            InfoTF(title: "Email", text: $viewModel.email)
            VStack(spacing: 24) {
                PasswordTF(title: "password", text: $viewModel.password)
                HStack {
                    Toggle(isOn: $Remenber, label:{
                        Text ("label")
                    })
                    .toggleStyle(RememberStyle())
                    Spacer()
                    Button (action:{
                        showForgotView.toggle()
                    }, label:{
                        Text("Forget password ?") .bold()
                            .font(.footnote)
                    })
                    .tint(.white)
                }
            }
            SignButton(title: "sign in", action: {
                viewModel.signin()
            })
            .navigationDestination(isPresented: $viewModel.isLoggedIn) {ProfilView()}

            OrView(title: "Or")

            HStack(spacing: 65){
                signAccount(icon: "apple.logo", width: 32, height: 32, action: {})
                // signAccount(icon: "email.logo", width: 32, height: 32, action: {})
                // signAccount(icon: "google.logo", width: 32, height: 32, action: {})
                
            }

            Spacer()

            Button {
                withAnimation {
                    showSignUp.toggle()
                }
            } label: {
                Text("Dont have an Account? ***sign up*** ")
            }
            .tint(.white)
        }
        .padding()
        .sheet(isPresented: $showForgotView, content: {
            ForgotView()
                .presentationDetents([.fraction(0.40)])
        })
    }
}
    
    struct SignIn_Previews: PreviewProvider {
        static var previews: some View {
            //SignIn()
            ContentView()
        }
    }
    
    struct TopView: View {
        var title:String
        var details:String
        var body: some View{
            VStack {
                
                Text(title).font(/*@START_MENU_TOKEN@*/.title/*@END_MENU_TOKEN@*/.bold()).foregroundColor(Color.white)
                Text(details).foregroundColor(Color.white)
            }
            .frame(maxWidth: /*@START_MENU_TOKEN@*/.infinity/*@END_MENU_TOKEN@*/)
        }
    }
    
    
    struct InfoTF: View {
        var title:String
        @Binding var text:String
        @FocusState var isActive
        var body: some View {
            ZStack (alignment: .leading) {
                TextField("",text: $text)
                    .padding(.leading)
                    .frame(maxWidth: .infinity)
                    .frame(height: 55).focused($isActive)
                    .background(.gray.opacity(0.3))
                    .cornerRadius(15)
                
                Text(title).foregroundColor(Color.white)
                    .padding(.leading)
                    .offset(y:(isActive || !text.isEmpty) ? -50 : 0)
                    .animation(.spring(), value: isActive)
                    .foregroundStyle(isActive ?.white : .secondary)
                    .onTapGesture {
                        isActive = true
                    }
            }.padding()
            
        }
    }
    
    struct RememberStyle:ToggleStyle{
        func makeBody(configuration: Configuration) -> some View {
            Button {
                configuration.isOn.toggle()
            } label: {
                HStack{
                    Image(systemName: configuration.isOn ? "checkmark.square" :"square")
                    //.contentTransition(ContentTransition.)
                    Text ("Remember")
                }
                
            }
            .tint(.white)
        }
    }
    

struct SignButton: View {
    var title:String
    var action:() -> Void
    var body: some View {
        Button(action: {action()}, label: {
            Text(title).font(.title2.bold())
                .foregroundColor(Color.black)
                .frame(maxWidth: .infinity)
                .frame(height:55)
                .background(.primary)
                .cornerRadius(16)
            
        })
        .tint(.white)
    }
}

struct OrView: View {
    var title: String
    var body: some View {
        HStack{
            Rectangle()
                .frame(height: 1.5)
                .foregroundStyle(.gray.opacity(0.3))
                
            Text(title)
                .foregroundColor(Color.white)
            Rectangle()
                .frame(height: 1.5)
                .foregroundStyle(.gray.opacity(0.3))
        }
     
    }
}

struct signAccount: View {
    var icon: String
    var width : CGFloat
    var height : CGFloat
    var action:() -> Void
    var body: some View {
        Button(action: {action()}, label: {
            Image(systemName: icon).renderingMode(.template)
                .resizable().scaledToFill()
                .frame(width: width , height: height)
                .overlay {
                    RoundedRectangle(cornerRadius: 12).stroke(lineWidth: 1.5)
                        .frame(width: 50 , height: 50)
                        .foregroundStyle(.gray.opacity(0.3))
                }
        })
        .tint(.white)
    }
}
