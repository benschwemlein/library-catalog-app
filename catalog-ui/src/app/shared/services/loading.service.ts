import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';
import { distinctUntilChanged } from 'rxjs/operators';

/**
 * Tracks in-flight HTTP requests via a reference counter so that the global
 * loading spinner is shown during any pending request and hidden only when all
 * concurrent requests have finished.
 */
@Injectable({ providedIn: 'root' })
export class LoadingService {
  private loadingCount = 0;
  private loadingSubject = new BehaviorSubject<boolean>(false);

  /** Observable stream of the loading state. */
  readonly isLoading$: Observable<boolean> = this.loadingSubject
    .asObservable()
    .pipe(distinctUntilChanged());

  /** Increment the in-flight counter. Shows the spinner on the first call. */
  show(): void {
    this.loadingCount++;
    if (this.loadingCount === 1) {
      this.loadingSubject.next(true);
    }
  }

  /** Decrement the in-flight counter. Hides the spinner when it reaches zero. */
  hide(): void {
    this.loadingCount = Math.max(0, this.loadingCount - 1);
    if (this.loadingCount === 0) {
      this.loadingSubject.next(false);
    }
  }

  /** Force-reset the counter and hide the spinner (e.g. on route change). */
  reset(): void {
    this.loadingCount = 0;
    this.loadingSubject.next(false);
  }

  /** Synchronous snapshot of the current loading state. */
  get isLoading(): boolean {
    return this.loadingSubject.value;
  }
}
