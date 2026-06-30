import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { HoldService } from './hold.service';

describe('HoldService', () => {
  let service: HoldService;
  let httpMock: HttpTestingController;
  const apiUrl = 'http://localhost:8080/api/holds';

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [HoldService],
    });
    service = TestBed.inject(HoldService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('getHoldById() sends GET to /holds/{id}', () => {
    const mock = { id: 1, bookId: 10, status: 'PENDING' } as any;
    service.getHoldById(1).subscribe(h => expect(h as any).toEqual(mock));
    const req = httpMock.expectOne(`${apiUrl}/1`);
    expect(req.request.method).toBe('GET');
    req.flush(mock);
  });

  it('getReadyHolds() sends GET to /holds with status=READY', () => {
    service.getReadyHolds().subscribe();
    const req = httpMock.expectOne(r => r.url === apiUrl);
    expect(req.request.method).toBe('GET');
    expect(req.request.params.get('status')).toBe('READY');
    req.flush([]);
  });

  it('getReadyHolds() includes branchId when provided', () => {
    service.getReadyHolds(2).subscribe();
    const req = httpMock.expectOne(r => r.url === apiUrl);
    expect(req.request.params.get('branchId')).toBe('2');
    req.flush([]);
  });

  it('getReadyHolds() omits branchId when not provided', () => {
    service.getReadyHolds().subscribe();
    const req = httpMock.expectOne(r => r.url === apiUrl);
    expect(req.request.params.has('branchId')).toBeFalse();
    req.flush([]);
  });

  it('getPendingHolds() sends GET to /holds with status=PENDING and pagination', () => {
    service.getPendingHolds(1, 5).subscribe();
    const req = httpMock.expectOne(r => r.url === apiUrl);
    expect(req.request.params.get('status')).toBe('PENDING');
    expect(req.request.params.get('page')).toBe('1');
    expect(req.request.params.get('size')).toBe('5');
    req.flush([]);
  });

  it('placeHold() sends POST to /holds with request body', () => {
    const request = { bookId: 5, memberId: 3, pickupBranchId: 1 };
    const mockHold = { id: 10, status: 'PENDING' } as any;
    service.placeHold(request).subscribe(h => expect(h as any).toEqual(mockHold));
    const req = httpMock.expectOne(apiUrl);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(request);
    req.flush(mockHold);
  });

  it('cancelHold() sends DELETE to /holds/{id}', () => {
    service.cancelHold(7).subscribe();
    const req = httpMock.expectOne(`${apiUrl}/7`);
    expect(req.request.method).toBe('DELETE');
    req.flush(null);
  });

  it('fulfillHold() sends POST to /holds/{id}/fulfill with copyBarcode', () => {
    service.fulfillHold(3, 'BC-00042').subscribe();
    const req = httpMock.expectOne(`${apiUrl}/3/fulfill`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({ copyBarcode: 'BC-00042' });
    req.flush({});
  });

  it('getQueuePosition() sends GET to /holds/queue-position with bookId and memberId', () => {
    service.getQueuePosition(10, 3).subscribe();
    const req = httpMock.expectOne(r => r.url === `${apiUrl}/queue-position`);
    expect(req.request.method).toBe('GET');
    expect(req.request.params.get('bookId')).toBe('10');
    expect(req.request.params.get('memberId')).toBe('3');
    req.flush({ position: 2 });
  });
});
