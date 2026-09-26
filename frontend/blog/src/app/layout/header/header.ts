import { Component } from '@angular/core';
import {
    RouterLink,
    RouterLinkActive
} from '@angular/router';
import { Logo } from '../../shared/components/logo/logo';

@Component({
    selector: 'app-header',
    imports: [
        Logo,
        RouterLink,
        RouterLinkActive
    ],
    templateUrl: './header.html'
})
export class Header { }