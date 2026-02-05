# MoltChat iOS

MoltChat 的 iOS 客户端：配置员工连接信息后即可登录，支持与员工聊天、获取员工列表、查询员工信息。

- **连接方式**：仅通过 MQTT 直连 Broker（CocoaMQTT），无需 Gateway Bridge
- **功能**：设置中配置 Broker 地址、用户名、密码、员工 ID → 连接 → 聊天页选择员工进行私聊，支持消息收发与员工列表
- **技术**：Swift + SwiftUI，iOS 15+

## 打开项目

### 方式一：使用 XcodeGen（推荐）

```bash
cd client/ios
brew install xcodegen   # 若未安装
xcodegen
open MoltChat.xcodeproj
```

### 方式二：手动创建 Xcode 项目

1. 在 Xcode 中创建新的 iOS App 项目，命名为 MoltChat
2. 添加 `MoltChat` 目录下所有 Swift 文件到项目
3. 通过 File > Add Package Dependencies 添加 `https://github.com/emqx/CocoaMQTT.git` (2.1.6+)
4. 设置 Bundle ID 为 `com.atorber.moltchat`

## 构建与安装

在 Xcode 中选择目标设备或模拟器，按 Cmd+R 运行。

## 使用

1. 启动 MoltChat 服务端并确保 MQTT Broker 可用
2. 在应用中打开 **设置**（右上角齿轮或顶部状态栏），填写：
   - **Broker 地址**：如 `tcp://broker.example.com:1883` 或 `ssl://broker.example.com:8883`
   - **用户名** / **密码**：Broker 认证
   - **员工 ID**：已在服务端/管理后台创建并下发的员工 ID
3. 点击 **连接**，状态栏显示已连接后，进入 **聊天** 即可选择员工进行私聊、收发消息

## 与 Android 功能对标

| 功能 | Android | iOS |
|------|---------|-----|
| 设置 | ✓ | ✓ |
| 联系人列表 | ✓ | ✓ |
| 私聊 | ✓ | ✓ |
| 消息本地缓存 | ✓ | ✓ |
| 搜索联系人 | ✓ | ✓ |
| 最近会话 | ✓ | ✓ |
