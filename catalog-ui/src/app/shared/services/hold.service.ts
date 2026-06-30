import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Hold, PlaceHoldRequest } from '../models/hold.model';

@Injectable({ providedIn: 'root' })
export class HoldService {
  private apiUrl = 'http://localhost:8080/api/holds';

  constructor(private http: HttpClient) {}

  getHoldById(id: number): Observable<Hold> {
    return this.http.get<Hold>(`${this.apiUrl}/${id}`);
  }

  getReadyHolds(branchId?: number): Observable<Hold[]> {
    let params = new HttpParams().set('status', 'READY');
    if (branchId) params = params.set('branchId', branchId);
    return this.http.get<Hold[]>(this.apiUrl, { params });
  }

  getPendingHolds(page: number = 0, size: number = 20): Observable<Hold[]> {
    const params = new HttpParams().set('status', 'PENDING').set('page', page).set('size', size);
    return this.http.get<Hold[]>(this.apiUrl, { params });
  }

  placeHold(request: PlaceHoldRequest): Observable<Hold> {
    return this.http.post<Hold>(this.apiUrl, request);
  }

  cancelHold(holdId: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${holdId}`);
  }

  fulfillHold(holdId: number, copyBarcode: string): Observable<Hold> {
    return this.http.post<Hold>(`${this.apiUrl}/${holdId}/fulfill`, { copyBarcode });
  }

  getQueuePosition(bookId: number, memberId: number): Observable<{ position: number; estimatedDays: number }> {
    const params = new HttpParams().set('bookId', bookId).set('memberId', memberId);
    return this.http.get<{ position: number; estimatedDays: number }>(`${this.apiUrl}/queue-position`, { params });
  }
}
