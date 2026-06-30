import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { LoanService } from '../../shared/services/loan.service';
import { MemberService } from '../../shared/services/member.service';
import { Member } from '../../shared/models/member.model';
import { Loan } from '../../shared/models/loan.model';

@Component({
  selector: 'app-checkout',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './checkout.component.html',
  styleUrl: './checkout.component.css'
})
export class CheckoutComponent {
  step: number = 1;
  barcode: string = '';
  membershipNumber: string = '';
  branchId: number = 1;
  dueDays: number = 21;

  foundCopy: any = null;
  foundMember: Member | null = null;
  completedLoan: Loan | null = null;

  copyLoading: boolean = false;
  memberLoading: boolean = false;
  checkoutLoading: boolean = false;
  copyError: string = '';
  memberError: string = '';
  checkoutError: string = '';

  constructor(
    private loanService: LoanService,
    private memberService: MemberService
  ) {}

  findCopy(): void {
    if (!this.barcode.trim()) return;
    this.copyLoading = true;
    this.copyError = '';
    this.foundCopy = null;
    // In a real implementation we'd have a CopyService with a findByBarcode method
    // For now simulate a lookup via the loan service
    this.loanService.getLoanByCopyBarcode(this.barcode).subscribe({
      next: (loan) => {
        // If there's an active loan, copy is checked out
        this.copyError = 'This copy is already checked out.';
        this.copyLoading = false;
      },
      error: (err) => {
        if (err.status === 404) {
          // 404 means no active loan, so copy is available
          this.foundCopy = { barcode: this.barcode, status: 'AVAILABLE' };
          this.copyError = '';
          this.step = 2;
        } else {
          this.copyError = 'Failed to look up copy.';
        }
        this.copyLoading = false;
      }
    });
  }

  findMember(): void {
    if (!this.membershipNumber.trim()) return;
    this.memberLoading = true;
    this.memberError = '';
    this.foundMember = null;
    this.memberService.getMemberByMembershipNumber(this.membershipNumber).subscribe({
      next: (raw: any) => {
        if (!raw.active) {
          this.memberError = 'Member account is suspended.';
          this.memberLoading = false;
          return;
        }
        this.foundMember = {
          ...raw,
          firstName: raw.user?.firstName || raw.firstName || '',
          lastName: raw.user?.lastName || raw.lastName || '',
          email: raw.user?.email || raw.email || '',
          activeLoansCount: raw.activeLoansCount ?? 0,
        } as any;
        this.step = 3;
        this.memberLoading = false;
      },
      error: (err) => {
        this.memberError = err.status === 404 ? 'Member not found.' : 'Failed to find member.';
        this.memberLoading = false;
      }
    });
  }

  confirmCheckout(): void {
    if (!this.foundMember || !this.foundCopy) return;
    this.checkoutLoading = true;
    this.checkoutError = '';
    this.loanService.checkout({
      copyBarcode: this.barcode,
      membershipNumber: this.membershipNumber,
      branchId: this.branchId,
      dueDays: this.dueDays
    }).subscribe({
      next: (rawLoan: any) => {
        this.completedLoan = {
          ...rawLoan,
          bookTitle: rawLoan.bookCopy?.book?.title || rawLoan.bookTitle || 'Checked Out',
          memberName: `${rawLoan.member?.user?.firstName || ''} ${rawLoan.member?.user?.lastName || ''}`.trim() || this.membershipNumber,
          membershipNumber: rawLoan.member?.membershipNumber || this.membershipNumber,
          branchName: rawLoan.branch?.name || '',
        } as any;
        this.step = 4;
        this.checkoutLoading = false;
      },
      error: (err) => {
        this.checkoutError = err.error?.message || 'Checkout failed.';
        this.checkoutLoading = false;
      }
    });
  }

  reset(): void {
    this.step = 1;
    this.barcode = '';
    this.membershipNumber = '';
    this.foundCopy = null;
    this.foundMember = null;
    this.completedLoan = null;
    this.copyError = '';
    this.memberError = '';
    this.checkoutError = '';
  }
}
