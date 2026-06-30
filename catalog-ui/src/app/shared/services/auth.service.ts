import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { BehaviorSubject, Observable, of } from 'rxjs';
import { tap, switchMap, map, catchError } from 'rxjs/operators';

export interface LoginRequest {
  email: string;
  password: string;
}

export interface AuthResponse {
  access_token: string;
  refresh_token?: string;
  first_name?: string;
  last_name?: string;
  email?: string;
}

export interface UserInfo {
  username: string;
  roles: string[];
  memberId?: number;
  email?: string;
  firstName?: string;
  lastName?: string;
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly TOKEN_KEY = 'auth_token';
  private readonly REFRESH_TOKEN_KEY = 'refresh_token';
  private readonly USER_KEY = 'current_user';

  private currentUserSubject = new BehaviorSubject<UserInfo | null>(null);
  readonly currentUser$ = this.currentUserSubject.asObservable();

  constructor(private http: HttpClient, private router: Router) {
    // Restore session from localStorage on startup
    const stored = localStorage.getItem(this.USER_KEY);
    if (stored) {
      try {
        this.currentUserSubject.next(JSON.parse(stored) as UserInfo);
      } catch {
        localStorage.removeItem(this.USER_KEY);
      }
    }
  }

  login(credentials: LoginRequest): Observable<AuthResponse> {
    return this.http
      .post<AuthResponse>('http://localhost:8080/api/v1/auth/authenticate', credentials)
      .pipe(
        tap((response) => {
          localStorage.setItem(this.TOKEN_KEY, response.access_token);
          if (response.refresh_token) {
            localStorage.setItem(this.REFRESH_TOKEN_KEY, response.refresh_token);
          }
          const user = this.parseJwt(response.access_token);
          localStorage.setItem(this.USER_KEY, JSON.stringify(user));
          this.currentUserSubject.next(user);
        }),
        switchMap((response) =>
          this.http.get<any>('http://localhost:8080/api/members/me', {
            headers: { Authorization: `Bearer ${response.access_token}` }
          }).pipe(
            tap((member) => {
              const user = this.currentUserSubject.value;
              if (user && member?.id) {
                const updated = {
                  ...user,
                  memberId: member.id as number,
                  firstName: member.user?.firstName ?? null,
                  lastName: member.user?.lastName ?? null,
                };
                localStorage.setItem(this.USER_KEY, JSON.stringify(updated));
                this.currentUserSubject.next(updated);
              }
            }),
            map(() => response),
            catchError(() => of(response))
          )
        )
      );
  }

  getMemberId(): number {
    return this.currentUserSubject.value?.memberId ?? 1;
  }

  logout(): void {
    localStorage.removeItem(this.TOKEN_KEY);
    localStorage.removeItem(this.REFRESH_TOKEN_KEY);
    localStorage.removeItem(this.USER_KEY);
    this.currentUserSubject.next(null);
    this.router.navigate(['/login']);
  }

  getToken(): string | null {
    return localStorage.getItem(this.TOKEN_KEY);
  }

  getCurrentUser(): UserInfo | null {
    return this.currentUserSubject.value;
  }

  isLoggedIn(): boolean {
    const token = this.getToken();
    return !!token && !this.isTokenExpired(token);
  }

  hasRole(role: string): boolean {
    const user = this.getCurrentUser();
    return user?.roles?.includes(role) ?? false;
  }

  parseJwt(token: string): UserInfo {
    const base64Url = token.split('.')[1];
    const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
    const payload = JSON.parse(window.atob(base64)) as Record<string, unknown>;
    return {
      username: payload['sub'] as string,
      roles: (payload['roles'] as string[]) ?? [],
      memberId: payload['memberId'] as number | undefined,
      email: payload['email'] as string | undefined,
      firstName: payload['firstName'] as string | undefined,
      lastName: payload['lastName'] as string | undefined,
    };
  }

  isTokenExpired(token: string): boolean {
    try {
      const base64 = token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/');
      const payload = JSON.parse(window.atob(base64)) as { exp?: number };
      if (!payload.exp) return true;
      return payload.exp * 1000 < Date.now();
    } catch {
      return true;
    }
  }

  refreshToken(): Observable<AuthResponse> {
    const refreshToken = localStorage.getItem(this.REFRESH_TOKEN_KEY);
    return this.http
      .post<AuthResponse>('http://localhost:8080/api/v1/auth/refresh-token', { refreshToken })
      .pipe(
        tap((response) => {
          localStorage.setItem(this.TOKEN_KEY, response.access_token);
          const user = this.parseJwt(response.access_token);
          localStorage.setItem(this.USER_KEY, JSON.stringify(user));
          this.currentUserSubject.next(user);
        })
      );
  }
}
