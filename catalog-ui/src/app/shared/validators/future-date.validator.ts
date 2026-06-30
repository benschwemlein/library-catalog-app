import { AbstractControl, ValidationErrors, ValidatorFn } from '@angular/forms';

/**
 * Validates that a form control value represents a date in the future
 * (strictly after today's date at midnight).
 *
 * Works with Date objects, ISO date strings, and date-only strings (YYYY-MM-DD).
 *
 * Usage:
 *   dueDate: ['', [Validators.required, futureDateValidator()]]
 */
export function futureDateValidator(): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    const value = control.value as Date | string | null | undefined;
    if (!value) return null; // defer to Validators.required

    const date = value instanceof Date ? value : new Date(value);

    if (isNaN(date.getTime())) {
      return { pastDate: { value, message: 'Invalid date' } };
    }

    // Compare against today at midnight so a date entered as "today" is also
    // considered past
    const today = new Date();
    today.setHours(0, 0, 0, 0);

    if (date <= today) {
      return { pastDate: { value, message: 'Date must be in the future' } };
    }

    return null;
  };
}
