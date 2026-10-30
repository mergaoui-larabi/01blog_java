import { Component } from "@angular/core";
import { Logo } from "../../../shared/components/logo/logo";
import { RouterOutlet } from "@angular/router";
import { ThemeToggle } from "../../../shared/ui/theme-toggle/theme-toggle";


@Component({
    selector: 'auth-layout',
    imports: [
        RouterOutlet,
        Logo,
        ThemeToggle
    ],
    templateUrl: 'auth-layout.html',
    styleUrl: 'auth-layout.scss',
})
export class AuthLayout { }