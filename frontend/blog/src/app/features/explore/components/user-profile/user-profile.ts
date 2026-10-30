import { Component, computed, inject, input, output } from '@angular/core';
import { Avatar } from '../../../../shared/ui/avatar/avatar';
import { FollowButton } from '../../../../shared/ui/buttons/follow/follow-button';
import { ReportButton } from '../../../../shared/ui/buttons/report/report-button';
import { UserSearch } from '../../data-access/explore.api.service';
import { NavigationEnd, Router } from '@angular/router';
import { filter, map, startWith } from 'rxjs';
import { toSignal } from '@angular/core/rxjs-interop';

@Component({
  selector: 'app-user-profile',
  imports: [Avatar, FollowButton, ReportButton],
  templateUrl: './user-profile.html',
})
export class UserProfile {
  readonly user = input.required<UserSearch | null>();
  private readonly router = inject(Router);

  private readonly currentUrl = toSignal(
    this.router.events.pipe(
      filter((event): event is NavigationEnd => event instanceof NavigationEnd),
      map((event) => event.urlAfterRedirects),
      startWith(this.router.url),
    ),
    { initialValue: this.router.url },
  );
  readonly isOwnProfile = computed(() => this.currentUrl() === '/block/me');

  readonly followChange = output<{
    userId: string;
    followed: boolean;
  }>();

  onFollowChange(followed: boolean): void {
    const user = this.user();
    if (!user) return;

    this.followChange.emit({
      userId: user.id,
      followed,
    });
  }
}
