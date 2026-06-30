import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { MostBorrowedReport, OverdueReport, BranchStatsReport, MemberActivityReport } from '../models/report.model';

@Injectable({ providedIn: 'root' })
export class ReportService {
  private apiUrl = 'http://localhost:8080/api/reports';

  constructor(private http: HttpClient) {}

  getMostBorrowed(startDate?: string, endDate?: string, limit: number = 20): Observable<MostBorrowedReport> {
    let params = new HttpParams().set('limit', limit);
    if (startDate) params = params.set('startDate', startDate);
    if (endDate) params = params.set('endDate', endDate);
    return this.http.get<MostBorrowedReport>(`${this.apiUrl}/most-borrowed`, { params });
  }

  getOverdueReport(): Observable<OverdueReport> {
    return this.http.get<OverdueReport>(`${this.apiUrl}/overdue`);
  }

  getBranchStats(startDate?: string, endDate?: string): Observable<BranchStatsReport> {
    let params = new HttpParams();
    if (startDate) params = params.set('startDate', startDate);
    if (endDate) params = params.set('endDate', endDate);
    return this.http.get<BranchStatsReport>(`${this.apiUrl}/branch-stats`, { params });
  }

  getMemberActivity(period: string = 'LAST_30_DAYS'): Observable<MemberActivityReport> {
    const params = new HttpParams().set('period', period);
    return this.http.get<MemberActivityReport>(`${this.apiUrl}/member-activity`, { params });
  }

  exportReport(reportType: string, format: 'CSV' | 'PDF'): Observable<Blob> {
    const params = new HttpParams().set('format', format);
    return this.http.get(`${this.apiUrl}/${reportType}/export`, { params, responseType: 'blob' });
  }
}
