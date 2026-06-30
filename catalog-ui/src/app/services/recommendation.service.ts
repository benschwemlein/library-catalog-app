import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { RecommendationDTO } from '../models/recommendation.model';

const BASE = 'http://localhost:8080/api/library';

@Injectable({ providedIn: 'root' })
export class RecommendationService {
  private http = inject(HttpClient);

  getMyRecommendations(limit = 10): Observable<RecommendationDTO[]> {
    const params = new HttpParams().set('limit', limit);
    return this.http.get<RecommendationDTO[]>(`${BASE}/recommendations/me`, { params });
  }

  getMemberRecommendations(memberId: number, limit = 10): Observable<RecommendationDTO[]> {
    const params = new HttpParams().set('limit', limit);
    return this.http.get<RecommendationDTO[]>(`${BASE}/recommendations/member/${memberId}`, { params });
  }

  evictCache(memberId: number): Observable<void> {
    return this.http.delete<void>(`${BASE}/recommendations/cache/member/${memberId}`);
  }
}
