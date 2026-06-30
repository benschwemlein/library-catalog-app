import { TestBed, fakeAsync, tick } from '@angular/core/testing';
import {
  HttpClient,
  HttpErrorResponse,
  HttpRequest,
  HttpResponse,
  provideHttpClient,
  withInterceptors,
} from '@angular/common/http';
import {
  HttpTestingController,
  provideHttpClientTesting,
} from '@angular/common/http/testing';
import { Router } from '@angular/router';

import { authInterceptor } from './auth.interceptor';
import { cacheInterceptor } from './cache.interceptor';
import { errorInterceptor } from './error.interceptor';
import { loadingInterceptor } from './loading.interceptor';

import { AuthService } from '../shared/services/auth.service';
import { ToastService } from '../shared/services/toast.service';
import { LoadingService } from '../shared/services/loading.service';

// ---------------------------------------------------------------------------
// Helpers
// ---------------------------------------------------------------------------

const TEST_URL = '/api/test';

function setup(interceptors: Parameters<typeof withInterceptors>[0]) {
  TestBed.configureTestingModule({
    providers: [
      provideHttpClient(withInterceptors(interceptors)),
      provideHttpClientTesting(),
    ],
  });
  return {
    http: TestBed.inject(HttpClient),
    controller: TestBed.inject(HttpTestingController),
  };
}

// ---------------------------------------------------------------------------
// authInterceptor
// ---------------------------------------------------------------------------

describe('authInterceptor', () => {
  let http: HttpClient;
  let controller: HttpTestingController;
  let mockAuthService: jasmine.SpyObj<AuthService>;
  let mockRouter: jasmine.SpyObj<Router>;

  beforeEach(() => {
    mockAuthService = jasmine.createSpyObj<AuthService>('AuthService', [
      'getToken',
      'isTokenExpired',
      'logout',
    ]);
    mockRouter = jasmine.createSpyObj<Router>('Router', ['navigate']);

    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptors([authInterceptor])),
        provideHttpClientTesting(),
        { provide: AuthService, useValue: mockAuthService },
        { provide: Router, useValue: mockRouter },
      ],
    });

    http = TestBed.inject(HttpClient);
    controller = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    controller.verify();
  });

  it('attaches Authorization header when token is present and not expired', () => {
    mockAuthService.getToken.and.returnValue('valid-token');
    mockAuthService.isTokenExpired.and.returnValue(false);

    http.get(TEST_URL).subscribe();

    const req = controller.expectOne(TEST_URL);
    expect(req.request.headers.get('Authorization')).toBe('Bearer valid-token');
    req.flush({});
  });

  it('does not attach Authorization header when there is no token', () => {
    mockAuthService.getToken.and.returnValue(null);

    http.get(TEST_URL).subscribe();

    const req = controller.expectOne(TEST_URL);
    expect(req.request.headers.has('Authorization')).toBeFalse();
    req.flush({});
  });

  it('does not attach Authorization header when token is expired', () => {
    mockAuthService.getToken.and.returnValue('expired-token');
    mockAuthService.isTokenExpired.and.returnValue(true);

    http.get(TEST_URL).subscribe();

    const req = controller.expectOne(TEST_URL);
    expect(req.request.headers.has('Authorization')).toBeFalse();
    req.flush({});
  });

  it('calls authService.logout() and navigates to /login?sessionExpired=true on 401', () => {
    mockAuthService.getToken.and.returnValue(null);

    http.get(TEST_URL).subscribe({ error: () => {} });

    const req = controller.expectOne(TEST_URL);
    req.flush('Unauthorized', { status: 401, statusText: 'Unauthorized' });

    expect(mockAuthService.logout).toHaveBeenCalledTimes(1);
    expect(mockRouter.navigate).toHaveBeenCalledWith(['/login'], {
      queryParams: { sessionExpired: true },
    });
  });

  it('does not call logout or navigate for non-401 errors', () => {
    mockAuthService.getToken.and.returnValue(null);

    http.get(TEST_URL).subscribe({ error: () => {} });

    const req = controller.expectOne(TEST_URL);
    req.flush('Forbidden', { status: 403, statusText: 'Forbidden' });

    expect(mockAuthService.logout).not.toHaveBeenCalled();
    expect(mockRouter.navigate).not.toHaveBeenCalled();
  });

  it('still propagates the error after handling 401', () => {
    mockAuthService.getToken.and.returnValue(null);

    let capturedError: HttpErrorResponse | undefined;
    http.get(TEST_URL).subscribe({ error: (err) => (capturedError = err) });

    const req = controller.expectOne(TEST_URL);
    req.flush('Unauthorized', { status: 401, statusText: 'Unauthorized' });

    expect(capturedError).toBeInstanceOf(HttpErrorResponse);
    expect(capturedError!.status).toBe(401);
  });

  it('passes the request through unchanged when there is no token', () => {
    mockAuthService.getToken.and.returnValue(null);

    let result: unknown;
    http.get(TEST_URL).subscribe((res) => (result = res));

    const req = controller.expectOne(TEST_URL);
    expect(req.request.url).toBe(TEST_URL);
    req.flush({ data: 'ok' });

    expect(result).toEqual({ data: 'ok' });
  });
});

