import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute } from '@angular/router';
import { DigitalResourceService } from '../../services/digital-resource.service';
import { DigitalResource, DigitalLoan } from '../../models/digital-resource.model';
import { AuthService } from '../../shared/services/auth.service';

@Component({
  selector: 'app-digital-reader',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './digital-reader.component.html'
})
export class DigitalReaderComponent implements OnInit {
  resource: DigitalResource | null = null;
  loan: DigitalLoan | null = null;
  loading = false;
  error = '';
  successMessage = '';
  checkoutLoading = false;
  returnLoading = false;
  downloadLoading = false;

  get memberId(): number { return this.authService.getMemberId(); }

  constructor(
    private route: ActivatedRoute,
    private digitalService: DigitalResourceService,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    if (id) {
      this.loadResource(id);
      this.loadMemberLoans();
    }
  }

  loadResource(id: number): void {
    this.loading = true;
    this.error = '';
    this.digitalService.getResource(id).subscribe({
      next: (res) => {
        this.resource = res;
        this.loading = false;
      },
      error: (err) => {
        this.error = err.message || 'Failed to load resource.';
        this.loading = false;
      }
    });
  }

  loadMemberLoans(): void {
    this.digitalService.getMemberLoans(this.memberId).subscribe({
      next: (loans) => {
        if (this.resource) {
          this.loan = loans.find(l => l.resourceId === this.resource!.id && l.status === 'ACTIVE') ?? null;
        } else {
          // store and match after resource loads
          this.loan = loans.find(l => l.status === 'ACTIVE') ?? null;
        }
      },
      error: () => { /* ignore loan fetch error */ }
    });
  }

  checkout(): void {
    if (!this.resource) return;
    this.checkoutLoading = true;
    this.successMessage = '';
    this.error = '';
    this.digitalService.checkout(this.resource.id, this.memberId).subscribe({
      next: (loan) => {
        this.loan = loan;
        this.successMessage = `Successfully checked out "${this.resource?.title}". Expires: ${new Date(loan.expiryDate).toLocaleDateString()}`;
        this.checkoutLoading = false;
        if (this.resource) this.resource.availableNow = false;
      },
      error: (err) => {
        this.error = err.error?.message || err.message || 'Checkout failed.';
        this.checkoutLoading = false;
      }
    });
  }

  returnResource(): void {
    if (!this.loan) return;
    this.returnLoading = true;
    this.successMessage = '';
    this.error = '';
    this.digitalService.returnResource(this.loan.loanId).subscribe({
      next: () => {
        this.successMessage = 'Resource returned successfully.';
        this.loan = null;
        this.returnLoading = false;
        if (this.resource) this.resource.availableNow = true;
      },
      error: (err) => {
        this.error = err.error?.message || err.message || 'Return failed.';
        this.returnLoading = false;
      }
    });
  }

  trackDownload(): void {
    if (!this.loan) return;
    this.downloadLoading = true;
    this.digitalService.trackDownload(this.loan.loanId).subscribe({
      next: (updated) => {
        this.loan = updated;
        this.downloadLoading = false;
        this.successMessage = 'Download tracked. Your file is ready.';
      },
      error: () => { this.downloadLoading = false; }
    });
  }

  getTypeIcon(): string {
    const icons: Record<string, string> = {
      EBOOK: '📖', AUDIOBOOK: '🎧', VIDEO: '🎬', ARTICLE: '📰', DATABASE: '🗄️'
    };
    return this.resource ? (icons[this.resource.resourceType] ?? '📄') : '📄';
  }

  formatFileSize(bytes: number): string {
    if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`;
    if (bytes < 1024 * 1024 * 1024) return `${(bytes / (1024 * 1024)).toFixed(1)} MB`;
    return `${(bytes / (1024 * 1024 * 1024)).toFixed(2)} GB`;
  }

  isExpiringSoon(): boolean {
    if (!this.loan) return false;
    const days = (new Date(this.loan.expiryDate).getTime() - Date.now()) / (1000 * 60 * 60 * 24);
    return days <= 3;
  }
}
