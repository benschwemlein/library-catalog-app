import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { ReadingList, ReadingListItem, ListVisibility } from '../../models/reading-list.model';
import { AuthService } from '../../shared/services/auth.service';

@Component({
  selector: 'app-my-lists',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './my-lists.component.html',
  styleUrl: './my-lists.component.css'
})
export class MyListsComponent implements OnInit {
  private http = inject(HttpClient);
  private authService = inject(AuthService);
  private base = 'http://localhost:8080/api/library/reading-lists';

  lists: ReadingList[] = [];
  selectedList: ReadingList | null = null;
  items: ReadingListItem[] = [];
  loading = false;
  error = '';
  newListName = '';
  newListDescription = '';
  newListVisibility: ListVisibility = 'PRIVATE';
  showCreateForm = false;
  listNameTouched = false;

  get memberId(): number { return this.authService.getMemberId(); }

  ngOnInit(): void {
    this.loadLists();
  }

  loadLists(): void {
    this.loading = true;
    this.error = '';
    this.http.get<ReadingList[]>(`${this.base}/member/${this.memberId}?requestingMemberId=${this.memberId}`)
      .subscribe({
        next: lists => { this.lists = lists; this.loading = false; },
        error: () => { this.error = 'Failed to load reading lists.'; this.loading = false; }
      });
  }

  selectList(list: ReadingList): void {
    this.selectedList = list;
    this.items = [];
    this.http.get<ReadingList>(`${this.base}/${list.id}?requestingMemberId=${this.memberId}`)
      .subscribe({
        next: detail => { this.items = detail.items || []; },
        error: () => { this.error = 'Failed to load list items.'; }
      });
  }

  createList(): void {
    if (!this.newListName.trim()) return;
    const body = {
      memberId: this.memberId,
      name: this.newListName,
      description: this.newListDescription,
      visibility: this.newListVisibility
    };
    this.http.post<ReadingList>(this.base, body).subscribe({
      next: list => {
        this.lists.push(list);
        this.newListName = '';
        this.newListDescription = '';
        this.newListVisibility = 'PRIVATE';
        this.listNameTouched = false;
        this.showCreateForm = false;
      },
      error: () => { this.error = 'Failed to create list.'; }
    });
  }

  deleteList(list: ReadingList): void {
    if (!confirm(`Delete "${list.name}"?`)) return;
    this.http.delete(`${this.base}/${list.id}?requestingMemberId=${this.memberId}`).subscribe({
      next: () => {
        this.lists = this.lists.filter(l => l.id !== list.id);
        if (this.selectedList?.id === list.id) {
          this.selectedList = null;
          this.items = [];
        }
      },
      error: () => { this.error = 'Failed to delete list.'; }
    });
  }

  markItemRead(item: ReadingListItem): void {
    if (!this.selectedList) return;
    this.http.put<ReadingListItem>(
      `${this.base}/${this.selectedList.id}/books/${item.bookId}/read?requestingMemberId=${this.memberId}`, {}
    ).subscribe({
      next: updated => {
        const idx = this.items.findIndex(i => i.bookId === item.bookId);
        if (idx !== -1) this.items[idx] = updated;
      },
      error: () => { this.error = 'Failed to mark item as read.'; }
    });
  }

  removeItem(item: ReadingListItem): void {
    if (!this.selectedList) return;
    this.http.delete(`${this.base}/${this.selectedList.id}/books/${item.bookId}?requestingMemberId=${this.memberId}`)
      .subscribe({
        next: () => { this.items = this.items.filter(i => i.bookId !== item.bookId); },
        error: () => { this.error = 'Failed to remove item.'; }
      });
  }

  getProgressPercent(list: ReadingList): number {
    if (!list.itemCount || list.itemCount === 0) return 0;
    return Math.round((list.readCount || 0) / list.itemCount * 100);
  }

  getVisibilityColor(visibility: ListVisibility): string {
    switch (visibility) {
      case 'PUBLIC': return '#22c55e';
      case 'FRIENDS_ONLY': return '#3b82f6';
      case 'PRIVATE': return '#6b7280';
      default: return '#6b7280';
    }
  }

  getPriorityColor(priority: string): string {
    switch (priority) {
      case 'HIGH': return '#ef4444';
      case 'MEDIUM': return '#f59e0b';
      case 'LOW': return '#22c55e';
      default: return '#6b7280';
    }
  }
}
