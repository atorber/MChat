import SwiftUI

struct StatusPill: View {
    let connectionState: MChatConnectionState
    let onClick: () -> Void
    
    private var statusText: String {
        switch connectionState {
        case .disconnected: return "未连接"
        case .connecting: return "连接中…"
        case .connected: return "已连接"
        case .error(let msg): return "错误: \(msg)"
        }
    }
    
    private var dotColor: Color {
        switch connectionState {
        case .connected: return Color(red: 0.18, green: 0.8, blue: 0.44)
        case .connecting: return Color(red: 0.95, green: 0.77, blue: 0.06)
        case .error: return Color(red: 0.91, green: 0.3, blue: 0.24)
        case .disconnected: return Color.gray
        }
    }
    
    var body: some View {
        Button(action: onClick) {
            HStack(spacing: 10) {
                HStack(spacing: 8) {
                    Circle()
                        .fill(dotColor)
                        .frame(width: 9, height: 9)
                    VStack(alignment: .leading, spacing: 0) {
                        Text(statusTitle)
                            .font(.subheadline.weight(.semibold))
                        Text(statusText)
                            .font(.caption2)
                            .foregroundColor(.secondary)
                    }
                }
            }
            .padding(.horizontal, 12)
            .padding(.vertical, 8)
            .background(Color(.systemBackground).opacity(0.9))
            .clipShape(RoundedRectangle(cornerRadius: 14))
        }
        .buttonStyle(.plain)
    }
    
    private var statusTitle: String {
        switch connectionState {
        case .connected: return "已连接"
        case .connecting: return "连接中…"
        case .error: return "错误"
        case .disconnected: return "离线"
        }
    }
}
