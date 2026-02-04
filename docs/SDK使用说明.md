# SDK 使用说明

MChat 提供 **Node.js/TypeScript** 与 **Python** 客户端 SDK，封装 MQTT 连接、请求-响应、收件箱/群消息订阅与事件，便于脚本、服务端或 CLI 集成。两套 SDK 的 API 设计对齐，可按所用语言选择。

**文档版本**：1.0

---

## 一、概述

| 语言 / 运行时 | 包名 | 安装来源 |
|---------------|------|----------|
| Node.js ≥ 18 | `mchat-client` | [npm](https://www.npmjs.com/package/mchat-client) |
| Python ≥ 3.10 | `mchat-client` | [PyPI](https://pypi.org/project/mchat-client/) 或本仓库 `client/python` |

连接所需信息（Broker 地址、用户名、密码、员工 ID）通常由管理后台「员工管理」创建员工后下发，或与 `employee.create` 返回的 `mqtt_connection` 一致。消息交互约定见 [消息交互接口与示例](消息交互接口与示例.md)。

---

## 二、Node.js / TypeScript SDK

### 2.1 安装

从 npm 安装（发布后）：

```bash
npm install mchat-client
```

从本仓库引用：

```bash
cd client/node && npm install && npm run build
```

### 2.2 连接选项

与 `employee.create` 返回的 `mqtt_connection` 对应：

| 选项 | 说明 |
|------|------|
| `brokerHost` / `brokerPort` / `useTls` | Broker 地址与是否 TLS |
| `username` / `password` | MQTT 用户名（如 employee_id）、密码 |
| `employeeId` | 当前员工 ID，用于 auth.bind、收件箱订阅、在线状态 |
| `clientId`（可选） | 不传则自动生成，格式 `{employeeId}_{deviceId}_{uuid}` |
| `deviceId`（可选） | 设备标识，默认 `node` |
| `requestTimeoutMs`（可选） | 请求超时毫秒，默认 30000 |
| `skipAuthBind`（可选） | 为 true 时连接后不调用 auth.bind |

### 2.3 基本用法

```ts
import { MChatClient, sendPrivateMessage, getOrgTree } from 'mchat-client';

const client = new MChatClient({
  brokerHost: 'broker.example.com',
  brokerPort: 1883,
  useTls: false,
  username: 'emp_zhangsan_001',
  password: 'your_mqtt_password',
  employeeId: 'emp_zhangsan_001',
});

await client.connect();

// 事件
client.on('inbox', (payload) => console.log('收件箱:', payload));
client.on('group', (groupId, payload) => console.log('群消息', groupId, payload));
client.on('connect', () => console.log('已连接'));
client.on('offline', () => console.log('已断开'));
client.on('error', (err) => console.error('错误:', err));

// 发单聊
await sendPrivateMessage(client, 'emp_lisi_002', '你好');

// 获取组织树
const tree = await getOrgTree(client);
console.log(tree.data?.employees);

// 订阅某群（需已知 group_id）
await client.subscribeGroup('grp_xxx');

await client.disconnect();
```

### 2.4 API 概览

- **MChatClient**
  - `connect()` / `disconnect()`
  - `request(action, params)`：通用请求
  - `subscribeGroup(groupId)` / `unsubscribeGroup(groupId)`
  - `on('inbox' | 'group' | 'connect' | 'offline' | 'error', fn)`
- **便捷方法**：`sendPrivateMessage`、`sendGroupMessage`、`getOrgTree`、`getStorageConfig`、`getAgentCapabilityList`

可运行示例见仓库 `client/node/example/`，执行 `npm install` 与 `npm start`，通过环境变量配置连接信息。

---

## 三、Python SDK

### 3.1 安装

从 PyPI 安装（发布后）：

```bash
pip install mchat-client
```

从本仓库以可编辑方式安装：

```bash
cd client/python && pip install -e .
```

**要求**：Python ≥ 3.10，依赖 paho-mqtt ≥ 2.0.0。

### 3.2 连接参数

构造 `MChatClient` 时传入，与 Node 版选项一一对应（命名为 snake_case）：

| 参数 | 说明 |
|------|------|
| `broker_host` / `broker_port` / `use_tls` | Broker 地址与是否 TLS |
| `username` / `password` | MQTT 用户名、密码 |
| `employee_id` | 当前员工 ID |
| `client_id`（可选） | 不传则自动生成 |
| `device_id`（可选） | 默认 `py` |
| `request_timeout_ms`（可选） | 默认 30000 |
| `skip_auth_bind`（可选） | 为 True 时不调用 auth.bind |

### 3.3 基本用法

```python
from mchat_client import (
    MChatClient,
    send_private_message,
    get_org_tree,
    get_agent_capability_list,
)

client = MChatClient(
    broker_host="broker.example.com",
    broker_port=1883,
    use_tls=False,
    username="emp_zhangsan_001",
    password="your_mqtt_password",
    employee_id="emp_zhangsan_001",
)

client.connect()

# 事件
client.on("inbox", lambda payload: print("收件箱:", payload))
client.on("group", lambda group_id, payload: print("群消息", group_id, payload))
client.on("connect", lambda: print("已连接"))
client.on("offline", lambda: print("已断开"))
client.on("error", lambda err: print("错误:", err))

# 发单聊
send_private_message(client, "emp_lisi_002", "你好")

# 获取组织树
tree = get_org_tree(client)
print(tree.get("data", {}).get("employees"))

# 订阅某群
client.subscribe_group("grp_xxx")

client.disconnect()
```

### 3.4 API 概览

- **MChatClient**
  - `connect()` / `disconnect()`
  - `request(action, params)`：通用请求，成功返回完整响应体，失败抛异常
  - `subscribe_group(group_id)` / `unsubscribe_group(group_id)`
  - `on("inbox" | "group" | "connect" | "offline" | "error", callback)`
- **便捷方法**：`send_private_message`、`send_group_message`、`get_org_tree`、`get_storage_config`、`get_agent_capability_list`

可运行示例见仓库 `client/python/example/`，在 `client/python` 下执行 `python example/main.py`，通过环境变量配置连接信息。

---

## 四、通用说明

### 4.1 连接与身份

- 连接成功后 SDK 会**先订阅** `mchat/msg/resp/{client_id}/+` 再发请求，避免丢响应。
- 默认会调用 **auth.bind**（payload 含 `employee_id`），在服务端建立 client_id 与 employee_id 的映射；若 Broker 已按身份认证且服务端能解析，可设置 `skipAuthBind` / `skip_auth_bind` 为 true 跳过。
- 会发布**在线状态**到 `mchat/status/{employee_id}`（online），并设置 LWT 为 offline。

### 4.2 收件箱与群消息

- **收件箱**（单聊、系统通知等）：连接后自动订阅 `mchat/inbox/{employee_id}`，通过 `on('inbox', ...)` 接收。
- **群消息**：需先获得已加入的群列表（如通过 `getOrgTree` / `get_org_tree` 或后续 `group.list`），再对每个 `group_id` 调用 `subscribeGroup` / `subscribe_group`；通过 `on('group', ...)` 接收。

### 4.3 请求与超时

- 单次请求默认超时 30 秒，可在连接选项中修改。
- 响应中 `code !== 0` 时，Node 版会 reject Promise，Python 版会抛出异常；错误码含义见 [消息交互接口与示例](消息交互接口与示例.md)。

### 4.4 环境变量示例（运行 example）

| 变量 | 说明 |
|------|------|
| `MCHAT_BROKER_HOST` | Broker 主机 |
| `MCHAT_BROKER_PORT` | 端口（如 1883） |
| `MCHAT_USERNAME` | MQTT 用户名 |
| `MCHAT_PASSWORD` | MQTT 密码 |
| `MCHAT_EMPLOYEE_ID` | 员工 ID（不设则用 USERNAME） |
| `MCHAT_USE_TLS` | 为 `1` 时使用 TLS |
| `MCHAT_SEND_TO` | 可选，连接后向该 employee_id 发一条测试消息 |

---

## 五、相关文档

- [消息交互接口与示例](消息交互接口与示例.md)：Topic、Payload、错误码及完整接口说明
- [产品使用手册](产品使用手册.md)：管理员与员工使用说明、典型场景与常见问题
