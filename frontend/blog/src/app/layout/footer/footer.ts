import { Component } from "@angular/core";
import { RouterLink, RouterLinkActive } from "@angular/router";
import { Logo } from "../../shared/components/logo/logo";

@Component({
    selector: 'app-footer',
    imports: [
        Logo,
        RouterLink,
        RouterLinkActive
    ],
    templateUrl: './footer.html'
})
export class Footer { }