import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { InterLibraryLoanRequest, SubmitILLRequest, PartnerLibrary } from '../models/interlibrary-loan.model';

const BASE = 'http://localhost:8080/api/library';

@Injectable({ providedIn: 'root' })
export class InterlibraryLoanService {
  private http = inject(HttpClient);

  submitRequest(request: SubmitILLRequest): Observable<InterLibraryLoanRequest> {
    return this.http.post<InterLibraryLoanRequest>(`${BASE}/ill/requests`, request);
  }

  getRequest(id: number): Observable<InterLibraryLoanRequest> {
    return this.http.get<InterLibraryLoanRequest>(`${BASE}/ill/requests/${id}`);
  }

  getMemberRequests(memberId: number): Observable<InterLibraryLoanRequest[]> {
    return this.http.get<InterLibraryLoanRequest[]>(`${BASE}/ill/requests/member/${memberId}`);
  }

  getPendingRequests(): Observable<InterLibraryLoanRequest[]> {
    return this.http.get<InterLibraryLoanRequest[]>(`${BASE}/ill/requests/pending`);
  }

  approveRequest(id: number, staffName: string, note: string): Observable<InterLibraryLoanRequest> {
    const params = new HttpParams().set('staffName', staffName).set('note', note);
    return this.http.put<InterLibraryLoanRequest>(`${BASE}/ill/requests/${id}/approve`, {}, { params });
  }

  denyRequest(id: number, staffName: string, reason: string): Observable<InterLibraryLoanRequest> {
    const params = new HttpParams().set('staffName', staffName).set('reason', reason);
    return this.http.put<InterLibraryLoanRequest>(`${BASE}/ill/requests/${id}/deny`, {}, { params });
  }

  orderFromPartner(id: number, partnerLibraryId: number): Observable<InterLibraryLoanRequest> {
    const params = new HttpParams().set('partnerLibraryId', partnerLibraryId);
    return this.http.put<InterLibraryLoanRequest>(`${BASE}/ill/requests/${id}/order`, {}, { params });
  }

  markReceived(id: number): Observable<InterLibraryLoanRequest> {
    return this.http.put<InterLibraryLoanRequest>(`${BASE}/ill/requests/${id}/received`, {});
  }

  markAvailable(id: number): Observable<InterLibraryLoanRequest> {
    return this.http.put<InterLibraryLoanRequest>(`${BASE}/ill/requests/${id}/available`, {});
  }

  processReturn(id: number): Observable<InterLibraryLoanRequest> {
    return this.http.put<InterLibraryLoanRequest>(`${BASE}/ill/requests/${id}/return`, {});
  }

  getPartners(): Observable<PartnerLibrary[]> {
    return this.http.get<PartnerLibrary[]>(`${BASE}/ill/partners`);
  }
}
