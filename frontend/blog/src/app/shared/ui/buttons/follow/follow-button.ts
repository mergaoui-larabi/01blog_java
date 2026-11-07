import { Component, input, output, signal } from '@angular/core';
import { throttle } from '../../../../shared/utils/throttle';

@Component({
  selector: 'app-follow-button',
  templateUrl: './follow-button.html',
  styleUrl: './follow-button.scss',
})
export class FollowButton {
  readonly followed = input(false);
  readonly followChange = output<boolean>();

  readonly hovering = signal(false);

  /**
   * Throttled follow toggle so rapid repeated clicks cannot fire multiple
   * follow/unfollow requests. Runs at most once per 1000 ms.
   */
  private readonly followThrottled = throttle(() => {
    this.followChange.emit(!this.followed());
  }, 1000);

  toggleFollow(): void {
    this.followThrottled();
  }
}
