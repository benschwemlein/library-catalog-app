export interface RecommendationDTO {
  bookId: number;
  title: string;
  isbn: string;
  authors: string[];
  genres: string[];
  reason: string;
  score: number;
  publicationYear: number;
  available: boolean;
}
