import { Pipe, PipeTransform } from '@angular/core';

/**
 * Converts a byte count to a human-readable file-size string.
 *
 * Usage: {{ file.size | fileSize }}         → "1.5 MB"
 *        {{ file.size | fileSize:2 }}       → "1.54 MB"
 */
@Pipe({ name: 'fileSize', standalone: true })
export class FileSizePipe implements PipeTransform {
  private static readonly UNITS = ['B', 'KB', 'MB', 'GB', 'TB'] as const;

  transform(bytes: number | null | undefined, precision = 1): string {
    if (bytes === null || bytes === undefined || isNaN(bytes)) return '0 B';
    if (bytes === 0) return '0 B';

    const absBytes = Math.abs(bytes);
    const unitIndex = Math.min(
      Math.floor(Math.log(absBytes) / Math.log(1024)),
      FileSizePipe.UNITS.length - 1
    );

    const value = absBytes / Math.pow(1024, unitIndex);
    const formatted = unitIndex === 0
      ? value.toFixed(0)           // Bytes have no fractional part
      : value.toFixed(precision);

    return `${formatted} ${FileSizePipe.UNITS[unitIndex]}`;
  }
}
