import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { DigitalResourceService } from '../../services/digital-resource.service';
import { AuthService } from '../../shared/services/auth.service';
import { DigitalResource, DigitalLoan, DigitalResourceType, DigitalResourceFormat } from '../../models/digital-resource.model';

@Component({
  selector: 'app-digital-catalog',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './digital-catalog.component.html'
})
export class DigitalCatalogComponent implements OnInit {
  private authService = inject(AuthService);
  get memberId(): number { return this.authService.getMemberId(); }

  resources: DigitalResource[] = [];
  checkoutLoading: { [id: number]: boolean } = {};
  checkoutError: { [id: number]: string } = {};
  checkoutSuccess: { [id: number]: boolean } = {};
  loading = false;
  error = '';
  searchQuery = '';
  selectedType: DigitalResourceType | '' = '';
  currentPage = 0;
  pageSize = 20;
  totalPages = 0;
  totalElements = 0;

  readonly resourceTypes: Array<{ value: DigitalResourceType | ''; label: string }> = [
    { value: '', label: 'All Types' },
    { value: 'EBOOK', label: 'eBook' },
    { value: 'AUDIOBOOK', label: 'Audiobook' },
    { value: 'VIDEO', label: 'Video' },
    { value: 'ARTICLE', label: 'Article' },
    { value: 'DATABASE', label: 'Database' }
  ];

  constructor(private digitalService: DigitalResourceService) {}

  ngOnInit(): void {
    this.search();
  }

  search(): void {
    this.currentPage = 0;
    this.loadPage(0);
  }

  loadPage(page: number): void {
    this.loading = true;
    this.error = '';
    const type = this.selectedType || undefined;
    const query = this.searchQuery.trim() || undefined;
    this.digitalService.searchResources(query, type as DigitalResourceType | undefined, page, this.pageSize).subscribe({
      next: (data) => {
        this.resources = data.content;
        this.totalPages = data.totalPages;
        this.totalElements = data.totalElements;
        this.currentPage = data.number;
        this.loading = false;
      },
      error: (err) => {
        this.error = err.message || 'Failed to load digital resources.';
        this.loading = false;
      }
    });
  }

  getTypeIcon(type: DigitalResourceType): string {
    const icons: Record<DigitalResourceType, string> = {
      EBOOK: '📖',
      AUDIOBOOK: '🎧',
      VIDEO: '🎬',
      ARTICLE: '📰',
      DATABASE: '🗄️'
    };
    return icons[type] ?? '📄';
  }

  getTypeColor(type: DigitalResourceType): string {
    const colors: Record<DigitalResourceType, string> = {
      EBOOK: '#4f46e5',
      AUDIOBOOK: '#0891b2',
      VIDEO: '#dc2626',
      ARTICLE: '#059669',
      DATABASE: '#d97706'
    };
    return colors[type] ?? '#6b7280';
  }

  getFormatLabel(format: DigitalResourceFormat): string {
    const labels: Record<DigitalResourceFormat, string> = {
      PDF: 'PDF',
      EPUB: 'EPUB',
      MOBI: 'MOBI',
      MP3: 'MP3',
      MP4: 'MP4',
      HTML: 'HTML',
      UNKNOWN: 'Unknown'
    };
    return labels[format] ?? format;
  }

  formatFileSize(bytes: number): string {
    if (bytes < 1024) return `${bytes} B`;
    if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`;
    if (bytes < 1024 * 1024 * 1024) return `${(bytes / (1024 * 1024)).toFixed(1)} MB`;
    return `${(bytes / (1024 * 1024 * 1024)).toFixed(2)} GB`;
  }

  formatDuration(minutes: number): string {
    if (minutes < 60) return `${minutes} min`;
    const h = Math.floor(minutes / 60);
    const m = minutes % 60;
    return m > 0 ? `${h}h ${m}m` : `${h}h`;
  }

  checkout(resource: DigitalResource): void {
    this.checkoutLoading[resource.id] = true;
    this.checkoutError[resource.id] = '';
    this.checkoutSuccess[resource.id] = false;
    this.digitalService.checkout(resource.id, this.memberId).subscribe({
      next: (_loan: DigitalLoan) => {
        this.checkoutSuccess[resource.id] = true;
        this.checkoutLoading[resource.id] = false;
        resource.activeLoans = (resource.activeLoans || 0) + 1;
        if (resource.maxConcurrentUsers != null) {
          resource.availableNow = resource.activeLoans < resource.maxConcurrentUsers;
        }
      },
      error: (err) => {
        this.checkoutError[resource.id] = err.error?.message || 'Checkout failed.';
        this.checkoutLoading[resource.id] = false;
      }
    });
  }

  get pages(): number[] {
    return Array.from({ length: this.totalPages }, (_, i) => i);
  }

  trackById(_index: number, item: DigitalResource): number {
    return item.id;
  }
}
