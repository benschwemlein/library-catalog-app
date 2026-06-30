import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../shared/services/auth.service';

export const staffGuard: CanActivateFn = (route, state) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  // First check if the user is logged in at all
  if (!authService.isLoggedIn()) {
    router.navigate(['/login'], { queryParams: { returnUrl: state.url } });
    return false;
  }

  // Check if the token is still valid
  const token = authService.getToken();
  if (!token || authService.isTokenExpired(token)) {
    authService.logout();
    router.navigate(['/login'], { queryParams: { returnUrl: state.url, sessionExpired: true } });
    return false;
  }

  // Check for staff or admin role — admins can do everything staff can
  if (authService.hasRole('ROLE_STAFF') || authService.hasRole('ROLE_ADMIN')) {
    return true;
  }

  // User is logged in but does not have the required role
  router.navigate(['/unauthorized']);
  return false;
};
