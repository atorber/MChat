/**
 * openclaw-channel-mchat
 * OpenClaw 渠道插件：将 MoltChat 作为聊天渠道接入 OpenClaw
 */

import { createMChatChannel } from './provider';
import type { MChatChannelConfig, MChatChannelProvider, MChatInboundMessage, MChatSendParams } from './types';
import { CHANNEL_ID } from './types';

export { createMChatChannel, CHANNEL_ID };
export type { MChatChannelConfig, MChatChannelProvider, MChatInboundMessage, MChatSendParams };
