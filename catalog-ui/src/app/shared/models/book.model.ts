export interface Book {
  id: number;
  isbn: string;
  title: string;
  subtitle?: string;
  description?: string;
  publicationYear: number;
  pageCount: number;
  language: string;
  coverImageUrl?: string;
  authorNames: string[];
  publisherName: string;
  genreNames: string[];
  subjectNames: string[];
  availableCopies: number;
  totalCopies: number;
  averageRating?: number;
}

export interface BookSummary {
  id: number;
  isbn: string;
  title: string;
  authorNames: string[];
  publisherName: string;
  publicationYear: number;
  coverImageUrl?: string;
  availableCopies: number;
}

export interface BookSearchResult {
  books: BookSummary[];
  totalElements: number;
  totalPages: number;
  currentPage: number;
  pageSize: number;
}

export interface CreateBookRequest {
  isbn: string;
  title: string;
  subtitle?: string;
  description?: string;
  publicationYear: number;
  pageCount: number;
  language: string;
  coverImageUrl?: string;
  authorIds: number[];
  publisherId: number;
  genreIds: number[];
  subjectIds: number[];
}
