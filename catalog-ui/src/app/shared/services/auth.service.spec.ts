import { TestBed, fakeAsync, tick } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { Router } from '@angular/router';
import { AuthService } from './auth.service';

const NOT_EXPIRED = 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ0ZXN0QGV4YW1wbGUuY29tIiwicm9sZXMiOlsiUk9MRV9VU0VSIl0sImV4cCI6OTk5OTk5OTk5OX0.fake';
const EXPIRED = 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ0ZXN0QGV4YW1wbGUuY29tIiwicm9sZXMiOltdLCJleHAiOjF9.fake';

describe('AuthService', () => {
  let service: AuthService;
  let httpMock: HttpTestingController;
  let router: Router;

  beforeEach(() => {
    localStorage.clear();

    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule, RouterTestingModule],
      providers: [AuthService],
    });

    httpMock = TestBed.inject(HttpTestingController);
    router = TestBed.inject(Router);
  });

  afterEach(() => {
    httpMock.verify();
    localStorage.clear();
  });

  describe('constructor', () => {
    it('should restore user from localStorage on init', () => {
      const storedUser = { username: 'test@example.com', roles: ['ROLE_USER'], memberId: 42 };
      localStorage.setItem('current_user', JSON.stringify(storedUser));

      service = TestBed.inject(AuthService);

      expect(service.getCurrentUser()).toEqual(storedUser);
    });

    it('should ignore invalid JSON in localStorage and remove the key', () => {
      localStorage.setItem('current_user', 'not-valid-json{{{');

      service = TestBed.inject(AuthService);

      expect(service.getCurrentUser()).toBeNull();
      expect(localStorage.getItem('current_user')).toBeNull();
    });
  });

  describe('login()', () => {
    beforeEach(() => {
      service = TestBed.inject(AuthService);
    });

    it('should POST to the correct URL and store the access token', fakeAsync(() => {
      const credentials = { email: 'test@example.com', password: 'secret' };
      const authResponse = { access_token: NOT_EXPIRED, refresh_token: 'refresh-abc' };

      let result: any;
      service.login(credentials).subscribe((r) => (result = r));

      const authReq = httpMock.expectOne('http://localhost:8080/api/v1/auth/authenticate');
      expect(authReq.request.method).toBe('POST');
      expect(authReq.request.body).toEqual(credentials);
      authReq.flush(authResponse);

      const memberReq = httpMock.expectOne('http://localhost:8080/api/members/me');
      memberReq.flush({ id: 7, user: { firstName: 'Jane', lastName: 'Doe' } });

      tick();

      expect(localStorage.getItem('auth_token')).toBe(NOT_EXPIRED);
      expect(localStorage.getItem('refresh_token')).toBe('refresh-abc');
      expect(result).toEqual(authResponse);
    }));

    it('should store refresh_token only when present in the response', fakeAsync(() => {
      const credentials = { email: 'test@example.com', password: 'pass' };
      const authResponse = { access_token: NOT_EXPIRED };

      service.login(credentials).subscribe();

      httpMock.expectOne('http://localhost:8080/api/v1/auth/authenticate').flush(authResponse);
      httpMock.expectOne('http://localhost:8080/api/members/me').flush({});

      tick();

      expect(localStorage.getItem('refresh_token')).toBeNull();
    }));

    it('should chain /members/me call and update memberId, firstName, and lastName', fakeAsync(() => {
      const credentials = { email: 'test@example.com', password: 'pass' };
      const authResponse = { access_token: NOT_EXPIRED };
      const memberPayload = { id: 99, user: { firstName: 'John', lastName: 'Smith' } };

      service.login(credentials).subscribe();

      httpMock.expectOne('http://localhost:8080/api/v1/auth/authenticate').flush(authResponse);
      httpMock.expectOne('http://localhost:8080/api/members/me').flush(memberPayload);

      tick();

      const user = service.getCurrentUser();
      expect(user?.memberId).toBe(99);
      expect(user?.firstName).toBe('John');
      expect(user?.lastName).toBe('Smith');

      const stored = JSON.parse(localStorage.getItem('current_user')!);
      expect(stored.memberId).toBe(99);
      expect(stored.firstName).toBe('John');
      expect(stored.lastName).toBe('Smith');
    }));

    it('should handle /members/me failure gracefully and return the original auth response', fakeAsync(() => {
      const credentials = { email: 'test@example.com', password: 'pass' };
      const authResponse = { access_token: NOT_EXPIRED };

      let result: any;
      let error: any;
      service.login(credentials).subscribe({
        next: (r) => (result = r),
        error: (e) => (error = e),
      });

      httpMock.expectOne('http://localhost:8080/api/v1/auth/authenticate').flush(authResponse);
      httpMock.expectOne('http://localhost:8080/api/members/me').flush('Server Error', { status: 500, statusText: 'Internal Server Error' });

      tick();

      expect(error).toBeUndefined();
      expect(result).toEqual(authResponse);
    }));
  });

  describe('logout()', () => {
    beforeEach(() => {
      service = TestBed.inject(AuthService);
    });

    it('should clear all auth keys from localStorage and navigate to /login', () => {
      localStorage.setItem('auth_token', 'some-token');
      localStorage.setItem('refresh_token', 'some-refresh');
      localStorage.setItem('current_user', JSON.stringify({ username: 'u', roles: [] }));

      spyOn(router, 'navigate');

      service.logout();

      expect(localStorage.getItem('auth_token')).toBeNull();
      expect(localStorage.getItem('refresh_token')).toBeNull();
      expect(localStorage.getItem('current_user')).toBeNull();
      expect(router.navigate).toHaveBeenCalledWith(['/login']);
    });

    it('should set currentUserSubject to null', () => {
      localStorage.setItem('current_user', JSON.stringify({ username: 'u', roles: [] }));
      service = TestBed.inject(AuthService);

      spyOn(router, 'navigate');
      service.logout();

      expect(service.getCurrentUser()).toBeNull();

      let emitted: any = 'unset';
      service.currentUser$.subscribe((u) => (emitted = u));
      expect(emitted).toBeNull();
    });
  });

  describe('isLoggedIn()', () => {
    beforeEach(() => {
      service = TestBed.inject(AuthService);
    });

    it('should return true when a valid, non-expired token is present', () => {
      localStorage.setItem('auth_token', NOT_EXPIRED);
      expect(service.isLoggedIn()).toBeTrue();
    });

    it('should return false when no token is present', () => {
      localStorage.removeItem('auth_token');
      expect(service.isLoggedIn()).toBeFalse();
    });

    it('should return false when the token is expired', () => {
      localStorage.setItem('auth_token', EXPIRED);
      expect(service.isLoggedIn()).toBeFalse();
    });
  });

  describe('isTokenExpired()', () => {
    beforeEach(() => {
      service = TestBed.inject(AuthService);
    });

    it('should return false for a token with a future exp', () => {
      expect(service.isTokenExpired(NOT_EXPIRED)).toBeFalse();
    });

    it('should return true for a token with a past exp', () => {
      expect(service.isTokenExpired(EXPIRED)).toBeTrue();
    });

    it('should return true for a malformed token', () => {
      expect(service.isTokenExpired('not.a.token')).toBeTrue();
      expect(service.isTokenExpired('completelywrong')).toBeTrue();
      expect(service.isTokenExpired('')).toBeTrue();
    });
  });

  describe('hasRole()', () => {
    beforeEach(() => {
      service = TestBed.inject(AuthService);
    });

    it('should return true when the current user has the specified role', () => {
      (service as any).currentUserSubject.next({ username: 'u', roles: ['ROLE_ADMIN', 'ROLE_USER'] });

      expect(service.hasRole('ROLE_ADMIN')).toBeTrue();
      expect(service.hasRole('ROLE_USER')).toBeTrue();
    });

    it('should return false when the current user does not have the specified role', () => {
      (service as any).currentUserSubject.next({ username: 'u', roles: ['ROLE_USER'] });

      expect(service.hasRole('ROLE_ADMIN')).toBeFalse();
    });

    it('should return false when there is no current user', () => {
      expect(service.hasRole('ROLE_USER')).toBeFalse();
    });
  });

  describe('getMemberId()', () => {
    beforeEach(() => {
      service = TestBed.inject(AuthService);
    });

    it('should return the memberId from the current user', () => {
      (service as any).currentUserSubject.next({ username: 'u', roles: [], memberId: 55 });

      expect(service.getMemberId()).toBe(55);
    });

    it('should return 1 as a fallback when there is no current user', () => {
      expect(service.getMemberId()).toBe(1);
    });

    it('should return 1 as a fallback when the user has no memberId', () => {
      (service as any).currentUserSubject.next({ username: 'u', roles: [] });

      expect(service.getMemberId()).toBe(1);
    });
  });

  describe('getToken()', () => {
    beforeEach(() => {
      service = TestBed.inject(AuthService);
    });

    it('should return the token from localStorage', () => {
      localStorage.setItem('auth_token', NOT_EXPIRED);
      expect(service.getToken()).toBe(NOT_EXPIRED);
    });

    it('should return null when no token is stored', () => {
      expect(service.getToken()).toBeNull();
    });
  });

  describe('parseJwt()', () => {
    beforeEach(() => {
      service = TestBed.inject(AuthService);
    });

    it('should correctly parse the JWT payload into a UserInfo object', () => {
      const result = service.parseJwt(NOT_EXPIRED);
      expect(result.username).toBe('test@example.com');
      expect(result.roles).toEqual(['ROLE_USER']);
    });
  });
});
