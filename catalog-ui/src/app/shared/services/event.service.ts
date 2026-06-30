import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { LibraryEvent, EventRegistration, CreateEventRequest } from '../models/event.model';

@Injectable({ providedIn: 'root' })
export class EventService {
  private apiUrl = 'http://localhost:8080/api/events';

  constructor(private http: HttpClient) {}

  getEvents(page: number = 0, size: number = 20): Observable<LibraryEvent[]> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.http.get<LibraryEvent[]>(this.apiUrl, { params });
  }

  getUpcomingEvents(branchId?: number): Observable<LibraryEvent[]> {
    let params = new HttpParams();
    if (branchId) params = params.set('branchId', branchId);
    return this.http.get<LibraryEvent[]>(`${this.apiUrl}/upcoming`, { params });
  }

  getEventById(id: number): Observable<LibraryEvent> {
    return this.http.get<LibraryEvent>(`${this.apiUrl}/${id}`);
  }

  getEventRegistrations(eventId: number): Observable<EventRegistration[]> {
    return this.http.get<EventRegistration[]>(`${this.apiUrl}/${eventId}/registrations`);
  }

  getMemberRegistrations(memberId: number): Observable<EventRegistration[]> {
    return this.http.get<EventRegistration[]>(`${this.apiUrl}/member/${memberId}/registrations`);
  }

  registerForEvent(eventId: number, memberId: number): Observable<EventRegistration> {
    return this.http.post<EventRegistration>(`${this.apiUrl}/${eventId}/register`, { memberId });
  }

  cancelRegistration(eventId: number, memberId: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${eventId}/register/${memberId}`);
  }

  createEvent(event: CreateEventRequest): Observable<LibraryEvent> {
    return this.http.post<LibraryEvent>(this.apiUrl, event);
  }

  updateEvent(id: number, event: Partial<CreateEventRequest>): Observable<LibraryEvent> {
    return this.http.put<LibraryEvent>(`${this.apiUrl}/${id}`, event);
  }

  deleteEvent(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
