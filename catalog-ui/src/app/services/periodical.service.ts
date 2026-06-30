import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Periodical, PeriodicalIssue } from '../models/periodical.model';

@Injectable({ providedIn: 'root' })
export class PeriodicalService {
  private http = inject(HttpClient);
  private base = 'http://localhost:8080/api/library/periodicals';

  search(query?: string, category?: string, page = 0, size = 20): Observable<any> {
    let params = new HttpParams().set('page', page).set('size', size);
    if (query) params = params.set('query', query);
    if (category) params = params.set('category', category);
    return this.http.get<any>(this.base, { params });
  }

  getPeriodical(id: number): Observable<Periodical> {
    return this.http.get<Periodical>(`${this.base}/${id}`);
  }

  getIssues(periodicalId: number): Observable<PeriodicalIssue[]> {
    return this.http.get<PeriodicalIssue[]>(`${this.base}/${periodicalId}/issues`);
  }

  getCurrentIssues(periodicalId: number): Observable<PeriodicalIssue[]> {
    return this.http.get<PeriodicalIssue[]>(`${this.base}/${periodicalId}/issues/current`);
  }

  getCategories(): Observable<string[]> {
    return this.http.get<string[]>(`${this.base}/categories`);
  }

  addIssue(periodicalId: number, body: { volumeNumber: number; issueNumber: number; publicationDate: string; title?: string }): Observable<PeriodicalIssue> {
    return this.http.post<PeriodicalIssue>(`${this.base}/${periodicalId}/issues`, body);
  }
}
