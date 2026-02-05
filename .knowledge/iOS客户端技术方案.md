# iOS 客户端技术方案

> 参考 Android 客户端实现，保持功能和 UI 交互一致。

## 功能对标

| 功能 | Android | iOS |
|------|---------|-----|
| 连接配置 | Broker 地址、用户名、密码、员工 ID | 同左 |
| 连接/断开 | 按钮切换 | 同左 |
| 联系人列表 | org.tree 获取，搜索、最近会话、全部联系人 | 同左 |
| 私聊 | msg.send_private，收件箱接收 | 同左 |
| 消息缓存 | ChatHistoryCache 本地 JSON | 同左 |
| 设置入口 | 顶部 StatusPill 点击 / 右上角齿轮 | 同左 |

## 技术选型

| 层级 | Android | iOS |
|------|---------|-----|
| UI | Jetpack Compose | SwiftUI |
| MQTT | Paho (Eclipse) | CocoaMQTT |
| 配置存储 | EncryptedSharedPreferences | Keychain + UserDefaults |
| 历史缓存 | File JSON | FileManager JSON |
| 并发 | Kotlin Coroutines | Swift async/await |
| 最低版本 | minSdk 31 | iOS 15+ |

## 方案对比

### 方案 A：原生 Swift/SwiftUI（推荐）
- **优点**：遵循 iOS HIG，维护简单，与 Android 各自独立演进
- **缺点**：业务逻辑需双端实现，协议层可参考 Node/Python SDK
- **适用**：当前阶段，快速交付

### 方案 B：Kotlin Multiplatform (KMP)
- **优点**：共享 MChatConnection、MChatChatController、ChatHistoryCache 等逻辑
- **缺点**：需要重构 Android，引入 KMP 构建，iOS MQTT 仍需原生桥接
- **适用**：中长期，团队熟悉 Kotlin

### 方案 C：Compose Multiplatform
- **优点**：可共享 UI 与逻辑
- **缺点**：Compose for iOS 仍在演进，包体积较大
- **适用**：实验性

**结论**：采用方案 A，先实现 Swift 原生 iOS 客户端，与 Android 功能一致。
