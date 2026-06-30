import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { ReadingChallengeService } from '../../services/reading-challenge.service';
import { ReadingChallenge, ChallengeParticipation } from '../../models/reading-challenge.model';
import { AuthService } from '../../shared/services/auth.service';

@Component({
  selector: 'app-challenge-list',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './challenge-list.component.html'
})
export class ChallengeListComponent implements OnInit {
  challenges: ReadingChallenge[] = [];
  participations: ChallengeParticipation[] = [];

  loadingChallenges = false;
  loadingParticipations = false;
  challengesError = '';
  participationsError = '';

  enrollingId: number | null = null;
  enrolledIds = new Set<number>();
  enrollMessages: Record<number, { text: string; isError: boolean }> = {};

  get memberId(): number { return this.authService.getMemberId(); }

  constructor(
    private challengeService: ReadingChallengeService,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    this.loadChallenges();
    this.loadParticipations();
  }

  loadChallenges(): void {
    this.loadingChallenges = true;
    this.challengesError = '';
    this.challengeService.getActiveChallenges().subscribe({
      next: (data) => {
        this.challenges = data;
        this.loadingChallenges = false;
      },
      error: (err) => {
        this.challengesError = err.error?.message || 'Failed to load challenges.';
        this.loadingChallenges = false;
      }
    });
  }

  loadParticipations(): void {
    this.loadingParticipations = true;
    this.participationsError = '';
    this.challengeService.getMemberParticipations(this.memberId).subscribe({
      next: (data) => {
        this.participations = data;
        this.enrolledIds = new Set(data.map(p => p.challengeId));
        this.loadingParticipations = false;
      },
      error: (err) => {
        this.participationsError = err.error?.message || 'Failed to load your participations.';
        this.loadingParticipations = false;
      }
    });
  }

  enroll(challenge: ReadingChallenge): void {
    if (this.enrolledIds.has(challenge.id) || this.enrollingId !== null) return;
    this.enrollingId = challenge.id;
    this.challengeService.enroll(challenge.id, this.memberId).subscribe({
      next: (participation) => {
        this.enrolledIds.add(challenge.id);
        this.participations = [...this.participations, participation];
        challenge.enrolledCount += 1;
        this.enrollMessages[challenge.id] = { text: 'You are now enrolled!', isError: false };
        this.enrollingId = null;
        setTimeout(() => delete this.enrollMessages[challenge.id], 4000);
      },
      error: (err) => {
        this.enrollMessages[challenge.id] = {
          text: err.error?.message || 'Enrollment failed.',
          isError: true
        };
        this.enrollingId = null;
        setTimeout(() => delete this.enrollMessages[challenge.id], 4000);
      }
    });
  }

  isEnrolled(id: number): boolean {
    return this.enrolledIds.has(id);
  }

  getParticipation(challengeId: number): ChallengeParticipation | undefined {
    return this.participations.find(p => p.challengeId === challengeId);
  }

  getDaysRemaining(endDate: string): number {
    const end = new Date(endDate).getTime();
    const now = Date.now();
    return Math.max(0, Math.ceil((end - now) / 86400000));
  }

  formatDateRange(start: string, end: string): string {
    const s = new Date(start).toLocaleDateString('en-US', { month: 'short', day: 'numeric' });
    const e = new Date(end).toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' });
    return `${s} – ${e}`;
  }

  getProgressColor(pct: number): string {
    if (pct >= 100) return '#059669';
    if (pct >= 50) return '#7c3aed';
    return '#0891b2';
  }

  trackById(_index: number, item: { id: number }): number {
    return item.id;
  }

  trackByParticipationId(_index: number, item: ChallengeParticipation): number {
    return item.participationId;
  }
}
