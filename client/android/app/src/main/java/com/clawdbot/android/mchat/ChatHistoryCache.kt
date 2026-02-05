package com.clawdbot.android.mchat

import android.content.Context
import android.util.Log
import com.clawdbot.android.chat.ChatMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File

private const val TAG = "ChatHistoryCache"
private const val FILENAME = "mchat_history.json"
private const val MAX_MESSAGES_PER_PEER = 500
private const val MAX_PEERS = 100

/**
 * 本地历史消息缓存：按会话（peer employee_id）持久化消息，切换回对话时先显示缓存。
 */
class ChatHistoryCache(context: Context) {
  private val file = File(context.filesDir, FILENAME)
  private val json = Json {
    ignoreUnknownKeys = true
    encodeDefaults = true
  }

  @Serializable
  private data class CacheRoot(val peers: Map<String, List<ChatMessage>> = emptyMap())

  /**
   * 加载全部缓存，用于连接后恢复会话列表与各会话历史。
   */
  suspend fun loadAll(): Map<String, List<ChatMessage>> = withContext(Dispatchers.IO) {
    if (!file.exists()) return@withContext emptyMap<String, List<ChatMessage>>()
    try {
      val raw = file.readText(Charsets.UTF_8).trim()
      if (raw.isEmpty()) return@withContext emptyMap()
      val root = json.decodeFromString<CacheRoot>(raw)
      root.peers
    } catch (e: Throwable) {
      Log.w(TAG, "loadAll failed", e)
      emptyMap()
    }
  }

  /**
   * 保存某会话的消息列表（会裁剪到 MAX_MESSAGES_PER_PEER 条）。
   */
  suspend fun save(peerId: String, messages: List<ChatMessage>) = withContext(Dispatchers.IO) {
    if (messages.isEmpty()) return@withContext
    try {
      val current = loadAll().toMutableMap()
      val trimmed = messages.takeLast(MAX_MESSAGES_PER_PEER)
      current[peerId] = trimmed
      if (current.size > MAX_PEERS) {
        val byLast = current.entries
          .mapNotNull { (id, list) -> list.maxOfOrNull { it.timestampMs ?: 0L }?.let { id to it } }
          .sortedBy { it.second }
        val toRemove = byLast.take((current.size - MAX_PEERS).coerceAtLeast(0)).map { it.first }
        toRemove.forEach { current.remove(it) }
      }
      val root = CacheRoot(current)
      file.writeText(json.encodeToString(CacheRoot.serializer(), root), Charsets.UTF_8)
    } catch (e: Throwable) {
      Log.w(TAG, "save failed peer=$peerId", e)
    }
  }
}
