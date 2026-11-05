import { Component, computed, effect, inject, input, output } from '@angular/core';
import { Avatar } from '../../../../shared/ui/avatar/avatar';
import { TimeAgoPipe } from '../../../../shared/pipes/time-ago.pipe';
import { Notification } from '../../data-access/notification.api.service';
import { NotificationService } from '../../state/notification.service';
import { PostAuthorService } from '../../../posts/state/post-author.service';

@Component({
  selector: 'app-notification-item',
  imports: [Avatar, TimeAgoPipe],
  templateUrl: './notification-item.html',
  styleUrl: './notification-item.scss',
})
export class NotificationItem {
  readonly notification = input.required<Notification>();

  /** The page owns opening the post, because that is where the viewer dialog lives. */
  readonly opened = output<Notification>();

  private readonly notificationService = inject(NotificationService);
  private readonly authorService = inject(PostAuthorService);

  protected readonly actor = computed(() => this.authorService.authorOf(this.notification().actorId));
  protected readonly text = computed(() => this.notification().message ?? this.fallbackText());
  protected readonly pending = computed(() =>
    this.notificationService.readPending(this.notification().id),
  );

  constructor() {
    effect(() => {
      this.authorService.load(this.notification().actorId);
    });
  }

  protected view(): void {
    this.opened.emit(this.notification());
  }

  protected toggleRead(): void {
    this.notificationService.toggleRead(this.notification());
  }

  private fallbackText(): string {
    const actor = this.actor()?.username;

    return actor ? `@${actor} published a new post` : 'A user published a new post';
  }
}
