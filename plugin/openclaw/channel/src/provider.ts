/**
 * MoltChat 渠道 Provider：使用 mchat-client 连接 MChat，归一化收/发消息
 */

import { MChatClient, sendPrivateMessage, sendGroupMessage } from '@atorber/mchat-client';
import type { InboxMessage, GroupMessage } from '@atorber/mchat-client';
import type { MChatChannelConfig, MChatInboundMessage, MChatSendParams } from './types';
import { CHANNEL_ID } from './types';

const GROUP_ID_PREFIX = 'grp_';

function isGroupThread(thread: string, config: MChatChannelConfig): boolean {
  if (thread.startsWith(GROUP_ID_PREFIX)) return true;
  if (config.groupIds?.length && config.groupIds.includes(thread)) return true;
  return false;
}

export function createMChatChannel(config: MChatChannelConfig): import('./types').MChatChannelProvider {
  if (config.enabled === false) {
    throw new Error('MoltChat channel is disabled in config');
  }

  const client = new MChatClient({
    brokerHost: config.brokerHost,
    brokerPort: config.brokerPort,
    useTls: config.useTls ?? false,
    username: config.username,
    password: config.password,
    employeeId: config.employeeId,
    clientId: config.clientId,
    requestTimeoutMs: config.requestTimeoutMs ?? 30000,
  });

  const inboundListeners: ((msg: MChatInboundMessage) => void)[] = [];

  function emitInbound(msg: MChatInboundMessage): void {
    inboundListeners.forEach((cb) => {
      try {
        cb(msg);
      } catch (e) {
        console.error('[openclaw-channel-mchat] inbound callback error:', e);
      }
    });
  }

  client.on('inbox', (payload: InboxMessage) => {
    const from = payload.from_employee_id;
    const thread = from ?? '';
    emitInbound({
      channel: CHANNEL_ID,
      thread,
      isGroup: false,
      msgId: payload.msg_id,
      fromEmployeeId: from,
      content: payload.content,
      sentAt: payload.sent_at,
      quoteMsgId: payload.quote_msg_id,
    });
  });

  client.on('group', (groupId: string, payload: GroupMessage) => {
    emitInbound({
      channel: CHANNEL_ID,
      thread: groupId,
      isGroup: true,
      msgId: payload.msg_id,
      fromEmployeeId: payload.from_employee_id,
      content: payload.content,
      sentAt: payload.sent_at,
      quoteMsgId: payload.quote_msg_id,
    });
  });

  return {
    get connected(): boolean {
      return client.connected;
    },

    onInbound(cb: (msg: MChatInboundMessage) => void): void {
      inboundListeners.push(cb);
    },

    async start(): Promise<void> {
      await client.connect();
      const groupIds = config.groupIds ?? [];
      for (const gid of groupIds) {
        await client.subscribeGroup(gid);
      }
    },

    async stop(): Promise<void> {
      await client.disconnect();
    },

    async send(params: MChatSendParams): Promise<void> {
      if (!client.connected) {
        throw new Error('MoltChat channel not connected');
      }
      const content = typeof params.content === 'string' ? params.content : params.content;
      const quoteMsgId = params.quoteMsgId;
      if (isGroupThread(params.thread, config)) {
        await sendGroupMessage(client, params.thread, content, quoteMsgId);
      } else {
        await sendPrivateMessage(client, params.thread, content, quoteMsgId);
      }
    },
  };
}
