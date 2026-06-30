import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Book, BookSummary, BookSearchResult, CreateBookRequest } from '../models/book.model';

@Injectable({ providedIn: 'root' })
export class BookService {
  private apiUrl = 'http://localhost:8080/api/books';

  constructor(private http: HttpClient) {}

  getBooks(): Observable<Book[]> {
    return this.http.get<Book[]>(this.apiUrl);
  }

  getBookById(id: number): Observable<Book> {
    return this.http.get<Book>(`${this.apiUrl}/${id}`);
  }

  getBookByIsbn(isbn: string): Observable<Book> {
    return this.http.get<Book>(`${this.apiUrl}/isbn/${isbn}`);
  }

  searchBooks(query: string, page: number = 0, size: number = 20): Observable<BookSearchResult> {
    const params = new HttpParams().set('q', query).set('page', page).set('size', size);
    return this.http.get<BookSearchResult>('http://localhost:8080/api/search/books', { params });
  }

  getBookCopies(bookId: number): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/${bookId}/copies`);
  }

  getBookReviews(bookId: number): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/${bookId}/reviews`);
  }

  getNewArrivals(days: number = 30): Observable<BookSummary[]> {
    const params = new HttpParams().set('days', days);
    return this.http.get<BookSummary[]>(`${this.apiUrl}/new-arrivals`, { params });
  }

  createBook(book: CreateBookRequest): Observable<Book> {
    return this.http.post<Book>(this.apiUrl, book);
  }

  updateBook(id: number, book: Partial<CreateBookRequest>): Observable<Book> {
    return this.http.put<Book>(`${this.apiUrl}/${id}`, book);
  }

  deleteBook(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
