import {
  Component,
  EventEmitter,
  Input,
  OnDestroy,
  OnInit,
  Output,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Subject } from 'rxjs';
import { debounceTime, distinctUntilChanged, takeUntil } from 'rxjs/operators';

@Component({
  selector: 'app-search-bar',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './search-bar.component.html',
})
export class SearchBarComponent implements OnInit, OnDestroy {
  @Input() placeholder = 'Search...';
  @Input() initialValue = '';
  @Input() debounceMs = 300;
  @Input() isLoading = false;

  @Output() search = new EventEmitter<string>();
  @Output() cleared = new EventEmitter<void>();

  searchTerm = '';
  private inputSubject = new Subject<string>();
  private destroy$ = new Subject<void>();

  ngOnInit(): void {
    this.searchTerm = this.initialValue;

    this.inputSubject
      .pipe(
        debounceTime(this.debounceMs),
        distinctUntilChanged(),
        takeUntil(this.destroy$)
      )
      .subscribe((term) => this.search.emit(term));
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  onInput(value: string): void {
    this.searchTerm = value;
    this.inputSubject.next(value);
  }

  clear(): void {
    this.searchTerm = '';
    this.inputSubject.next('');
    this.cleared.emit();
  }
}
