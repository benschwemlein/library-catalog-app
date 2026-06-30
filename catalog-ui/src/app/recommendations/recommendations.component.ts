import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RecommendationService } from '../services/recommendation.service';
import { RecommendationDTO } from '../models/recommendation.model';
import { AuthService } from '../shared/services/auth.service';

@Component({
  selector: 'app-recommendations',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './recommendations.component.html'
})
export class RecommendationsComponent implements OnInit {
  recommendations: RecommendationDTO[] = [];
  loading = false;
  error = '';
  limit = 10;

  get memberId(): number { return this.authService.getMemberId(); }

  private coverColors = [
    '#4f46e5', '#0891b2', '#059669', '#d97706',
    '#dc2626', '#7c3aed', '#db2777', '#0284c7'
  ];

  constructor(
    private recommendationService: RecommendationService,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    this.loadRecommendations();
  }

  loadRecommendations(): void {
    this.loading = true;
    this.error = '';
    this.recommendationService.getMemberRecommendations(this.memberId, this.limit).subscribe({
      next: (data) => {
        this.recommendations = data;
        this.loading = false;
      },
      error: (err) => {
        this.error = err.message || 'Failed to load recommendations.';
        this.loading = false;
      }
    });
  }

  getReasonIcon(reason: string): string {
    const r = reason.toLowerCase();
    if (r.includes('similar')) return '👥';
    if (r.includes('genre')) return '📚';
    if (r.includes('author')) return '✍️';
    return '⭐';
  }

  getStars(score: number): string {
    const filled = Math.round(score * 5);
    return '★'.repeat(filled) + '☆'.repeat(5 - filled);
  }

  getCoverColor(title: string): string {
    const idx = title.charCodeAt(0) % this.coverColors.length;
    return this.coverColors[idx];
  }

  getCoverInitial(title: string): string {
    return title.charAt(0).toUpperCase();
  }

  trackById(_index: number, item: RecommendationDTO): number {
    return item.bookId;
  }
}
