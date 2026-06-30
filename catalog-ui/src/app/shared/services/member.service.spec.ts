import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { MemberService } from './member.service';

describe('MemberService', () => {
  let service: MemberService;
  let httpMock: HttpTestingController;
  const apiUrl = 'http://localhost:8080/api/members';

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [MemberService],
    });
    service = TestBed.inject(MemberService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('getMembers() sends GET with page and size params', () => {
    service.getMembers(0, 20).subscribe();
    const req = httpMock.expectOne(r => r.url === apiUrl);
    expect(req.request.method).toBe('GET');
    expect(req.request.params.get('page')).toBe('0');
    expect(req.request.params.get('size')).toBe('20');
    req.flush([]);
  });

  it('getMemberById() sends GET to /members/{id}', () => {
    const mock = { id: 1, membershipNumber: 'MEM-001' } as any;
    service.getMemberById(1).subscribe(m => expect(m as any).toEqual(mock));
    const req = httpMock.expectOne(`${apiUrl}/1`);
    expect(req.request.method).toBe('GET');
    req.flush(mock);
  });

  it('getMemberByMembershipNumber() sends GET to /members/membership/{num}', () => {
    service.getMemberByMembershipNumber('MEM-001').subscribe();
    const req = httpMock.expectOne(`${apiUrl}/membership/MEM-001`);
    expect(req.request.method).toBe('GET');
    req.flush({});
  });

  it('getMemberProfile() sends GET to /members/{id}/profile', () => {
    service.getMemberProfile(2).subscribe();
    const req = httpMock.expectOne(`${apiUrl}/2/profile`);
    expect(req.request.method).toBe('GET');
    req.flush({});
  });

  it('searchMembers() sends GET to /members/search with query params', () => {
    service.searchMembers('carol', 0, 10).subscribe();
    const req = httpMock.expectOne(r => r.url === `${apiUrl}/search`);
    expect(req.request.method).toBe('GET');
    expect(req.request.params.get('q')).toBe('carol');
    req.flush([]);
  });

  it('updateProfile() sends PUT to /members/{id}/profile', () => {
    const profile = { firstName: 'Carol', lastName: 'Updated', email: 'carol@example.com' };
    service.updateProfile(3, profile).subscribe();
    const req = httpMock.expectOne(`${apiUrl}/3/profile`);
    expect(req.request.method).toBe('PUT');
    expect(req.request.body).toEqual(profile);
    req.flush({});
  });

  it('suspendMember() sends POST to /members/{id}/suspend', () => {
    service.suspendMember(3, 'Policy violation').subscribe();
    const req = httpMock.expectOne(`${apiUrl}/3/suspend`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({ reason: 'Policy violation' });
    req.flush({});
  });

  it('reactivateMember() sends POST to /members/{id}/reactivate', () => {
    service.reactivateMember(3).subscribe();
    const req = httpMock.expectOne(`${apiUrl}/3/reactivate`);
    expect(req.request.method).toBe('POST');
    req.flush({});
  });

  it('getMemberLoans() sends GET to /members/{id}/loans without status', () => {
    service.getMemberLoans(3).subscribe();
    const req = httpMock.expectOne(r => r.url === `${apiUrl}/3/loans`);
    expect(req.request.method).toBe('GET');
    expect(req.request.params.has('status')).toBeFalse();
    req.flush([]);
  });

  it('getMemberLoans() includes status param when provided', () => {
    service.getMemberLoans(3, 'ACTIVE').subscribe();
    const req = httpMock.expectOne(r => r.url === `${apiUrl}/3/loans`);
    expect(req.request.params.get('status')).toBe('ACTIVE');
    req.flush([]);
  });

  it('getMemberHolds() sends GET to /members/{id}/holds', () => {
    service.getMemberHolds(3).subscribe();
    const req = httpMock.expectOne(`${apiUrl}/3/holds`);
    expect(req.request.method).toBe('GET');
    req.flush([]);
  });

  it('getMemberFines() sends GET to /members/{id}/fines with unpaidOnly param', () => {
    service.getMemberFines(3, true).subscribe();
    const req = httpMock.expectOne(r => r.url === `${apiUrl}/3/fines`);
    expect(req.request.params.get('unpaidOnly')).toBe('true');
    req.flush([]);
  });

  it('getMemberNotifications() sends GET to /members/{id}/notifications', () => {
    service.getMemberNotifications(3).subscribe();
    const req = httpMock.expectOne(`${apiUrl}/3/notifications`);
    expect(req.request.method).toBe('GET');
    req.flush([]);
  });

  it('updateMemberTier() sends PUT to /members/{id}/tier', () => {
    service.updateMemberTier(3, 'PREMIUM').subscribe();
    const req = httpMock.expectOne(`${apiUrl}/3/tier`);
    expect(req.request.method).toBe('PUT');
    expect(req.request.body).toEqual({ tier: 'PREMIUM' });
    req.flush({});
  });
});
