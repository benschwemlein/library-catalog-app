import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Notification } from '../models/notification.model';

@Injectable({ providedIn: 'root' })
export class NotificationService {
  private apiUrl = 'http://localhost:8080/api/notifications';

  constructor(private http: HttpClient) {}

  getMyNotifications(memberId: number): Observable<Notification[]> {
    return this.http.get<Notification[]>(`${this.apiUrl}/member/${memberId}`);
  }

  getUnreadCount(memberId: number): Observable<{ count: number }> {
    return this.http.get<{ count: number }>(`${this.apiUrl}/member/${memberId}/unread-count`);
  }

  markAsRead(notificationId: number): Observable<Notification> {
    return this.http.put<Notification>(`${this.apiUrl}/${notificationId}/read`, {});
  }

  markAllAsRead(memberId: number): Observable<void> {
    return this.http.put<void>(`${this.apiUrl}/member/${memberId}/read-all`, {});
  }

  deleteNotification(notificationId: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${notificationId}`);
  }
}
