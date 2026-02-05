import SwiftUI

struct ChatEmployeeList: View {
    let employees: [EmployeeEntry]
    let messagesByPeer: [String: [ChatMessage]]
    let myEmployeeId: String
    let isConnected: Bool
    let onSelectEmployee: (EmployeeEntry) -> Void
    
    @State private var searchQuery = ""
    
    private var recentFirst: (recent: [EmployeeEntry], rest: [EmployeeEntry]) {
        let query = searchQuery.trimmingCharacters(in: .whitespaces).lowercased()
        let withMessages = messagesByPeer.compactMap { peerId, msgs -> (EmployeeEntry, Int64)? in
            guard let emp = employees.first(where: { $0.employeeId == peerId }) else { return nil }
            let t = msgs.last?.timestampMs ?? 0
            return (emp, t)
        }.sorted { $0.1 > $1.1 }.map(\.0)
        let recentIds = Set(withMessages.map(\.employeeId))
        let rest = employees.filter { !recentIds.contains($0.employeeId) }
        if query.isEmpty {
            return (withMessages, rest)
        }
        let filter: (EmployeeEntry) -> Bool = {
            $0.name.lowercased().contains(query) || $0.employeeId.lowercased().contains(query)
        }
        return (withMessages.filter(filter), rest.filter(filter))
    }
    
    var body: some View {
        VStack(spacing: 0) {
            Text("联系人")
                .font(.title2)
                .frame(maxWidth: .infinity, alignment: .leading)
                .padding(.horizontal, 16)
                .padding(.vertical, 12)
            
            if !isConnected {
                Text("请先在设置中连接")
                    .font(.caption)
                    .foregroundColor(.red)
                    .frame(maxWidth: .infinity, alignment: .leading)
                    .padding(.horizontal, 16)
                    .padding(.bottom, 4)
            }
            
            HStack {
                Image(systemName: "magnifyingglass")
                    .foregroundColor(.secondary)
                TextField("搜索姓名或 ID", text: $searchQuery)
            }
            .padding(12)
            .background(Color(.systemGray6))
            .cornerRadius(10)
            .padding(.horizontal, 16)
            .padding(.vertical, 8)
            
            let (recent, rest) = recentFirst
            if recent.isEmpty && rest.isEmpty {
                Text(searchQuery.isEmpty ? "暂无联系人" : "无匹配结果")
                    .font(.body)
                    .foregroundColor(.secondary)
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
            } else {
                List {
                    if !recent.isEmpty {
                        Section {
                            ForEach(recent) { emp in
                                ContactListItem(
                                    name: emp.name,
                                    employeeId: emp.employeeId,
                                    isAiAgent: emp.isAiAgent,
                                    isMe: emp.employeeId == myEmployeeId.trimmingCharacters(in: .whitespaces),
                                    lastPreview: lastMessagePreview(messagesByPeer[emp.employeeId] ?? []),
                                    enabled: isConnected
                                ) { onSelectEmployee(emp) }
                            }
                        } header: { Text("最近会话") }
                    }
                    Section {
                        ForEach(rest) { emp in
                            ContactListItem(
                                name: emp.name,
                                employeeId: emp.employeeId,
                                isAiAgent: emp.isAiAgent,
                                isMe: emp.employeeId == myEmployeeId.trimmingCharacters(in: .whitespaces),
                                lastPreview: nil,
                                enabled: isConnected
                            ) { onSelectEmployee(emp) }
                        }
                    } header: { Text("全部联系人") }
                }
                .listStyle(.plain)
            }
        }
    }
    
    private func lastMessagePreview(_ messages: [ChatMessage]) -> String? {
        guard let last = messages.last else { return nil }
        let text = last.content.first?.text?.trimmingCharacters(in: .whitespacesAndNewlines) ?? ""
        if text.count > 40 { return String(text.prefix(40)) + "…" }
        return text.isEmpty ? nil : text
    }
}

struct ContactListItem: View {
    let name: String
    let employeeId: String
    let isAiAgent: Bool
    let isMe: Bool
    let lastPreview: String?
    let enabled: Bool
    let onClick: () -> Void
    
    var body: some View {
        Button(action: onClick) {
            HStack(spacing: 14) {
                Circle()
                    .fill(Color.accentColor.opacity(0.3))
                    .frame(width: 44, height: 44)
                    .overlay(
                        Text(String(name.prefix(1)).uppercased())
                            .font(.headline)
                            .foregroundColor(.accentColor)
                    )
                VStack(alignment: .leading, spacing: 2) {
                    HStack(spacing: 6) {
                        Text(name.isEmpty ? employeeId : name)
                            .font(.body)
                            .lineLimit(1)
                        if isMe {
                            Text("我")
                                .font(.caption2)
                                .foregroundColor(.secondary)
                        }
                        if isAiAgent {
                            Text("AI")
                                .font(.caption2)
                                .foregroundColor(.accentColor)
                        }
                    }
                    Text(lastPreview ?? employeeId)
                        .font(.caption)
                        .foregroundColor(.secondary)
                        .lineLimit(1)
                }
                Spacer()
            }
            .padding(.vertical, 12)
        }
        .disabled(!enabled)
    }
}
