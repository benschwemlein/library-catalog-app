import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import {
  CatalogItemRequestDTO,
  CatalogItemResponseDTO,
} from '../models/catalog-item.models';

@Injectable({
  providedIn: 'root',
})
export class CatalogItemService {
  private baseUrl = 'http://localhost:8080/api';
  private catalogUrl = this.baseUrl + '/catalog/catalog-items';

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

  createCatalogItem(
    catalogItem: CatalogItemRequestDTO
  ): Observable<CatalogItemResponseDTO> {
    return this.http.post<CatalogItemResponseDTO>(
      this.catalogUrl,
      catalogItem,
      this.getHttpOptions() // Use the HTTP options with the headers
    );
  }

  getAllCatalogItems(): Observable<CatalogItemResponseDTO[]> {
    return this.http.get<CatalogItemResponseDTO[]>(
      this.catalogUrl,
      this.getHttpOptions() // Use the HTTP options with the headers
    );
  }

  getCatalogItemById(id: number): Observable<CatalogItemRequestDTO> {
    return this.http.get<CatalogItemRequestDTO>(
      `${this.catalogUrl}/${id}`,
      this.getHttpOptions() // Use the HTTP options with the headers
    );
  }

  updateCatalogItem(
    id: number,
    catalogItem: CatalogItemRequestDTO
  ): Observable<CatalogItemResponseDTO> {
    return this.http.put<CatalogItemResponseDTO>(
      `${this.catalogUrl}/${id}`,
      catalogItem,
      this.getHttpOptions() // Use the HTTP options with the headers
    );
  }

  deleteCatalogItem(id: number): Observable<void> {
    return this.http.delete<void>(
      `${this.catalogUrl}/${id}`,
      this.getHttpOptions() // Use the HTTP options with the headers
    );
  }
}
