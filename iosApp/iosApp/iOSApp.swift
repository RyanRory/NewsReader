import SwiftUI
import SharedLibrary

@main
struct iOSApp: App {
    @StateObject private var viewModel: NewsViewModel = {
        DependencyInjectionKt.doInitKoin()
        let helper = KoinHelper()
        do {
            return NewsViewModel(
                fetchNewsService: try helper.getFetchNewsService(),
                toggleStarService: try helper.getToggleStarService()
            )
        } catch {
            fatalError("Koin error: \(error)")
        }
    }()
    
    var body: some Scene {
        WindowGroup {
            ContentView()
                .environmentObject(viewModel)
        }
    }
}
