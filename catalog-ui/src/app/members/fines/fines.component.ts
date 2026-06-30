import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MemberService } from '../../shared/services/member.service';
import { FineService } from '../../shared/services/fine.service';
import { Fine } from '../../shared/models/fine.model';
import { AuthService } from '../../shared/services/auth.service';

@Component({
  selector: 'app-fines',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './fines.component.html',
  styleUrl: './fines.component.css'
})
export class FinesComponent implements OnInit {
  fines: Fine[] = [];
  loading: boolean = false;
  payLoading: { [fineId: number]: boolean } = {};
  payError: { [fineId: number]: string } = {};
  paySuccess: { [fineId: number]: boolean } = {};

  get memberId(): number { return this.authService.getMemberId(); }

  get totalUnpaid(): number {
    return this.fines.filter(f => f.status === 'UNPAID').reduce((sum, f) => sum + f.remainingAmount, 0);
  }

  constructor(
    private memberService: MemberService,
    private fineService: FineService,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    this.loadFines();
  }

  loadFines(): void {
    this.loading = true;
    this.memberService.getMemberFines(this.memberId).subscribe({
      next: (rawFines: any[]) => {
        this.fines = rawFines.map(f => {
          const isPaid = !!f.paidDate;
          const isWaived = !!f.waived && !!f.waivedBy;
          return {
            ...f,
            bookTitle: f.loan?.bookCopy?.book?.title || 'Unknown',
            loanId: f.loan?.id,
            memberId: f.member?.id,
            memberName: `${f.member?.user?.firstName || ''} ${f.member?.user?.lastName || ''}`.trim(),
            paidAmount: isPaid ? f.amount : 0,
            remainingAmount: isPaid ? 0 : f.amount,
            status: isPaid ? 'PAID' : (isWaived ? 'WAIVED' : 'UNPAID'),
            createdDate: f.issuedDate,
          };
        });
        this.loading = false;
      },
      error: (err) => {
        console.error('Failed to load fines', err);
        this.loading = false;
      }
    });
  }

  payFine(fine: Fine): void {
    if (!confirm(`Pay $${fine.remainingAmount.toFixed(2)} for "${fine.bookTitle}"?`)) return;
    this.payLoading[fine.id] = true;
    this.payError[fine.id] = '';
    this.fineService.payFine({ fineId: fine.id, amount: fine.remainingAmount, paymentMethod: 'CARD' }).subscribe({
      next: () => {
        const idx = this.fines.findIndex(f => f.id === fine.id);
        if (idx !== -1) {
          this.fines[idx] = { ...this.fines[idx], status: 'PAID' as any, remainingAmount: 0, paidAmount: fine.amount };
        }
        this.payLoading[fine.id] = false;
        this.paySuccess[fine.id] = true;
      },
      error: (err) => {
        this.payError[fine.id] = err.error?.message || 'Payment failed.';
        this.payLoading[fine.id] = false;
      }
    });
  }

  getStatusClass(status: string): string {
    const map: Record<string, string> = { UNPAID: 'badge-danger', PAID: 'badge-success', WAIVED: 'badge-secondary' };
    return map[status] || 'badge-secondary';
  }
}
