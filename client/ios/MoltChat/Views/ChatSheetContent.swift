import SwiftUI

struct ChatSheetContent: View {
    @ObservedObject var runtime: MChatRuntime
    
    var body: some View {
        if let peerId = runtime.chatCurrentPeerId {
            let peerName = runtime.chatEmployees.first { $0.employeeId == peerId }?.name ?? peerId
            ChatConversation(
                peerId: peerId,
                peerName: peerName,
                messages: runtime.chatMessagesByPeer[peerId] ?? [],
                errorText: runtime.chatErrorText,
                canSend: runtime.isConnected,
                onBack: { runtime.selectPeer(nil) },
                onSend: { runtime.sendMessage(peerEmployeeId: peerId, text: $0) }
            )
        } else {
            ChatEmployeeList(
                employees: runtime.chatEmployees,
                messagesByPeer: runtime.chatMessagesByPeer,
                myEmployeeId: runtime.mchatEmployeeId,
                isConnected: runtime.isConnected,
                onSelectEmployee: { runtime.selectPeer($0.employeeId) }
            )
        }
    }
}
