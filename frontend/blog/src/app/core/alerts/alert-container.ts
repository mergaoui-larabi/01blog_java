import { Component, inject } from '@angular/core';
import { NgClass } from '@angular/common';
import { AlertService } from './alert.service';

@Component({
  selector: 'app-alert-container',
  templateUrl: './alert-container.html',
  styleUrl: './alert-container.scss',
  imports: [NgClass],
})
export class AlertContainer {
  readonly alertService = inject(AlertService);
}
