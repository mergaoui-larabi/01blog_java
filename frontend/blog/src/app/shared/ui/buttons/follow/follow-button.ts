import { Component, input, output, signal } from '@angular/core';

@Component({
  selector: 'app-follow-button',
  templateUrl: './follow-button.html',
  styleUrl: './follow-button.scss',
})
export class FollowButton {
  readonly followed = input(false);
  readonly followChange = output<boolean>();

  readonly hovering = signal(false);

  toggleFollow(): void {
    this.followChange.emit(!this.followed());
  }
}
