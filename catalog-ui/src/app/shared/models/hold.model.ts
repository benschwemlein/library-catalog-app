export enum HoldStatus {
  PENDING = 'PENDING',
  READY = 'READY',
  FULFILLED = 'FULFILLED',
  CANCELLED = 'CANCELLED',
  EXPIRED = 'EXPIRED'
}

export interface Hold {
  id: number;
  bookId: number;
  bookTitle: string;
  bookIsbn: string;
  authorNames: string[];
  coverImageUrl?: string;
  memberId: number;
  memberName: string;
  membershipNumber: string;
  pickupBranchId: number;
  pickupBranchName: string;
  placedDate: string;
  expiryDate?: string;
  readyDate?: string;
  status: HoldStatus;
  queuePosition?: number;
  estimatedWaitDays?: number;
}

export interface PlaceHoldRequest {
  bookId: number;
  memberId: number;
  pickupBranchId: number;
}
