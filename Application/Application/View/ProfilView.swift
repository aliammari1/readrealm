//
//  ProfilView.swift
//  Application
//
//  Created by Apple Esprit on 7/11/2024.
//

import SwiftUI

struct ProfilView: View {
    var body: some View {
        NavigationView {
            ScrollView{
                VStack {
                    Image("logouser").resizable().aspectRatio(contentMode: .fit)
                        .frame(width: 130,height: 130)
                        .cornerRadius(100)
                    Text ("profile view")
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
