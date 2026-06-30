import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { NotificationService } from './notification.service';

describe('NotificationService', () => {
  let service: NotificationService;
  let httpMock: HttpTestingController;
  const apiUrl = 'http://localhost:8080/api/notifications';

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [NotificationService],
    });
    service = TestBed.inject(NotificationService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('getMyNotifications() sends GET to /notifications/member/{memberId}', () => {
    const mockNotifications = [{ id: 1, message: 'Hold ready', read: false }] as any[];
    service.getMyNotifications(3).subscribe(n => expect(n as any).toEqual(mockNotifications));
    const req = httpMock.expectOne(`${apiUrl}/member/3`);
    expect(req.request.method).toBe('GET');
    req.flush(mockNotifications);
  });

  it('getUnreadCount() sends GET to /notifications/member/{memberId}/unread-count', () => {
    service.getUnreadCount(3).subscribe(r => expect(r.count).toBe(2));
    const req = httpMock.expectOne(`${apiUrl}/member/3/unread-count`);
    expect(req.request.method).toBe('GET');
    req.flush({ count: 2 });
  });

  it('markAsRead() sends PUT to /notifications/{id}/read', () => {
    const mockNotif = { id: 5, read: true } as any;
    service.markAsRead(5).subscribe(n => expect(n as any).toEqual(mockNotif));
    const req = httpMock.expectOne(`${apiUrl}/5/read`);
    expect(req.request.method).toBe('PUT');
    req.flush(mockNotif);
  });

  it('markAllAsRead() sends PUT to /notifications/member/{memberId}/read-all', () => {
    service.markAllAsRead(3).subscribe();
    const req = httpMock.expectOne(`${apiUrl}/member/3/read-all`);
    expect(req.request.method).toBe('PUT');
    req.flush(null);
  });

  it('deleteNotification() sends DELETE to /notifications/{id}', () => {
    service.deleteNotification(5).subscribe();
    const req = httpMock.expectOne(`${apiUrl}/5`);
    expect(req.request.method).toBe('DELETE');
    req.flush(null);
  });
});
