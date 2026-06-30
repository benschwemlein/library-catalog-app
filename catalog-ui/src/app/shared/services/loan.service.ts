import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Loan, LoanSummary, CheckoutRequest, RenewRequest } from '../models/loan.model';

@Injectable({ providedIn: 'root' })
export class LoanService {
  private apiUrl = 'http://localhost:8080/api/loans';

  constructor(private http: HttpClient) {}

  getLoanById(id: number): Observable<Loan> {
    return this.http.get<Loan>(`${this.apiUrl}/${id}`);
  }

  getLoanByCopyBarcode(barcode: string): Observable<Loan> {
    return this.http.get<Loan>(`${this.apiUrl}/barcode/${barcode}`);
  }

  getActiveLoans(page: number = 0, size: number = 20): Observable<LoanSummary[]> {
    const params = new HttpParams().set('page', page).set('size', size).set('status', 'ACTIVE');
    return this.http.get<LoanSummary[]>(this.apiUrl, { params });
  }

  getOverdueLoans(page: number = 0, size: number = 20): Observable<LoanSummary[]> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.http.get<LoanSummary[]>(`${this.apiUrl}/overdue`, { params });
  }

  getTodaysCheckouts(): Observable<LoanSummary[]> {
    return this.http.get<LoanSummary[]>(`${this.apiUrl}/today`);
  }

  checkout(request: CheckoutRequest): Observable<Loan> {
    return this.http.post<Loan>(`${this.apiUrl}/checkout`, request);
  }

  checkoutByIds(memberId: number, bookCopyId: number): Observable<Loan> {
    return this.http.post<Loan>(`${this.apiUrl}/checkout`, { memberId, bookCopyId });
  }

  returnBook(loanId: number, condition?: string): Observable<Loan> {
    return this.http.post<Loan>(`${this.apiUrl}/${loanId}/return`, { condition });
  }

  renewLoan(loanId: number, extendDays?: number): Observable<Loan> {
    const request: RenewRequest = { loanId, extendDays };
    return this.http.post<Loan>(`${this.apiUrl}/${loanId}/renew`, request);
  }
}
