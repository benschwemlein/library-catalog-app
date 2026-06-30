import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { DonationService, Donation } from '../../services/donation.service';

@Component({
  selector: 'app-donations',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './donations.component.html'
})
export class DonationsComponent implements OnInit {
  private http = inject(HttpClient);
  private donationService = inject(DonationService);

  donations: Donation[] = [];
  stats: any = { total: 0, pending: 0, accepted: 0, declined: 0 };
  selectedDonation: Donation | null = null;
  loading = false;
  error = '';
  successMessage = '';
  statusFilter = '';
  reviewForm = { reviewerName: '', notes: '', decision: '' };

  ngOnInit(): void {
    this.loadDonations();
    this.loadStats();
  }

  loadDonations(): void {
    this.loading = true;
    this.error = '';
    this.donationService.getDonations(this.statusFilter || undefined).subscribe({
      next: (resp: any) => {
        this.donations = resp.content || resp;
        this.loading = false;
      },
      error: (err: any) => {
        this.error = 'Failed to load donations.';
        this.loading = false;
      }
    });
  }

  loadStats(): void {
    this.donationService.getStats().subscribe({
      next: (s: any) => { this.stats = s; },
      error: () => {}
    });
  }

  selectDonation(d: Donation): void {
    this.selectedDonation = d;
    this.reviewForm = { reviewerName: '', notes: '', decision: '' };
    this.successMessage = '';
    this.error = '';
  }

  reviewDonation(decision: string): void {
    if (!this.selectedDonation) return;
    this.loading = true;
    this.donationService.reviewDonation(
      this.selectedDonation.id,
      this.reviewForm.reviewerName,
      this.reviewForm.notes,
      decision
    ).subscribe({
      next: () => {
        this.successMessage = `Donation ${decision.toLowerCase().replace('_', ' ')} successfully.`;
        this.selectedDonation = null;
        this.loadDonations();
        this.loadStats();
        this.loading = false;
      },
      error: () => {
        this.error = 'Failed to submit review.';
        this.loading = false;
      }
    });
  }

  sendAcknowledgement(d: Donation): void {
    this.donationService.sendAcknowledgement(d.id).subscribe({
      next: () => {
        this.successMessage = 'Acknowledgement sent to donor.';
        this.loadDonations();
      },
      error: () => {
        this.error = 'Failed to send acknowledgement.';
      }
    });
  }

  getStatusColor(status: string): string {
    switch (status) {
      case 'PENDING':            return '#f59e0b';
      case 'ACCEPTED':           return '#22c55e';
      case 'PARTIALLY_ACCEPTED': return '#3b82f6';
      case 'DECLINED':           return '#ef4444';
      default:                   return '#6b7280';
    }
  }

  getStatusBg(status: string): string {
    switch (status) {
      case 'PENDING':            return '#fef3c7';
      case 'ACCEPTED':           return '#dcfce7';
      case 'PARTIALLY_ACCEPTED': return '#dbeafe';
      case 'DECLINED':           return '#fee2e2';
      default:                   return '#f3f4f6';
    }
  }

  formatDate(dateStr: string): string {
    if (!dateStr) return '';
    return new Date(dateStr).toLocaleDateString();
  }

  applyFilter(): void {
    this.selectedDonation = null;
    this.loadDonations();
  }
}
