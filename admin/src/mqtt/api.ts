/**
 * MQTT 请求-响应封装：连接 Broker，订阅 resp，通过 request(action, params) 发请求并等待响应
 */

import mqtt, { type MqttClient } from 'mqtt';

/** 根据 serviceId 生成 topic 前缀：有 serviceId 则 "{serviceId}/mchat"，否则 "mchat" */
function getTopicPrefix(serviceId?: string): string {
  const sid = serviceId?.trim();
  return sid ? `${sid}/mchat` : 'mchat';
}

export interface MqttResponse {
  code: number;
  message: string;
  data?: unknown;
}

type Pending = { resolve: (r: MqttResponse) => void; reject: (e: Error) => void; timeout: ReturnType<typeof setTimeout> };

const REQUEST_TIMEOUT_MS = 30000;

function genSeqId(): string {
  return 'seq_' + Math.random().toString(36).slice(2, 12) + '_' + Date.now();
}

/** 未指定时：优先用用户名原样（部分 Broker 要求 ClientId=用户名）；若含非法字符可被 Broker 拒绝，此时需在登录页填写「Client ID」 */
function defaultClientId(username: string): string {
  const sanitized = username.replace(/[^a-zA-Z0-9_.-]/g, '_').slice(0, 64);
  return sanitized || 'admin';
}

export class MqttApi {
  private client: MqttClient | null = null;
  private clientId: string = '';
  private serviceId: string = '';
  private pending = new Map<string, Pending>();

  get connected(): boolean {
    return this.client?.connected ?? false;
  }

  getClientId(): string {
    return this.clientId;
  }

  private get topicPrefix(): string {
    return getTopicPrefix(this.serviceId);
  }

  connect(wsUrl: string, username: string, password: string, clientIdOverride?: string, serviceId?: string): Promise<void> {
    this.clientId = (clientIdOverride?.trim() || defaultClientId(username));
    this.serviceId = serviceId?.trim() || '';
    const RESP_PREFIX = `${this.topicPrefix}/msg/resp/`;
    return new Promise((resolve, reject) => {
      const c = mqtt.connect(wsUrl, {
        clientId: this.clientId,
        username,
        password,
        clean: true,
        connectTimeout: 8000,
      });
      this.client = c;
      c.on('connect', () => {
        c.subscribe(`${RESP_PREFIX}${this.clientId}/+`, { qos: 1 }, (err) => {
          if (err) {
            reject(err);
            return;
          }
          resolve();
        });
      });
      c.on('message', (topic: string, payload: Buffer) => {
        const respPrefix = `${this.topicPrefix}/msg/resp/`;
        if (!topic.startsWith(respPrefix + this.clientId + '/')) return;
        const seqId = topic.slice((respPrefix + this.clientId + '/').length);
        const p = this.pending.get(seqId);
        if (!p) return;
        this.pending.delete(seqId);
        clearTimeout(p.timeout);
        try {
          const body = JSON.parse(payload.toString('utf8')) as MqttResponse;
          p.resolve(body);
        } catch {
          p.reject(new Error('Invalid response JSON'));
        }
      });
      c.on('error', (err) => reject(err));
    });
  }

  request<T = unknown>(action: string, params: Record<string, unknown> = {}): Promise<MqttResponse & { data?: T }> {
    if (!this.client?.connected) return Promise.reject(new Error('Not connected'));
    const seqId = genSeqId();
    const REQ_PREFIX = `${this.topicPrefix}/msg/req/`;
    const topic = `${REQ_PREFIX}${this.clientId}/${seqId}`;
    const payload = JSON.stringify({ action, ...params });
    return new Promise((resolve, reject) => {
      const timeout = setTimeout(() => {
        if (this.pending.delete(seqId)) reject(new Error('Request timeout'));
      }, REQUEST_TIMEOUT_MS);
      this.pending.set(seqId, { resolve: resolve as (r: MqttResponse) => void, reject, timeout });
      this.client!.publish(topic, payload, { qos: 1 }, (err) => {
        if (err) {
          this.pending.delete(seqId);
          clearTimeout(timeout);
          reject(err);
        }
      });
    });
  }

  disconnect(): void {
    this.pending.forEach((p) => {
      clearTimeout(p.timeout);
      p.reject(new Error('Disconnected'));
    });
    this.pending.clear();
    this.client?.end(true);
    this.client = null;
    this.serviceId = '';
  }
}

export const mqttApi = new MqttApi();
