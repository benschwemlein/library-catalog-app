import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';
import {
  AuthenticationRequest,
  AuthenticationResponse,
  RegisterRequest,
} from '../models/authentication.models';

@Injectable({
  providedIn: 'root',
})
export class AuthenticationService {
  private baseUrl = 'http://localhost:8080';
  private authUrl = this.baseUrl + '/api/v1/auth';

  private _userDetails = new BehaviorSubject<{
    firstName: string;
    lastName: string;
  } | null>(null);
  userDetails$ = this._userDetails.asObservable();

  constructor(private http: HttpClient) {}

  authenticate(
    request: AuthenticationRequest
  ): Observable<AuthenticationResponse> {
    return this.http.post<AuthenticationResponse>(
      `${this.authUrl}/authenticate`,
      request
    );
  }

  register(request: RegisterRequest): Observable<AuthenticationResponse> {
    return this.http.post<AuthenticationResponse>(
      `${this.authUrl}/register`,
      request
    );
  }

  updateUserDetails(firstName: string, lastName: string) {
    this._userDetails.next({ firstName, lastName });
  }

  clearUserDetails() {
    this._userDetails.next(null);
  }

  // Other methods can be added here, for example, register, refreshToken, etc.
}
