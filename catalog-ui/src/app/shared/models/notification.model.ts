export enum NotificationType {
  LOAN_DUE_REMINDER = 'LOAN_DUE_REMINDER',
  LOAN_OVERDUE = 'LOAN_OVERDUE',
  HOLD_READY = 'HOLD_READY',
  HOLD_EXPIRING = 'HOLD_EXPIRING',
  FINE_ISSUED = 'FINE_ISSUED',
  FINE_PAID = 'FINE_PAID',
  MEMBERSHIP_EXPIRING = 'MEMBERSHIP_EXPIRING',
  EVENT_REMINDER = 'EVENT_REMINDER',
  GENERAL = 'GENERAL'
}

export enum NotificationChannel {
  EMAIL = 'EMAIL',
  SMS = 'SMS',
  IN_APP = 'IN_APP',
  PUSH = 'PUSH'
}

export interface Notification {
  id: number;
  memberId: number;
  type: NotificationType;
  channel: NotificationChannel;
  subject: string;
  message: string;
  read: boolean;
  sentAt: string;
  readAt?: string;
  relatedEntityId?: number;
  relatedEntityType?: string;
}
