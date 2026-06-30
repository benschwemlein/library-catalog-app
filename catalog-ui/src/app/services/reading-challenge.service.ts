import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ReadingChallenge, ChallengeParticipation, LeaderboardEntry, LogProgressRequest } from '../models/reading-challenge.model';

const BASE = 'http://localhost:8080/api/library';

@Injectable({ providedIn: 'root' })
export class ReadingChallengeService {
  private http = inject(HttpClient);

  getActiveChallenges(): Observable<ReadingChallenge[]> {
    return this.http.get<ReadingChallenge[]>(`${BASE}/challenges/active`);
  }

  getChallenge(id: number): Observable<ReadingChallenge> {
    return this.http.get<ReadingChallenge>(`${BASE}/challenges/${id}`);
  }

  enroll(challengeId: number, memberId: number): Observable<ChallengeParticipation> {
    return this.http.post<ChallengeParticipation>(`${BASE}/challenges/${challengeId}/enroll/${memberId}`, {});
  }

  logProgress(challengeId: number, req: LogProgressRequest): Observable<ChallengeParticipation> {
    return this.http.post<ChallengeParticipation>(`${BASE}/challenges/${challengeId}/progress`, req);
  }

  getLeaderboard(challengeId: number): Observable<LeaderboardEntry[]> {
    return this.http.get<LeaderboardEntry[]>(`${BASE}/challenges/${challengeId}/leaderboard`);
  }

  getMemberParticipations(memberId: number): Observable<ChallengeParticipation[]> {
    return this.http.get<ChallengeParticipation[]>(`${BASE}/challenges/member/${memberId}`);
  }
}
