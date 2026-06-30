import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Author, CreateAuthorRequest } from '../models/author.model';

@Injectable({ providedIn: 'root' })
export class AuthorService {
  private apiUrl = 'http://localhost:8080/api/authors';

  constructor(private http: HttpClient) {}

  getAuthors(page: number = 0, size: number = 20): Observable<Author[]> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.http.get<Author[]>(this.apiUrl, { params });
  }

  getAuthorById(id: number): Observable<Author> {
    return this.http.get<Author>(`${this.apiUrl}/${id}`);
  }

  searchAuthors(query: string): Observable<Author[]> {
    const params = new HttpParams().set('q', query);
    return this.http.get<Author[]>(`${this.apiUrl}/search`, { params });
  }

  getAuthorBooks(authorId: number): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/${authorId}/books`);
  }

  createAuthor(author: CreateAuthorRequest): Observable<Author> {
    return this.http.post<Author>(this.apiUrl, author);
  }

  updateAuthor(id: number, author: Partial<CreateAuthorRequest>): Observable<Author> {
    return this.http.put<Author>(`${this.apiUrl}/${id}`, author);
  }

  deleteAuthor(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
