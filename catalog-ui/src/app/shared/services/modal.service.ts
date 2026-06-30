import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable, Subject } from 'rxjs';

export interface ModalConfig<T = unknown> {
  /** The Angular component class to render inside the modal. */
  component: unknown;
  /** Optional data passed to the component via the modal context. */
  data?: T;
}

export interface ModalState<T = unknown> {
  config: ModalConfig<T>;
  /** Resolved when the modal closes; emits the return value (if any). */
  resultSubject: Subject<unknown>;
}

export interface ConfirmOptions {
  title?: string;
  message: string;
  confirmText?: string;
  cancelText?: string;
}

/**
 * Service for programmatically opening modals and confirmation dialogs.
 *
 * Components that wish to host modals should subscribe to `modalState$` and
 * render the appropriate component when the value is non-null.
 */
@Injectable({ providedIn: 'root' })
export class ModalService {
  private modalStateSubject = new BehaviorSubject<ModalState | null>(null);

  /** Emits the active modal config, or null when no modal is open. */
  readonly modalState$: Observable<ModalState | null> =
    this.modalStateSubject.asObservable();

  /**
   * Open a modal for the given component.
   *
   * @param component - The component class to render inside the modal.
   * @param data      - Optional data to pass to the component.
   * @returns An observable that emits the result value when the modal closes.
   */
  open<TData = unknown, TResult = unknown>(
    component: unknown,
    data?: TData
  ): Observable<TResult> {
    const resultSubject = new Subject<TResult>();

    this.modalStateSubject.next({
      config: { component, data },
      resultSubject: resultSubject as Subject<unknown>,
    });

    return resultSubject.asObservable();
  }

  /**
   * Close the currently open modal, optionally passing a result value back to
   * the caller that opened it.
   */
  close(result?: unknown): void {
    const state = this.modalStateSubject.value;
    if (state) {
      state.resultSubject.next(result);
      state.resultSubject.complete();
    }
    this.modalStateSubject.next(null);
  }

  /**
   * Show a simple confirm/cancel dialog.
   *
   * @returns Observable<boolean> — `true` if the user confirmed, `false` if cancelled.
   */
  confirm(message: string, title = 'Confirm'): Observable<boolean> {
    const answerSubject = new Subject<boolean>();

    // We emit a special confirmation state that a hosted ConfirmationModal
    // component can listen to.
    const confirmState: ModalState = {
      config: {
        component: '__confirm__',
        data: { message, title } as ConfirmOptions,
      },
      resultSubject: answerSubject as Subject<unknown>,
    };

    this.modalStateSubject.next(confirmState);

    return answerSubject.asObservable();
  }

  /** Programmatic confirm answer — called by the hosted modal component. */
  confirmAnswer(confirmed: boolean): void {
    const state = this.modalStateSubject.value;
    if (state) {
      state.resultSubject.next(confirmed);
      state.resultSubject.complete();
    }
    this.modalStateSubject.next(null);
  }
}
