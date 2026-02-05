import Foundation

struct ChatMessage: Codable, Identifiable {
    let id: String
    let role: String
    let content: [ChatMessageContent]
    let timestampMs: Int64?
}

struct ChatMessageContent: Codable {
    var type: String = "text"
    var text: String?
    var mimeType: String?
    var fileName: String?
    var base64: String?
}

struct ChatSessionEntry: Identifiable {
    let key: String
    let updatedAtMs: Int64?
    let displayName: String?
    var id: String { key }
}

struct EmployeeEntry: Identifiable {
    let employeeId: String
    let name: String
    let departmentId: String?
    let isAiAgent: Bool
    var id: String { employeeId }
}