// ---------------------------------------------------------------------------
// cacheInterceptor
// ---------------------------------------------------------------------------

describe('cacheInterceptor', () => {
  let http: HttpClient;
  let controller: HttpTestingController;

  // The module-level cache Map is shared across tests. We flush the controller
  // in afterEach to prevent pending-request leaks, but we also use
  // Cache-Control: no-cache in isolation tests so the persistent cache does
  // not bleed into unrelated assertions.

  beforeEach(() => {
    ({ http, controller } = setup([cacheInterceptor]));
  });

  afterEach(() => {
    controller.verify();
  });

  it('passes non-GET requests through without caching', () => {
    http.post(TEST_URL, { body: true }).subscribe();

    const req = controller.expectOne(TEST_URL);
    expect(req.request.method).toBe('POST');
    req.flush({ ok: true });
  });

  it('caches a GET response and returns the cached value on the second call', () => {
    const uniqueUrl = '/api/cache-test-hit-' + Date.now();
    const responseBody = { cached: true };

    // First call — real HTTP request
    let firstResult: unknown;
    http.get(uniqueUrl).subscribe((res) => (firstResult = res));

    const firstReq = controller.expectOne(uniqueUrl);
    firstReq.flush(responseBody, { status: 200, statusText: 'OK' });
    expect(firstResult).toEqual(responseBody);

    // Second call — should be served from cache; no network request expected
    let secondResult: unknown;
    http.get(uniqueUrl).subscribe((res) => (secondResult = res));

    // HttpTestingController.expectNone() asserts no request was made
    controller.expectNone(uniqueUrl);
    expect(secondResult).toEqual(responseBody);
  });

  it('bypasses cache when Cache-Control: no-cache header is present', () => {
    const uniqueUrl = '/api/cache-test-nocache-' + Date.now();

    // Prime the cache for this URL
    http.get(uniqueUrl).subscribe();
    const primeReq = controller.expectOne(uniqueUrl);
    primeReq.flush({ primed: true }, { status: 200, statusText: 'OK' });

    // Second call with no-cache should hit the network again
    http
      .get(uniqueUrl, { headers: { 'Cache-Control': 'no-cache' } })
      .subscribe();
    const bypassReq = controller.expectOne(uniqueUrl);
    expect(bypassReq.request.headers.get('Cache-Control')).toBe('no-cache');
    bypassReq.flush({ fresh: true }, { status: 200, statusText: 'OK' });
  });

  it('does not cache non-200 responses', () => {
    const uniqueUrl = '/api/cache-test-non200-' + Date.now();

    // First call returns 201 — should NOT be cached
    http.get(uniqueUrl).subscribe();
    const firstReq = controller.expectOne(uniqueUrl);
    firstReq.flush({ created: true }, { status: 201, statusText: 'Created' });

    // Second call must still hit the network
    http.get(uniqueUrl).subscribe();
    const secondReq = controller.expectOne(uniqueUrl);
    secondReq.flush({ data: 'second' }, { status: 200, statusText: 'OK' });
  });

  it('returns a clone of the cached response on cache hit', () => {
    const uniqueUrl = '/api/cache-test-clone-' + Date.now();
    const body = { cloneMe: true };

    // Prime
    http.get(uniqueUrl).subscribe();
    controller.expectOne(uniqueUrl).flush(body, { status: 200, statusText: 'OK' });

    // Cache hit — get two results back-to-back
    const results: unknown[] = [];
    http.get(uniqueUrl).subscribe((r) => results.push(r));
    http.get(uniqueUrl).subscribe((r) => results.push(r));

    controller.expectNone(uniqueUrl);
    expect(results.length).toBe(2);
    // Both should equal the original body but be independent references
    expect(results[0]).toEqual(body);
    expect(results[1]).toEqual(body);
  });
});

