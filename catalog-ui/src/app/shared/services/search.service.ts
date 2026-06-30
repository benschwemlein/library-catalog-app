import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { SearchRequest, AdvancedSearchRequest, SearchResult } from '../models/search.model';

@Injectable({ providedIn: 'root' })
export class SearchService {
  private apiUrl = 'http://localhost:8080/api/search';

  constructor(private http: HttpClient) {}

  searchBooks(request: SearchRequest): Observable<SearchResult> {
    let params = new HttpParams().set('q', request.query);
    if (request.page !== undefined) params = params.set('page', request.page);
    if (request.size !== undefined) params = params.set('size', request.size);
    if (request.sortBy) params = params.set('sortBy', request.sortBy);
    if (request.sortDirection) params = params.set('sortDirection', request.sortDirection);
    return this.http.get<SearchResult>(`${this.apiUrl}/books`, { params });
  }

  advancedSearch(request: AdvancedSearchRequest): Observable<SearchResult> {
    return this.http.post<SearchResult>(`${this.apiUrl}/advanced`, request);
  }

  getGenres(): Observable<string[]> {
    return this.http.get<string[]>(`${this.apiUrl}/genres`);
  }

  getLanguages(): Observable<string[]> {
    return this.http.get<string[]>(`${this.apiUrl}/languages`);
  }

  getSubjects(): Observable<string[]> {
    return this.http.get<string[]>(`${this.apiUrl}/subjects`);
  }
}
