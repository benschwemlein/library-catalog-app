import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MemberService } from '../../shared/services/member.service';
import { Loan, LoanStatus } from '../../shared/models/loan.model';
import { AuthService } from '../../shared/services/auth.service';

@Component({
  selector: 'app-loan-history',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './loan-history.component.html'
})
export class LoanHistoryComponent implements OnInit {
  loans: Loan[] = [];
  filteredLoans: Loan[] = [];
  statusFilter: string = '';
  loading: boolean = false;
  LoanStatus = LoanStatus;

  get memberId(): number { return this.authService.getMemberId(); }

  statuses = ['', 'ACTIVE', 'RETURNED', 'OVERDUE', 'RENEWED', 'LOST'];

  constructor(
    private memberService: MemberService,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    this.loadLoans();
  }

  loadLoans(): void {
    this.loading = true;
    this.memberService.getMemberLoans(this.memberId).subscribe({
      next: (rawLoans: any[]) => {
        this.loans = rawLoans.map(l => ({
          ...l,
          bookTitle: l.bookCopy?.book?.title || l.bookTitle || 'Unknown',
          branchName: l.branch?.name || l.branchName || '',
          maxRenewals: l.maxRenewals ?? 3,
        }));
        this.applyFilter();
        this.loading = false;
      },
      error: (err) => {
        console.error('Failed to load loan history', err);
        this.loading = false;
      }
    });
  }

  applyFilter(): void {
    if (!this.statusFilter) {
      this.filteredLoans = [...this.loans];
    } else {
      this.filteredLoans = this.loans.filter(l => l.status === this.statusFilter);
    }
  }

  getStatusClass(status: string): string {
    const map: Record<string, string> = {
      ACTIVE: 'badge-success',
      RETURNED: 'badge-secondary',
      OVERDUE: 'badge-danger',
      RENEWED: 'badge-info',
      LOST: 'badge-dark'
    };
    return map[status] || 'badge-secondary';
  }
}
