import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { ReadingChallengeService } from '../../services/reading-challenge.service';
import {
  ReadingChallenge,
  ChallengeParticipation,
  LeaderboardEntry,
  LogProgressRequest
} from '../../models/reading-challenge.model';
import { AuthService } from '../../shared/services/auth.service';

@Component({
  selector: 'app-challenge-detail',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './challenge-detail.component.html'
})
export class ChallengeDetailComponent implements OnInit {
  challenge: ReadingChallenge | null = null;
  participation: ChallengeParticipation | null = null;
  leaderboard: LeaderboardEntry[] = [];

  loadingChallenge = false;
  loadingLeaderboard = false;
  loggingBook = false;

  challengeError = '';
  leaderboardError = '';
  logError = '';
  logSuccess = '';

  // Log a book form
  bookTitle = '';
  completedDate = new Date().toISOString().substring(0, 10);
  logNotes = '';

  get memberId(): number { return this.authService.getMemberId(); }

  constructor(
    private route: ActivatedRoute,
    private challengeService: ReadingChallengeService,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    this.route.paramMap.subscribe(params => {
      const id = Number(params.get('id'));
      if (id) {
        this.loadChallenge(id);
        this.loadLeaderboard(id);
        this.loadMyParticipation(id);
      }
    });
  }

  loadChallenge(id: number): void {
    this.loadingChallenge = true;
    this.challengeError = '';
    this.challengeService.getChallenge(id).subscribe({
      next: (data) => {
        this.challenge = data;
        this.loadingChallenge = false;
      },
      error: (err) => {
        this.challengeError = err.error?.message || 'Failed to load challenge.';
        this.loadingChallenge = false;
      }
    });
  }

  loadLeaderboard(id: number): void {
    this.loadingLeaderboard = true;
    this.leaderboardError = '';
    this.challengeService.getLeaderboard(id).subscribe({
      next: (data) => {
        this.leaderboard = data;
        this.loadingLeaderboard = false;
      },
      error: (err) => {
        this.leaderboardError = err.error?.message || 'Failed to load leaderboard.';
        this.loadingLeaderboard = false;
      }
    });
  }

  loadMyParticipation(challengeId: number): void {
    this.challengeService.getMemberParticipations(this.memberId).subscribe({
      next: (participations) => {
        this.participation = participations.find(p => p.challengeId === challengeId) ?? null;
      },
      error: () => {
        // Non-critical — silently ignore
      }
    });
  }

  logBook(): void {
    if (!this.challenge || !this.bookTitle.trim() || this.loggingBook) return;
    this.loggingBook = true;
    this.logError = '';
    this.logSuccess = '';

    const req: LogProgressRequest = {
      memberId: this.memberId,
      bookTitle: this.bookTitle.trim(),
      completedDate: this.completedDate,
      notes: this.logNotes.trim() || undefined
    };

    this.challengeService.logProgress(this.challenge.id, req).subscribe({
      next: (updated) => {
        this.participation = updated;
        this.bookTitle = '';
        this.logNotes = '';
        this.completedDate = new Date().toISOString().substring(0, 10);
        this.loggingBook = false;
        this.logSuccess = `"${req.bookTitle}" logged! You've now completed ${updated.completedBooks} of ${updated.targetBooks} books.`;
        setTimeout(() => this.logSuccess = '', 6000);
        // Refresh leaderboard to reflect new standing
        this.loadLeaderboard(this.challenge!.id);
      },
      error: (err) => {
        this.logError = err.error?.message || 'Failed to log progress.';
        this.loggingBook = false;
        setTimeout(() => this.logError = '', 5000);
      }
    });
  }

  getProgressPercent(): number {
    if (this.participation) return Math.min(100, this.participation.progressPercentage);
    return 0;
  }

  getProgressColor(pct: number): string {
    if (pct >= 100) return '#059669';
    if (pct >= 60) return '#7c3aed';
    return '#0891b2';
  }

  getDaysRemaining(): number {
    if (!this.challenge) return 0;
    return Math.max(0, Math.ceil((new Date(this.challenge.endDate).getTime() - Date.now()) / 86400000));
  }

  formatDate(dateStr: string): string {
    return new Date(dateStr).toLocaleDateString('en-US', {
      month: 'long', day: 'numeric', year: 'numeric'
    });
  }

  getRankLabel(rank: number): string {
    if (rank === 1) return '🥇';
    if (rank === 2) return '🥈';
    if (rank === 3) return '🥉';
    return `#${rank}`;
  }

  isCurrentUser(entry: LeaderboardEntry): boolean {
    return entry.memberId === this.memberId;
  }

  trackByRank(_index: number, entry: LeaderboardEntry): number {
    return entry.rank;
  }
}
