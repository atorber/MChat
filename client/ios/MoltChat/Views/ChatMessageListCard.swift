import SwiftUI

struct ChatMessageListCard: View {
    let messages: [ChatMessage]
    
    var body: some View {
        ScrollViewReader { proxy in
            ScrollView {
                LazyVStack(alignment: .leading, spacing: 14) {
                    ForEach(messages) { msg in
                        ChatMessageBubble(message: msg)
                    }
                }
                .padding(12)
            }
            .onChange(of: messages.count) { _ in
                if let last = messages.last {
                    withAnimation {
                        proxy.scrollTo(last.id, anchor: .bottom)
                    }
                }
            }
        }
    }
}

struct ChatMessageBubble: View {
    let message: ChatMessage
    
    private var isUser: Bool { message.role.lowercased() == "user" }
    
    var body: some View {
        HStack {
            if isUser { Spacer(minLength: 40) }
            VStack(alignment: isUser ? .trailing : .leading, spacing: 4) {
                ForEach(Array(message.content.enumerated()), id: \.offset) { _, part in
                    if part.type == "text", let text = part.text {
                        Text(text)
                            .font(.body)
                            .foregroundColor(isUser ? .white : .primary)
                    }
                }
            }
            .padding(.horizontal, 12)
            .padding(.vertical, 10)
            .background(isUser ? Color.accentColor : Color(.systemGray5))
            .clipShape(RoundedRectangle(cornerRadius: 16))
            if !isUser { Spacer(minLength: 40) }
        }
    }
}
