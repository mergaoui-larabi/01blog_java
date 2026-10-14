import { Component } from '@angular/core';
import {
    RouterLink,
    RouterLinkActive
} from '@angular/router';
import { Logo } from '../../shared/components/logo/logo';
import { ThemeToggle } from '../../shared/ui/theme-toggle/theme-toggle';

@Component({
    selector: 'app-header',
    imports: [
        Logo,
        RouterLink,
        RouterLinkActive,
        ThemeToggle
    ],
    templateUrl: './header.html'
})
export class Header { }