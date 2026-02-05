# 通用约定

## Topic 前缀与多实例隔离

为支持同一 MQTT Broker 部署多套 MoltChat 服务实例，Topic 采用可选前缀：

- **默认（单实例/兼容模式）**：不设置 `serviceId`，Topic 为 `mchat/...`
- **多实例隔离**：设置 `serviceId`（如 `org_acme`），Topic 变为 `{serviceId}/mchat/...`

示例：

| serviceId 配置 | 请求 Topic 格式 | 收件箱 Topic 格式 |
|---------------|----------------|------------------|
| 不设置/空 | `mchat/msg/req/{client_id}/{seq_id}` | `mchat/inbox/{employee_id}` |
| `org_acme` | `org_acme/mchat/msg/req/{client_id}/{seq_id}` | `org_acme/mchat/inbox/{employee_id}` |

> 后续文档中以 `mchat/...` 为例，实际使用时根据 `serviceId` 配置自动添加前缀。

## 请求与响应

- **请求**：客户端发布到 **`mchat/msg/req/{client_id}/{seq_id}`**。`client_id` 为当前连接身份，`seq_id` 为本次请求唯一序号（如 UUID），**以 Topic 为准**，用于响应路由与去重。
- **响应**：服务端发布到 **`mchat/msg/resp/{client_id}/{seq_id}`**（与请求的 seq_id 一致）。客户端须**先订阅** `mchat/msg/resp/{client_id}/+` 再发请求；可根据 Topic 最后一层解析 seq_id 做响应关联，无需解析 Payload。
- **Payload**：均为 JSON，UTF-8 编码。
  - **请求**：必须含 **`action`**；**`seq_id` 不必在 Payload 中重复**（Topic 已带 seq_id）。若 Payload 中包含 `seq_id`，则必须与 Topic 中的一致，便于日志与校验。
  - **响应**：必须含 **`code`**（0 成功，非 0 失败）、**`message`**、**`data`**（成功时可为对象或空）；**`seq_id` 可选**，若含则须与 Topic 一致。

## 错误码

| code | 含义     |
|------|----------|
| 0    | 成功     |
| 400  | 参数错误 |
| 401  | 未认证   |
| 403  | 无权限   |
| 404  | 资源不存在 |
| 429  | 限流     |
| 500  | 服务端错误 |
| 504  | 超时（如 Agent 调用超时） |

## 消息 content 类型（发消息时）

| type   | 说明 | body 示例 |
|--------|------|-----------|
| text   | 纯文本 | string 或 `{"text": "..."}` |
| image  | 图片 | `{"url": "https://...", "width", "height"}` |
| file   | 文件 | `{"url": "...", "name", "size"}` |

## 错误响应示例

以下为典型错误响应，Topic 均为 `mchat/msg/resp/{client_id}/{seq_id}`。

**403 无权限**：

```json
{
  "seq_id": "550e8400-e29b-41d4-a716-446655440000",
  "code": 403,
  "message": "无权限执行该操作",
  "data": null
}
```

**400 参数错误**：

```json
{
  "seq_id": "660e8400-e29b-41d4-a716-446655440001",
  "code": 400,
  "message": "member_ids 不能为空",
  "data": null
}
```

**504 超时**：

```json
{
  "seq_id": "770e8400-e29b-41d4-a716-446655440002",
  "code": 504,
  "message": "Agent 响应超时",
  "data": null
}
```
