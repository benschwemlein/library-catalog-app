import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import {
  CheckInOutRequestDTO,
  CheckoutResponseDTO,
} from '../models/checkout.models';

@Injectable({
  providedIn: 'root',
})
export class CheckoutService {
  private baseUrl = 'http://localhost:8080/api';
  private checkoutUrl = this.baseUrl + '/catalog/borrow';

  httpOptions = {
    headers: new HttpHeaders({ 'Content-Type': 'application/json' }),
  };

  constructor(private http: HttpClient) {}

  private getHttpOptions() {
    const jwtToken = sessionStorage.getItem('jwtToken'); // Retrieve the JWT token from sessionStorage
    const httpOptions = {
      headers: new HttpHeaders({
        Authorization: `Bearer ${jwtToken}`, // Add the JWT token to the Authorization header
      }),
    };
    return httpOptions;
  }

  // Check in a catalog item
  checkin(
    checkInOutRequestDTO: CheckInOutRequestDTO
  ): Observable<CheckoutResponseDTO> {
    return this.http.put<CheckoutResponseDTO>(
      `${this.checkoutUrl}/checkin`,
      checkInOutRequestDTO,
      this.getHttpOptions()
    );
  }

  // Check out a catalog item
  checkout(
    checkInOutRequestDTO: CheckInOutRequestDTO
  ): Observable<CheckoutResponseDTO> {
    return this.http.put<CheckoutResponseDTO>(
      `${this.checkoutUrl}/checkout`,
      checkInOutRequestDTO,
      this.getHttpOptions()
    );
  }
}
