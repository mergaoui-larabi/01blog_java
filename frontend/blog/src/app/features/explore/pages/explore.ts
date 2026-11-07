import { Component, inject, OnInit } from '@angular/core';
import { ExploreService } from '../state/explore.service';
import { UserCard } from '../components/user-card/user-card';
import { debounce } from '../../../shared/utils/debounce';

@Component({
  selector: 'app-explore',
  imports: [UserCard],
  templateUrl: './explore.html',
})
export class Explore implements OnInit {
  readonly exploreService = inject(ExploreService);

  /**
   * Debounced search so the API is not hit on every keystroke. Fires once the
   * user has stopped typing for 300 ms.
   */
  protected readonly search = debounce((term: string) => {
    this.exploreService.searchUsers(term);
  }, 300);

  ngOnInit(): void {
    this.exploreService.loadUsers();
  }

  loadMore(): void {
    this.exploreService.loadMore();
  }

  onFollowChange(event: { userId: string; followed: boolean }): void {
    if (event.followed) this.exploreService.followUser(event.userId);
    if (!event.followed) this.exploreService.unfollowUser(event.userId);
  }
}
