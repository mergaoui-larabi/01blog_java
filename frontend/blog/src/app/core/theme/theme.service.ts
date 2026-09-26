import { DOCUMENT } from '@angular/common';
import { Injectable, inject, signal } from '@angular/core';

export type Theme = 'light' | 'dark';

@Injectable({
  providedIn: 'root',
})
export class ThemeService {
  private readonly document = inject(DOCUMENT);

  private readonly currentTheme = signal<Theme>(this.getInitialTheme());

  readonly theme = this.currentTheme.asReadonly();

  constructor() {
    this.applyTheme(this.currentTheme());
  }

  toggle(): void {
    this.setTheme(
      this.currentTheme() === 'light' ? 'dark' : 'light',
    );
  }

  setTheme(theme: Theme): void {
    this.currentTheme.set(theme);
    this.applyTheme(theme);
    localStorage.setItem('theme', theme);
  }

  private applyTheme(theme: Theme): void {
    this.document.documentElement.classList.toggle(
      'dark',
      theme === 'dark',
    );
  }

  private getInitialTheme(): Theme {
    const savedTheme = localStorage.getItem('theme');

    if (savedTheme === 'light' || savedTheme === 'dark') {
      return savedTheme;
    }

    return 'light';
  }
}