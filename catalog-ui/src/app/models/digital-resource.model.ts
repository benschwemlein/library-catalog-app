export type DigitalResourceType = 'EBOOK' | 'AUDIOBOOK' | 'VIDEO' | 'ARTICLE' | 'DATABASE';
export type DigitalResourceFormat = 'PDF' | 'EPUB' | 'MOBI' | 'MP3' | 'MP4' | 'HTML' | 'UNKNOWN';
export type LicenseType = 'SINGLE_USER' | 'MULTI_USER' | 'UNLIMITED';
export type DigitalLoanStatus = 'ACTIVE' | 'EXPIRED' | 'RETURNED';

export interface DigitalResource {
  id: number;
  title: string;
  description: string;
  resourceType: DigitalResourceType;
  format: DigitalResourceFormat;
  fileUrl: string;
  fileSizeBytes: number;
  durationMinutes?: number;
  publisher: string;
  isbn?: string;
  licenseType: LicenseType;
  maxConcurrentUsers?: number;
  publicationYear?: number;
  language?: string;
  coverImageUrl?: string;
  active: boolean;
  availableNow: boolean;
  activeLoans: number;
}

export interface DigitalLoan {
  loanId: number;
  resourceId: number;
  resourceTitle: string;
  memberId: number;
  startDate: string;
  expiryDate: string;
  downloadCount: number;
  status: DigitalLoanStatus;
  fileUrl?: string;
}

export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}
