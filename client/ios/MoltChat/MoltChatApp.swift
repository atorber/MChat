import SwiftUI

@main
struct MoltChatApp: App {
    @StateObject private var runtime = MChatRuntime()
    
    var body: some Scene {
        WindowGroup {
            RootScreen(runtime: runtime)
        }
    }
}
