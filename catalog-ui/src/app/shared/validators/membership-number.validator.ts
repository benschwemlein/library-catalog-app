import { AbstractControl, ValidationErrors, ValidatorFn } from '@angular/forms';

/**
 * Pattern for a valid library membership number.
 * Format: LIB-YYYY-NNNNNN where YYYY is 2000–2099 and NNNNNN is 6 digits.
 */
export const MEMBERSHIP_NUMBER_PATTERN = /^LIB-20\d{2}-\d{6}$/;

/**
 * Validates that a form control value matches the membership number format
 * `LIB-YYYY-NNNNNN`.
 *
 * Usage:
 *   membershipNumber: ['', [Validators.required, membershipNumberValidator()]]
 */
export function membershipNumberValidator(): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    const value = control.value as string | null | undefined;
    if (!value) return null; // use Validators.required separately

    if (MEMBERSHIP_NUMBER_PATTERN.test(value)) {
      return null;
    }

    return {
      invalidMembershipNumber: {
        value,
        message: 'Must match format LIB-YYYY-NNNNNN',
      },
    };
  };
}
