import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { LoanService } from '../../shared/services/loan.service';
import { FineService } from '../../shared/services/fine.service';
import { Loan } from '../../shared/models/loan.model';

@Component({
  selector: 'app-returns',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './returns.component.html',
  styleUrl: './returns.component.css'
})
export class ReturnsComponent {
  step: number = 1;
  barcode: string = '';
  condition: string = 'GOOD';
  foundLoan: Loan | null = null;
  completedReturn: Loan | null = null;
  estimatedFine: { amount: number; daysOverdue: number } | null = null;

  lookupLoading: boolean = false;
  returnLoading: boolean = false;
  lookupError: string = '';
  returnError: string = '';

  conditions = ['NEW', 'GOOD', 'FAIR', 'POOR', 'DAMAGED'];

  constructor(
    private loanService: LoanService,
    private fineService: FineService
  ) {}

  findLoan(): void {
    if (!this.barcode.trim()) return;
    this.lookupLoading = true;
    this.lookupError = '';
    this.foundLoan = null;
    this.estimatedFine = null;

    this.loanService.getLoanByCopyBarcode(this.barcode).subscribe({
      next: (rawLoan: any) => {
        this.foundLoan = {
          ...rawLoan,
          bookTitle: rawLoan.bookCopy?.book?.title || rawLoan.bookTitle || 'Unknown',
          memberName: `${rawLoan.member?.user?.firstName || ''} ${rawLoan.member?.user?.lastName || ''}`.trim() || 'Member',
          membershipNumber: rawLoan.member?.membershipNumber || '',
          branchName: rawLoan.branch?.name || '',
        } as any;
        this.lookupLoading = false;
        this.step = 2;
        this.assessFine(rawLoan.id);
      },
      error: (err) => {
        this.lookupError = err.status === 404 ? 'No active loan found for this barcode.' : 'Failed to find loan.';
        this.lookupLoading = false;
      }
    });
  }

  assessFine(loanId: number): void {
    this.fineService.calculateOverdueFine(loanId).subscribe({
      next: (result) => {
        if (result.amount > 0) this.estimatedFine = result;
      },
      error: () => {}
    });
  }

  completeReturn(): void {
    if (!this.foundLoan) return;
    this.returnLoading = true;
    this.returnError = '';

    this.loanService.returnBook(this.foundLoan.id, this.condition).subscribe({
      next: (rawLoan: any) => {
        this.completedReturn = {
          ...rawLoan,
          bookTitle: rawLoan.bookCopy?.book?.title || rawLoan.bookTitle || this.foundLoan?.bookTitle || 'Unknown',
          memberName: rawLoan.member?.user?.firstName ? `${rawLoan.member.user.firstName} ${rawLoan.member.user.lastName}`.trim() : (this.foundLoan as any)?.memberName || 'Member',
          returnDate: rawLoan.returnDate || new Date().toISOString(),
          fineAmount: rawLoan.fineAmount || 0,
        } as any;
        this.step = 3;
        this.returnLoading = false;
      },
      error: (err) => {
        this.returnError = err.error?.message || 'Return failed.';
        this.returnLoading = false;
      }
    });
  }

  isOverdue(loan: Loan): boolean {
    return new Date(loan.dueDate) < new Date();
  }

  getDaysOverdue(loan: Loan): number {
    const diff = new Date().getTime() - new Date(loan.dueDate).getTime();
    return Math.max(0, Math.floor(diff / (1000 * 60 * 60 * 24)));
  }

  reset(): void {
    this.step = 1;
    this.barcode = '';
    this.condition = 'GOOD';
    this.foundLoan = null;
    this.completedReturn = null;
    this.estimatedFine = null;
    this.lookupError = '';
    this.returnError = '';
  }
}
