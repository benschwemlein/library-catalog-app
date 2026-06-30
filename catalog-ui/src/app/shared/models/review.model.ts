export interface BookReview {
  id: number;
  bookId: number;
  bookTitle: string;
  memberId: number;
  memberName: string;
  rating: number;
  title?: string;
  body?: string;
  createdAt: string;
  updatedAt?: string;
  verified: boolean;
  helpfulCount: number;
}

export interface CreateReviewRequest {
  bookId: number;
  rating: number;
  title?: string;
  body?: string;
}
