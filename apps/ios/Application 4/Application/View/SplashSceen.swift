//
//  SplashSceen.swift
//  Application
//
//  Created by Apple Esprit on 7/11/2024.
//

import SwiftUI

struct SplashSceen: View {
    @State private var isActive = false
    var body: some View {
        if isActive{
            ContentView()
        } else {
            ZStack {
                Color(.black).ignoresSafeArea()
                
                VStack {
                    Image("valo")
                        .resizable()
                        .cornerRadius(25)
                        .aspectRatio(contentMode: /*@START_MENU_TOKEN@*/.fit/*@END_MENU_TOKEN@*/)
                        .padding(/*@START_MENU_TOKEN@*/.all/*@END_MENU_TOKEN@*/)
                    Text("Application").font(/*@START_MENU_TOKEN@*/.title3/*@END_MENU_TOKEN@*/).foregroundColor(Color.white).multilineTextAlignment(.center)
                    
                }
                
            }
            .onAppear{
                DispatchQueue.main.asyncAfter(deadline: .now() + 2.0) {
                    self.isActive = true
                }
            }
        }
    }
}

struct SplashSceen_Previews: PreviewProvider {
    static var previews: some View {
        SplashSceen()
    }
}
