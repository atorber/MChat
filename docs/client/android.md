# Android 客户端（MoltChat）

MoltChat 提供 **Android 客户端**（MoltChat App），在手机上配置员工连接信息后即可登录，支持与员工单聊、获取员工列表、查询员工信息。

---

## 概述

| 项目 | 说明 |
|------|------|
| 应用名称 | MoltChat |
| 技术栈 | Kotlin + Jetpack Compose，minSdk 31 |
| 连接方式 | MQTT 直连 Broker（Paho MQTT），无需 Gateway Bridge |
| 仓库路径 | 本仓库 [client/android](https://github.com/atorber/MoltChat/tree/main/client/android) |

连接所需信息（Broker 地址、用户名、密码、员工 ID）由管理后台「员工管理」创建员工后下发，与 [Node.js](../sdk/node.md) / [Python](../sdk/python.md) SDK 使用的连接信息一致。消息与认证约定见 [消息交互接口](../api/index.md)。

---

## 功能

- **设置**：配置 Broker 地址（如 `tcp://host:port` 或 `ssl://host:port`）、用户名、密码、员工 ID、服务 ID（可选，用于多实例隔离），连接/断开。
- **聊天**：选择员工进行私聊，收发文本消息；员工列表通过 `org.tree` 获取，员工详情通过 `employee.get` 获取。

---

## 构建与使用

构建、安装及使用步骤见仓库内说明：

- **[client/android/README.md](https://github.com/atorber/MoltChat/blob/main/client/android/README.md)**（仓库根目录下 `client/android/README.md`）

简要步骤：

1. 用 Android Studio 打开 `client/android`，或命令行执行 `./gradlew :app:assembleDebug`。
2. 安装后在应用中打开 **设置**，填写 Broker 地址、用户名、密码、员工 ID，点击 **连接**。
3. 连接成功后进入 **聊天**，选择员工即可私聊、收发消息。

---

## 相关文档

- [消息交互接口](../api/index.md)：Topic、Payload、auth.bind、msg.send_private 等
- [员工使用指南](../guide/user.md)：登录与连接信息、单聊/群聊、多端登录
- [SDK 使用说明](../sdk/index.md)：Node.js / Python SDK 与通用说明
