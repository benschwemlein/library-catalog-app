import { Pipe, PipeTransform } from '@angular/core';

/**
 * Truncates a string to `limit` characters without cutting inside a word,
 * appending `ellipsis` when truncation occurs.
 *
 * Usage: {{ text | truncate:100:'...' }}
 */
@Pipe({ name: 'truncate', standalone: true })
export class TruncatePipe implements PipeTransform {
  transform(value: string | null | undefined, limit = 100, ellipsis = '...'): string {
    if (!value) return '';
    if (value.length <= limit) return value;

    // Avoid cutting in the middle of a word
    const truncated = value.substring(0, limit);
    const lastSpace = truncated.lastIndexOf(' ');
    const cut = lastSpace > 0 ? truncated.substring(0, lastSpace) : truncated;
    return cut + ellipsis;
  }
}
