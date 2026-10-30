import { Component, inject } from '@angular/core';
import { ThemeService } from '../../../core/theme/theme.service';

@Component({
    selector: 'app-theme-toggle',
    templateUrl: './theme-toggle.html',
    styleUrl: './theme-toggle.scss',
})
export class ThemeToggle {
    protected readonly themeService = inject(ThemeService);
}