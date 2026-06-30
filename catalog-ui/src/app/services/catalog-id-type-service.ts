// catalog-id-type.service.ts
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { CatalogIdType } from '../models/catalog-item.models';

@Injectable({
  providedIn: 'root',
})
export class CatalogIdTypeService {
  private catalogIdTypesUrl = 'http://localhost:8080/api/catalog/catalog-id-types';

  private getHttpOptions() {
    const jwtToken = sessionStorage.getItem('jwtToken'); // Retrieve the JWT token from sessionStorage
    const httpOptions = {
      headers: new HttpHeaders({
        Authorization: `Bearer ${jwtToken}`, // Add the JWT token to the Authorization header
      }),
    };
    return httpOptions;
  }

  constructor(private http: HttpClient) {}

  getAllCatalogIdTypes(): Observable<CatalogIdType[]> {
    return this.http.get<CatalogIdType[]>(
      this.catalogIdTypesUrl,
      this.getHttpOptions()
    );
  }

  // ... other service methods
}
