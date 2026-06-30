import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ReadingList, ReadingListItem, ListVisibility, ItemPriority } from '../models/reading-list.model';
import { Page } from '../models/digital-resource.model';

const BASE = 'http://localhost:8080/api/library';

@Injectable({ providedIn: 'root' })
export class ReadingListService {
  private http = inject(HttpClient);

  getPublicLists(page = 0, size = 20): Observable<Page<ReadingList>> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.http.get<Page<ReadingList>>(`${BASE}/reading-lists/public`, { params });
  }

  getMemberLists(memberId: number, requestingMemberId: number): Observable<ReadingList[]> {
    const params = new HttpParams().set('requestingMemberId', requestingMemberId);
    return this.http.get<ReadingList[]>(`${BASE}/reading-lists/member/${memberId}`, { params });
  }

  getList(listId: number, requestingMemberId: number): Observable<ReadingListItem[]> {
    const params = new HttpParams().set('requestingMemberId', requestingMemberId);
    return this.http.get<ReadingListItem[]>(`${BASE}/reading-lists/${listId}/items`, { params });
  }

  createList(memberId: number, name: string, description: string, visibility: ListVisibility): Observable<ReadingList> {
    return this.http.post<ReadingList>(`${BASE}/reading-lists`, { memberId, name, description, visibility });
  }

  updateList(listId: number, name: string, description: string, visibility: ListVisibility, requestingMemberId: number): Observable<ReadingList> {
    const params = new HttpParams().set('requestingMemberId', requestingMemberId);
    return this.http.put<ReadingList>(`${BASE}/reading-lists/${listId}`, { name, description, visibility }, { params });
  }

  deleteList(listId: number, requestingMemberId: number): Observable<void> {
    const params = new HttpParams().set('requestingMemberId', requestingMemberId);
    return this.http.delete<void>(`${BASE}/reading-lists/${listId}`, { params });
  }

  addBook(listId: number, bookId: number, priority: ItemPriority, notes: string, requestingMemberId: number): Observable<ReadingListItem> {
    const params = new HttpParams().set('requestingMemberId', requestingMemberId);
    return this.http.post<ReadingListItem>(`${BASE}/reading-lists/${listId}/items`, { bookId, priority, notes }, { params });
  }

  removeBook(listId: number, bookId: number, requestingMemberId: number): Observable<void> {
    const params = new HttpParams().set('requestingMemberId', requestingMemberId);
    return this.http.delete<void>(`${BASE}/reading-lists/${listId}/items/${bookId}`, { params });
  }

  markRead(listId: number, bookId: number, requestingMemberId: number): Observable<ReadingListItem> {
    const params = new HttpParams().set('requestingMemberId', requestingMemberId);
    return this.http.put<ReadingListItem>(`${BASE}/reading-lists/${listId}/items/${bookId}/read`, {}, { params });
  }

  copyList(listId: number, targetMemberId: number, newName: string): Observable<ReadingList> {
    return this.http.post<ReadingList>(`${BASE}/reading-lists/${listId}/copy`, { targetMemberId, newName });
  }
}
