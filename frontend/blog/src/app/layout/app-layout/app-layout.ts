import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { Header } from '../header/header';
import { Footer } from '../footer/footer';



@Component({
  selector: 'app-root',
  imports: [
    RouterOutlet,
    Header,
    Footer
  ],
  templateUrl: './app-layout.html'
})
export class AppLayout {}