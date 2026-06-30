import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { Periodical, PeriodicalIssue, PeriodicalFrequency, PeriodicalIssueStatus } from '../../models/periodical.model';

@Component({
  selector: 'app-periodical-list',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './periodical-list.component.html'
})
export class PeriodicalListComponent implements OnInit {
  private http = inject(HttpClient);

  private apiBase = 'http://localhost:8080/api/library/periodicals';

  periodicals: Periodical[] = [];
  selectedPeriodical: Periodical | null = null;
  issues: PeriodicalIssue[] = [];
  categories: string[] = [];
  loading = false;
  error = '';
  searchQuery = '';
  selectedCategory = '';

  ngOnInit(): void {
    this.loadCategories();
    this.search();
  }

  search(): void {
    this.loading = true;
    this.error = '';
    const params: Record<string, string> = {};
    if (this.searchQuery.trim()) {
      params['query'] = this.searchQuery.trim();
    }
    if (this.selectedCategory) {
      params['category'] = this.selectedCategory;
    }
    this.http.get<any>(this.apiBase, { params }).subscribe({
      next: (response) => {
        this.periodicals = Array.isArray(response) ? response : (response.content ?? []);
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Failed to load periodicals. Please try again.';
        this.loading = false;
      }
    });
  }

  loadCategories(): void {
    this.http.get<string[]>(`${this.apiBase}/categories`).subscribe({
      next: (cats) => {
        this.categories = cats;
      },
      error: () => {
        this.categories = [];
      }
    });
  }

  selectPeriodical(p: Periodical): void {
    this.selectedPeriodical = p;
    this.issues = [];
    this.http.get<PeriodicalIssue[]>(`${this.apiBase}/${p.id}/issues`).subscribe({
      next: (issues) => {
        this.issues = issues;
      },
      error: () => {
        this.issues = [];
      }
    });
  }

  getFrequencyLabel(freq: PeriodicalFrequency | string): string {
    const labels: Record<string, string> = {
      DAILY: 'Daily',
      WEEKLY: 'Weekly',
      BIWEEKLY: 'Bi-Weekly',
      MONTHLY: 'Monthly',
      QUARTERLY: 'Quarterly',
      BIANNUAL: 'Bi-Annual',
      ANNUAL: 'Annual'
    };
    return labels[freq] ?? freq;
  }

  getIssueStatusColor(status: PeriodicalIssueStatus | string): string {
    const colors: Record<string, string> = {
      CURRENT: '#22c55e',
      ARCHIVED: '#6b7280',
      MISSING: '#f97316',
      DAMAGED: '#ef4444',
      WITHDRAWN: '#8b5cf6'
    };
    return colors[status] ?? '#6b7280';
  }
}