// ---------------------------------------------------------------------------
// errorInterceptor
// ---------------------------------------------------------------------------

describe('errorInterceptor', () => {
  let http: HttpClient;
  let controller: HttpTestingController;
  let mockToastService: jasmine.SpyObj<ToastService>;

  beforeEach(() => {
    mockToastService = jasmine.createSpyObj<ToastService>('ToastService', [
      'error',
      'warning',
      'success',
      'info',
    ]);

    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptors([errorInterceptor])),
        provideHttpClientTesting(),
        { provide: ToastService, useValue: mockToastService },
      ],
    });

    http = TestBed.inject(HttpClient);
    controller = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    controller.verify();
  });

  it('calls toastService.error() with title "Server Error" on 500', () => {
    http.get(TEST_URL).subscribe({ error: () => {} });

    controller
      .expectOne(TEST_URL)
      .flush('Internal Server Error', { status: 500, statusText: 'Internal Server Error' });

    expect(mockToastService.error).toHaveBeenCalledWith(
      jasmine.any(String),
      'Server Error'
    );
    expect(mockToastService.warning).not.toHaveBeenCalled();
  });

  it('calls toastService.warning() with title "Request Error" on 400', () => {
    http.get(TEST_URL).subscribe({ error: () => {} });

    controller
      .expectOne(TEST_URL)
      .flush('Bad Request', { status: 400, statusText: 'Bad Request' });

    expect(mockToastService.warning).toHaveBeenCalledWith(
      jasmine.any(String),
      'Request Error'
    );
    expect(mockToastService.error).not.toHaveBeenCalled();
  });

  it('calls toastService.warning() with title "Request Error" on 404', () => {
    http.get(TEST_URL).subscribe({ error: () => {} });

    controller
      .expectOne(TEST_URL)
      .flush('Not Found', { status: 404, statusText: 'Not Found' });

    expect(mockToastService.warning).toHaveBeenCalledWith(
      jasmine.any(String),
      'Request Error'
    );
  });

  it('prefers error.error.message from response body over generic message', () => {
    http.get(TEST_URL).subscribe({ error: () => {} });

    controller.expectOne(TEST_URL).flush(
      { message: 'Custom body message' },
      { status: 500, statusText: 'Internal Server Error' }
    );

    expect(mockToastService.error).toHaveBeenCalledWith(
      'Custom body message',
      'Server Error'
    );
  });

  it('falls back to generic message when response body has no message property', fakeAsync(() => {
    http.get(TEST_URL).subscribe({ error: () => {} });

    controller.expectOne(TEST_URL).flush({}, { status: 503, statusText: 'Service Unavailable' });

    tick(1000);
    controller.expectOne(TEST_URL).flush({}, { status: 503, statusText: 'Service Unavailable' });

    tick(2000);
    controller.expectOne(TEST_URL).flush({}, { status: 503, statusText: 'Service Unavailable' });

    expect(mockToastService.error).toHaveBeenCalledWith(
      'Service temporarily unavailable',
      'Server Error'
    );
  }));

  it('retries 503 up to 2 times before showing the toast', fakeAsync(() => {
    http.get(TEST_URL).subscribe({ error: () => {} });

    // Initial attempt
    controller
      .expectOne(TEST_URL)
      .flush('Unavailable', { status: 503, statusText: 'Service Unavailable' });

    // First retry after 1000ms delay
    tick(1000);
    controller
      .expectOne(TEST_URL)
      .flush('Unavailable', { status: 503, statusText: 'Service Unavailable' });

    // Second retry after 2000ms delay
    tick(2000);
    controller
      .expectOne(TEST_URL)
      .flush('Unavailable', { status: 503, statusText: 'Service Unavailable' });

    // After all retries exhausted the toast should appear
    expect(mockToastService.error).toHaveBeenCalledTimes(1);
    expect(mockToastService.error).toHaveBeenCalledWith(
      jasmine.any(String),
      'Server Error'
    );
  }));

  it('does not retry on non-503 5xx errors', () => {
    http.get(TEST_URL).subscribe({ error: () => {} });

    // Only one request should ever be made for a 500
    controller
      .expectOne(TEST_URL)
      .flush('Error', { status: 500, statusText: 'Internal Server Error' });

    expect(mockToastService.error).toHaveBeenCalledTimes(1);
  });

  it('does not retry on 4xx errors', () => {
    http.get(TEST_URL).subscribe({ error: () => {} });

    controller
      .expectOne(TEST_URL)
      .flush('Forbidden', { status: 403, statusText: 'Forbidden' });

    expect(mockToastService.warning).toHaveBeenCalledTimes(1);
  });

  it('propagates the error to the subscriber after showing the toast', () => {
    let capturedError: HttpErrorResponse | undefined;
    http.get(TEST_URL).subscribe({ error: (err) => (capturedError = err) });

    controller
      .expectOne(TEST_URL)
      .flush('Server error', { status: 500, statusText: 'Internal Server Error' });

    expect(capturedError).toBeInstanceOf(HttpErrorResponse);
    expect(capturedError!.status).toBe(500);
  });

  it('uses error.error.message for 4xx toast message', () => {
    http.get(TEST_URL).subscribe({ error: () => {} });

    controller.expectOne(TEST_URL).flush(
      { message: 'Email already in use' },
      { status: 409, statusText: 'Conflict' }
    );

    expect(mockToastService.warning).toHaveBeenCalledWith(
      'Email already in use',
      'Request Error'
    );
  });
});

