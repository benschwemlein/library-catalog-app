import {
  HttpInterceptorFn,
  HttpRequest,
  HttpHandlerFn,
} from '@angular/common/http';
import { inject } from '@angular/core';
import { finalize } from 'rxjs/operators';
import { LoadingService } from '../shared/services/loading.service';

/**
 * Tracks in-flight HTTP requests and drives the global loading indicator.
 *
 * LoadingService maintains an internal counter so concurrent requests all
 * contribute: the spinner appears on the first request and disappears only
 * when the last one completes (or errors).
 */
export const loadingInterceptor: HttpInterceptorFn = (
  req: HttpRequest<unknown>,
  next: HttpHandlerFn
) => {
  const loadingService = inject(LoadingService);

  loadingService.show();

  return next(req).pipe(
    finalize(() => {
      // Always decrement — whether the request succeeded, errored, or was cancelled
      loadingService.hide();
    })
  );
};
