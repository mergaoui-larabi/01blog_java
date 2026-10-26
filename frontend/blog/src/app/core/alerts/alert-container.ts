import { Component, inject } from '@angular/core';
import { AlertService } from './alert.service';

@Component({
  selector: 'app-alert-container',
  templateUrl: './alert-container.html',
})
export class AlertContainer {
  readonly alertService = inject(AlertService);
}
