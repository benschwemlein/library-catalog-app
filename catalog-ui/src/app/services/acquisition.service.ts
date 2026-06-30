import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AcquisitionRequest, PurchaseOrder, AcquisitionStatus, PurchaseOrderStatus } from '../models/acquisition.model';

@Injectable({ providedIn: 'root' })
export class AcquisitionService {
  private http = inject(HttpClient);
  private base = 'http://localhost:8080/api/library/acquisitions';

  submitMemberRequest(body: { memberId: number; title: string; author?: string; isbn?: string; reason?: string; priority?: string }): Observable<AcquisitionRequest> {
    return this.http.post<AcquisitionRequest>(`${this.base}/requests/member`, body);
  }

  getRequests(statuses?: string[]): Observable<AcquisitionRequest[]> {
    let params = new HttpParams();
    if (statuses && statuses.length > 0) {
      params = params.set('status', statuses.join(','));
    }
    return this.http.get<AcquisitionRequest[]>(`${this.base}/requests`, { params });
  }

  getRequest(id: number): Observable<AcquisitionRequest> {
    return this.http.get<AcquisitionRequest>(`${this.base}/requests/${id}`);
  }

  getMemberRequests(memberId: number): Observable<AcquisitionRequest[]> {
    return this.http.get<AcquisitionRequest[]>(`${this.base}/requests/member/${memberId}`);
  }

  reviewRequest(id: number, staffName: string, note: string): Observable<AcquisitionRequest> {
    return this.http.put<AcquisitionRequest>(`${this.base}/requests/${id}/review`, { staffName, note });
  }

  approveRequest(id: number, estimatedCost: number): Observable<AcquisitionRequest> {
    return this.http.put<AcquisitionRequest>(`${this.base}/requests/${id}/approve`, { estimatedCost });
  }

  denyRequest(id: number, staffName: string, reason: string): Observable<AcquisitionRequest> {
    return this.http.put<AcquisitionRequest>(`${this.base}/requests/${id}/deny`, { staffName, reason });
  }

  getOrders(): Observable<PurchaseOrder[]> {
    return this.http.get<PurchaseOrder[]>(`${this.base}/orders`);
  }

  createOrder(requestIds: number[], supplier: string, expectedDelivery: string, submittedBy: string): Observable<PurchaseOrder> {
    return this.http.post<PurchaseOrder>(`${this.base}/orders`, { requestIds, supplier, expectedDelivery, submittedBy });
  }

  submitOrder(orderId: number): Observable<PurchaseOrder> {
    return this.http.put<PurchaseOrder>(`${this.base}/orders/${orderId}/submit`, {});
  }

  receiveOrder(orderId: number): Observable<PurchaseOrder> {
    return this.http.put<PurchaseOrder>(`${this.base}/orders/${orderId}/receive`, {});
  }
}
