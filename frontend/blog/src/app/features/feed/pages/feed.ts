import { Component } from '@angular/core';
import { CreatePostButton } from '../../posts/components/create-post-button/create-post-button';

@Component({
  selector: 'app-feed',
  imports: [CreatePostButton],
  templateUrl: './feed.html',
})
export class Feed {}
