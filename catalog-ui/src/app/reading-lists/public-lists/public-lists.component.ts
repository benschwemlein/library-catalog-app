import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { ReadingList } from '../../models/reading-list.model';
import { AuthService } from '../../shared/services/auth.service';

@Component({
  selector: 'app-public-lists',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './public-lists.component.html'
})
export class PublicListsComponent implements OnInit {
  private http = inject(HttpClient);
  private authService = inject(AuthService);
  private base = 'http://localhost:8080/api/library/reading-lists';

  lists: ReadingList[] = [];
  loading = false;
  error = '';
  successMessage = '';
  searchQuery = '';
  currentPage = 0;
  totalPages = 0;

  get memberId(): number { return this.authService.getMemberId(); }

  ngOnInit(): void {
    this.loadPublicLists();
  }

  loadPublicLists(): void {
    this.loading = true;
    this.error = '';
    this.http.get<any>(`${this.base}?page=${this.currentPage}&size=20`).subscribe({
      next: resp => {
        this.lists = resp.content || resp;
        this.totalPages = resp.totalPages || 0;
        this.loading = false;
      },
      error: () => {
        this.error = 'Failed to load public lists.';
        this.loading = false;
      }
    });
  }

  get filteredLists(): ReadingList[] {
    if (!this.searchQuery.trim()) return this.lists;
    const q = this.searchQuery.toLowerCase();
    return this.lists.filter(l =>
      l.name?.toLowerCase().includes(q) ||
      l.description?.toLowerCase().includes(q) ||
      l.memberName?.toLowerCase().includes(q)
    );
  }

  copyList(list: ReadingList): void {
    this.http.post<ReadingList>(`${this.base}/${list.id}/copy`, {
      targetMemberId: this.memberId,
      newName: `Copy of ${list.name}`
    }).subscribe({
      next: () => {
        this.successMessage = `"${list.name}" copied to your lists!`;
        setTimeout(() => this.successMessage = '', 3000);
      },
      error: () => {
        this.error = 'Failed to copy list.';
      }
    });
  }

  getMemberInitials(name: string): string {
    if (!name) return '?';
    return name.split(' ').map(n => n[0]).join('').toUpperCase().slice(0, 2);
  }

  getProgressPercent(list: ReadingList): number {
    if (!list.itemCount || list.itemCount === 0) return 0;
    return Math.round((list.readCount || 0) / list.itemCount * 100);
  }

  getProgressColor(percent: number): string {
    if (percent >= 75) return '#22c55e';
    if (percent >= 40) return '#3b82f6';
    return '#f59e0b';
  }

  getAvatarColor(name: string): string {
    if (!name) return '#6b7280';
    const colors = ['#6366f1', '#8b5cf6', '#ec4899', '#f43f5e', '#f59e0b', '#10b981', '#14b8a6', '#3b82f6'];
    let hash = 0;
    for (let i = 0; i < name.length; i++) {
      hash = name.charCodeAt(i) + ((hash << 5) - hash);
    }
    return colors[Math.abs(hash) % colors.length];
  }

  prevPage(): void {
    if (this.currentPage > 0) {
      this.currentPage--;
      this.loadPublicLists();
    }
  }

  nextPage(): void {
    if (this.currentPage < this.totalPages - 1) {
      this.currentPage++;
      this.loadPublicLists();
    }
  }

  dismissError(): void {
    this.error = '';
  }

  dismissSuccess(): void {
    this.successMessage = '';
  }
}
