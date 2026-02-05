import Foundation

/// 本地历史消息缓存，对标 Android ChatHistoryCache
final class ChatHistoryCache {
    private let fileURL: URL
    private let encoder = JSONEncoder()
    private let decoder = JSONDecoder()
    private let queue = DispatchQueue(label: "mchat.history", qos: .utility)
    
    private let maxMessagesPerPeer = 500
    private let maxPeers = 100
    
    init() {
        let dir = FileManager.default.urls(for: .documentDirectory, in: .userDomainMask)[0]
        self.fileURL = dir.appendingPathComponent("mchat_history.json")
    }
    
    private struct CacheRoot: Codable {
        var peers: [String: [ChatMessage]] = [:]
    }
    
    func loadAll() async -> [String: [ChatMessage]] {
        await withCheckedContinuation { cont in
            queue.async {
                guard FileManager.default.fileExists(atPath: self.fileURL.path),
                      let data = try? Data(contentsOf: self.fileURL),
                      let root = try? self.decoder.decode(CacheRoot.self, from: data) else {
                    cont.resume(returning: [:])
                    return
                }
                cont.resume(returning: root.peers)
            }
        }
    }
    
    func save(peerId: String, messages: [ChatMessage]) async {
        guard !messages.isEmpty else { return }
        await withCheckedContinuation { (cont: CheckedContinuation<Void, Never>) in
            queue.async {
                var current: [String: [ChatMessage]] = [:]
                if FileManager.default.fileExists(atPath: self.fileURL.path),
                   let data = try? Data(contentsOf: self.fileURL),
                   let root = try? self.decoder.decode(CacheRoot.self, from: data) {
                    current = root.peers
                }
                let trimmed = Array(messages.suffix(self.maxMessagesPerPeer))
                current[peerId] = trimmed
                if current.count > self.maxPeers {
                    let byLast = current.compactMap { (id, list) -> (String, Int64)? in
                        let t = list.compactMap(\.timestampMs).max() ?? 0
                        return (id, t)
                    }.sorted { $0.1 < $1.1 }
                    let toRemove = byLast.prefix(current.count - self.maxPeers).map(\.0)
                    toRemove.forEach { current.removeValue(forKey: $0) }
                }
                let root = CacheRoot(peers: current)
                if let data = try? self.encoder.encode(root) {
                    try? data.write(to: self.fileURL)
                }
                cont.resume()
            }
        }
    }
}
