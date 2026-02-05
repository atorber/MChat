package com.clawdbot.android.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.clawdbot.android.MainViewModel
import com.clawdbot.android.mchat.MChatConnectionState
import com.clawdbot.android.ui.chat.ChatSheetContent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RootScreen(viewModel: MainViewModel) {
  var sheet by remember { mutableStateOf<Sheet?>(null) }
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
  val safeOverlayInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal)
  val connectionState by viewModel.connectionState.collectAsState()
  val currentPeerId by viewModel.chatCurrentPeerId.collectAsState()
  val statusText = when (connectionState) {
    is MChatConnectionState.Disconnected -> "未连接"
    is MChatConnectionState.Connecting -> "连接中…"
    is MChatConnectionState.Connected -> "已连接"
    is MChatConnectionState.Error -> "错误: ${(connectionState as MChatConnectionState.Error).message}"
  }

  Box(modifier = Modifier.fillMaxSize()) {
    Box(
      modifier = Modifier
        .fillMaxSize()
        .windowInsetsPadding(safeOverlayInsets)
        .padding(top = 56.dp),
    ) {
      ChatSheetContent(viewModel = viewModel)
    }

    if (currentPeerId == null) {
      Popup(alignment = Alignment.TopStart, properties = PopupProperties(focusable = false)) {
        StatusPill(
          gateway = if (connectionState is MChatConnectionState.Connected) GatewayState.Connected
          else if (connectionState is MChatConnectionState.Connecting) GatewayState.Connecting
          else if (connectionState is MChatConnectionState.Error) GatewayState.Error
          else GatewayState.Disconnected,
          voiceEnabled = false,
          activity = null,
          mqttStatus = statusText,
          onClick = { sheet = Sheet.Settings },
          modifier = Modifier.windowInsetsPadding(safeOverlayInsets).padding(start = 12.dp, top = 12.dp),
        )
      }
    }

    Popup(alignment = Alignment.TopEnd, properties = PopupProperties(focusable = false)) {
      Box(modifier = Modifier.windowInsetsPadding(safeOverlayInsets).padding(end = 12.dp, top = 12.dp)) {
        OverlayIconButton(
          onClick = { sheet = Sheet.Settings },
          icon = { Icon(Icons.Default.Settings, contentDescription = "设置") },
        )
      }
    }
  }

  val currentSheet = sheet
  if (currentSheet != null) {
    ModalBottomSheet(
      onDismissRequest = { sheet = null },
      sheetState = sheetState,
    ) {
      SettingsSheet(viewModel = viewModel)
    }
  }
}

private enum class Sheet { Settings }

@Composable
private fun OverlayIconButton(
  onClick: () -> Unit,
  icon: @Composable () -> Unit,
  containerColor: androidx.compose.ui.graphics.Color? = null,
  contentColor: androidx.compose.ui.graphics.Color? = null,
) {
  FilledTonalIconButton(
    onClick = onClick,
    modifier = Modifier.size(44.dp),
    colors = IconButtonDefaults.filledTonalIconButtonColors(
      containerColor = containerColor ?: overlayContainerColor(),
      contentColor = contentColor ?: overlayIconColor(),
    ),
  ) {
    icon()
  }
}
