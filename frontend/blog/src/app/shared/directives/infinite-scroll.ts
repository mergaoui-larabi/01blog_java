import {
  afterNextRender,
  DestroyRef,
  Directive,
  ElementRef,
  inject,
  NgZone,
  output,
} from '@angular/core';

// Distance from the bottom of the viewport at which the next page is requested.
const PREFETCH_PX = 240;

@Directive({
  selector: '[appInfiniteScroll]',
})
export class InfiniteScroll {
  readonly reached = output<void>();

  private readonly element = inject(ElementRef);
  private readonly zone = inject(NgZone);
  private readonly destroyRef = inject(DestroyRef);

  constructor() {
    afterNextRender(() => {
      const observer = new IntersectionObserver(
        (entries) => {
          if (!entries.some((entry) => entry.isIntersecting)) return;

          // IntersectionObserver callbacks run outside the zone, so the signals the
          // handler writes would otherwise not schedule change detection.
          this.zone.run(() => this.reached.emit());
        },
        { rootMargin: `0px 0px ${PREFETCH_PX}px` },
      );

      observer.observe(this.element.nativeElement);
      this.destroyRef.onDestroy(() => observer.disconnect());
    });
  }
}
