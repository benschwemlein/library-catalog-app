import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AcquisitionRequest, AcquisitionStatus, PurchaseOrder, PurchaseOrderStatus } from '../../models/acquisition.model';
import { AcquisitionService } from '../../services/acquisition.service';

@Component({
  selector: 'app-acquisitions',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './acquisitions.component.html'
})
export class AcquisitionsComponent implements OnInit {
  private acquisitionService = inject(AcquisitionService);

  requests: AcquisitionRequest[] = [];
  orders: PurchaseOrder[] = [];
  loading = false;
  error = '';
  successMessage = '';
  activeTab: 'requests' | 'orders' = 'requests';
  statusFilter = '';
  selectedRequests: number[] = [];
  showOrderForm = false;
  orderFormData = { supplier: '', expectedDelivery: '', submittedBy: '' };
  staffName = 'Staff Member';

  ngOnInit(): void {
    this.loadRequests();
    this.loadOrders();
  }

  loadRequests(): void {
    this.loading = true;
    this.error = '';
    const statuses = this.statusFilter ? [this.statusFilter as AcquisitionStatus] : undefined;
    this.acquisitionService.getRequests(statuses).subscribe({
      next: (data) => {
        this.requests = data;
        this.loading = false;
      },
      error: () => {
        this.error = 'Failed to load acquisition requests.';
        this.loading = false;
      }
    });
  }

  loadOrders(): void {
    this.acquisitionService.getOrders().subscribe({
      next: (data) => {
        this.orders = data;
      },
      error: () => {
        this.error = 'Failed to load purchase orders.';
      }
    });
  }

  reviewRequest(req: AcquisitionRequest): void {
    this.clearMessages();
    this.acquisitionService.reviewRequest(req.id, this.staffName, 'Under review').subscribe({
      next: () => {
        this.successMessage = `Request "${req.title}" marked as under review.`;
        this.loadRequests();
      },
      error: () => {
        this.error = 'Failed to update request status.';
      }
    });
  }

  approveRequest(req: AcquisitionRequest): void {
    this.clearMessages();
    const input = window.prompt(`Enter estimated cost for "${req.title}":`, '0.00');
    if (input === null) return;
    const cost = parseFloat(input);
    if (isNaN(cost)) {
      this.error = 'Invalid cost value entered.';
      return;
    }
    this.acquisitionService.approveRequest(req.id, cost).subscribe({
      next: () => {
        this.successMessage = `Request "${req.title}" approved with estimated cost $${cost.toFixed(2)}.`;
        this.loadRequests();
      },
      error: () => {
        this.error = 'Failed to approve request.';
      }
    });
  }

  denyRequest(req: AcquisitionRequest): void {
    this.clearMessages();
    const reason = window.prompt(`Enter reason for denying "${req.title}":`);
    if (reason === null || reason.trim() === '') return;
    this.acquisitionService.denyRequest(req.id, this.staffName, reason).subscribe({
      next: () => {
        this.successMessage = `Request "${req.title}" denied.`;
        this.loadRequests();
      },
      error: () => {
        this.error = 'Failed to deny request.';
      }
    });
  }

  toggleRequestSelection(id: number): void {
    const idx = this.selectedRequests.indexOf(id);
    if (idx > -1) {
      this.selectedRequests.splice(idx, 1);
    } else {
      this.selectedRequests.push(id);
    }
  }

  isSelected(id: number): boolean {
    return this.selectedRequests.includes(id);
  }

  createPurchaseOrder(): void {
    this.clearMessages();
    this.acquisitionService.createOrder(
      this.selectedRequests,
      this.orderFormData.supplier,
      this.orderFormData.expectedDelivery,
      this.orderFormData.submittedBy
    ).subscribe({
      next: () => {
        this.successMessage = 'Purchase order created successfully.';
        this.selectedRequests = [];
        this.showOrderForm = false;
        this.orderFormData = { supplier: '', expectedDelivery: '', submittedBy: '' };
        this.loadOrders();
        this.loadRequests();
      },
      error: () => {
        this.error = 'Failed to create purchase order.';
      }
    });
  }

  submitOrder(order: PurchaseOrder): void {
    this.clearMessages();
    this.acquisitionService.submitOrder(order.id).subscribe({
      next: () => {
        this.successMessage = `Order #${order.id} submitted.`;
        this.loadOrders();
      },
      error: () => {
        this.error = 'Failed to submit order.';
      }
    });
  }

  receiveOrder(order: PurchaseOrder): void {
    this.clearMessages();
    this.acquisitionService.receiveOrder(order.id).subscribe({
      next: () => {
        this.successMessage = `Order #${order.id} marked as received.`;
        this.loadOrders();
      },
      error: () => {
        this.error = 'Failed to mark order as received.';
      }
    });
  }

  getStatusBadgeStyle(status: AcquisitionStatus | PurchaseOrderStatus | string): { backgroundColor: string; color: string } {
    const styles: Record<string, { backgroundColor: string; color: string }> = {
      // AcquisitionStatus
      PENDING:      { backgroundColor: '#fef9c3', color: '#854d0e' },
      UNDER_REVIEW: { backgroundColor: '#dbeafe', color: '#1e40af' },
      APPROVED:     { backgroundColor: '#dcfce7', color: '#166534' },
      ORDERED:      { backgroundColor: '#ede9fe', color: '#5b21b6' },
      RECEIVED:     { backgroundColor: '#d1fae5', color: '#065f46' },
      DENIED:       { backgroundColor: '#fee2e2', color: '#991b1b' },
      // PurchaseOrderStatus
      DRAFT:        { backgroundColor: '#f1f5f9', color: '#475569' },
      SUBMITTED:    { backgroundColor: '#dbeafe', color: '#1e40af' },
      CONFIRMED:    { backgroundColor: '#ede9fe', color: '#5b21b6' },
      SHIPPED:      { backgroundColor: '#fef3c7', color: '#92400e' },
      CANCELLED:    { backgroundColor: '#fee2e2', color: '#991b1b' }
    };
    return styles[status] ?? { backgroundColor: '#f1f5f9', color: '#475569' };
  }

  getPriorityBadgeStyle(priority: string): { backgroundColor: string; color: string } {
    const styles: Record<string, { backgroundColor: string; color: string }> = {
      LOW:    { backgroundColor: '#f1f5f9', color: '#475569' },
      MEDIUM: { backgroundColor: '#fef9c3', color: '#854d0e' },
      HIGH:   { backgroundColor: '#fed7aa', color: '#9a3412' },
      URGENT: { backgroundColor: '#fee2e2', color: '#991b1b' }
    };
    return styles[priority] ?? { backgroundColor: '#f1f5f9', color: '#475569' };
  }

  onStatusFilterChange(): void {
    this.loadRequests();
  }

  private clearMessages(): void {
    this.error = '';
    this.successMessage = '';
  }
}
