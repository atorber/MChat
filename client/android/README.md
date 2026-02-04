## MoltChat (Android)

MoltChat 的 Android 客户端：配置员工连接信息后即可登录，支持与员工聊天、获取员工列表、查询员工信息。

- **连接方式**：仅通过 MQTT 直连 Broker（Paho MQTT），无需 Gateway Bridge。
- **功能**：设置中配置 Broker 地址、用户名、密码、员工 ID → 连接 → 聊天页选择员工进行私聊，支持消息收发与员工列表/详情。
- **技术**：Kotlin + Jetpack Compose，`minSdk 31`。

## 打开项目

用 Android Studio 打开本目录：`client/android`。

## 构建与安装

```bash
cd client/android
./gradlew :app:assembleDebug
./gradlew :app:installDebug
```

未设置 `ANDROID_SDK_ROOT` / `ANDROID_HOME` 时，Gradle 会使用 macOS 默认路径 `~/Library/Android/sdk`。

## 使用

1. 启动 MoltChat 服务端并确保 MQTT Broker 可用。
2. 在手机端打开 **MoltChat** → **设置**，填写：
   - **Broker 地址**：如 `tcp://broker.example.com:1883` 或 `ssl://broker.example.com:8883`
   - **用户名** / **密码**：Broker 认证
   - **员工 ID**：已在服务端/管理后台创建并下发的员工 ID
3. 点击 **连接**，状态栏显示已连接后，进入 **聊天** 即可选择员工进行私聊、收发消息。

## 权限

- 通知（Android 13+）：`POST_NOTIFICATIONS`（用于连接状态等提示）
