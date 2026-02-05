package com.clawdbot.android.ui.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.clawdbot.android.MainViewModel
import com.clawdbot.android.chat.ChatMessage
import com.clawdbot.android.mchat.MChatChatController

@Composable
fun ChatSheetContent(viewModel: MainViewModel) {
  val employees by viewModel.chatEmployees.collectAsState()
  val currentPeerId by viewModel.chatCurrentPeerId.collectAsState()
  val messagesByPeer by viewModel.chatMessagesByPeer.collectAsState()
  val messages = currentPeerId?.let { messagesByPeer[it] ?: emptyList() } ?: emptyList()
  val errorText by viewModel.chatErrorText.collectAsState()
  val isConnected by viewModel.isConnected.collectAsState()

  if (currentPeerId != null) {
    ChatConversation(
      peerId = currentPeerId!!,
      peerName = employees.find { it.employee_id == currentPeerId }?.name ?: currentPeerId!!,
      messages = messages,
      errorText = errorText,
      canSend = isConnected,
      onBack = { viewModel.selectPeer(null) },
      onSend = { text -> viewModel.sendMessage(currentPeerId!!, text) },
    )
  } else {
    val myEmployeeId by viewModel.mchatEmployeeId.collectAsState()
    ChatEmployeeList(
      employees = employees,
      messagesByPeer = messagesByPeer,
      myEmployeeId = myEmployeeId,
      isConnected = isConnected,
      onSelectEmployee = { viewModel.selectPeer(it.employee_id) },
    )
  }
}

private fun lastMessagePreview(messages: List<ChatMessage>): String? {
  val last = messages.lastOrNull() ?: return null
  val text = last.content.firstOrNull()?.text?.trim()
  return text?.take(40)?.let { if (it.length == 40) "$it…" else it }
}

private fun lastMessageTimeMs(messages: List<ChatMessage>): Long? =
  messages.lastOrNull()?.timestampMs

@Composable
private fun ChatEmployeeList(
  employees: List<MChatChatController.EmployeeEntry>,
  messagesByPeer: Map<String, List<ChatMessage>>,
  myEmployeeId: String,
  isConnected: Boolean,
  onSelectEmployee: (MChatChatController.EmployeeEntry) -> Unit,
) {
  val myId = myEmployeeId.trim()
  var searchQuery by remember { mutableStateOf("") }
  val query = searchQuery.trim().lowercase()

  val recentFirst = remember(employees, messagesByPeer, query) {
    val withMessages = messagesByPeer.mapNotNull { (peerId, msgs) ->
      val emp = employees.find { it.employee_id == peerId } ?: return@mapNotNull null
      Triple(emp, msgs, lastMessageTimeMs(msgs) ?: 0L)
    }.sortedByDescending { it.third }
    val recentIds = withMessages.map { it.first.employee_id }.toSet()
    val rest = employees.filter { it.employee_id !in recentIds }
    val filteredRecent = if (query.isEmpty()) withMessages
      else withMessages.filter { (e, _, _) -> e.name.lowercase().contains(query) || e.employee_id.lowercase().contains(query) }
    val filteredRest = if (query.isEmpty()) rest
      else rest.filter { it.name.lowercase().contains(query) || it.employee_id.lowercase().contains(query) }
    filteredRecent.map { it.first } to filteredRest
  }

  val (recentList, restList) = recentFirst
  val hasRecent = recentList.isNotEmpty()
  val totalCount = recentList.size + restList.size

  Column(Modifier.fillMaxSize()) {
    Text(
      "联系人",
      style = MaterialTheme.typography.titleLarge,
      modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
    )
    if (!isConnected) {
      Text(
        "请先在设置中连接",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.error,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
      )
    }
    OutlinedTextField(
      value = searchQuery,
      onValueChange = { searchQuery = it },
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 8.dp),
      placeholder = { Text("搜索姓名或 ID") },
      leadingIcon = {
        Icon(Icons.Default.Search, contentDescription = null, Modifier.size(20.dp))
      },
      singleLine = true,
      shape = MaterialTheme.shapes.medium,
      colors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = MaterialTheme.colorScheme.outline,
        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
      ),
    )
    if (totalCount == 0) {
      Text(
        if (query.isEmpty()) "暂无联系人" else "无匹配结果",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(32.dp),
      )
    } else {
      LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(0.dp),
      ) {
        if (hasRecent) {
          item {
            Text(
              "最近会话",
              style = MaterialTheme.typography.labelMedium,
              color = MaterialTheme.colorScheme.primary,
              modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )
          }
          items(recentList) { emp ->
            val msgs = messagesByPeer[emp.employee_id] ?: emptyList()
            ContactListItem(
              name = emp.name,
              employeeId = emp.employee_id,
              isAiAgent = emp.is_ai_agent,
              isMe = emp.employee_id == myId,
              lastPreview = lastMessagePreview(msgs),
              enabled = isConnected,
              onClick = { onSelectEmployee(emp) },
            )
          }
          item {
            Spacer(Modifier.height(8.dp))
            Text(
              "全部联系人",
              style = MaterialTheme.typography.labelMedium,
              color = MaterialTheme.colorScheme.primary,
              modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )
          }
        } else if (restList.isNotEmpty()) {
          item {
            Text(
              "全部联系人",
              style = MaterialTheme.typography.labelMedium,
              color = MaterialTheme.colorScheme.primary,
              modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )
          }
        }
        items(restList) { emp ->
          ContactListItem(
            name = emp.name,
            employeeId = emp.employee_id,
            isAiAgent = emp.is_ai_agent,
            isMe = emp.employee_id == myId,
            lastPreview = null,
            enabled = isConnected,
            onClick = { onSelectEmployee(emp) },
          )
        }
      }
    }
  }
}

