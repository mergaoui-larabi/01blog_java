import { Component, computed, input } from '@angular/core';
import { UserSummary } from '../../../features/profiles/models/user-profile.model';

export type AvatarSize = 'xs' | 'sm' | 'md' | 'lg';

const SIZE_CLASSES: Record<AvatarSize, string> = {
  xs: 'h-6 w-6 text-[10px]',
  sm: 'h-8 w-8 text-xs',
  md: 'h-10 w-10 text-sm',
  lg: 'h-16 w-16 text-xl',
};

/**
 * User avatar: renders the image when available, otherwise a deterministic
 * initials circle consistent with the design tokens.
 *
 * `name` provides the accessible name for the avatar image; when the avatar
 * sits next to the user's name (e.g. in a post header) pass `decorative` so
 * screen readers don't announce it twice.
 */
@Component({
  selector: 'app-avatar',
  templateUrl: './avatar.html',
})
export class Avatar {
  readonly user = input.required<UserSummary>();
  readonly size = input<AvatarSize>('md');
  readonly name = input<string>('');
  readonly decorative = input(false);

  protected readonly sizeClass = computed(() => SIZE_CLASSES[this.size()]);

  protected readonly initials = computed(() => {
    const source = this.user().displayName?.trim() || this.user().username;
    const parts = source.split(/[\s._-]+/).filter(Boolean);

    if (parts.length === 0) {
      return '?';
    }

    if (parts.length === 1) {
      return parts[0].slice(0, 2).toUpperCase();
    }

    return (parts[0][0] + parts[1][0]).toUpperCase();
  });
}
