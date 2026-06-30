import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { LoanService } from './loan.service';

describe('LoanService', () => {
  let service: LoanService;
  let httpMock: HttpTestingController;
  const baseUrl = 'http://localhost:8080/api/loans';

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [LoanService]
    });
    service = TestBed.inject(LoanService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  // ---------------------------------------------------------------------------
  // getLoanById
  // ---------------------------------------------------------------------------
  describe('getLoanById', () => {
    it('should issue a GET to /api/loans/:id', () => {
      const mockLoan = { id: 42, status: 'ACTIVE' };

      service.getLoanById(42).subscribe(loan => {
        expect(loan as any).toEqual(mockLoan);
      });

      const req = httpMock.expectOne(`${baseUrl}/42`);
      expect(req.request.method).toBe('GET');
      req.flush(mockLoan);
    });

    it('should embed the id in the URL for different ids', () => {
      service.getLoanById(7).subscribe();

      const req = httpMock.expectOne(`${baseUrl}/7`);
      expect(req.request.method).toBe('GET');
      req.flush({});
    });
  });

  // ---------------------------------------------------------------------------
  // getLoanByCopyBarcode
  // ---------------------------------------------------------------------------
  describe('getLoanByCopyBarcode', () => {
    it('should issue a GET to /api/loans/barcode/:barcode', () => {
      const mockLoan = { id: 1, barcode: 'BC-001' };

      service.getLoanByCopyBarcode('BC-001').subscribe(loan => {
        expect(loan as any).toEqual(mockLoan);
      });

      const req = httpMock.expectOne(`${baseUrl}/barcode/BC-001`);
      expect(req.request.method).toBe('GET');
      req.flush(mockLoan);
    });

    it('should encode the barcode as part of the path', () => {
      service.getLoanByCopyBarcode('XYZ-999').subscribe();

      const req = httpMock.expectOne(`${baseUrl}/barcode/XYZ-999`);
      expect(req.request.method).toBe('GET');
      req.flush({});
    });
  });

  // ---------------------------------------------------------------------------
  // getActiveLoans
  // ---------------------------------------------------------------------------
  describe('getActiveLoans', () => {
    it('should issue a GET to /api/loans with default page/size and status=ACTIVE', () => {
      const mockLoans = [{ id: 1 }, { id: 2 }];

      service.getActiveLoans().subscribe(loans => {
        expect(loans as any).toEqual(mockLoans);
      });

      const req = httpMock.expectOne(r =>
        r.url === baseUrl &&
        r.params.get('page') === '0' &&
        r.params.get('size') === '20' &&
        r.params.get('status') === 'ACTIVE'
      );
      expect(req.request.method).toBe('GET');
      req.flush(mockLoans);
    });

    it('should pass custom page and size parameters', () => {
      service.getActiveLoans(2, 10).subscribe();

      const req = httpMock.expectOne(r =>
        r.url === baseUrl &&
        r.params.get('page') === '2' &&
        r.params.get('size') === '10' &&
        r.params.get('status') === 'ACTIVE'
      );
      expect(req.request.method).toBe('GET');
      req.flush([]);
    });

    it('should always include status=ACTIVE regardless of page/size', () => {
      service.getActiveLoans(5, 50).subscribe();

      const req = httpMock.expectOne(r =>
        r.url === baseUrl && r.params.get('status') === 'ACTIVE'
      );
      expect(req.request.params.get('status')).toBe('ACTIVE');
      req.flush([]);
    });
  });

  // ---------------------------------------------------------------------------
  // getOverdueLoans
  // ---------------------------------------------------------------------------
  describe('getOverdueLoans', () => {
    it('should issue a GET to /api/loans/overdue with default page/size', () => {
      const mockLoans = [{ id: 3 }];

      service.getOverdueLoans().subscribe(loans => {
        expect(loans as any).toEqual(mockLoans);
      });

      const req = httpMock.expectOne(r =>
        r.url === `${baseUrl}/overdue` &&
        r.params.get('page') === '0' &&
        r.params.get('size') === '20'
      );
      expect(req.request.method).toBe('GET');
      req.flush(mockLoans);
    });

    it('should pass custom page and size parameters', () => {
      service.getOverdueLoans(1, 5).subscribe();

      const req = httpMock.expectOne(r =>
        r.url === `${baseUrl}/overdue` &&
        r.params.get('page') === '1' &&
        r.params.get('size') === '5'
      );
      expect(req.request.method).toBe('GET');
      req.flush([]);
    });

    it('should NOT include a status query parameter', () => {
      service.getOverdueLoans().subscribe();

      const req = httpMock.expectOne(r => r.url === `${baseUrl}/overdue`);
      expect(req.request.params.has('status')).toBeFalse();
      req.flush([]);
    });
  });

  // ---------------------------------------------------------------------------
  // getTodaysCheckouts
  // ---------------------------------------------------------------------------
  describe('getTodaysCheckouts', () => {
    it('should issue a GET to /api/loans/today', () => {
      const mockLoans = [{ id: 10 }, { id: 11 }];

      service.getTodaysCheckouts().subscribe(loans => {
        expect(loans as any).toEqual(mockLoans);
      });

      const req = httpMock.expectOne(`${baseUrl}/today`);
      expect(req.request.method).toBe('GET');
      req.flush(mockLoans);
    });

    it('should not include any query parameters', () => {
      service.getTodaysCheckouts().subscribe();

      const req = httpMock.expectOne(`${baseUrl}/today`);
      expect(req.request.params.keys().length).toBe(0);
      req.flush([]);
    });
  });

  // ---------------------------------------------------------------------------
  // checkout
  // ---------------------------------------------------------------------------
  describe('checkout', () => {
    it('should issue a POST to /api/loans/checkout with the request body', () => {
      const checkoutRequest = { copyBarcode: 'BC-001', membershipNumber: 'MEM-2024-00003', branchId: 1 };
      const mockLoan = { id: 20, status: 'ACTIVE' } as any;

      service.checkout(checkoutRequest).subscribe(loan => {
        expect(loan as any).toEqual(mockLoan);
      });

      const req = httpMock.expectOne(`${baseUrl}/checkout`);
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual(checkoutRequest);
      req.flush(mockLoan);
    });

    it('should forward all fields of the checkout request unchanged', () => {
      const checkoutRequest = { copyBarcode: 'BC-002', membershipNumber: 'MEM-2024-00004', branchId: 1, dueDays: 21 };

      service.checkout(checkoutRequest).subscribe();

      const req = httpMock.expectOne(`${baseUrl}/checkout`);
      expect(req.request.body).toEqual(checkoutRequest);
      req.flush({});
    });
  });

  // ---------------------------------------------------------------------------
  // returnBook
  // ---------------------------------------------------------------------------
  describe('returnBook', () => {
    it('should issue a POST to /api/loans/:loanId/return with the condition in the body', () => {
      const mockLoan = { id: 15, status: 'RETURNED' };

      service.returnBook(15, 'GOOD').subscribe(loan => {
        expect(loan as any).toEqual(mockLoan);
      });

      const req = httpMock.expectOne(`${baseUrl}/15/return`);
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual({ condition: 'GOOD' });
      req.flush(mockLoan);
    });

    it('should send condition as undefined when omitted', () => {
      service.returnBook(15).subscribe();

      const req = httpMock.expectOne(`${baseUrl}/15/return`);
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual({ condition: undefined });
      req.flush({});
    });

    it('should embed the loanId in the URL', () => {
      service.returnBook(99, 'DAMAGED').subscribe();

      const req = httpMock.expectOne(`${baseUrl}/99/return`);
      expect(req.request.url).toContain('/99/return');
      req.flush({});
    });
  });

  // ---------------------------------------------------------------------------
  // renewLoan
  // ---------------------------------------------------------------------------
  describe('renewLoan', () => {
    it('should issue a POST to /api/loans/:loanId/renew with loanId and extendDays in the body', () => {
      const mockLoan = { id: 30, status: 'ACTIVE' };

      service.renewLoan(30, 14).subscribe(loan => {
        expect(loan as any).toEqual(mockLoan);
      });

      const req = httpMock.expectOne(`${baseUrl}/30/renew`);
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual({ loanId: 30, extendDays: 14 });
      req.flush(mockLoan);
    });

    it('should send extendDays as undefined when omitted', () => {
      service.renewLoan(30).subscribe();

      const req = httpMock.expectOne(`${baseUrl}/30/renew`);
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual({ loanId: 30, extendDays: undefined });
      req.flush({});
    });

    it('should embed the loanId in the URL', () => {
      service.renewLoan(77, 7).subscribe();

      const req = httpMock.expectOne(`${baseUrl}/77/renew`);
      expect(req.request.url).toContain('/77/renew');
      req.flush({});
    });

    it('should include loanId in the body even when extendDays is not provided', () => {
      service.renewLoan(55).subscribe();

      const req = httpMock.expectOne(`${baseUrl}/55/renew`);
      expect(req.request.body.loanId).toBe(55);
      req.flush({});
    });
  });
});
