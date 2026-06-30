import {
  HttpInterceptorFn,
  HttpRequest,
  HttpHandlerFn,
  HttpResponse,
} from '@angular/common/http';
import { of } from 'rxjs';
import { tap } from 'rxjs/operators';

const TTL_MS = 5 * 60 * 1000; // 5 minutes

interface CacheEntry {
  response: HttpResponse<unknown>;
  timestamp: number;
}

// Module-level cache shared across all uses of this interceptor function
const cache = new Map<string, CacheEntry>();

function buildCacheKey(req: HttpRequest<unknown>): string {
  return `${req.url}::${JSON.stringify(req.params.toString())}`;
}

function isFresh(entry: CacheEntry): boolean {
  return Date.now() - entry.timestamp < TTL_MS;
}

/**
 * Caches successful GET responses for 5 minutes.
 *
 * Cache is bypassed when:
 *  - The request method is not GET
 *  - The request carries a `Cache-Control: no-cache` header
 */
export const cacheInterceptor: HttpInterceptorFn = (
  req: HttpRequest<unknown>,
  next: HttpHandlerFn
) => {
  // Only cache GET requests
  if (req.method !== 'GET') {
    return next(req);
  }

  // Honour explicit cache-busting headers
  if (req.headers.get('Cache-Control') === 'no-cache') {
    return next(req);
  }

  const key = buildCacheKey(req);
  const cached = cache.get(key);

  if (cached && isFresh(cached)) {
    // Return a clone so downstream consumers each receive a pristine response
    return of(cached.response.clone());
  }

  // Cache miss — make the real request and store the result
  return next(req).pipe(
    tap((event) => {
      if (event instanceof HttpResponse && event.status === 200) {
        cache.set(key, { response: event.clone(), timestamp: Date.now() });
      }
    })
  );
};
