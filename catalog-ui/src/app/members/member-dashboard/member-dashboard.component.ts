import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { MemberService } from '../../shared/services/member.service';
import { NotificationService } from '../../shared/services/notification.service';
import { Member } from '../../shared/models/member.model';
import { Loan } from '../../shared/models/loan.model';
import { Hold } from '../../shared/models/hold.model';
import { Fine } from '../../shared/models/fine.model';
import { Notification } from '../../shared/models/notification.model';
import { AuthService } from '../../shared/services/auth.service';

@Component({
  selector: 'app-member-dashboard',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './member-dashboard.component.html',
  styleUrl: './member-dashboard.component.css'
})
export class MemberDashboardComponent implements OnInit {
  member: Member | null = null;
  currentLoans: Loan[] = [];
  holds: Hold[] = [];
  unpaidFines: Fine[] = [];
  notifications: Notification[] = [];
  loading: boolean = false;

  constructor(
    private memberService: MemberService,
    private notificationService: NotificationService,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    this.loadDashboard();
  }

  loadDashboard(): void {
    this.loading = true;
    // Always resolve the member via /members/me (JWT-authenticated) so the
    // correct member is shown regardless of what is cached in localStorage.
    this.memberService.getMe().subscribe({
      next: (raw: any) => {
        const id: number = raw.id;
        this.member = {
          ...raw,
          firstName: raw.user?.firstName || raw.firstName || '',
          lastName: raw.user?.lastName || raw.lastName || '',
          email: raw.user?.email || raw.email || '',
          memberSince: raw.joinDate || raw.memberSince,
          expirationDate: raw.expiryDate || raw.expirationDate,
        };
        this.loading = false;
        this.loadMemberData(id);
      },
      error: (err) => {
        console.error('Failed to load member', err);
        this.loading = false;
      }
    });
  }

  private loadMemberData(id: number): void {
    this.memberService.getMemberLoans(id, 'ACTIVE').subscribe({
      next: (rawLoans: any[]) => {
        this.currentLoans = rawLoans
          .filter(l => l.status === 'ACTIVE' || l.status === 'OVERDUE')
          .map(l => ({
            ...l,
            bookTitle: l.bookCopy?.book?.title || l.bookTitle || 'Unknown',
            branchName: l.branch?.name || l.branchName || '',
          }));
      },
      error: (err) => console.error('Failed to load loans', err)
    });

    this.memberService.getMemberHolds(id).subscribe({
      next: (rawHolds: any[]) => {
        this.holds = rawHolds.map(h => ({
          ...h,
          bookTitle: h.book?.title || h.bookTitle || 'Unknown',
        }));
      },
      error: (err) => console.error('Failed to load holds', err)
    });

    this.memberService.getMemberFines(id, true).subscribe({
      next: (rawFines: any[]) => {
        this.unpaidFines = rawFines
          .filter(f => !f.paidDate)
          .map(f => ({
            ...f,
            bookTitle: f.loan?.bookCopy?.book?.title || f.bookTitle || 'Unknown',
            remainingAmount: f.amount || 0,
            status: 'UNPAID',
          }));
      },
      error: (err) => console.error('Failed to load fines', err)
    });

    this.notificationService.getMyNotifications(id).subscribe({
      next: (rawNotifs: any[]) => {
        this.notifications = rawNotifs.slice(0, 5).map(n => ({
          ...n,
          subject: this.getNotifLabel(n.type),
          read: !!n.readDate,
          sentAt: n.sentDate,
        }));
      },
      error: (err) => console.error('Failed to load notifications', err)
    });
  }

  private getNotifLabel(type: string): string {
    const labels: Record<string, string> = {
      LOAN_DUE_REMINDER: 'Due Date Reminder',
      LOAN_OVERDUE: 'Overdue Notice',
      HOLD_READY: 'Hold Ready for Pickup',
      FINE_ISSUED: 'Fine Issued',
      GENERAL: 'Library Notification',
    };
    return labels[type] || 'Notification';
  }

  get totalFinesAmount(): number {
    return this.unpaidFines.reduce((sum, f) => sum + f.remainingAmount, 0);
  }

  get unreadNotificationCount(): number {
    return this.notifications.filter(n => !n.read).length;
  }

  get overdueLoansCount(): number {
    return this.currentLoans.filter(l => l.status === 'OVERDUE' as any).length;
  }

  get readyHoldsCount(): number {
    return this.holds.filter(h => h.status === 'READY').length;
  }
}
