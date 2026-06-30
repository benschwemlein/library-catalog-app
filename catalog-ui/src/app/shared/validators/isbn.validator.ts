import { AbstractControl, ValidationErrors, ValidatorFn } from '@angular/forms';

/**
 * Validates that a form control value is a valid ISBN-10 or ISBN-13.
 *
 * Hyphens and spaces are stripped before validation so both formatted and
 * unformatted values are accepted.
 *
 * Usage:
 *   isbn: ['', [Validators.required, isbnValidator()]]
 */
export function isbnValidator(): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    const value = control.value as string | null | undefined;
    if (!value) return null; // use Validators.required separately

    const cleaned = value.replace(/[-\s]/g, '');

    if (cleaned.length === 10) {
      return validateIsbn10(cleaned)
        ? null
        : { invalidIsbn: { value, message: 'Invalid ISBN-10 checksum' } };
    }

    if (cleaned.length === 13) {
      return validateIsbn13(cleaned)
        ? null
        : { invalidIsbn: { value, message: 'Invalid ISBN-13 checksum' } };
    }

    return { invalidIsbn: { value, message: 'ISBN must be 10 or 13 digits' } };
  };
}

/**
 * Validates an ISBN-10 using the standard modulus-11 check.
 * The last character may be 'X' (representing 10).
 */
function validateIsbn10(isbn: string): boolean {
  if (!/^\d{9}[\dX]$/i.test(isbn)) return false;

  let sum = 0;
  for (let i = 0; i < 9; i++) {
    sum += parseInt(isbn[i], 10) * (10 - i);
  }
  const checkChar = isbn[9].toUpperCase();
  const check = checkChar === 'X' ? 10 : parseInt(checkChar, 10);
  sum += check;

  return sum % 11 === 0;
}

/**
 * Validates an ISBN-13 using the standard modulus-10 check.
 */
function validateIsbn13(isbn: string): boolean {
  if (!/^\d{13}$/.test(isbn)) return false;

  let sum = 0;
  for (let i = 0; i < 12; i++) {
    sum += parseInt(isbn[i], 10) * (i % 2 === 0 ? 1 : 3);
  }
  const check = (10 - (sum % 10)) % 10;

  return check === parseInt(isbn[12], 10);
}
