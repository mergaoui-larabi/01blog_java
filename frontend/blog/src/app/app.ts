import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { AlertContainer } from './core/alerts/alert-container';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, AlertContainer],
  templateUrl: './app.html',
})
export class App {}
