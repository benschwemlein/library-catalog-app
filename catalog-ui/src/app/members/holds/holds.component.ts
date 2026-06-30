import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MemberService } from '../../shared/services/member.service';
import { HoldService } from '../../shared/services/hold.service';
import { Hold } from '../../shared/models/hold.model';
import { AuthService } from '../../shared/services/auth.service';

@Component({
  selector: 'app-holds',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './holds.component.html',
  styleUrl: './holds.component.css'
})
export class HoldsComponent implements OnInit {
  holds: Hold[] = [];
  loading: boolean = false;
  cancelLoading: { [holdId: number]: boolean } = {};
  cancelError: { [holdId: number]: string } = {};

  get memberId(): number { return this.authService.getMemberId(); }

  constructor(
    private memberService: MemberService,
    private holdService: HoldService,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    this.loadHolds();
  }

  loadHolds(): void {
    this.loading = true;
    this.memberService.getMemberHolds(this.memberId).subscribe({
      next: (rawHolds: any[]) => {
        this.holds = rawHolds.map(h => ({
          ...h,
          bookTitle: h.book?.title || 'Unknown',
          bookIsbn: h.book?.isbn || '',
          authorNames: (h.book?.authors || []).map((a: any) => `${a.firstName} ${a.lastName}`),
          memberId: h.member?.id,
          memberName: `${h.member?.user?.firstName || ''} ${h.member?.user?.lastName || ''}`.trim(),
          membershipNumber: h.member?.membershipNumber || '',
          pickupBranchId: h.pickupBranch?.id,
          pickupBranchName: h.pickupBranch?.name || 'Main Branch',
          placedDate: h.requestDate || h.placedDate,
        }));
        this.loading = false;
      },
      error: (err) => {
        console.error('Failed to load holds', err);
        this.loading = false;
      }
    });
  }

  cancelHold(holdId: number): void {
    if (!confirm('Cancel this hold?')) return;
    this.cancelLoading[holdId] = true;
    this.holdService.cancelHold(holdId).subscribe({
      next: () => {
        this.holds = this.holds.filter(h => h.id !== holdId);
        this.cancelLoading[holdId] = false;
      },
      error: (err) => {
        this.cancelError[holdId] = err.error?.message || 'Failed to cancel hold.';
        this.cancelLoading[holdId] = false;
      }
    });
  }

  getStatusClass(status: string): string {
    const map: Record<string, string> = {
      PENDING: 'badge-warning', READY: 'badge-success',
      FULFILLED: 'badge-secondary', CANCELLED: 'badge-dark', EXPIRED: 'badge-danger'
    };
    return map[status] || 'badge-secondary';
  }
}
