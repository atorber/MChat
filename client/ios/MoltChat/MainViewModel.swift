import Foundation
import Combine

/// 主界面 ViewModel
@MainActor
final class MainViewModel: ObservableObject {
    private let runtime: MChatRuntime
    
    init(runtime: MChatRuntime) {
        self.runtime = runtime
    }
    
    var connectionState: MChatConnectionState { runtime.connectionState }
    var isConnected: Bool { runtime.isConnected }
    var mqttBrokerUrl: String {
        get { runtime.mqttBrokerUrl }
        set { runtime.mqttBrokerUrl = newValue }
    }
    var mqttUsername: String {
        get { runtime.mqttUsername }
        set { runtime.mqttUsername = newValue }
    }
    var mqttPassword: String {
        get { runtime.mqttPassword }
        set { runtime.mqttPassword = newValue }
    }
    var mchatEmployeeId: String {
        get { runtime.mchatEmployeeId }
        set { runtime.mchatEmployeeId = newValue }
    }
    
    var chatEmployees: [EmployeeEntry] { runtime.chatEmployees }
    var chatMessagesByPeer: [String: [ChatMessage]] { runtime.chatMessagesByPeer }
    var chatCurrentPeerId: String? { runtime.chatCurrentPeerId }
    var chatErrorText: String? { runtime.chatErrorText }
    var chatSessions: [ChatSessionEntry] { runtime.chatSessions }
    
    func connect() { runtime.connect() }
    func disconnect() { runtime.disconnect() }
    func loadEmployees() { runtime.loadEmployees() }
    func selectPeer(_ employeeId: String?) { runtime.selectPeer(employeeId) }
    func sendMessage(peerEmployeeId: String, text: String) {
        runtime.sendMessage(peerEmployeeId: peerEmployeeId, text: text)
    }
    func currentChatMessages() -> [ChatMessage] { runtime.currentChatMessages() }
}
