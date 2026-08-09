import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Report, CreateReportRequest } from '../models/report.model';

@Injectable({ providedIn: 'root' })
export class ReportService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = '/api/reports';

  createReport(data: CreateReportRequest): Observable<Report> {
    return this.http.post<Report>(this.apiUrl, data);
  }
}
