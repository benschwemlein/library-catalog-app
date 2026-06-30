import { inject } from '@angular/core';
import { ActivatedRouteSnapshot, CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../shared/services/auth.service';

/**
 * Allows access if the requesting user is the member referenced in the route
 * OR if the user has ROLE_STAFF or ROLE_ADMIN.
 *
 * Usage: Apply to routes that expose a :memberId param.
 */
export const memberOwnerGuard: CanActivateFn = (route: ActivatedRouteSnapshot, state) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  // Redirect to login if not authenticated
  if (!authService.isLoggedIn()) {
    router.navigate(['/login'], { queryParams: { returnUrl: state.url } });
    return false;
  }

  // Verify token freshness
  const token = authService.getToken();
  if (!token || authService.isTokenExpired(token)) {
    authService.logout();
    router.navigate(['/login'], { queryParams: { returnUrl: state.url, sessionExpired: true } });
    return false;
  }

  // Staff and admins may access any member's data
  if (authService.hasRole('ROLE_STAFF') || authService.hasRole('ROLE_ADMIN')) {
    return true;
  }

  // Check whether the route's memberId matches the current user's memberId
  const routeMemberId = route.params['memberId'];
  const currentUser = authService.getCurrentUser();

  if (routeMemberId !== undefined && currentUser?.memberId !== undefined) {
    const routeId = Number(routeMemberId);
    if (!isNaN(routeId) && routeId === currentUser.memberId) {
      return true;
    }
  }

  // User is neither the owner nor privileged
  router.navigate(['/unauthorized']);
  return false;
};
