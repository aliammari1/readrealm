import SwiftUI

struct ProfilView: View {
    var body: some View {
        NavigationView {
            ScrollView{
                VStack {
                    Image("logouser") // Ensure you have the image in your assets
                        .resizable()
                        .aspectRatio(contentMode: .fit)
                        .frame(width: 130, height: 130)
                        .cornerRadius(100)
                    Text("Profile View")
                        .font(.title)
                        .padding(.top, 20)
                }
                .padding()
            }
            .navigationTitle("Profile") // Set the title for the navigation bar
            .navigationBarTitleDisplayMode(.inline) // Set the display mode of the title
        }
    }
}

struct ProfilView_Previews: PreviewProvider {
    static var previews: some View {
        ProfilView()
    }
}
