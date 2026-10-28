import { Component, inject, OnInit } from '@angular/core';
import { ExploreService } from '../state/explore.service';
import { UserCard } from '../components/user-card/user-card';

@Component({
  selector: 'app-explore',
  imports: [UserCard],
  templateUrl: './explore.html',
})
export class Explore implements OnInit {
  readonly exploreService = inject(ExploreService);

  ngOnInit(): void {
    this.exploreService.loadUsers();
  }

  search(term: string): void {
    this.exploreService.searchUsers(term);
  }

  loadMore(): void {
    this.exploreService.loadMore();
  }

  onFollowChange(event: { userId: string; followed: boolean }): void {
    if (event.followed) this.exploreService.followUser(event.userId);
    if (!event.followed) this.exploreService.unfollowUser(event.userId);
  }
}
