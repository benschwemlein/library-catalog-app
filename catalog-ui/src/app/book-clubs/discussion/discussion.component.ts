import { Component, Input, Output, EventEmitter, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { BookClubService } from '../../services/book-club.service';
import { DiscussionPost } from '../../models/book-club.model';

@Component({
  selector: 'app-discussion',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './discussion.component.html'
})
export class DiscussionComponent implements OnInit {
  @Input() post!: DiscussionPost;
  @Input() clubId!: number;
  @Input() memberId!: number;
  @Output() replied = new EventEmitter<{ parentId: number; reply: DiscussionPost }>();

  replies: DiscussionPost[] = [];
  loadingReplies = false;
  repliesVisible = false;
  repliesError = '';

  replyFormVisible = false;
  replyContent = '';
  postingReply = false;
  replyError = '';

  constructor(private bookClubService: BookClubService) {}

  ngOnInit(): void {
    // Auto-load replies if there are some
    if (this.post.replyCount && this.post.replyCount > 0) {
      this.loadReplies();
    }
  }

  toggleReplies(): void {
    if (this.repliesVisible) {
      this.repliesVisible = false;
      return;
    }
    if (this.replies.length > 0) {
      this.repliesVisible = true;
      return;
    }
    this.loadReplies();
  }

  loadReplies(): void {
    this.loadingReplies = true;
    this.repliesError = '';
    this.bookClubService.getReplies(this.post.id).subscribe({
      next: (replies) => {
        this.replies = replies;
        this.repliesVisible = true;
        this.loadingReplies = false;
      },
      error: (err) => {
        this.repliesError = err.error?.message || 'Failed to load replies.';
        this.loadingReplies = false;
      }
    });
  }

  toggleReplyForm(): void {
    this.replyFormVisible = !this.replyFormVisible;
    if (!this.replyFormVisible) {
      this.replyContent = '';
      this.replyError = '';
    }
  }

  submitReply(): void {
    if (!this.replyContent.trim() || this.postingReply) return;
    this.postingReply = true;
    this.replyError = '';
    this.bookClubService.postDiscussion(this.clubId, this.memberId, this.replyContent.trim(), this.post.id).subscribe({
      next: (reply) => {
        this.replies = [...this.replies, reply];
        this.repliesVisible = true;
        this.replyContent = '';
        this.replyFormVisible = false;
        this.postingReply = false;
        this.post.replyCount = (this.post.replyCount ?? 0) + 1;
        this.replied.emit({ parentId: this.post.id, reply });
      },
      error: (err) => {
        this.replyError = err.error?.message || 'Failed to post reply.';
        this.postingReply = false;
      }
    });
  }

  formatTimestamp(ts: string): string {
    const date = new Date(ts);
    const now = new Date();
    const diffMs = now.getTime() - date.getTime();
    const diffMins = Math.floor(diffMs / 60000);
    if (diffMins < 1) return 'just now';
    if (diffMins < 60) return `${diffMins}m ago`;
    const diffHours = Math.floor(diffMins / 60);
    if (diffHours < 24) return `${diffHours}h ago`;
    const diffDays = Math.floor(diffHours / 24);
    if (diffDays < 7) return `${diffDays}d ago`;
    return date.toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' });
  }

  getInitials(name: string): string {
    return name.split(' ').map(n => n[0]).slice(0, 2).join('').toUpperCase();
  }

  getAvatarColor(name: string): string {
    const colors = ['#7c3aed', '#0891b2', '#059669', '#d97706', '#dc2626', '#4f46e5', '#db2777'];
    return colors[name.charCodeAt(0) % colors.length];
  }

  trackById(_index: number, item: { id: number }): number {
    return item.id;
  }
}
