import { Component, computed, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { ProfilesService } from '../../../profiles/data-access/profiles.service';
import { Avatar } from '../../../../shared/ui/avatar/avatar';

/**
 * Lightweight discovery page: browse writer Blocks and subscribe/unsubscribe.
 * Subscriptions feed directly into the feed query in PostsService.
 */
@Component({
  selector: 'app-explore',
  templateUrl: './explore.html',
  imports: [RouterLink, Avatar],
})
export class Explore {
  private readonly profilesService = inject(ProfilesService);

  protected readonly query = signal('');

  protected readonly profiles = computed(() => {
    const query = this.query().trim().toLowerCase();
    const profiles = this.profilesService.profiles();

    if (!query) {
      return profiles;
    }

    return profiles.filter(
      (profile) =>
        profile.username.toLowerCase().includes(query) ||
        (profile.displayName ?? '').toLowerCase().includes(query),
    );
  });

  protected isFollowing(username: string): boolean {
    return this.profilesService.isFollowing(username);
  }

  protected toggleFollow(username: string): void {
    this.profilesService.toggleFollow(username);
  }
}
