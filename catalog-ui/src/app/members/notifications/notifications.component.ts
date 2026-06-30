import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NotificationService } from '../../shared/services/notification.service';
import { Notification } from '../../shared/models/notification.model';
import { AuthService } from '../../shared/services/auth.service';

@Component({
  selector: 'app-notifications',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './notifications.component.html',
  styleUrl: './notifications.component.css'
})
export class NotificationsComponent implements OnInit {
  notifications: Notification[] = [];
  loading: boolean = false;
  markingAll: boolean = false;

  get memberId(): number { return this.authService.getMemberId(); }

  get unreadCount(): number {
    return this.notifications.filter(n => !n.read).length;
  }

  constructor(
    private notificationService: NotificationService,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    this.loadNotifications();
  }

  loadNotifications(): void {
    this.loading = true;
    this.notificationService.getMyNotifications(this.memberId).subscribe({
      next: (rawNotifs: any[]) => {
        this.notifications = rawNotifs.map(n => ({
          ...n,
          memberId: n.member?.id,
          subject: this.getTypeLabel(n.type),
          read: !!n.readDate,
          sentAt: n.sentDate,
          readAt: n.readDate,
        }));
        this.loading = false;
      },
      error: (err) => {
        console.error('Failed to load notifications', err);
        this.loading = false;
      }
    });
  }

  private getTypeLabel(type: string): string {
    const labels: Record<string, string> = {
      LOAN_DUE_REMINDER: 'Due Date Reminder',
      LOAN_OVERDUE: 'Overdue Notice',
      HOLD_READY: 'Hold Ready for Pickup',
      HOLD_EXPIRING: 'Hold Expiring Soon',
      FINE_ISSUED: 'Fine Issued',
      FINE_PAID: 'Payment Confirmed',
      MEMBERSHIP_EXPIRING: 'Membership Expiring',
      EVENT_REMINDER: 'Event Reminder',
      GENERAL: 'Library Notification',
    };
    return labels[type] || 'Notification';
  }

  markRead(notification: Notification): void {
    if (notification.read) return;
    this.notificationService.markAsRead(notification.id).subscribe({
      next: () => {
        const idx = this.notifications.findIndex(n => n.id === notification.id);
        if (idx !== -1) this.notifications[idx] = { ...this.notifications[idx], read: true };
      },
      error: (err) => console.error('Failed to mark as read', err)
    });
  }

  markAllRead(): void {
    this.markingAll = true;
    this.notificationService.markAllAsRead(this.memberId).subscribe({
      next: () => {
        this.notifications = this.notifications.map(n => ({ ...n, read: true }));
        this.markingAll = false;
      },
      error: (err) => {
        console.error('Failed to mark all as read', err);
        this.markingAll = false;
      }
    });
  }

  getTypeIcon(type: string): string {
    const icons: Record<string, string> = {
      LOAN_DUE_REMINDER: '📅',
      LOAN_OVERDUE: '⚠️',
      HOLD_READY: '✅',
      FINE_ISSUED: '💰',
      EVENT_REMINDER: '🎉',
      GENERAL: '📢'
    };
    return icons[type] || '🔔';
  }
}
