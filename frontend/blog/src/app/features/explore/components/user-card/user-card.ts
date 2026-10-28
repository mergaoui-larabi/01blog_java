import { Component, input, output } from '@angular/core';
import { Avatar } from '../../../../shared/ui/avatar/avatar';
import { FollowButton } from '../../../../shared/ui/buttons/follow/follow-button';

export interface ExploreUser {
  id: string;
  username: string;
  avatarMediaId: string | null;
  followedByCurrentUser: boolean;
}

@Component({
  selector: 'app-user-card',
  imports: [Avatar, FollowButton],
  templateUrl: './user-card.html',
})
export class UserCard {
  readonly user = input.required<ExploreUser>();

  readonly followChange = output<{
    userId: string;
    followed: boolean;
  }>();

  onFollowChange(followed: boolean): void {
    this.followChange.emit({
      userId: this.user().id,
      followed,
    });
  }
}
