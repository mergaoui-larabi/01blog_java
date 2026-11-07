import { inject, Service, signal } from '@angular/core';
import { EMPTY } from 'rxjs';
import { catchError } from 'rxjs/operators';

import { ExploreApiService, UserSearch } from '../../explore/data-access/explore.api.service';


@Service()
export class PostAuthorService {
  private readonly exploreApi = inject(ExploreApiService);

  private readonly authors = signal<Record<string, UserSearch>>({});
  readonly entries = this.authors.asReadonly();

  private readonly pending = new Set<string>();

  authorOf(userId: string): UserSearch | null {
    return this.authors()[userId] ?? null;
  }

  load(userId: string): void {
    if (!userId || this.authors()[userId] || this.pending.has(userId)) {
      return;
    }

    this.pending.add(userId);

    this.exploreApi
      .getUserById(userId)
      .pipe(
        catchError(() => {
          this.pending.delete(userId);
          return EMPTY;
        }),
      )
      .subscribe((author) => {
        this.pending.delete(userId);
        this.authors.update((entries) => ({ ...entries, [userId]: author }));
      });
  }
}
