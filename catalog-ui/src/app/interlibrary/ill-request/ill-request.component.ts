import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { InterLibraryLoanRequest, SubmitILLRequest, ILLStatus } from '../../models/interlibrary-loan.model';
import { AuthService } from '../../shared/services/auth.service';

@Component({
  selector: 'app-ill-request',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './ill-request.component.html'
})
export class IllRequestComponent implements OnInit {
  private http = inject(HttpClient);
  private authService = inject(AuthService);
  private base = 'http://localhost:8080/api/library/ill';

  requests: InterLibraryLoanRequest[] = [];
  loading = false;
  error = '';
  successMessage = '';
  showRequestForm = false;
  branchId = 1;

  get memberId(): number { return this.authService.getMemberId(); }

  formData: SubmitILLRequest = {
    memberId: this.authService.getMemberId(),
    branchId: 1,
    bookTitle: '',
    authorName: '',
    isbn: '',
    neededByDate: '',
    notes: ''
  };

  ngOnInit(): void {
    this.loadMyRequests();
  }

  loadMyRequests(): void {
    this.loading = true;
    this.error = '';
    this.http.get<InterLibraryLoanRequest[]>(`${this.base}/requests/member/${this.memberId}`).subscribe({
      next: reqs => {
        this.requests = reqs;
        this.loading = false;
      },
      error: () => {
        this.error = 'Failed to load your requests.';
        this.loading = false;
      }
    });
  }

  submitRequest(): void {
    if (!this.formData.bookTitle.trim()) return;
    this.http.post<InterLibraryLoanRequest>(`${this.base}/requests`, this.formData).subscribe({
      next: () => {
        this.successMessage = 'Request submitted successfully!';
        this.showRequestForm = false;
        this.resetForm();
        this.loadMyRequests();
        setTimeout(() => this.successMessage = '', 4000);
      },
      error: () => {
        this.error = 'Failed to submit request.';
      }
    });
  }

  cancelForm(): void {
    this.showRequestForm = false;
    this.resetForm();
  }

  openForm(): void {
    this.showRequestForm = true;
    this.error = '';
  }

  private resetForm(): void {
    this.formData = {
      memberId: this.memberId,
      branchId: this.branchId,
      bookTitle: '',
      authorName: '',
      isbn: '',
      neededByDate: '',
      notes: ''
    };
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
      default:          return '#9ca3af';
    }
  }

  getStatusLabel(status: string): string {
    switch (status as ILLStatus) {
      case 'PENDING':   return 'Pending Review';
      case 'APPROVED':  return 'Approved';
      case 'ORDERED':   return 'Ordered';
      case 'RECEIVED':  return 'Received';
      case 'AVAILABLE': return 'Available for Pickup';
      case 'RETURNED':  return 'Returned';
      case 'DENIED':    return 'Denied';
      default:          return status;
    }
  }

  getStatusBgColor(status: string): string {
    const color = this.getStatusColor(status);
    return color + '1a';
  }

  isDenied(status: string): boolean {
    return status === 'DENIED';
  }

  isAvailable(status: string): boolean {
    return status === 'AVAILABLE';
  }

  dismissError(): void {
    this.error = '';
  }

  dismissSuccess(): void {
    this.successMessage = '';
  }
}
