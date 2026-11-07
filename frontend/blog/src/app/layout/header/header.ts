import { Component, HostListener, computed, effect, inject, signal } from '@angular/core';
import { NavigationEnd, Router, RouterLink, RouterLinkActive } from '@angular/router';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { filter } from 'rxjs/operators';
import { Logo } from '../../shared/components/logo/logo';
import { ThemeToggle } from '../../shared/ui/theme-toggle/theme-toggle';
import { AuthService } from '../../features/auth/state/auth.service';
import { NotificationService } from '../../features/notifications/state/notification.service';
import { Avatar } from '../../shared/ui/avatar/avatar';
import { throttle } from '../../shared/utils/throttle';

/**
 * Authenticated application header: brand, primary navigation with an unread
 * notifications badge, a mobile disclosure menu, and an account menu with
 * the current user's Block and logout actions.
 */
@Component({
  selector: 'app-header',
  templateUrl: './header.html',
  styleUrl: './header.scss',
  imports: [RouterLink, RouterLinkActive, Logo, ThemeToggle, Avatar],
})
export class Header {
  protected readonly authService = inject(AuthService);
  private readonly router = inject(Router);
  protected readonly user = this.authService.user;
  protected readonly isAdmin = computed(() => this.authService.role() === 'ADMIN');

  private readonly notificationService = inject(NotificationService);
  protected readonly unreadCount = this.notificationService.unreadCount;
  protected readonly unreadCountLabel = computed(() =>
    this.unreadCount() > 99 ? '99+' : `${this.unreadCount()}`,
  );

  private readonly onNotificationsRoute = signal(this.router.url.startsWith('/notifications'));

  /**
   * A push the reader cannot already see. On /notifications the list itself shows the unread rows, so the dot
   * would only repeat what is on screen.
   */
  protected readonly showsUnreadDot = computed(
    () => this.unreadCount() > 0 && !this.onNotificationsRoute(),
  );

  protected readonly mobileMenuOpen = signal(false);
  protected readonly accountMenuOpen = signal(false);

  /**
   * Throttled resize handler so the mobile menu is not closed dozens of times
   * per second while the user is dragging the window edges. Runs at most once
   * per 200 ms.
   */
  private readonly closeMenusOnResize = throttle(() => {
    if (window.innerWidth >= 768) {
      this.closeMenus();
    }
  }, 200);

  constructor() {
    this.router.events
      .pipe(
        filter((event): event is NavigationEnd => event instanceof NavigationEnd),
        takeUntilDestroyed(),
      )
      .subscribe((event) =>
        this.onNotificationsRoute.set(event.urlAfterRedirects.startsWith('/notifications')),
      );

    effect(() => {
      if (this.authService.user()) {
        this.notificationService.loadUnreadCount();
      }
    });
  }

  protected readonly userSummary = computed(() => {
    const user = this.user();
    return user ? { username: user.username, displayName: user.username } : null;
  });

  protected ariaLabel(): string {
    const count = this.unreadCount();

    return count > 0 ? `Notifications, ${count} unread` : 'Notifications';
  }

  protected toggleMobileMenu(): void {
    this.mobileMenuOpen.update((open) => !open);
    this.accountMenuOpen.set(false);
  }

  protected toggleAccountMenu(): void {
    this.accountMenuOpen.update((open) => !open);
    this.mobileMenuOpen.set(false);
  }

  protected closeMenus(): void {
    this.mobileMenuOpen.set(false);
    this.accountMenuOpen.set(false);
  }

  protected logout(): void {
    this.closeMenus();
    this.authService.logout();
  }

  /** Closes popovers on Escape. */
  @HostListener('document:keydown.escape')
  onEscape(): void {
    this.closeMenus();
  }

  /** Closes popovers when clicking anywhere outside the header. */
  @HostListener('document:click', ['$event'])
  onDocumentClick(event: MouseEvent): void {
    const target = event.target as HTMLElement | null;
    if (target && !target.closest('header')) {
      this.closeMenus();
    }
  }

  /** Closes the disclosure menu once the viewport is wide enough for the inline navigation. */
  @HostListener('window:resize')
  onResize(): void {
    this.closeMenusOnResize();
  }
}
