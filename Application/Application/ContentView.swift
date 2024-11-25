//
//  ContentView.swift
//  Application
//
//  Created by Apple Esprit on 7/11/2024.
//

import SwiftUI

struct ContentView: View {
    @State var Remenber = false
    @State var showSignUp = false
    var body: some View {
        ScrollView (.vertical,showsIndicators: false){
            
            if showSignUp {
                SignUp (Remenber: $Remenber, showSignIn:$showSignUp, action:{})
                    .transition(.asymmetric(insertion: .move(edge: .trailing), removal: .move(edge: .trailing)))
            } else {
                SignIn (Remenber: $Remenber, showSignUp:$showSignUp, action:{})
                    .transition(.asymmetric(insertion: .move(edge: .trailing), removal: .move(edge: .trailing)))
            }
        }
       .background(Color.black)
        .ignoresSafeArea(.keyboard)
    }
    
}


struct ContentView_Previews: PreviewProvider {
    static var previews: some View {
        ContentView()
    }
}
