import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';

export interface Toast {
  id: string;
  type: 'success' | 'error' | 'warning' | 'info';
  message: string;
  title?: string;
  /** Duration in milliseconds before auto-dismiss. 0 = never auto-dismiss. */
  duration: number;
  dismissible: boolean;
}

@Injectable({ providedIn: 'root' })
export class ToastService {
  private toastsSubject = new BehaviorSubject<Toast[]>([]);

  /** Observable list of active toasts. */
  readonly toasts$: Observable<Toast[]> = this.toastsSubject.asObservable();

  success(message: string, title = 'Success', duration = 3000): void {
    this.add('success', message, title, duration);
  }

  error(message: string, title = 'Error', duration = 5000): void {
    this.add('error', message, title, duration);
  }

  warning(message: string, title = 'Warning', duration = 4000): void {
    this.add('warning', message, title, duration);
  }

  info(message: string, title = 'Info', duration = 3000): void {
    this.add('info', message, title, duration);
  }

  dismiss(id: string): void {
    const filtered = this.toastsSubject.value.filter((t) => t.id !== id);
    this.toastsSubject.next(filtered);
  }

  dismissAll(): void {
    this.toastsSubject.next([]);
  }

  get toasts(): Toast[] {
    return this.toastsSubject.value;
  }

  private add(
    type: Toast['type'],
    message: string,
    title: string,
    duration: number
  ): void {
    const toast: Toast = {
      id: `toast-${Date.now()}-${Math.random().toString(36).substring(2, 11)}`,
      type,
      message,
      title,
      duration,
      dismissible: true,
    };

    this.toastsSubject.next([...this.toastsSubject.value, toast]);

    if (duration > 0) {
      setTimeout(() => this.dismiss(toast.id), duration);
    }
  }
}
