import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { InterLibraryLoanRequest, PartnerLibrary, ILLStatus } from '../../models/interlibrary-loan.model';

@Component({
  selector: 'app-ill-management',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './ill-management.component.html'
})
export class IllManagementComponent implements OnInit {
  private http = inject(HttpClient);
  private readonly apiBase = 'http://localhost:8080/api/library/ill';

  allRequests: InterLibraryLoanRequest[] = [];
  pendingRequests: InterLibraryLoanRequest[] = [];
  partners: PartnerLibrary[] = [];

  loading = false;
  error = '';
  successMessage = '';

  selectedRequest: InterLibraryLoanRequest | null = null;
  staffName = 'Librarian';
  activeTab: 'pending' | 'all' = 'pending';

  partnerSelectId: number | null = null;
  reviewNote = '';
  denialReason = '';
  showDenialForm = false;

  ngOnInit(): void {
    this.loadPendingRequests();
    this.loadPartners();
  }

  loadPendingRequests(): void {
    this.loading = true;
    this.http.get<InterLibraryLoanRequest[]>(`${this.apiBase}/requests/pending`).subscribe({
      next: data => { this.pendingRequests = data; this.loading = false; },
      error: () => { this.error = 'Failed to load pending requests.'; this.loading = false; }
    });
  }

  loadAllRequests(): void {
    this.loading = true;
    this.http.get<InterLibraryLoanRequest[]>(`${this.apiBase}/requests`).subscribe({
      next: data => { this.allRequests = data; this.loading = false; },
      error: () => { this.error = 'Failed to load requests.'; this.loading = false; }
    });
  }

  loadPartners(): void {
    this.http.get<PartnerLibrary[]>(`${this.apiBase}/partners`).subscribe({
      next: data => { this.partners = data; },
      error: () => {}
    });
  }

  selectRequest(req: InterLibraryLoanRequest): void {
    this.selectedRequest = req;
    this.reviewNote = '';
    this.denialReason = '';
    this.showDenialForm = false;
    this.partnerSelectId = null;
    this.successMessage = '';
    this.error = '';
  }

  onTabChange(tab: 'pending' | 'all'): void {
    this.activeTab = tab;
    this.selectedRequest = null;
    if (tab === 'all') {
      this.loadAllRequests();
    } else {
      this.loadPendingRequests();
    }
  }

  approveRequest(req: InterLibraryLoanRequest): void {
    this.http.put(`${this.apiBase}/requests/${req.id}/approve`, { staffName: this.staffName, notes: this.reviewNote }).subscribe({
      next: () => {
        this.successMessage = 'Request approved.';
        this.selectedRequest = null;
        this.reload();
      },
      error: () => { this.error = 'Failed to approve request.'; }
    });
  }

  denyRequest(req: InterLibraryLoanRequest): void {
    this.http.put(`${this.apiBase}/requests/${req.id}/deny`, { staffName: this.staffName, reason: this.denialReason }).subscribe({
      next: () => {
        this.successMessage = 'Request denied.';
        this.selectedRequest = null;
        this.showDenialForm = false;
        this.reload();
      },
      error: () => { this.error = 'Failed to deny request.'; }
    });
  }

  orderFromPartner(req: InterLibraryLoanRequest): void {
    this.http.put(`${this.apiBase}/requests/${req.id}/order`, { partnerId: this.partnerSelectId, staffName: this.staffName }).subscribe({
      next: () => {
        this.successMessage = 'Order placed with partner library.';
        this.selectedRequest = null;
        this.reload();
      },
      error: () => { this.error = 'Failed to place order.'; }
    });
  }

  markReceived(req: InterLibraryLoanRequest): void {
    this.http.put(`${this.apiBase}/requests/${req.id}/received`, { staffName: this.staffName }).subscribe({
      next: () => { this.successMessage = 'Marked as received.'; this.selectedRequest = null; this.reload(); },
      error: () => { this.error = 'Failed to mark received.'; }
    });
  }

  markAvailable(req: InterLibraryLoanRequest): void {
    this.http.put(`${this.apiBase}/requests/${req.id}/available`, {}).subscribe({
      next: () => { this.successMessage = 'Marked as available for pickup.'; this.selectedRequest = null; this.reload(); },
      error: () => { this.error = 'Failed to mark available.'; }
    });
  }

  processReturn(req: InterLibraryLoanRequest): void {
    this.http.put(`${this.apiBase}/requests/${req.id}/returned`, {}).subscribe({
      next: () => { this.successMessage = 'Return processed.'; this.selectedRequest = null; this.reload(); },
      error: () => { this.error = 'Failed to process return.'; }
    });
  }

  private reload(): void {
    if (this.activeTab === 'pending') {
      this.loadPendingRequests();
    } else {
      this.loadAllRequests();
    }
  }

  getStatusColor(status: string): string {
    switch (status as ILLStatus) {
      case 'PENDING':   return '#f59e0b';
      case 'APPROVED':  return '#3b82f6';
      case 'ORDERED':   return '#8b5cf6';
      case 'RECEIVED':  return '#06b6d4';
      case 'AVAILABLE': return '#22c55e';
      case 'RETURNED':  return '#6b7280';
      case 'DENIED':    return '#ef4444';
      default:          return '#6b7280';
    }
  }

  getStatusLabel(status: string): string {
    switch (status as ILLStatus) {
      case 'PENDING':   return 'Pending';
      case 'APPROVED':  return 'Approved';
      case 'ORDERED':   return 'Ordered';
      case 'RECEIVED':  return 'Received';
      case 'AVAILABLE': return 'Available';
      case 'RETURNED':  return 'Returned';
      case 'DENIED':    return 'Denied';
      default:          return status;
    }
  }

  canApprove(req: InterLibraryLoanRequest): boolean { return req.status === 'PENDING'; }
  canOrder(req: InterLibraryLoanRequest): boolean   { return req.status === 'APPROVED'; }
  canMarkReceived(req: InterLibraryLoanRequest): boolean  { return req.status === 'ORDERED'; }
  canMarkAvailable(req: InterLibraryLoanRequest): boolean { return req.status === 'RECEIVED'; }
  canReturn(req: InterLibraryLoanRequest): boolean  { return req.status === 'AVAILABLE'; }

  get displayedRequests(): InterLibraryLoanRequest[] {
    return this.activeTab === 'pending' ? this.pendingRequests : this.allRequests;
  }

  get pendingCount(): number   { return this.pendingRequests.filter(r => r.status === 'PENDING').length; }
  get approvedCount(): number  { return this.allRequests.filter(r => r.status === 'APPROVED').length; }
  get orderedCount(): number   { return this.allRequests.filter(r => r.status === 'ORDERED').length; }
  get availableCount(): number { return this.allRequests.filter(r => r.status === 'AVAILABLE').length; }
}
