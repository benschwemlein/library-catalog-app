import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Member, MemberSummary, MemberProfile, UpdateMemberRequest } from '../models/member.model';
import { Loan } from '../models/loan.model';
import { Hold } from '../models/hold.model';
import { Fine } from '../models/fine.model';
import { Notification } from '../models/notification.model';

@Injectable({ providedIn: 'root' })
export class MemberService {
  private apiUrl = 'http://localhost:8080/api/members';

  constructor(private http: HttpClient) {}

  getMembers(page: number = 0, size: number = 20): Observable<MemberSummary[]> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.http.get<MemberSummary[]>(this.apiUrl, { params });
  }

  getMe(): Observable<Member> {
    return this.http.get<Member>(`${this.apiUrl}/me`);
  }

  getMemberById(id: number): Observable<Member> {
    return this.http.get<Member>(`${this.apiUrl}/${id}`);
  }

  getMemberByMembershipNumber(membershipNumber: string): Observable<Member> {
    return this.http.get<Member>(`${this.apiUrl}/membership/${membershipNumber}`);
  }

  getMemberProfile(id: number): Observable<MemberProfile> {
    return this.http.get<MemberProfile>(`${this.apiUrl}/${id}/profile`);
  }

  searchMembers(query: string, page: number = 0, size: number = 20): Observable<MemberSummary[]> {
    const params = new HttpParams().set('q', query).set('page', page).set('size', size);
    return this.http.get<MemberSummary[]>(`${this.apiUrl}/search`, { params });
  }

  updateProfile(id: number, profile: UpdateMemberRequest): Observable<MemberProfile> {
    return this.http.put<MemberProfile>(`${this.apiUrl}/${id}/profile`, profile);
  }

  suspendMember(id: number, reason: string): Observable<Member> {
    return this.http.post<Member>(`${this.apiUrl}/${id}/suspend`, { reason });
  }

  reactivateMember(id: number): Observable<Member> {
    return this.http.post<Member>(`${this.apiUrl}/${id}/reactivate`, {});
  }

  getMemberLoans(memberId: number, status?: string): Observable<Loan[]> {
    let params = new HttpParams();
    if (status) params = params.set('status', status);
    return this.http.get<Loan[]>(`${this.apiUrl}/${memberId}/loans`, { params });
  }

  getMemberHolds(memberId: number): Observable<Hold[]> {
    return this.http.get<Hold[]>(`${this.apiUrl}/${memberId}/holds`);
  }

  getMemberFines(memberId: number, unpaidOnly: boolean = false): Observable<Fine[]> {
    const params = new HttpParams().set('unpaidOnly', unpaidOnly);
    return this.http.get<Fine[]>(`${this.apiUrl}/${memberId}/fines`, { params });
  }

  getMemberNotifications(memberId: number): Observable<Notification[]> {
    return this.http.get<Notification[]>(`${this.apiUrl}/${memberId}/notifications`);
  }

  updateMemberTier(memberId: number, tier: string): Observable<Member> {
    return this.http.put<Member>(`${this.apiUrl}/${memberId}/tier`, { tier });
  }
}
