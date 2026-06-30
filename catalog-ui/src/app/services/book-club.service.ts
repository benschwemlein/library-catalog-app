import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { BookClub, BookClubMeeting, DiscussionPost } from '../models/book-club.model';

const BASE = 'http://localhost:8080/api/library';

@Injectable({ providedIn: 'root' })
export class BookClubService {
  private http = inject(HttpClient);

  getClubs(branchId?: number): Observable<BookClub[]> {
    let params = new HttpParams();
    if (branchId != null) params = params.set('branchId', branchId);
    return this.http.get<BookClub[]>(`${BASE}/book-clubs`, { params });
  }

  getClub(id: number): Observable<BookClub> {
    return this.http.get<BookClub>(`${BASE}/book-clubs/${id}`);
  }

  searchClubs(query: string): Observable<BookClub[]> {
    const params = new HttpParams().set('query', query);
    return this.http.get<BookClub[]>(`${BASE}/book-clubs/search`, { params });
  }

  joinClub(clubId: number, memberId: number): Observable<void> {
    return this.http.post<void>(`${BASE}/book-clubs/${clubId}/members/${memberId}`, {});
  }

  leaveClub(clubId: number, memberId: number): Observable<void> {
    return this.http.delete<void>(`${BASE}/book-clubs/${clubId}/members/${memberId}`);
  }

  getUpcomingMeetings(clubId: number): Observable<BookClubMeeting[]> {
    return this.http.get<BookClubMeeting[]>(`${BASE}/book-clubs/${clubId}/meetings/upcoming`);
  }

  postDiscussion(clubId: number, memberId: number, content: string, parentId?: number): Observable<DiscussionPost> {
    let params = new HttpParams().set('memberId', memberId).set('content', content);
    if (parentId != null) params = params.set('parentId', parentId);
    return this.http.post<DiscussionPost>(`${BASE}/book-clubs/${clubId}/discussions`, {}, { params });
  }

  getDiscussions(clubId: number, meetingId?: number): Observable<DiscussionPost[]> {
    let params = new HttpParams();
    if (meetingId != null) params = params.set('meetingId', meetingId);
    return this.http.get<DiscussionPost[]>(`${BASE}/book-clubs/${clubId}/discussions`, { params });
  }

  getReplies(discussionId: number): Observable<DiscussionPost[]> {
    return this.http.get<DiscussionPost[]>(`${BASE}/book-clubs/discussions/${discussionId}/replies`);
  }

  setCurrentBook(clubId: number, bookId: number): Observable<BookClub> {
    return this.http.put<BookClub>(`${BASE}/book-clubs/${clubId}/current-book/${bookId}`, {});
  }
}
