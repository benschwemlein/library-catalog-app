import { TestBed } from '@angular/core/testing';
import { ActivatedRouteSnapshot, CanActivateFn, Router, RouterStateSnapshot } from '@angular/router';
import { AuthService } from '../shared/services/auth.service';

import { authGuard } from './auth.guard';
import { adminGuard } from './admin.guard';
import { staffGuard } from './staff.guard';
import { memberOwnerGuard } from './member-owner.guard';

describe('Route Guards', () => {
  let mockAuthService: jasmine.SpyObj<{
    isLoggedIn: () => boolean;
    getToken: () => string | null;
    isTokenExpired: (token: string) => boolean;
    hasRole: (role: string) => boolean;
    logout: () => void;
    getCurrentUser: () => { memberId: number } | null;
  }>;
  let mockRouter: jasmine.SpyObj<Router>;

  const mockRoute = {} as ActivatedRouteSnapshot;
  const mockState = { url: '/protected' } as RouterStateSnapshot;

  const VALID_TOKEN = 'valid.jwt.token';

  beforeEach(() => {
    mockAuthService = jasmine.createSpyObj('AuthService', [
      'isLoggedIn',
      'getToken',
      'isTokenExpired',
      'hasRole',
      'logout',
      'getCurrentUser',
    ]);

    mockRouter = jasmine.createSpyObj('Router', ['navigate']);

    TestBed.configureTestingModule({
      providers: [
        { provide: AuthService, useValue: mockAuthService },
        { provide: Router, useValue: mockRouter },
      ],
    });
  });

  // ---------------------------------------------------------------------------
  // Helper – run a functional guard inside an injection context
  // ---------------------------------------------------------------------------
  function runGuard(
    guard: CanActivateFn,
    route: ActivatedRouteSnapshot = mockRoute,
    state: RouterStateSnapshot = mockState
  ): boolean {
    return TestBed.runInInjectionContext(() => guard(route, state)) as boolean;
  }

  // ---------------------------------------------------------------------------
  // authGuard
  // ---------------------------------------------------------------------------
  describe('authGuard', () => {
    it('should return true when logged in with a valid token', () => {
      mockAuthService.isLoggedIn.and.returnValue(true);
      mockAuthService.getToken.and.returnValue(VALID_TOKEN);
      mockAuthService.isTokenExpired.and.returnValue(false);

      const result = runGuard(authGuard);

      expect(result).toBeTrue();
      expect(mockRouter.navigate).not.toHaveBeenCalled();
    });

    it('should navigate to /login with returnUrl and return false when not logged in', () => {
      mockAuthService.isLoggedIn.and.returnValue(false);

      const result = runGuard(authGuard);

      expect(result).toBeFalse();
      expect(mockRouter.navigate).toHaveBeenCalledWith(
        ['/login'],
        { queryParams: { returnUrl: mockState.url } }
      );
    });

    it('should navigate to /login and return false when logged in but token is expired', () => {
      mockAuthService.isLoggedIn.and.returnValue(true);
      mockAuthService.getToken.and.returnValue(VALID_TOKEN);
      mockAuthService.isTokenExpired.and.returnValue(true);

      const result = runGuard(authGuard);

      expect(result).toBeFalse();
      expect(mockRouter.navigate).toHaveBeenCalledWith(
        ['/login'],
        { queryParams: { returnUrl: mockState.url } }
      );
    });
  });

  // ---------------------------------------------------------------------------
  // adminGuard
  // ---------------------------------------------------------------------------
  describe('adminGuard', () => {
    it('should return true when logged in with valid token and ROLE_ADMIN', () => {
      mockAuthService.isLoggedIn.and.returnValue(true);
      mockAuthService.getToken.and.returnValue(VALID_TOKEN);
      mockAuthService.isTokenExpired.and.returnValue(false);
      mockAuthService.hasRole.and.callFake((role: string) => role === 'ROLE_ADMIN');

      const result = runGuard(adminGuard);

      expect(result).toBeTrue();
      expect(mockRouter.navigate).not.toHaveBeenCalled();
    });

    it('should navigate to /login and return false when not logged in', () => {
      mockAuthService.isLoggedIn.and.returnValue(false);

      const result = runGuard(adminGuard);

      expect(result).toBeFalse();
      expect(mockRouter.navigate).toHaveBeenCalledWith(
        ['/login'],
        { queryParams: { returnUrl: mockState.url } }
      );
    });

    it('should navigate to /unauthorized and return false when logged in without ROLE_ADMIN', () => {
      mockAuthService.isLoggedIn.and.returnValue(true);
      mockAuthService.getToken.and.returnValue(VALID_TOKEN);
      mockAuthService.isTokenExpired.and.returnValue(false);
      mockAuthService.hasRole.and.returnValue(false);

      const result = runGuard(adminGuard);

      expect(result).toBeFalse();
      expect(mockRouter.navigate).toHaveBeenCalledWith(['/unauthorized']);
    });

    it('should call logout and navigate to /login with sessionExpired when token is expired', () => {
      mockAuthService.isLoggedIn.and.returnValue(true);
      mockAuthService.getToken.and.returnValue(VALID_TOKEN);
      mockAuthService.isTokenExpired.and.returnValue(true);

      const result = runGuard(adminGuard);

      expect(result).toBeFalse();
      expect(mockAuthService.logout).toHaveBeenCalled();
      expect(mockRouter.navigate).toHaveBeenCalledWith(
        ['/login'],
        { queryParams: { returnUrl: mockState.url, sessionExpired: true } }
      );
    });
  });

  // ---------------------------------------------------------------------------
  // staffGuard
  // ---------------------------------------------------------------------------
  describe('staffGuard', () => {
    it('should return true when user has ROLE_STAFF', () => {
      mockAuthService.isLoggedIn.and.returnValue(true);
      mockAuthService.getToken.and.returnValue(VALID_TOKEN);
      mockAuthService.isTokenExpired.and.returnValue(false);
      mockAuthService.hasRole.and.callFake((role: string) => role === 'ROLE_STAFF');

      const result = runGuard(staffGuard);

      expect(result).toBeTrue();
      expect(mockRouter.navigate).not.toHaveBeenCalled();
    });

    it('should return true when user has ROLE_ADMIN (admins pass staffGuard)', () => {
      mockAuthService.isLoggedIn.and.returnValue(true);
      mockAuthService.getToken.and.returnValue(VALID_TOKEN);
      mockAuthService.isTokenExpired.and.returnValue(false);
      mockAuthService.hasRole.and.callFake((role: string) => role === 'ROLE_ADMIN');

      const result = runGuard(staffGuard);

      expect(result).toBeTrue();
      expect(mockRouter.navigate).not.toHaveBeenCalled();
    });

    it('should navigate to /login and return false when not logged in', () => {
      mockAuthService.isLoggedIn.and.returnValue(false);

      const result = runGuard(staffGuard);

      expect(result).toBeFalse();
      expect(mockRouter.navigate).toHaveBeenCalledWith(
        ['/login'],
        { queryParams: { returnUrl: mockState.url } }
      );
    });

    it('should navigate to /unauthorized and return false when logged in without ROLE_STAFF or ROLE_ADMIN', () => {
      mockAuthService.isLoggedIn.and.returnValue(true);
      mockAuthService.getToken.and.returnValue(VALID_TOKEN);
      mockAuthService.isTokenExpired.and.returnValue(false);
      mockAuthService.hasRole.and.returnValue(false);

      const result = runGuard(staffGuard);

      expect(result).toBeFalse();
      expect(mockRouter.navigate).toHaveBeenCalledWith(['/unauthorized']);
    });
  });

  // ---------------------------------------------------------------------------
  // memberOwnerGuard
  // ---------------------------------------------------------------------------
  describe('memberOwnerGuard', () => {
    function routeWithMemberId(memberId: string): ActivatedRouteSnapshot {
      return { params: { memberId } } as unknown as ActivatedRouteSnapshot;
    }

    it('should navigate to /login and return false when not logged in', () => {
      mockAuthService.isLoggedIn.and.returnValue(false);

      const result = runGuard(memberOwnerGuard, routeWithMemberId('5'));

      expect(result).toBeFalse();
      expect(mockRouter.navigate).toHaveBeenCalledWith(
        ['/login'],
        { queryParams: { returnUrl: mockState.url } }
      );
    });

    it('should call logout and navigate to /login with sessionExpired when token is expired', () => {
      mockAuthService.isLoggedIn.and.returnValue(true);
      mockAuthService.getToken.and.returnValue(VALID_TOKEN);
      mockAuthService.isTokenExpired.and.returnValue(true);

      const result = runGuard(memberOwnerGuard, routeWithMemberId('5'));

      expect(result).toBeFalse();
      expect(mockAuthService.logout).toHaveBeenCalled();
      expect(mockRouter.navigate).toHaveBeenCalledWith(
        ['/login'],
        { queryParams: { returnUrl: mockState.url, sessionExpired: true } }
      );
    });

    it('should return true when user has ROLE_STAFF (bypasses ownership check)', () => {
      mockAuthService.isLoggedIn.and.returnValue(true);
      mockAuthService.getToken.and.returnValue(VALID_TOKEN);
      mockAuthService.isTokenExpired.and.returnValue(false);
      mockAuthService.hasRole.and.callFake((role: string) => role === 'ROLE_STAFF');

      const result = runGuard(memberOwnerGuard, routeWithMemberId('99'));

      expect(result).toBeTrue();
      expect(mockRouter.navigate).not.toHaveBeenCalled();
    });

    it('should return true when user has ROLE_ADMIN (bypasses ownership check)', () => {
      mockAuthService.isLoggedIn.and.returnValue(true);
      mockAuthService.getToken.and.returnValue(VALID_TOKEN);
      mockAuthService.isTokenExpired.and.returnValue(false);
      mockAuthService.hasRole.and.callFake((role: string) => role === 'ROLE_ADMIN');

      const result = runGuard(memberOwnerGuard, routeWithMemberId('99'));

      expect(result).toBeTrue();
      expect(mockRouter.navigate).not.toHaveBeenCalled();
    });

    it('should return true when route memberId matches currentUser.memberId', () => {
      mockAuthService.isLoggedIn.and.returnValue(true);
      mockAuthService.getToken.and.returnValue(VALID_TOKEN);
      mockAuthService.isTokenExpired.and.returnValue(false);
      mockAuthService.hasRole.and.returnValue(false);
      mockAuthService.getCurrentUser.and.returnValue({ memberId: 5 });

      const result = runGuard(memberOwnerGuard, routeWithMemberId('5'));

      expect(result).toBeTrue();
      expect(mockRouter.navigate).not.toHaveBeenCalled();
    });

    it('should navigate to /unauthorized and return false when memberId mismatches and no privileged role', () => {
      mockAuthService.isLoggedIn.and.returnValue(true);
      mockAuthService.getToken.and.returnValue(VALID_TOKEN);
      mockAuthService.isTokenExpired.and.returnValue(false);
      mockAuthService.hasRole.and.returnValue(false);
      mockAuthService.getCurrentUser.and.returnValue({ memberId: 7 });

      const result = runGuard(memberOwnerGuard, routeWithMemberId('5'));

      expect(result).toBeFalse();
      expect(mockRouter.navigate).toHaveBeenCalledWith(['/unauthorized']);
    });
  });
});
