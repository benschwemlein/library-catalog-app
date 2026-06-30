import { Pipe, PipeTransform } from '@angular/core';

/**
 * Formats an ISBN string with standard hyphens.
 *
 * - ISBN-10  → X-XX-XXXXXX-X
 * - ISBN-13  → 978-X-XXX-XXXXX-X
 *
 * Non-digit characters (including existing hyphens) are stripped before
 * formatting. If the cleaned value is not 10 or 13 digits the original
 * input is returned unchanged.
 *
 * Usage: {{ book.isbn | isbnFormat }}
 */
@Pipe({ name: 'isbnFormat', standalone: true })
export class IsbnFormatPipe implements PipeTransform {
  transform(value: string | null | undefined): string {
    if (!value) return '';

    const digits = value.replace(/\D/g, '');

    if (digits.length === 10) {
      return this.formatIsbn10(digits);
    }

    if (digits.length === 13) {
      return this.formatIsbn13(digits);
    }

    // Length is not 10 or 13 — return original unchanged
    return value;
  }

  /**
   * Formats a 10-digit ISBN as X-XX-XXXXXX-X.
   * (Group identifier is 1 digit, publisher is 2 digits, title is 6 digits,
   *  check digit is 1 digit — simplified canonical split for display.)
   */
  private formatIsbn10(d: string): string {
    return `${d.substring(0, 1)}-${d.substring(1, 3)}-${d.substring(3, 9)}-${d.substring(9)}`;
  }

  /**
   * Formats a 13-digit ISBN as 978-X-XXX-XXXXX-X.
   * Prefix (3) + group (1) + publisher (3) + title (5) + check (1).
   */
  private formatIsbn13(d: string): string {
    return `${d.substring(0, 3)}-${d.substring(3, 4)}-${d.substring(4, 7)}-${d.substring(7, 12)}-${d.substring(12)}`;
  }
}
