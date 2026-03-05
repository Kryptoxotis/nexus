import SwiftUI
import ComposeApp

@main
struct NexusApp: App {
    init() {
        MainViewControllerKt.initApp(
            supabaseUrl: ProcessInfo.processInfo.environment["SUPABASE_URL"] ?? "",
            supabaseAnonKey: ProcessInfo.processInfo.environment["SUPABASE_ANON_KEY"] ?? ""
        )
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
