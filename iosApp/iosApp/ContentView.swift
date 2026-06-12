import SwiftUI
import SharedLibrary

struct ContentView: View {
    var body: some View {
        TabView {
            NewsListView()
                .tabItem {
                    Label("Headlines", systemImage: "newspaper")
                }
                .accessibilityLabel("Headlines tab")
            StarredListView()
                .tabItem {
                    Label("Starred", systemImage: "star.fill")
                }
                .accessibilityLabel("Starred articles tab")
        }
    }
}