@Composable
private fun ContactListItem(
  name: String,
  employeeId: String,
  isAiAgent: Boolean,
  isMe: Boolean,
  lastPreview: String?,
  enabled: Boolean,
  onClick: () -> Unit,
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clickable(enabled = enabled) { onClick() }
      .padding(horizontal = 16.dp, vertical = 12.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Box(
      modifier = Modifier
        .size(44.dp)
        .clip(CircleShape)
        .background(MaterialTheme.colorScheme.primaryContainer),
      contentAlignment = Alignment.Center,
    ) {
      Text(
        text = name.take(1).uppercase().ifEmpty { "?" },
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onPrimaryContainer,
      )
    }
    Spacer(Modifier.width(14.dp))
    Column(modifier = Modifier.weight(1f)) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
          text = name.ifEmpty { employeeId },
          style = MaterialTheme.typography.bodyLarge,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
        if (isMe) {
          Spacer(Modifier.width(6.dp))
          Text(
            "我",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.tertiary,
          )
        }
        if (isAiAgent) {
          Spacer(Modifier.width(6.dp))
          Text(
            "AI",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
          )
        }
      }
      Text(
        text = lastPreview ?: employeeId,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
      )
    }
  }
  HorizontalDivider(
    modifier = Modifier.padding(start = 74.dp),
    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
  )
}

@Composable
private fun ChatConversation(
  peerId: String,
  peerName: String,
  messages: List<ChatMessage>,
  errorText: String?,
  canSend: Boolean,
  onBack: () -> Unit,
  onSend: (String) -> Unit,
) {
  var input by rememberSaveable { mutableStateOf("") }

  Column(Modifier.fillMaxSize()) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(8.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      IconButton(onClick = onBack) {
        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
      }
      Text("$peerName ($peerId)", style = MaterialTheme.typography.titleMedium)
    }
    if (errorText != null) {
      Text(errorText, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(horizontal = 12.dp))
    }
    ChatMessageListCard(
      messages = messages,
      pendingRunCount = 0,
      pendingToolCalls = emptyList(),
      streamingAssistantText = null,
      modifier = Modifier.weight(1f),
    )
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(8.dp),
      verticalAlignment = Alignment.Bottom,
      horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      OutlinedTextField(
        value = input,
        onValueChange = { input = it },
        modifier = Modifier.weight(1f),
        placeholder = { Text("输入消息") },
        singleLine = false,
        maxLines = 4,
      )
      androidx.compose.material3.FilledTonalButton(
        onClick = {
          val t = input.trim()
          if (t.isNotEmpty() && canSend) {
            onSend(t)
            input = ""
          }
        },
        enabled = canSend && input.trim().isNotEmpty(),
      ) {
        Icon(Icons.Default.ArrowUpward, contentDescription = "发送")
      }
    }
  }
}
