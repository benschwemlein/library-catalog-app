import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { BookClubService } from '../../services/book-club.service';
import { BookClub, BookClubMeeting, DiscussionPost } from '../../models/book-club.model';
import { DiscussionComponent } from '../discussion/discussion.component';
import { AuthService } from '../../shared/services/auth.service';

@Component({
  selector: 'app-club-detail',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule, DiscussionComponent],
  templateUrl: './club-detail.component.html'
})
export class ClubDetailComponent implements OnInit {
  club: BookClub | null = null;
  meetings: BookClubMeeting[] = [];
  discussions: DiscussionPost[] = [];

  loadingClub = false;
  loadingMeetings = false;
  loadingDiscussions = false;
  joiningOrLeaving = false;

  clubError = '';
  meetingsError = '';
  discussionsError = '';
  actionMessage = '';
  actionError = '';

  newPostContent = '';
  postingDiscussion = false;

  get memberId(): number { return this.authService.getMemberId(); }
  readonly memberName = 'Current User';
  isMember = false;

  constructor(
    private route: ActivatedRoute,
    private bookClubService: BookClubService,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    this.route.paramMap.subscribe(params => {
      const id = Number(params.get('id'));
      if (id) {
        this.loadClub(id);
        this.loadMeetings(id);
        this.loadDiscussions(id);
      }
    });
  }

  loadClub(id: number): void {
    this.loadingClub = true;
    this.clubError = '';
    this.bookClubService.getClub(id).subscribe({
      next: (club) => {
        this.club = club;
        this.loadingClub = false;
      },
      error: (err) => {
        this.clubError = err.error?.message || 'Failed to load club details.';
        this.loadingClub = false;
      }
    });
  }

  loadMeetings(clubId: number): void {
    this.loadingMeetings = true;
    this.meetingsError = '';
    this.bookClubService.getUpcomingMeetings(clubId).subscribe({
      next: (meetings) => {
        this.meetings = meetings;
        this.loadingMeetings = false;
      },
      error: (err) => {
        this.meetingsError = err.error?.message || 'Failed to load meetings.';
        this.loadingMeetings = false;
      }
    });
  }

  loadDiscussions(clubId: number): void {
    this.loadingDiscussions = true;
    this.discussionsError = '';
    this.bookClubService.getDiscussions(clubId).subscribe({
      next: (posts) => {
        this.discussions = posts.filter(p => !p.parentDiscussionId);
        this.loadingDiscussions = false;
      },
      error: (err) => {
        this.discussionsError = err.error?.message || 'Failed to load discussions.';
        this.loadingDiscussions = false;
      }
    });
  }

  toggleMembership(): void {
    if (!this.club || this.joiningOrLeaving) return;
    this.joiningOrLeaving = true;
    this.actionMessage = '';
    this.actionError = '';

    const action = this.isMember
      ? this.bookClubService.leaveClub(this.club.id, this.memberId)
      : this.bookClubService.joinClub(this.club.id, this.memberId);

    action.subscribe({
      next: () => {
        if (this.club) {
          if (this.isMember) {
            this.isMember = false;
            this.club.memberCount = Math.max(0, this.club.memberCount - 1);
            this.actionMessage = 'You have left this club.';
          } else {
            this.isMember = true;
            this.club.memberCount += 1;
            this.actionMessage = 'You have joined this club!';
          }
        }
        this.joiningOrLeaving = false;
        setTimeout(() => this.actionMessage = '', 4000);
      },
      error: (err) => {
        this.actionError = err.error?.message || 'Action failed. Please try again.';
        this.joiningOrLeaving = false;
        setTimeout(() => this.actionError = '', 4000);
      }
    });
  }

  submitDiscussion(): void {
    if (!this.club || !this.newPostContent.trim() || this.postingDiscussion) return;
    this.postingDiscussion = true;
    this.bookClubService.postDiscussion(this.club.id, this.memberId, this.newPostContent.trim()).subscribe({
      next: (post) => {
        this.discussions = [post, ...this.discussions];
        this.newPostContent = '';
        this.postingDiscussion = false;
      },
      error: (err) => {
        this.actionError = err.error?.message || 'Failed to post discussion.';
        this.postingDiscussion = false;
        setTimeout(() => this.actionError = '', 4000);
      }
    });
  }

  onReplied(event: { parentId: number; reply: DiscussionPost }): void {
    const parent = this.discussions.find(d => d.id === event.parentId);
    if (parent) {
      parent.replyCount = (parent.replyCount ?? 0) + 1;
    }
  }

  getStatusColor(status: string): string {
    return status === 'ACTIVE' ? '#059669' : status === 'PAUSED' ? '#d97706' : '#6b7280';
  }

  getStatusLabel(status: string): string {
    return status === 'ACTIVE' ? 'Active' : status === 'PAUSED' ? 'Paused' : 'Closed';
  }

  isClubFull(): boolean {
    return !!this.club && this.club.memberCount >= this.club.maxMembers;
  }

  getMemberFillPercent(): number {
    if (!this.club) return 0;
    return Math.round((this.club.memberCount / this.club.maxMembers) * 100);
  }

  formatMeetingDate(dateStr: string): string {
    return new Date(dateStr).toLocaleDateString('en-US', {
      weekday: 'short', month: 'short', day: 'numeric',
      year: 'numeric', hour: 'numeric', minute: '2-digit'
    });
  }

  trackById(_index: number, item: { id: number }): number {
    return item.id;
  }
}
