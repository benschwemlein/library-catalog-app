import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { BookClubService } from '../../services/book-club.service';
import { BookClub } from '../../models/book-club.model';
import { AuthService } from '../../shared/services/auth.service';

@Component({
  selector: 'app-club-list',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './club-list.component.html'
})
export class ClubListComponent implements OnInit {
  clubs: BookClub[] = [];
  loading = false;
  error = '';
  searchQuery = '';
  selectedBranch: number | null = null;
  joinedClubIds = new Set<number>();
  joinMessages: Record<number, string> = {};

  get memberId(): number { return this.authService.getMemberId(); }

  private coverColors = [
    '#7c3aed', '#0891b2', '#059669', '#d97706', '#dc2626', '#4f46e5', '#db2777'
  ];

  constructor(
    private bookClubService: BookClubService,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    this.loadClubs();
  }

  loadClubs(): void {
    this.loading = true;
    this.error = '';
    this.bookClubService.getClubs(this.selectedBranch ?? undefined).subscribe({
      next: (data) => {
        this.clubs = data;
        this.loading = false;
      },
      error: (err) => {
        this.error = err.message || 'Failed to load book clubs.';
        this.loading = false;
      }
    });
  }

  searchClubs(): void {
    if (!this.searchQuery.trim()) {
      this.loadClubs();
      return;
    }
    this.loading = true;
    this.error = '';
    this.bookClubService.searchClubs(this.searchQuery.trim()).subscribe({
      next: (data) => {
        this.clubs = data;
        this.loading = false;
      },
      error: (err) => {
        this.error = err.message || 'Search failed.';
        this.loading = false;
      }
    });
  }

  joinClub(club: BookClub): void {
    if (this.joinedClubIds.has(club.id) || this.isClubFull(club)) return;
    this.bookClubService.joinClub(club.id, this.memberId).subscribe({
      next: () => {
        this.joinedClubIds.add(club.id);
        club.memberCount += 1;
        this.joinMessages[club.id] = 'You have joined this club!';
        setTimeout(() => delete this.joinMessages[club.id], 3000);
      },
      error: (err) => {
        this.joinMessages[club.id] = err.error?.message || 'Failed to join club.';
        setTimeout(() => delete this.joinMessages[club.id], 3000);
      }
    });
  }

  isClubFull(club: BookClub): boolean {
    return club.memberCount >= club.maxMembers;
  }

  getStatusColor(status: string): string {
    return status === 'ACTIVE' ? '#059669' : status === 'PAUSED' ? '#d97706' : '#6b7280';
  }

  getStatusLabel(status: string): string {
    return status === 'ACTIVE' ? 'Active' : status === 'PAUSED' ? 'Paused' : 'Closed';
  }

  getCoverColor(name: string): string {
    return this.coverColors[name.charCodeAt(0) % this.coverColors.length];
  }

  truncate(text: string, max: number): string {
    return text && text.length > max ? text.slice(0, max) + '...' : text;
  }

  getMemberFillPercent(club: BookClub): number {
    return Math.round((club.memberCount / club.maxMembers) * 100);
  }

  trackById(_index: number, club: BookClub): number {
    return club.id;
  }
}
