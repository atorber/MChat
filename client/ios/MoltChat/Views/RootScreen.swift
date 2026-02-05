import SwiftUI

struct RootScreen: View {
    @ObservedObject var runtime: MChatRuntime
    
    var body: some View {
        ZStack(alignment: .top) {
            ChatSheetContent(runtime: runtime)
                .padding(.top, 56)
            
            if runtime.chatCurrentPeerId == nil {
                VStack {
                    StatusPill(
                        connectionState: runtime.connectionState,
                        onClick: { showSettings = true }
                    )
                    .padding(12)
                    Spacer()
                }
                .frame(maxWidth: .infinity, alignment: .leading)
            }
            
            VStack {
                Button(action: { showSettings = true }) {
                    Image(systemName: "gearshape.fill")
                        .font(.system(size: 20))
                        .foregroundColor(.primary)
                        .frame(width: 44, height: 44)
                }
                Spacer()
            }
            .frame(maxWidth: .infinity, alignment: .trailing)
            .padding(.trailing, 12)
            .padding(.top, 12)
        }
        .sheet(isPresented: $showSettings) {
            SettingsSheet(runtime: runtime)
        }
    }
    
    @State private var showSettings = false
}
