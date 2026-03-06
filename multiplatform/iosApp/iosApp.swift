import SwiftUI
import ComposeApp

@main
struct NexusApp: App {
    init() {
        MainViewControllerKt.doInitApp()
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
                .onOpenURL { url in
                    MainViewControllerKt.handleDeepLink(url: url.absoluteString)
                }
        }
    }
}
