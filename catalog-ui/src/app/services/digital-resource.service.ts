import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { DigitalResource, DigitalLoan, DigitalResourceType, Page } from '../models/digital-resource.model';

const BASE = 'http://localhost:8080/api/library';

@Injectable({ providedIn: 'root' })
export class DigitalResourceService {
  private http = inject(HttpClient);

  searchResources(query?: string, type?: DigitalResourceType, page = 0, size = 20): Observable<Page<DigitalResource>> {
    let params = new HttpParams().set('page', page).set('size', size);
    if (query) params = params.set('query', query);
    if (type) params = params.set('type', type);
    return this.http.get<Page<DigitalResource>>(`${BASE}/digital`, { params });
  }

  getResource(id: number): Observable<DigitalResource> {
    return this.http.get<DigitalResource>(`${BASE}/digital/${id}`);
  }

  checkout(resourceId: number, memberId: number): Observable<DigitalLoan> {
    return this.http.post<DigitalLoan>(`${BASE}/digital/${resourceId}/checkout`, { memberId });
  }

  returnResource(loanId: number): Observable<DigitalLoan> {
    return this.http.post<DigitalLoan>(`${BASE}/digital/loans/${loanId}/return`, {});
  }

  trackDownload(loanId: number): Observable<DigitalLoan> {
    return this.http.post<DigitalLoan>(`${BASE}/digital/loans/${loanId}/download`, {});
  }

  getMemberLoans(memberId: number): Observable<DigitalLoan[]> {
    return this.http.get<DigitalLoan[]>(`${BASE}/digital/member/${memberId}/loans`);
  }
}
