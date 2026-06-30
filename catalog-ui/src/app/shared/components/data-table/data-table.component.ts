import {
  Component,
  Input,
  Output,
  EventEmitter,
  OnChanges,
  SimpleChanges,
  ChangeDetectionStrategy,
} from '@angular/core';
import { CommonModule } from '@angular/common';

export interface TableColumn {
  header: string;
  field: string;
  sortable?: boolean;
  pipe?: 'date' | 'currency' | 'titlecase' | 'uppercase' | 'lowercase' | 'number' | 'percent';
  pipeArgs?: string;
  cssClass?: string;
  width?: string;
}

export interface SortEvent {
  field: string;
  direction: 'asc' | 'desc';
}

@Component({
  selector: 'app-data-table',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './data-table.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class DataTableComponent implements OnChanges {
  @Input() columns: TableColumn[] = [];
  @Input() data: any[] = [];
  @Input() trackByField = 'id';
  @Input() loading = false;
  @Input() emptyMessage = 'No data available.';
  @Input() emptySubMessage = '';
  @Input() striped = true;
  @Input() hoverable = true;
  @Input() bordered = false;
  @Input() small = false;

  @Output() rowClick = new EventEmitter<any>();
  @Output() sortChange = new EventEmitter<SortEvent>();

  sortField = '';
  sortDirection: 'asc' | 'desc' = 'asc';
  sortedData: any[] = [];

  readonly skeletonRows = Array(5).fill(null);

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['data']) {
      this.sortedData = [...(this.data || [])];
      if (this.sortField) {
        this.applySortLocally();
      }
    }
  }

  sort(column: TableColumn): void {
    if (!column.sortable) return;

    if (this.sortField === column.field) {
      this.sortDirection = this.sortDirection === 'asc' ? 'desc' : 'asc';
    } else {
      this.sortField = column.field;
      this.sortDirection = 'asc';
    }

    this.applySortLocally();
    this.sortChange.emit({ field: this.sortField, direction: this.sortDirection });
  }

  private applySortLocally(): void {
    const field = this.sortField;
    const dir = this.sortDirection;
    this.sortedData = [...(this.data || [])].sort((a, b) => {
      const aVal = this.getValue(a, field);
      const bVal = this.getValue(b, field);
      if (aVal == null && bVal == null) return 0;
      if (aVal == null) return dir === 'asc' ? 1 : -1;
      if (bVal == null) return dir === 'asc' ? -1 : 1;
      if (aVal < bVal) return dir === 'asc' ? -1 : 1;
      if (aVal > bVal) return dir === 'asc' ? 1 : -1;
      return 0;
    });
  }

  getValue(row: any, field: string): any {
    if (!row || !field) return null;
    // Support dot notation: 'author.name', 'member.firstName'
    return field.split('.').reduce((obj, key) => (obj != null ? obj[key] : null), row);
  }

  onRowClick(row: any): void {
    this.rowClick.emit(row);
  }

  getSortIcon(column: TableColumn): string {
    if (!column.sortable) return '';
    if (this.sortField !== column.field) return 'bi-chevron-expand';
    return this.sortDirection === 'asc' ? 'bi-chevron-up' : 'bi-chevron-down';
  }

  isSortedBy(field: string): boolean {
    return this.sortField === field;
  }

  trackBy(index: number, item: any): any {
    return item?.[this.trackByField] ?? index;
  }

  getTableClasses(): string {
    const classes = ['table', 'table-hover'];
    if (this.striped) classes.push('table-striped');
    if (this.hoverable) classes.push('table-hover');
    if (this.bordered) classes.push('table-bordered');
    if (this.small) classes.push('table-sm');
    return classes.join(' ');
  }
}
