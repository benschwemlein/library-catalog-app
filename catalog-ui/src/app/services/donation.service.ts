import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface DonationItem {
  title: string;
  author?: string;
  isbn?: string;
  quantity: number;
  condition?: string;
  disposition?: string;
}

export interface Donation {
  id: number;
  donorName: string;
  donorEmail: string;
  donorPhone?: string;
  donationDate: string;
  status: string;
  items: DonationItem[];
  targetBranchName?: string;
  reviewedByName?: string;
  notes?: string;
  acknowledgementSent: boolean;
}

@Injectable({ providedIn: 'root' })
export class DonationService {
  private http = inject(HttpClient);
  private base = 'http://localhost:8080/api/library/donations';

  recordDonation(body: {
    donorName: string;
    donorEmail: string;
    donorPhone?: string;
    targetBranchId?: number;
    items: DonationItem[];
    notes?: string;
  }): Observable<Donation> {
    return this.http.post<Donation>(this.base, body);
  }

  getDonation(id: number): Observable<Donation> {
    return this.http.get<Donation>(`${this.base}/${id}`);
  }

  getDonations(status?: string, page = 0): Observable<any> {
    let params = new HttpParams().set('page', page).set('size', 20);
    if (status) params = params.set('status', status);
    return this.http.get<any>(this.base, { params });
  }

  reviewDonation(id: number, reviewerName: string, notes: string, status: string): Observable<Donation> {
    return this.http.put<Donation>(`${this.base}/${id}/review`, { reviewerName, notes, status });
  }

  sendAcknowledgement(id: number): Observable<Donation> {
    return this.http.post<Donation>(`${this.base}/${id}/acknowledge`, {});
  }

  getStats(): Observable<any> {
    return this.http.get<any>(`${this.base}/stats`);
  }
}
