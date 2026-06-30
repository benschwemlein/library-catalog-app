import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Fine, PayFineRequest, WaiveFineRequest } from '../models/fine.model';

@Injectable({ providedIn: 'root' })
export class FineService {
  private apiUrl = 'http://localhost:8080/api/fines';

  constructor(private http: HttpClient) {}

  getFineById(id: number): Observable<Fine> {
    return this.http.get<Fine>(`${this.apiUrl}/${id}`);
  }

  getFinesByMember(memberId: number): Observable<Fine[]> {
    return this.http.get<Fine[]>(`${this.apiUrl}/member/${memberId}`);
  }

  getUnpaidFines(memberId: number): Observable<Fine[]> {
    const params = new HttpParams().set('status', 'UNPAID');
    return this.http.get<Fine[]>(`${this.apiUrl}/member/${memberId}`, { params });
  }

  getAllUnpaidFines(page: number = 0, size: number = 20): Observable<Fine[]> {
    const params = new HttpParams().set('status', 'UNPAID').set('page', page).set('size', size);
    return this.http.get<Fine[]>(this.apiUrl, { params });
  }

  payFine(request: PayFineRequest): Observable<Fine> {
    return this.http.post<Fine>(`${this.apiUrl}/${request.fineId}/pay`, request);
  }

  waiveFine(request: WaiveFineRequest): Observable<Fine> {
    return this.http.post<Fine>(`${this.apiUrl}/${request.fineId}/waive`, request);
  }

  calculateOverdueFine(loanId: number): Observable<{ amount: number; daysOverdue: number }> {
    return this.http.get<{ amount: number; daysOverdue: number }>(`${this.apiUrl}/calculate/${loanId}`);
  }
}
