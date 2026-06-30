import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { LoanService } from '../../shared/services/loan.service';
import { HoldService } from '../../shared/services/hold.service';

@Component({
  selector: 'app-staff-dashboard',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './staff-dashboard.component.html',
  styleUrl: './staff-dashboard.component.css'
})
export class StaffDashboardComponent implements OnInit {
  todaysCheckouts: any[] = [];
  pendingReturns: any[] = [];
  holdsToFulfill: any[] = [];
  overdueLoans: any[] = [];
  loading: boolean = false;

  constructor(
    private loanService: LoanService,
    private holdService: HoldService
  ) {}

  ngOnInit(): void {
    this.loadDashboardData();
  }

  private mapLoanSummary(raw: any): any {
    return {
      ...raw,
      bookTitle: raw.bookCopy?.book?.title || raw.bookTitle || 'Unknown',
      memberName: raw.member?.user?.firstName
        ? `${raw.member.user.firstName} ${raw.member.user.lastName}`.trim()
        : (raw.memberName || 'Member'),
      membershipNumber: raw.member?.membershipNumber || raw.membershipNumber || '',
      branchName: raw.branch?.name || raw.branchName || '',
    };
  }

  loadDashboardData(): void {
    this.loading = true;

    this.loanService.getTodaysCheckouts().subscribe({
      next: (loans: any[]) => {
        this.todaysCheckouts = loans.map(l => this.mapLoanSummary(l));
        this.loading = false;
      },
      error: (err) => {
        console.error('Failed to load today checkouts', err);
        this.loading = false;
      }
    });

    this.loanService.getOverdueLoans(0, 10).subscribe({
      next: (loans: any[]) => {
        this.overdueLoans = loans.map(l => this.mapLoanSummary(l));
      },
      error: (err) => console.error('Failed to load overdue', err)
    });

    this.holdService.getReadyHolds().subscribe({
      next: (rawHolds: any[]) => {
        this.holdsToFulfill = rawHolds.map(h => ({
          ...h,
          bookTitle: h.book?.title || h.bookTitle || 'Unknown',
          memberName: h.member?.user?.firstName
            ? `${h.member.user.firstName} ${h.member.user.lastName}`.trim()
            : (h.memberName || 'Member'),
          membershipNumber: h.member?.membershipNumber || h.membershipNumber || '',
        }));
      },
      error: (err) => console.error('Failed to load holds to fulfill', err)
    });

    this.loanService.getActiveLoans(0, 10).subscribe({
      next: (loans: any[]) => {
        this.pendingReturns = loans.slice(0, 10).map(l => this.mapLoanSummary(l));
      },
      error: (err) => console.error('Failed to load pending returns', err)
    });
  }
}
