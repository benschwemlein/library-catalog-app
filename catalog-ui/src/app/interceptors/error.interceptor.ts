import {
  HttpInterceptorFn,
  HttpRequest,
  HttpHandlerFn,
  HttpErrorResponse,
} from '@angular/common/http';
import { inject } from '@angular/core';
import { throwError, timer } from 'rxjs';
import { catchError, retry } from 'rxjs/operators';
import { ToastService } from '../shared/services/toast.service';

/** Maps HTTP status codes to user-friendly messages. */
const ERROR_MESSAGES: Record<number, string> = {
  400: 'Invalid request',
  401: 'Authentication required',
  403: 'You do not have permission',
  404: 'Not found',
  409: 'Conflict',
  422: 'Validation error',
  500: 'Server error, please try again',
  503: 'Service temporarily unavailable',
};

function friendlyMessage(error: HttpErrorResponse): string {
  // Prefer a message embedded in the response body when available
  if (error.error?.message && typeof error.error.message === 'string') {
    return error.error.message;
  }
  return ERROR_MESSAGES[error.status] ?? `Unexpected error (${error.status})`;
}

/**
 * Intercepts HTTP errors, shows a toast notification, and retries 503
 * responses up to 2 times with a 1-second delay between attempts.
 */
export const errorInterceptor: HttpInterceptorFn = (
  req: HttpRequest<unknown>,
  next: HttpHandlerFn
) => {
  const toastService = inject(ToastService);

  return next(req).pipe(
    // Retry automatically on 503 (service unavailable) — up to 2 extra attempts
    retry({
      count: 2,
      delay: (error: HttpErrorResponse, retryCount: number) => {
        if (error instanceof HttpErrorResponse && error.status === 503) {
          return timer(1000 * retryCount);
        }
        // For any other error, surface it immediately without retrying
        throw error;
      },
    }),
    catchError((error: HttpErrorResponse) => {
      const message = friendlyMessage(error);

      if (error.status >= 500) {
        toastService.error(message, 'Server Error');
      } else if (error.status >= 400) {
        toastService.warning(message, 'Request Error');
      } else {
        toastService.error(message, 'Error');
      }

      return throwError(() => error);
    })
  );
};
