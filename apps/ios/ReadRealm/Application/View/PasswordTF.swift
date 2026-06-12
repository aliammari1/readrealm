//
//  PasswordTF.swift
//  Application
//
//  Created by Apple Esprit on 7/11/2024.
//

import SwiftUI

struct PasswordTF: View {
    
    var title : String
    @Binding var text:String
    @FocusState var isActive
    @State var showPassword = false
    
    var body: some View {
        ZStack (alignment: .leading) {
            SecureField("", text: $text)
                .padding(.leading)
                .frame(maxWidth: .infinity)
                .frame(height: 55).focused($isActive)
                .background(.gray.opacity(0.3))
                .cornerRadius(15)
                .opacity(showPassword ? 0 : 1)
            
            TextField("",text: $text)
                .padding(.leading)
                .frame(maxWidth: .infinity)
                .frame(height: 55).focused($isActive)
                .background(.gray.opacity(0.3))
                .cornerRadius(15)
                .opacity(showPassword ? 1 : 0)
            
            Text(title).foregroundColor(Color.white)
                .padding(.leading)
                .offset(y:(isActive || !text.isEmpty) ? -50 : 0)
                .animation(.spring(), value: isActive)
                .foregroundStyle(isActive ?.white : .secondary)
                .onTapGesture {
                    isActive = true
                }
            
        }
        .padding()
            .overlay(alignment: .trailing) {
                Image(systemName: showPassword ? "eye.fill" : "eye.slash.fill")
                    .padding(16)
                    .contentShape(Rectangle())
                    .foregroundStyle(showPassword ? .primary:.secondary)
                    .onTapGesture {
                        showPassword.toggle()
                    }
            }
    }
}

struct PasswordTF_Previews: PreviewProvider {
    static var previews: some View {
        ContentView()
        //PasswordTF()
    }
}
