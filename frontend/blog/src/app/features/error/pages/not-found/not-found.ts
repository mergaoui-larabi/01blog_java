import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';

/**
 * 404 error page for unknown routes.
 *
 * The `path: '**'` wildcard in `app.routes.ts` always ends up here, so
 * requests for links that do not exist in the app's route table show this
 * page instead of silently landing on the feed.
 */
@Component({
  selector: 'app-not-found',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './not-found.html',
  styleUrl: './not-found.scss'
})
export class NotFound {}
