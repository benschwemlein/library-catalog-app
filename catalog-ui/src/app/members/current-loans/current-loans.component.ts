import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { LoanService } from '../../shared/services/loan.service';
import { MemberService } from '../../shared/services/member.service';
import { Loan } from '../../shared/models/loan.model';
import { AuthService } from '../../shared/services/auth.service';

@Component({
  selector: 'app-current-loans',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './current-loans.component.html',
  styleUrl: './current-loans.component.css'
})
export class CurrentLoansComponent implements OnInit {
  loans: Loan[] = [];
  loading: boolean = false;
  actionLoading: { [loanId: number]: boolean } = {};
  actionError: { [loanId: number]: string } = {};
  actionSuccess: { [loanId: number]: string } = {};

  get memberId(): number { return this.authService.getMemberId(); }

  constructor(
    private memberService: MemberService,
    private loanService: LoanService,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    this.loadLoans();
  }

  loadLoans(): void {
    this.loading = true;
    this.memberService.getMemberLoans(this.memberId).subscribe({
      next: (rawLoans: any[]) => {
        this.loans = rawLoans
          .filter(l => l.status === 'ACTIVE' || l.status === 'OVERDUE')
          .map(l => ({
            ...l,
            bookTitle: l.bookCopy?.book?.title || 'Unknown',
            bookIsbn: l.bookCopy?.book?.isbn || '',
            authorNames: (l.bookCopy?.book?.authors || []).map((a: any) => `${a.firstName} ${a.lastName}`),
            memberId: l.member?.id,
            memberName: `${l.member?.user?.firstName || ''} ${l.member?.user?.lastName || ''}`.trim(),
            membershipNumber: l.member?.membershipNumber || '',
            copyId: l.bookCopy?.id,
            copyBarcode: l.bookCopy?.barcode || '',
            branchId: l.branch?.id,
            branchName: l.branch?.name || '',
            maxRenewals: 3,
          }));
        this.loading = false;
      },
      error: (err) => {
        console.error('Failed to load loans', err);
        this.loading = false;
      }
    });
  }

  renewLoan(loanId: number): void {
    this.actionLoading[loanId] = true;
    this.actionError[loanId] = '';
    this.loanService.renewLoan(loanId).subscribe({
      next: (raw: any) => {
        const idx = this.loans.findIndex(l => l.id === loanId);
        if (idx !== -1) {
          this.loans[idx] = { ...this.loans[idx], renewalCount: raw.renewalCount, dueDate: raw.dueDate, status: raw.status };
        }
        this.actionLoading[loanId] = false;
        this.actionSuccess[loanId] = 'Renewed until ' + new Date(raw.dueDate).toLocaleDateString();
      },
      error: (err) => {
        this.actionError[loanId] = err.error?.message || 'Renewal failed.';
        this.actionLoading[loanId] = false;
      }
    });
  }

  returnBook(loanId: number): void {
    if (!confirm('Confirm return of this book?')) return;
    this.actionLoading[loanId] = true;
    this.loanService.returnBook(loanId).subscribe({
      next: () => {
        this.loans = this.loans.filter(l => l.id !== loanId);
        this.actionLoading[loanId] = false;
      },
      error: (err) => {
        this.actionError[loanId] = err.error?.message || 'Return failed.';
        this.actionLoading[loanId] = false;
      }
    });
  }

  isOverdue(loan: Loan): boolean {
    return new Date(loan.dueDate) < new Date() && !loan.returnDate;
  }

  getDaysOverdue(loan: Loan): number {
    const diff = new Date().getTime() - new Date(loan.dueDate).getTime();
    return Math.floor(diff / (1000 * 60 * 60 * 24));
  }
}
