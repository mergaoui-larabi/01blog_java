import { inject, Service } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environments';

@Service()
export class MediaApiService {
  private readonly http = inject(HttpClient);
  private readonly env = environment;

  getMediaContent(mediaId: string): Observable<Blob> {
    const url = this.env.getAvatarMediaEndpoint.replace('{mediaId}', encodeURIComponent(mediaId));
    return this.http.get(url, {
      responseType: 'blob',
    });
  }
}
