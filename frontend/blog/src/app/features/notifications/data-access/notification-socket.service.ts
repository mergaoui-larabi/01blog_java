import { DestroyRef, effect, inject, NgZone, Service, signal } from '@angular/core';
import { Subject } from 'rxjs';

import { TokenStorageService } from '../../auth/data-access/token-storage.service';
import { environment } from '../../../../environments/environments';
import { Notification } from './notification.api.service';

export type SocketStatus = 'unavailable' | 'connecting' | 'connected' | 'reconnecting' | 'closed';

/**
 * A read-state change made in another tab. The SharedWorker already fans a pushed notification out to every
 * tab, so the channel only has to carry what originates in a tab: the PATCH it sent on the user's behalf.
 */
export type RemoteNotificationEvent =
  { kind: 'read'; notificationId: string; read: boolean } | { kind: 'read-all' };

interface ChannelEnvelope {
  v: 1;
  source: string;
  kind: RemoteNotificationEvent['kind'];
  payload?: { notificationId: string; read: boolean };
  at: number;
}

/**
 * Talks to the SharedWorker that owns the single STOMP-over-WebSocket connection for this origin. Keeping the
 * connection in a worker means one socket for every tab rather than one per tab; the token has to be handed
 * over explicitly because a worker scope has no access to `localStorage`.
 */
@Service()
export class NotificationSocketService {
  private readonly tokenStorage = inject(TokenStorageService);
  private readonly zone = inject(NgZone);
  private readonly destroyRef = inject(DestroyRef);

  private readonly tabId = crypto.randomUUID();
  private readonly worker: SharedWorker | null = this.createWorker();
  private readonly port: MessagePort | null = this.worker?.port ?? null;
  private readonly channel: BroadcastChannel | null =
    typeof BroadcastChannel === 'function'
      ? new BroadcastChannel(environment.notificationsChannelName)
      : null;

  private readonly notificationSource = new Subject<Notification>();
  private readonly remoteSource = new Subject<RemoteNotificationEvent>();

  readonly notification$ = this.notificationSource.asObservable();
  readonly remote$ = this.remoteSource.asObservable();
  readonly status = signal<SocketStatus>(this.port ? 'closed' : 'unavailable');

  constructor() {
    if (this.port) {
      this.port.onmessage = (event: MessageEvent) =>
        this.zone.run(() => this.handleWorkerMessage(event.data));
      this.port.postMessage({ type: 'connect' });
      this.port.start();
    }

    if (this.channel) {
      this.channel.onmessage = (event: MessageEvent<ChannelEnvelope>) =>
        this.zone.run(() => this.handleChannelMessage(event.data));
    }

    // The token is the whole connection lifecycle: sign-in opens the socket, signing in as someone else
    // replaces it, and a null token closes it. `effect` is disposed with this service.
    effect(() => {
      const token = this.tokenStorage.token();
      this.port?.postMessage({
        type: 'token',
        token,
        socketUrl: environment.notificationsSocketEndpoint,
      });
    });

    this.destroyRef.onDestroy(() => this.teardown());
  }

  publishRead(notificationId: string, read: boolean): void {
    this.channel?.postMessage({
      v: 1,
      source: this.tabId,
      kind: 'read',
      payload: { notificationId, read },
      at: Date.now(),
    } satisfies ChannelEnvelope);
  }

  publishAllRead(): void {
    this.channel?.postMessage({
      v: 1,
      source: this.tabId,
      kind: 'read-all',
      at: Date.now(),
    } satisfies ChannelEnvelope);
  }

  private createWorker(): SharedWorker | null {
    // Safari below 16 and Android Chrome have no SharedWorker; the app keeps working over REST there, so this
    // degrades to "no live push" instead of opening a second, per-tab connection.
    if (typeof SharedWorker !== 'function') return null;

    try {
      return new SharedWorker(environment.notificationsWorkerUrl, { name: '01blog-notifications' });
    } catch {
      return null;
    }
  }

  private handleWorkerMessage(data: {
    type?: string;
    status?: SocketStatus;
    notification?: Notification;
  }): void {
    switch (data.type) {
      case 'hello':
      case 'status':
        if (data.status) this.status.set(data.status);
        break;
      case 'notification':
        if (data.notification) this.notificationSource.next(data.notification);
        break;
      case 'auth-suspected':
        this.status.set('closed');
        break;
      default:
        break;
    }
  }

  private handleChannelMessage(envelope: ChannelEnvelope): void {
    if (!envelope || envelope.source === this.tabId) return;

    if (envelope.kind === 'read-all') {
      this.remoteSource.next({ kind: 'read-all' });
      return;
    }

    if (envelope.kind === 'read' && envelope.payload) {
      this.remoteSource.next({
        kind: 'read',
        notificationId: envelope.payload.notificationId,
        read: envelope.payload.read,
      });
    }
  }

  private teardown(): void {
    this.port?.postMessage({ type: 'token', token: null });
    this.port?.close();
    this.channel?.close();
    this.notificationSource.complete();
    this.remoteSource.complete();
  }
}
