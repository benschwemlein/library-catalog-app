import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { FineService } from './fine.service';

describe('FineService', () => {
  let service: FineService;
  let httpMock: HttpTestingController;
  const apiUrl = 'http://localhost:8080/api/fines';

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [FineService],
    });
    service = TestBed.inject(FineService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('getFineById() sends GET to /fines/{id}', () => {
    const mock = { id: 1, amount: 1.50, status: 'UNPAID' } as any;
    service.getFineById(1).subscribe(f => expect(f as any).toEqual(mock));
    const req = httpMock.expectOne(`${apiUrl}/1`);
    expect(req.request.method).toBe('GET');
    req.flush(mock);
  });

  it('getFinesByMember() sends GET to /fines/member/{memberId}', () => {
    service.getFinesByMember(3).subscribe();
    const req = httpMock.expectOne(`${apiUrl}/member/3`);
    expect(req.request.method).toBe('GET');
    req.flush([]);
  });

  it('getUnpaidFines() sends GET to /fines/member/{memberId} with status=UNPAID', () => {
    service.getUnpaidFines(3).subscribe();
    const req = httpMock.expectOne(r => r.url === `${apiUrl}/member/3`);
    expect(req.request.method).toBe('GET');
    expect(req.request.params.get('status')).toBe('UNPAID');
    req.flush([]);
  });

  it('getAllUnpaidFines() sends GET to /fines with UNPAID status and pagination', () => {
    service.getAllUnpaidFines(0, 20).subscribe();
    const req = httpMock.expectOne(r => r.url === apiUrl);
    expect(req.request.method).toBe('GET');
    expect(req.request.params.get('status')).toBe('UNPAID');
    expect(req.request.params.get('page')).toBe('0');
    expect(req.request.params.get('size')).toBe('20');
    req.flush([]);
  });

  it('payFine() sends POST to /fines/{id}/pay', () => {
    const request = { fineId: 7, amount: 5.00, paymentMethod: 'CASH' };
    service.payFine(request).subscribe();
    const req = httpMock.expectOne(`${apiUrl}/7/pay`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(request);
    req.flush({ id: 7, status: 'PAID' });
  });

  it('waiveFine() sends POST to /fines/{id}/waive', () => {
    const request = { fineId: 7, reason: 'Hardship waiver' };
    service.waiveFine(request).subscribe();
    const req = httpMock.expectOne(`${apiUrl}/7/waive`);
    expect(req.request.method).toBe('POST');
    req.flush({ id: 7, status: 'WAIVED' });
  });

  it('calculateOverdueFine() sends GET to /fines/calculate/{loanId}', () => {
    service.calculateOverdueFine(10).subscribe(r => {
      expect(r.amount).toBe(2.5);
      expect(r.daysOverdue).toBe(5);
    });
    const req = httpMock.expectOne(`${apiUrl}/calculate/10`);
    expect(req.request.method).toBe('GET');
    req.flush({ amount: 2.5, daysOverdue: 5 });
  });
});