// ---------------------------------------------------------------------------
// loadingInterceptor
// ---------------------------------------------------------------------------

describe('loadingInterceptor', () => {
  let http: HttpClient;
  let controller: HttpTestingController;
  let mockLoadingService: jasmine.SpyObj<LoadingService>;

  beforeEach(() => {
    mockLoadingService = jasmine.createSpyObj<LoadingService>('LoadingService', [
      'show',
      'hide',
    ]);

    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptors([loadingInterceptor])),
        provideHttpClientTesting(),
        { provide: LoadingService, useValue: mockLoadingService },
      ],
    });

    http = TestBed.inject(HttpClient);
    controller = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    controller.verify();
  });

  it('calls loadingService.show() before the request is dispatched', () => {
    http.get(TEST_URL).subscribe();

    // show() is synchronous and called before the Observable emits
    expect(mockLoadingService.show).toHaveBeenCalledTimes(1);

    controller.expectOne(TEST_URL).flush({});
  });

  it('calls loadingService.hide() after a successful response', () => {
    http.get(TEST_URL).subscribe();

    const req = controller.expectOne(TEST_URL);
    expect(mockLoadingService.hide).not.toHaveBeenCalled();

    req.flush({ ok: true });

    expect(mockLoadingService.hide).toHaveBeenCalledTimes(1);
  });

  it('calls loadingService.hide() even when the request errors (finalize)', () => {
    http.get(TEST_URL).subscribe({ error: () => {} });

    controller
      .expectOne(TEST_URL)
      .flush('Server error', { status: 500, statusText: 'Internal Server Error' });

    expect(mockLoadingService.hide).toHaveBeenCalledTimes(1);
  });

  it('calls show() and hide() once per request for concurrent requests', () => {
    http.get('/api/first').subscribe();
    http.get('/api/second').subscribe();

    expect(mockLoadingService.show).toHaveBeenCalledTimes(2);
    expect(mockLoadingService.hide).toHaveBeenCalledTimes(0);

    controller.expectOne('/api/first').flush({});
    expect(mockLoadingService.hide).toHaveBeenCalledTimes(1);

    controller.expectOne('/api/second').flush({});
    expect(mockLoadingService.hide).toHaveBeenCalledTimes(2);
  });

  it('calls show() then hide() in order for a successful request', () => {
    const callOrder: string[] = [];
    mockLoadingService.show.and.callFake(() => callOrder.push('show'));
    mockLoadingService.hide.and.callFake(() => callOrder.push('hide'));

    http.get(TEST_URL).subscribe();
    controller.expectOne(TEST_URL).flush({});

    expect(callOrder).toEqual(['show', 'hide']);
  });
});
