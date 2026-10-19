import { Component, HostListener, computed, inject, signal } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { Logo } from '../../shared/components/logo/logo';
import { ThemeToggle } from '../../shared/ui/theme-toggle/theme-toggle';
import { AuthService } from '../../features/auth/state/auth.service';
import { NotificationsService } from '../../features/notifications/data-access/notifications.service';
import { Avatar } from '../../shared/ui/avatar/avatar';

/**
 * Authenticated application header: brand, primary navigation with an unread
 * notifications badge, a mobile disclosure menu, and an account menu with
 * the current user's Block and logout actions.
 */
@Component({
  selector: 'app-header',
  templateUrl: './header.html',
  imports: [RouterLink, RouterLinkActive, Logo, ThemeToggle, Avatar],
})
export class Header {
  protected readonly authService = inject(AuthService);
  private readonly notificationsService = inject(NotificationsService);

  protected readonly user = this.authService.user;
  protected readonly unreadCount = this.notificationsService.unreadCount;

  protected readonly mobileMenuOpen = signal(false);
  protected readonly accountMenuOpen = signal(false);

  protected readonly userSummary = computed(() => {
    const user = this.user();
    return user ? { username: user.username, displayName: user.username } : null;
  });

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
}
